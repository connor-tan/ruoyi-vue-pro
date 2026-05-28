package cn.iocoder.yudao.module.system.framework.datapermission;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MenuDataPermissionRule implements DataPermissionRule {

    @Resource
    private PermissionService permissionService;

    @Override
    public Set<String> getTableNames() {
        return Sets.newHashSet("system_menu");
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        if (!tableName.equals("system_menu")) {
            return null;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }
        Set<Long> menuScope = permissionService.getUserMenuScopeByUserId(userId);
        if (menuScope == null) {
            return null;
        }
        if (CollUtil.isEmpty(menuScope)) {
            return new EqualsTo(null, null);
        }
        ExpressionList<LongValue> right = new ExpressionList<>(
                CollectionUtils.convertList(menuScope, LongValue::new));
        return new InExpression(MyBatisUtils.buildColumn(tableName, tableAlias, "id"),
                new ParenthesedExpressionList<>(right));
    }
}
