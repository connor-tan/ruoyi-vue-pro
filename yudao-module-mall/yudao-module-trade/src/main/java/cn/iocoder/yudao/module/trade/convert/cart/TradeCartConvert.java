package cn.iocoder.yudao.module.trade.convert.cart;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentOrderContextRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.trade.controller.app.base.sku.AppProductSkuBaseRespVO;
import cn.iocoder.yudao.module.trade.controller.app.base.spu.AppProductSpuBaseRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppCartListRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper
public interface TradeCartConvert {

    TradeCartConvert INSTANCE = Mappers.getMapper(TradeCartConvert.class);

    default AppCartListRespVO convertList(List<CartDO> carts,
                                          List<ProductSpuRespDTO> spus, List<ProductSkuRespDTO> skus,
                                          Map<Long, EduStudentOrderContextRespDTO> studentMap) {
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spus, ProductSpuRespDTO::getId);
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        // 遍历，开始转换
        List<AppCartListRespVO.Cart> validList = new ArrayList<>(carts.size());
        List<AppCartListRespVO.Cart> invalidList = new ArrayList<>();
        Map<String, AppCartListRespVO.Group> groupMap = new LinkedHashMap<>();
        carts.forEach(cart -> {
            AppCartListRespVO.Cart cartVO = new AppCartListRespVO.Cart();
            cartVO.setId(cart.getId()).setCount(cart.getCount()).setSelected(cart.getSelected())
                    .setSubscriptionStudentId(cart.getSubscriptionStudentId())
                    .setSubscriptionOfferSkuId(cart.getSubscriptionOfferSkuId());
            ProductSpuRespDTO spu = spuMap.get(cart.getSpuId());
            ProductSkuRespDTO sku = skuMap.get(cart.getSkuId());
            cartVO.setSpu(BeanUtils.toBean(spu, AppProductSpuBaseRespVO.class))
                    .setSku(BeanUtils.toBean(sku, AppProductSkuBaseRespVO.class));
            boolean publication = spu != null && BizSceneEnum.isPublication(spu.getBizScene());
            // 如果 SPU 不存在，或者下架，或者普通商品库存不足，说明是无效的
            if (spu == null
                || !ProductSpuStatusEnum.isEnable(spu.getStatus())
                || (!publication && spu.getStock() <= 0)) {
                invalidList.add(cartVO);
            } else {
                validList.add(cartVO);
                AppCartListRespVO.Group group = resolveGroup(groupMap, spu, cart, studentMap);
                group.getItems().add(cartVO);
            }
        });
        return new AppCartListRespVO().setGroups(new ArrayList<>(groupMap.values()))
                .setValidList(validList).setInvalidList(invalidList);
    }

    private AppCartListRespVO.Group resolveGroup(Map<String, AppCartListRespVO.Group> groupMap,
                                                 ProductSpuRespDTO spu,
                                                 CartDO cart,
                                                 Map<Long, EduStudentOrderContextRespDTO> studentMap) {
        boolean publication = spu != null && BizSceneEnum.isPublication(spu.getBizScene());
        String groupKey = publication && cart.getSubscriptionStudentId() != null
                ? "publication:" + cart.getSubscriptionStudentId()
                : "normal";
        AppCartListRespVO.Group group = groupMap.get(groupKey);
        if (group != null) {
            return group;
        }
        group = new AppCartListRespVO.Group();
        group.setBizScene(publication ? BizSceneEnum.PUBLICATION.getCode() : BizSceneEnum.NORMAL.getCode());
        group.setItems(new ArrayList<>());
        if (publication && cart.getSubscriptionStudentId() != null) {
            EduStudentOrderContextRespDTO student = studentMap.get(cart.getSubscriptionStudentId());
            if (student != null) {
                group.setStudentId(student.getStudentId());
                group.setStudentName(student.getStudentName());
                group.setSchoolId(student.getSchoolId());
                group.setSchoolName(student.getSchoolName());
                group.setSchoolAddress(student.getSchoolAddress());
                group.setClassId(student.getClassId());
                group.setClassName(student.getClassName());
                group.setGradeCatalogId(student.getGradeCatalogId());
                group.setGradeName(student.getGradeName());
                group.setWarehouseId(student.getWarehouseId());
                group.setWarehouseName(student.getWarehouseName());
                group.setWarehouseAddress(student.getWarehouseAddress());
                group.setContactName(student.getContactName());
                group.setContactMobile(student.getContactMobile());
            } else {
                group.setStudentId(cart.getSubscriptionStudentId());
            }
        }
        groupMap.put(groupKey, group);
        return group;
    }

}
