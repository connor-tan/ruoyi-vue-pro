package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentOrderContextRespDTO;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 学生 Service 接口
 *
 * @author connor
 */
public interface StudentService {

    /**
     * 创建学生
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStudent(@Valid StudentSaveReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateStudent(@Valid StudentSaveReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 编号
     */
    void deleteStudent(Long id);

    /**
    * 批量删除学生
    *
    * @param ids 编号
    */
    void deleteStudentListByIds(List<Long> ids);

    /**
     * 获得学生
     *
     * @param id 编号
     * @return 学生
     */
    StudentRespVO getStudent(Long id);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<StudentRespVO> getStudentPage(StudentPageReqVO pageReqVO);

    /**
     * 获得家长端学生精简列表
     *
     * @param belongTo 家长编号
     * @return 学生精简列表
     */
    List<AppStudentSimpleRespVO> getAppStudentSimpleList(Long belongTo);

    /**
     * 获得用于交易链的学生归属快照。
     *
     * @param belongTo   家长编号
     * @param studentIds 学生编号
     * @return 学生归属快照
     */
    Map<Long, EduStudentOrderContextRespDTO> getOrderStudentContextMap(Long belongTo, Collection<Long> studentIds);

    Map<Long, EduStudentSubscriptionContextRespDTO> getSubscriptionStudentContextMap(
            Long belongTo,
            Collection<Long> studentIds,
            Integer targetYearStart,
            Integer targetYearEnd,
            Long targetYearCatalogId);

    // ==================== 子表（学生班级区间记录） ====================

    /**
     * 获得学生班级区间记录列表
     *
     * @param studentId 学生ID
     * @return 学生班级区间记录列表
     */
    List<StudentClassRespVO> getStudentClassListByStudentId(Long studentId);

}
