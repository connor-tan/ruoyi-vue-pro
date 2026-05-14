package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "用户 App - 订单配送组 Response VO")
@Data
public class AppTradeOrderDeliveryRespVO {

    @Schema(description = "配送组编号", example = "1")
    private Long id;

    @Schema(description = "订单编号", example = "1")
    private Long orderId;

    @Schema(description = "业务场景", example = "PUBLICATION")
    private String bizScene;

    @Schema(description = "配送方式", example = "1")
    private Integer deliveryType;

    @Schema(description = "配送状态", example = "10")
    private Integer status;

    @Schema(description = "商品数量", example = "2")
    private Integer productCount;

    @Schema(description = "实付金额，单位：分", example = "1000")
    private Integer payPrice;

    @Schema(description = "运费金额，单位：分", example = "100")
    private Integer deliveryPrice;

    @Schema(description = "发货物流公司编号", example = "10")
    private Long logisticsId;

    @Schema(description = "发货物流单号", example = "SF123456789")
    private String logisticsNo;

    @Schema(description = "发货时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime receiveTime;

    @Schema(description = "收件人名称", example = "张三")
    private String receiverName;

    @Schema(description = "收件人手机", example = "13800138000")
    private String receiverMobile;

    @Schema(description = "收件人地区编号", example = "110000")
    private Integer receiverAreaId;

    @Schema(description = "收件人地区名称", example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @Schema(description = "收件人详细地址", example = "中关村大街 1 号")
    private String receiverDetailAddress;

    @Schema(description = "自提门店编号", example = "100")
    private Long pickUpStoreId;

    @Schema(description = "自提核销码", example = "12345678")
    private String pickUpVerifyCode;

    @Schema(description = "订刊学生编号", example = "100")
    private Long studentId;

    @Schema(description = "订刊学生名称快照", example = "张小明")
    private String studentNameSnapshot;

    @Schema(description = "学校编号", example = "100")
    private Long schoolId;

    @Schema(description = "学校名称快照", example = "实验小学")
    private String schoolNameSnapshot;

    @Schema(description = "学校地址快照", example = "上海市普陀区曹杨路 1 号")
    private String schoolAddressSnapshot;

    @Schema(description = "班级编号", example = "300")
    private Long classId;

    @Schema(description = "班级名称快照", example = "2026级一年级1班")
    private String classNameSnapshot;

    @Schema(description = "年级目录编号", example = "4")
    private Long gradeCatalogId;

    @Schema(description = "年级名称快照", example = "一年级")
    private String gradeNameSnapshot;

    @Schema(description = "学校配送仓库编号", example = "200")
    private Long warehouseId;

    @Schema(description = "学校配送仓库名称快照", example = "滨湖履约仓")
    private String warehouseNameSnapshot;

    @Schema(description = "学校配送仓库地址快照", example = "上海市普陀区曹杨路 1 号")
    private String warehouseAddressSnapshot;

    @Schema(description = "学校配送联系人", example = "李老师")
    private String contactName;

    @Schema(description = "学校配送联系电话", example = "13800001111")
    private String contactMobile;
}
