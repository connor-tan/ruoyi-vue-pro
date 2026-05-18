package cn.iocoder.yudao.module.promotion.service.diy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyPageMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_PROPERTY_FORMAT_INVALID;

/**
 * 装修属性清理 Service 实现类
 *
 * @author Connor
 */
@Service
@Validated
@Slf4j
public class DiyPropertyCleanServiceImpl implements DiyPropertyCleanService {

    private static final Set<String> PRODUCT_COMPONENT_IDS = Set.of("ProductList", "ProductCard");

    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private DiyPageMapper diyPageMapper;

    @Override
    public String cleanInvalidSpuIds(String property) {
        if (StrUtil.isBlank(property)) {
            return property;
        }
        ObjectNode root = parseProperty(property);
        CleanResult result = cleanInvalidSpuIds(root);
        return result.changed ? JsonUtils.toJsonString(root) : property;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeSpuIdFromAllPages(Long spuId) {
        if (spuId == null) {
            return 0;
        }
        int updateCount = 0;
        List<DiyPageDO> pages = diyPageMapper.selectList();
        for (DiyPageDO page : pages) {
            String property = page.getProperty();
            if (StrUtil.isBlank(property)) {
                continue;
            }
            RemoveResult result;
            try {
                result = removeSpuId(property, spuId);
            } catch (RuntimeException ex) {
                log.warn("[removeSpuIdFromAllPages][页面({})装修属性解析失败，跳过清理商品({})]", page.getId(), spuId, ex);
                continue;
            }
            if (!result.changed) {
                continue;
            }
            diyPageMapper.updateById(new DiyPageDO().setId(page.getId()).setProperty(result.property));
            updateCount++;
        }
        return updateCount;
    }

    private ObjectNode parseProperty(String property) {
        try {
            JsonNode root = JsonUtils.parseTree(property);
            if (!root.isObject()) {
                throw exception(DIY_PROPERTY_FORMAT_INVALID);
            }
            return (ObjectNode) root;
        } catch (ServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw exception(DIY_PROPERTY_FORMAT_INVALID);
        }
    }

    private CleanResult cleanInvalidSpuIds(ObjectNode root) {
        List<ObjectNode> components = getProductComponents(root);
        if (CollUtil.isEmpty(components)) {
            return new CleanResult(false);
        }
        Set<Long> candidateIds = collectSpuIds(components);
        Set<Long> validSpuIds = getValidSpuIds(candidateIds);
        boolean changed = false;
        for (ObjectNode component : components) {
            ObjectNode property = getPropertyNode(component);
            if (property == null) {
                continue;
            }
            JsonNode spuIdsNode = property.get("spuIds");
            if (spuIdsNode == null || !spuIdsNode.isArray()) {
                continue;
            }
            changed |= rewriteSpuIds(property, spuIdsNode, validSpuIds);
        }
        return new CleanResult(changed);
    }

    private RemoveResult removeSpuId(String property, Long spuId) {
        ObjectNode root = parseProperty(property);
        List<ObjectNode> components = getProductComponents(root);
        boolean changed = false;
        for (ObjectNode component : components) {
            ObjectNode propertyNode = getPropertyNode(component);
            if (propertyNode == null) {
                continue;
            }
            JsonNode spuIdsNode = propertyNode.get("spuIds");
            if (spuIdsNode == null || !spuIdsNode.isArray()) {
                continue;
            }
            changed |= removeSpuId(propertyNode, spuIdsNode, spuId);
        }
        return new RemoveResult(changed, changed ? JsonUtils.toJsonString(root) : property);
    }

    private List<ObjectNode> getProductComponents(ObjectNode root) {
        JsonNode components = root.get("components");
        if (components == null || !components.isArray()) {
            return Collections.emptyList();
        }
        List<ObjectNode> result = CollUtil.newArrayList();
        for (JsonNode component : components) {
            if (component.isObject() && PRODUCT_COMPONENT_IDS.contains(component.path("id").asText())) {
                result.add((ObjectNode) component);
            }
        }
        return result;
    }

    private ObjectNode getPropertyNode(ObjectNode component) {
        JsonNode property = component.get("property");
        return property != null && property.isObject() ? (ObjectNode) property : null;
    }

    private Set<Long> collectSpuIds(List<ObjectNode> components) {
        Set<Long> ids = new LinkedHashSet<>();
        for (ObjectNode component : components) {
            ObjectNode property = getPropertyNode(component);
            if (property == null) {
                continue;
            }
            JsonNode spuIdsNode = property.get("spuIds");
            if (spuIdsNode == null || !spuIdsNode.isArray()) {
                continue;
            }
            for (JsonNode item : spuIdsNode) {
                Long id = parseLong(item);
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private Set<Long> getValidSpuIds(Set<Long> candidateIds) {
        if (CollUtil.isEmpty(candidateIds)) {
            return Collections.emptySet();
        }
        return productSpuService.getSpuList(candidateIds).stream()
                .filter(Objects::nonNull)
                .filter(spu -> ProductSpuStatusEnum.ENABLE.getStatus().equals(spu.getStatus()))
                .map(ProductSpuDO::getId)
                .collect(Collectors.toSet());
    }

    private boolean rewriteSpuIds(ObjectNode property, JsonNode oldSpuIdsNode, Set<Long> retainedSpuIds) {
        ArrayNode newSpuIdsNode = JsonUtils.getObjectMapper().createArrayNode();
        Set<Long> addedIds = new LinkedHashSet<>();
        boolean changed = false;
        for (JsonNode item : oldSpuIdsNode) {
            Long id = parseLong(item);
            if (id == null || !retainedSpuIds.contains(id) || !addedIds.add(id)) {
                changed = true;
                continue;
            }
            newSpuIdsNode.add(id);
            if (!item.isIntegralNumber()) {
                changed = true;
            }
        }
        if (newSpuIdsNode.size() != oldSpuIdsNode.size()) {
            changed = true;
        }
        if (changed) {
            property.set("spuIds", newSpuIdsNode);
        }
        return changed;
    }

    private boolean removeSpuId(ObjectNode property, JsonNode oldSpuIdsNode, Long removedSpuId) {
        ArrayNode newSpuIdsNode = JsonUtils.getObjectMapper().createArrayNode();
        boolean changed = false;
        for (JsonNode item : oldSpuIdsNode) {
            Long id = parseLong(item);
            if (Objects.equals(id, removedSpuId)) {
                changed = true;
                continue;
            }
            newSpuIdsNode.add(item);
        }
        if (changed) {
            property.set("spuIds", newSpuIdsNode);
        }
        return changed;
    }

    private Long parseLong(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isIntegralNumber() && node.canConvertToLong()) {
            return node.longValue();
        }
        if (!node.isTextual()) {
            return null;
        }
        try {
            return Long.valueOf(node.asText());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record CleanResult(boolean changed) {
    }

    private record RemoveResult(boolean changed, String property) {
    }

}
