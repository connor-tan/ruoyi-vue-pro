package cn.iocoder.yudao.module.subscription.controller.app.publication.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Schema(description = "用户 App - 订刊刊物分页 Request VO")
@Data
public class AppSubscriptionPublicationPageReqVO extends PageParam {

    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学生不能为空")
    private Long studentId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "属性值ID集合")
    private Set<Long> propertyValueIds;
}
