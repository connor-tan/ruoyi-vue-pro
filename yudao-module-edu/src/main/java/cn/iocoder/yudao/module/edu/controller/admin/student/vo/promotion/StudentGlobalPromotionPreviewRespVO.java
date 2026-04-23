package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生全局批量升班预览 Response VO")
@Data
public class StudentGlobalPromotionPreviewRespVO {

    @Schema(description = "汇总")
    private StudentGlobalPromotionSummaryRespVO summary;

    @Schema(description = "学校预览")
    private List<StudentGlobalPromotionSchoolRespVO> schools;

    @Schema(description = "学生预览明细")
    private List<StudentGlobalPromotionItemRespVO> items;

}
