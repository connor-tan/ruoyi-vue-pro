package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo.ProductPublicationSpuRelationRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspurelation.vo.ProductPublicationSpuRelationSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuPublicationDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSpuPublicationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductPublicationSpuRelationService {

    @Resource
    private ProductSpuPublicationMapper productSpuPublicationMapper;
    @Resource
    private ProductPublicationTitleService publicationTitleService;

    public ProductPublicationSpuRelationRespVO getBySpuId(Long productSpuId) {
        ProductSpuPublicationDO relation = productSpuPublicationMapper.selectByProductSpuId(productSpuId);
        if (relation == null) {
            return null;
        }
        ProductPublicationSpuRelationRespVO respVO = BeanUtils.toBean(relation, ProductPublicationSpuRelationRespVO.class);
        ProductPublicationTitleDO title = publicationTitleService.validateExists(relation.getPublicationTitleId());
        respVO.setPublicationTitleName(title.getName());
        return respVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(ProductPublicationSpuRelationSaveReqVO reqVO) {
        publicationTitleService.validateExists(reqVO.getPublicationTitleId());
        productSpuPublicationMapper.insertOrUpdate(BeanUtils.toBean(reqVO, ProductSpuPublicationDO.class));
    }
}
