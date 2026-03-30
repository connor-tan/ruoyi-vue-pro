package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 学生全局批量升班执行 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentGlobalPromotionExecuteReqVO extends StudentGlobalPromotionPreviewReqVO {
}
