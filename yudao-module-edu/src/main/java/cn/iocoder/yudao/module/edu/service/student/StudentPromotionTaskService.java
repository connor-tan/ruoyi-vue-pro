package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionPreviewRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionBatchRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionYearOptionRespVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 学生全局批量升班 Service
 */
public interface StudentPromotionTaskService {

    List<StudentPromotionYearOptionRespVO> getPromotionYearOptions();

    StudentGlobalPromotionPreviewRespVO previewGlobalStudentPromotion(@Valid StudentGlobalPromotionPreviewReqVO reqVO);

    StudentGlobalPromotionExecuteRespVO executeGlobalStudentPromotion(@Valid StudentGlobalPromotionExecuteReqVO reqVO);

    StudentGlobalPromotionRollbackRespVO rollbackGlobalStudentPromotion(@Valid StudentGlobalPromotionRollbackReqVO reqVO);

    PageResult<StudentPromotionTaskRespVO> getPromotionTaskPage(StudentPromotionTaskPageReqVO reqVO);

    List<StudentPromotionBatchRespVO> getPromotionBatchListByTaskId(Long taskId);

    PageResult<StudentFlowRespVO> getStudentFlowPage(StudentFlowPageReqVO reqVO);

}
