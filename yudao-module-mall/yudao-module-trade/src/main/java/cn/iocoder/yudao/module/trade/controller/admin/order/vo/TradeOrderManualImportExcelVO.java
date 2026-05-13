package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TradeOrderManualImportExcelVO {

    @ExcelProperty("导入订单号")
    private String importOrderNo;

    @ExcelProperty("商品SKU编号")
    private Long skuId;

    @ExcelProperty("数量")
    private Integer count;

    @ExcelProperty("配送方式")
    private Integer deliveryType;

    @ExcelProperty("学生编号")
    private Long studentId;

    @ExcelProperty("订刊窗口SKU编号")
    private Long offerSkuId;

    @ExcelProperty("商品单价(分)")
    private Integer manualUnitPrice;

    @ExcelProperty("整单金额(分)")
    private Integer manualOrderPrice;

    @ExcelProperty("收件人")
    private String receiverName;

    @ExcelProperty("收件手机号")
    private String receiverMobile;

    @ExcelProperty("收件地区编号")
    private Integer receiverAreaId;

    @ExcelProperty("收件详细地址")
    private String receiverDetailAddress;

    @ExcelProperty("自提门店编号")
    private Long pickUpStoreId;

    @ExcelProperty("商家备注")
    private String remark;

}
