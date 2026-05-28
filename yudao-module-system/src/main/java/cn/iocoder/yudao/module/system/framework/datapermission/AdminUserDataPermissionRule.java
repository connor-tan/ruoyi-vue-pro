package cn.iocoder.yudao.module.system.framework.datapermission;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminUserDataPermissionRule implements DataPermissionRule {

    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;

    @Override
    public Set<String> getTableNames() {
        return Sets.newHashSet("system_users");
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        if (!tableName.equals("system_users")) {
            return null;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }
        Set<Long> userRoleIds = permissionService.getUserRoleIdListByUserId(userId);
        if (roleService.hasAnySuperAdmin(userRoleIds)) {
            return null;
        }

        Set<Long> superAdminRoleIds = CollectionUtils.convertSet(roleService.getRoleList(), RoleDO::getId,
                role -> role != null && RoleCodeEnum.isSuperAdmin(role.getCode()));
        if (CollUtil.isEmpty(superAdminRoleIds)) {
            return null;
        }
        Set<Long> superAdminUserIds = permissionService.getUserRoleIdListByRoleId(superAdminRoleIds);
        if (CollUtil.isEmpty(superAdminUserIds)) {
            return null;
        }
        ExpressionList<LongValue> right = new ExpressionList<>(
                CollectionUtils.convertList(superAdminUserIds, LongValue::new));
        InExpression in = new InExpression(MyBatisUtils.buildColumn(tableName, tableAlias, "id"),
                new ParenthesedExpressionList<>(right));
        in.setNot(true);
        return in;
    }
}
