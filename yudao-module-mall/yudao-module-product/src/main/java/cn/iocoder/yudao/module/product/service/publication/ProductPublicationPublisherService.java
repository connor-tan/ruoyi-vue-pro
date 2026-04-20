package cn.iocoder.yudao.module.product.service.publication;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationPublisherMapper;
import com.github.promeg.pinyinhelper.Pinyin;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProductPublicationPublisherService {

    private static final String CODE_PREFIX = "PUB_";
    private static final String CODE_FALLBACK_BODY = "PUBLISHER";
    private static final int CODE_MAX_LENGTH = 64;
    private static final int CODE_SEQUENCE_MAX = 99;
    private static final String CODE_RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Resource
    private ProductPublicationPublisherMapper publicationPublisherMapper;
    @Resource
    private ProductPublicationTitleMapper publicationTitleMapper;

    public Long create(ProductPublicationPublisherSaveReqVO reqVO) {
        validateNameUnique(null, reqVO.getName());
        ProductPublicationPublisherDO publisher = BeanUtils.toBean(reqVO, ProductPublicationPublisherDO.class);
        publisher.setCode(generateUniqueCode(reqVO.getName()));
        publicationPublisherMapper.insert(publisher);
        return publisher.getId();
    }

    public void update(ProductPublicationPublisherSaveReqVO reqVO) {
        ProductPublicationPublisherDO oldPublisher = validateExists(reqVO.getId());
        validateNameUnique(reqVO.getId(), reqVO.getName());
        ProductPublicationPublisherDO updateObj = BeanUtils.toBean(reqVO, ProductPublicationPublisherDO.class);
        updateObj.setCode(oldPublisher.getCode());
        publicationPublisherMapper.updateById(updateObj);
    }

    public void delete(Long id) {
        validateExists(id);
        if (publicationTitleMapper.countByPublisherId(id) > 0) {
            throw exception(PUBLICATION_PUBLISHER_HAS_TITLES);
        }
        publicationPublisherMapper.deleteById(id);
    }

    public ProductPublicationPublisherRespVO get(Long id) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectById(id);
        return publisher == null ? null : BeanUtils.toBean(publisher, ProductPublicationPublisherRespVO.class);
    }

    public PageResult<ProductPublicationPublisherRespVO> getPage(ProductPublicationPublisherPageReqVO reqVO) {
        return BeanUtils.toBean(publicationPublisherMapper.selectPage(reqVO), ProductPublicationPublisherRespVO.class);
    }

    public List<ProductPublicationPublisherSimpleRespVO> getSimpleList() {
        return BeanUtils.toBean(
                publicationPublisherMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()),
                ProductPublicationPublisherSimpleRespVO.class);
    }

    public ProductPublicationPublisherDO validateExists(Long id) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectById(id);
        if (publisher == null) {
            throw exception(PUBLICATION_PUBLISHER_NOT_EXISTS);
        }
        return publisher;
    }

    private void validateNameUnique(Long id, String name) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectByName(name);
        if (publisher == null) {
            return;
        }
        if (id == null || !publisher.getId().equals(id)) {
            throw exception(PUBLICATION_PUBLISHER_NAME_EXISTS);
        }
    }

    private String generateUniqueCode(String name) {
        String codeBase = CODE_PREFIX + buildCodeBody(name);
        String code = truncateCodeBase(codeBase, "");
        if (!existsCode(code)) {
            return code;
        }
        for (int sequence = 2; sequence <= CODE_SEQUENCE_MAX; sequence++) {
            String suffix = "_" + String.format(Locale.ROOT, "%02d", sequence);
            code = truncateCodeBase(codeBase, suffix) + suffix;
            if (!existsCode(code)) {
                return code;
            }
        }
        for (int i = 0; i < 20; i++) {
            String suffix = "_" + RandomUtil.randomString(CODE_RANDOM_CHARS, 6);
            code = truncateCodeBase(codeBase, suffix) + suffix;
            if (!existsCode(code)) {
                return code;
            }
        }
        throw exception(PUBLICATION_PUBLISHER_CODE_EXISTS);
    }

    private String buildCodeBody(String name) {
        if (name == null || name.isBlank()) {
            return CODE_FALLBACK_BODY;
        }
        StringBuilder codeBody = new StringBuilder();
        for (char ch : name.toCharArray()) {
            if (Pinyin.isChinese(ch)) {
                String pinyin = Pinyin.toPinyin(ch);
                if (pinyin != null && !pinyin.isBlank()) {
                    codeBody.append(pinyin.charAt(0));
                }
            } else if (isAsciiLetterOrDigit(ch)) {
                codeBody.append(Character.toUpperCase(ch));
            }
        }
        return codeBody.isEmpty() ? CODE_FALLBACK_BODY : codeBody.toString().toUpperCase(Locale.ROOT);
    }

    private boolean isAsciiLetterOrDigit(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9');
    }

    private String truncateCodeBase(String codeBase, String suffix) {
        int maxBaseLength = CODE_MAX_LENGTH - suffix.length();
        if (codeBase.length() <= maxBaseLength) {
            return codeBase;
        }
        return codeBase.substring(0, maxBaseLength);
    }

    private boolean existsCode(String code) {
        return Objects.nonNull(publicationPublisherMapper.selectByCodeIncludeDeleted(code));
    }
}
