package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 站内信模版 Response VO")
@Data
public class NotifyTemplateRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模版名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试模版")
    private String name;

    @Schema(description = "模版编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SEND_TEST")
    private String code;

    @Schema(description = "模版类型，对应 system_notify_template_type 字典", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "发送人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "土豆")
    private String nickname;

    @Schema(description = "模版内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是模版内容")
    private String content;

    @Schema(description = "参数数组", example = "name,code")
    private List<String> params;

    @Schema(description = "默认接收人用户编号数组", example = "1,2")
    private List<Long> receiverUserIds;

    @Schema(description = "状态，参见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "备注", example = "我是备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
