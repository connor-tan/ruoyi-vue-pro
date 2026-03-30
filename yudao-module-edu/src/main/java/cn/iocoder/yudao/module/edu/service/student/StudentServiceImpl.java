package cn.iocoder.yudao.module.edu.service.student;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_CLASS_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.SCHOOL_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_DATE_OVERLAP;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_DUPLICATE_START_DATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_END_DATE_INVALID;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_ENTRY_YEAR_NOT_MATCH;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_MULTI_CURRENT;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_NOT_BELONG_TO_SCHOOL;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_CLASS_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.STUDENT_PARENT_NOT_EXISTS;

/**
 * 学生 Service 实现类
 *
 * @author connor
 */
@Service
@Validated
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudent(StudentSaveReqVO createReqVO) {
        validateStudentSaveReqVO(createReqVO);

        StudentDO student = BeanUtils.toBean(createReqVO, StudentDO.class);
        studentMapper.insert(student);
        createStudentClassList(student.getId(), createReqVO.getStudentClasses());
        return student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(StudentSaveReqVO updateReqVO) {
        validateStudentExists(updateReqVO.getId());
        validateStudentSaveReqVO(updateReqVO);

        StudentDO updateObj = BeanUtils.toBean(updateReqVO, StudentDO.class);
        studentMapper.updateById(updateObj);
        updateStudentClassList(updateReqVO.getId(), updateReqVO.getStudentClasses());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        validateStudentExists(id);
        studentMapper.deleteById(id);
        deleteStudentClassByStudentId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudentListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        studentMapper.deleteByIds(ids);
        deleteStudentClassByStudentIds(ids);
    }

    @Override
    public StudentRespVO getStudent(Long id) {
        StudentDO student = validateStudentExists(id);
        return buildStudentResp(student);
    }

    @Override
    public PageResult<StudentRespVO> getStudentPage(StudentPageReqVO pageReqVO) {
        PageResult<StudentDO> pageResult = studentMapper.selectPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        return new PageResult<>(buildStudentRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public List<StudentClassRespVO> getStudentClassListByStudentId(Long studentId) {
        validateStudentExists(studentId);
        return buildStudentClassRespList(studentClassMapper.selectListByStudentId(studentId));
    }

    private void validateStudentSaveReqVO(StudentSaveReqVO reqVO) {
        validateParentExists(reqVO.getBelongTo());
        validateSchoolExists(reqVO.getCurrentSchoolId());
        validateStudentClassList(reqVO.getCurrentSchoolId(), reqVO.getEntryYear(), reqVO.getStudentClasses());
    }

    private void validateParentExists(Long parentId) {
        if (memberUserApi.getUser(parentId) == null) {
            throw exception(STUDENT_PARENT_NOT_EXISTS);
        }
    }

    private void validateSchoolExists(Long schoolId) {
        if (schoolMapper.selectById(schoolId) == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
    }

    private StudentDO validateStudentExists(Long id) {
        StudentDO student = studentMapper.selectById(id);
        if (student == null) {
            throw exception(STUDENT_NOT_EXISTS);
        }
        return student;
    }

    private void validateStudentClassList(Long schoolId, Integer entryYear, List<StudentClassSaveReqVO> studentClasses) {
        if (CollUtil.isEmpty(studentClasses)) {
            return;
        }

        Set<LocalDate> startDates = new HashSet<>();
        long currentClassCount = 0L;
        Set<Long> classIds = convertSet(studentClasses, StudentClassSaveReqVO::getClassId);
        Map<Long, SchoolClassDO> schoolClassMap = getSchoolClassMap(classIds);
        if (schoolClassMap.size() != classIds.size()) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
        for (StudentClassSaveReqVO studentClass : studentClasses) {
            if (!startDates.add(studentClass.getStartDate())) {
                throw exception(STUDENT_CLASS_DUPLICATE_START_DATE);
            }
            if (studentClass.getEndDate() == null) {
                currentClassCount++;
            } else if (studentClass.getEndDate().isBefore(studentClass.getStartDate())) {
                throw exception(STUDENT_CLASS_END_DATE_INVALID);
            }

            SchoolClassDO schoolClass = schoolClassMap.get(studentClass.getClassId());
            if (!Objects.equals(schoolClass.getSchoolId(), schoolId)) {
                throw exception(STUDENT_CLASS_NOT_BELONG_TO_SCHOOL);
            }
            if (!Objects.equals(schoolClass.getEntryYear(), entryYear)) {
                throw exception(STUDENT_CLASS_ENTRY_YEAR_NOT_MATCH);
            }
        }
        if (currentClassCount > 1) {
            throw exception(STUDENT_CLASS_MULTI_CURRENT);
        }
        validateStudentClassDateRange(studentClasses);
    }

    private void validateStudentClassDateRange(List<StudentClassSaveReqVO> studentClasses) {
        List<StudentClassSaveReqVO> sortedStudentClasses = new ArrayList<>(studentClasses);
        sortedStudentClasses.sort((item1, item2) -> item1.getStartDate().compareTo(item2.getStartDate()));
        for (int i = 1; i < sortedStudentClasses.size(); i++) {
            StudentClassSaveReqVO previous = sortedStudentClasses.get(i - 1);
            StudentClassSaveReqVO current = sortedStudentClasses.get(i);
            if (previous.getEndDate() == null || !current.getStartDate().isAfter(previous.getEndDate())) {
                throw exception(STUDENT_CLASS_DATE_OVERLAP);
            }
        }
    }

    private Map<Long, SchoolClassDO> getSchoolClassMap(Set<Long> classIds) {
        if (CollUtil.isEmpty(classIds)) {
            return Collections.emptyMap();
        }
        return schoolClassMapper.selectList(SchoolClassDO::getId, classIds).stream()
                .collect(Collectors.toMap(SchoolClassDO::getId,
                        Function.identity(), (item1, item2) -> item1));
    }

    private List<StudentRespVO> buildStudentRespList(List<StudentDO> students) {
        Map<Long, MemberUserRespDTO> parentMap = memberUserApi.getUserMap(convertSet(students, StudentDO::getBelongTo));
        Map<Long, SchoolDO> schoolMap = schoolMapper.selectList(SchoolDO::getId, convertSet(students, StudentDO::getCurrentSchoolId))
                .stream()
                .collect(Collectors.toMap(SchoolDO::getId, Function.identity(), (item1, item2) -> item1));
        return students.stream()
                .map(student -> buildStudentResp(student, parentMap.get(student.getBelongTo()), schoolMap.get(student.getCurrentSchoolId())))
                .collect(Collectors.toList());
    }

    private StudentRespVO buildStudentResp(StudentDO student) {
        MemberUserRespDTO parent = memberUserApi.getUser(student.getBelongTo());
        SchoolDO school = schoolMapper.selectById(student.getCurrentSchoolId());
        return buildStudentResp(student, parent, school);
    }

    private StudentRespVO buildStudentResp(StudentDO student, MemberUserRespDTO parent, SchoolDO school) {
        StudentRespVO respVO = BeanUtils.toBean(student, StudentRespVO.class);
        if (parent != null) {
            respVO.setParentNickname(parent.getNickname());
            respVO.setParentMobile(parent.getMobile());
        }
        if (school != null) {
            respVO.setCurrentSchoolName(school.getSchoolName());
        }
        return respVO;
    }

    private List<StudentClassRespVO> buildStudentClassRespList(List<StudentClassDO> studentClasses) {
        if (CollUtil.isEmpty(studentClasses)) {
            return Collections.emptyList();
        }
        Map<Long, SchoolClassDO> schoolClassMap =
                getSchoolClassMap(convertSet(studentClasses, StudentClassDO::getClassId));
        return studentClasses.stream().map(studentClass -> {
            StudentClassRespVO respVO = BeanUtils.toBean(studentClass, StudentClassRespVO.class);
            SchoolClassDO schoolClass = schoolClassMap.get(studentClass.getClassId());
            if (schoolClass != null) {
                respVO.setClassName(schoolClass.getClassName());
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    // ==================== 子表（学生班级记录） ====================

    private void createStudentClassList(Long studentId, List<StudentClassSaveReqVO> list) {
        List<StudentClassDO> studentClasses = buildStudentClassDOList(studentId, list);
        if (CollUtil.isEmpty(studentClasses)) {
            return;
        }
        studentClassMapper.insertBatch(studentClasses);
    }

    private void updateStudentClassList(Long studentId, List<StudentClassSaveReqVO> list) {
        List<StudentClassDO> oldList = studentClassMapper.selectListByStudentId(studentId);
        Map<Long, StudentClassDO> oldMap = oldList.stream()
                .collect(Collectors.toMap(StudentClassDO::getId, Function.identity(), (item1, item2) -> item1));
        List<StudentClassDO> newList = buildStudentClassDOList(studentId, list);
        for (StudentClassDO studentClass : newList) {
            if (studentClass.getId() == null) {
                continue;
            }
            if (!oldMap.containsKey(studentClass.getId())) {
                throw exception(STUDENT_CLASS_RECORD_NOT_EXISTS);
            }
        }

        List<List<StudentClassDO>> classDiffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getId(), newVal.getId());
            if (same) {
                newVal.setId(oldVal.getId()).clean();
            }
            return same;
        });
        if (CollUtil.isNotEmpty(classDiffList.get(0))) {
            studentClassMapper.insertBatch(classDiffList.get(0));
        }
        if (CollUtil.isNotEmpty(classDiffList.get(1))) {
            studentClassMapper.updateBatch(classDiffList.get(1));
        }
        if (CollUtil.isNotEmpty(classDiffList.get(2))) {
            studentClassMapper.deleteByIds(convertList(classDiffList.get(2), StudentClassDO::getId));
        }
    }

    private List<StudentClassDO> buildStudentClassDOList(Long studentId, List<StudentClassSaveReqVO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(studentClass -> {
            StudentClassDO studentClassDO = BeanUtils.toBean(studentClass, StudentClassDO.class);
            studentClassDO.setStudentId(studentId);
            studentClassDO.clean();
            return studentClassDO;
        }).collect(Collectors.toList());
    }

    private void deleteStudentClassByStudentId(Long studentId) {
        studentClassMapper.deleteByStudentId(studentId);
    }

    private void deleteStudentClassByStudentIds(List<Long> studentIds) {
        studentClassMapper.deleteByStudentIds(studentIds);
    }

}
