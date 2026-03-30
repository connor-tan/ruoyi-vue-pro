package cn.iocoder.yudao.module.edu.service.student;

import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewRespVO;
import jakarta.validation.Valid;

/**
 * 学生一键升班 Service
 */
public interface StudentPromotionService {

    /**
     * 预览学生一键升班结果
     *
     * @param reqVO 预览请求
     * @return 预览结果
     */
    StudentPromotionPreviewRespVO previewStudentPromotion(@Valid StudentPromotionPreviewReqVO reqVO);

    /**
     * 执行学生一键升班
     *
     * @param reqVO 执行请求
     * @return 执行结果
     */
    StudentPromotionExecuteRespVO executeStudentPromotion(@Valid StudentPromotionExecuteReqVO reqVO);

    /**
     * 执行学生一键升班，并归属到全局任务
     *
     * @param reqVO 执行请求
     * @param taskId 全局任务编号
     * @return 执行结果
     */
    StudentPromotionExecuteRespVO executeStudentPromotion(@Valid StudentPromotionExecuteReqVO reqVO, Long taskId);

}
