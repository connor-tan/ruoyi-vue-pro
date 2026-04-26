package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.sku.AppProductSkuBaseRespVO;
import cn.iocoder.yudao.module.trade.controller.app.base.spu.AppProductSpuBaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 用户的购物列表 Response VO")
@Data
public class AppCartListRespVO {

    /**
     * 购物车分组数组
     */
    private List<Group> groups;

    /**
     * 有效的购物项数组
     */
    private List<Cart> validList;

    /**
     * 无效的购物项数组
     */
    private List<Cart> invalidList;

    @Schema(description = "购物项")
    @Data
    public static class Cart {

        @Schema(description = "购物项的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer count;

        @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean selected;

        @Schema(description = "订刊学生编号", example = "2048")
        private Long subscriptionStudentId;

        @Schema(description = "订刊窗口 SKU 编号（offerSku）", example = "4096")
        private Long subscriptionOfferSkuId;

        /**
         * 商品 SPU
         */
        private AppProductSpuBaseRespVO spu;
        /**
         * 商品 SKU
         */
        private AppProductSkuBaseRespVO sku;

    }

    @Schema(description = "购物车分组")
    @Data
    public static class Group {

        @Schema(description = "业务场景", example = "PUBLICATION")
        private String bizScene;

        @Schema(description = "订刊学生编号", example = "2048")
        private Long studentId;

        @Schema(description = "订刊学生名称", example = "张小明")
        private String studentName;

        @Schema(description = "学校编号", example = "1024")
        private Long schoolId;

        @Schema(description = "学校名称", example = "实验小学")
        private String schoolName;

        @Schema(description = "班级编号", example = "2048")
        private Long classId;

        @Schema(description = "班级名称", example = "2026级一年级1班")
        private String className;

        @Schema(description = "年级目录编号", example = "1")
        private Long gradeCatalogId;

        @Schema(description = "年级名称", example = "一年级")
        private String gradeName;

        @Schema(description = "站点编号", example = "300")
        private Long stationId;

        @Schema(description = "站点名称", example = "A站点")
        private String stationName;

        @Schema(description = "站点地址", example = "上海市普陀区曹杨路1号")
        private String stationAddress;

        @Schema(description = "联系人", example = "李老师")
        private String contactName;

        @Schema(description = "联系电话", example = "13800001111")
        private String contactMobile;

        @Schema(description = "购物项列表")
        private List<Cart> items;
    }

}
