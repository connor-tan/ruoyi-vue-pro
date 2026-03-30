package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生一键升班预览 Response VO")
@Data
public class StudentPromotionPreviewRespVO {

    @Schema(description = "汇总")
    private StudentPromotionSummaryRespVO summary;

    @Schema(description = "预览明细")
    private List<StudentPromotionItemRespVO> items;

}
