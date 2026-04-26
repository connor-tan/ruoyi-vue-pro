package cn.iocoder.yudao.module.product.api.publication;

import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.product.service.publication.ProductPublicationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Service
@Validated
public class ProductPublicationApiImpl implements ProductPublicationApi {

    @Resource
    private ProductPublicationService productPublicationService;

    @Override
    public ProductPublicationRespDTO getPublication(Long spuId) {
        return productPublicationService.getPublication(spuId);
    }

    @Override
    public List<ProductPublicationRespDTO> getPublicationList(Collection<Long> spuIds) {
        return productPublicationService.getPublicationList(spuIds);
    }

}
