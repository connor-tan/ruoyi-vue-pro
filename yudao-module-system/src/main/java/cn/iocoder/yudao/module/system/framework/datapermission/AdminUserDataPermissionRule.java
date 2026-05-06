package cn.iocoder.yudao.module.system.framework.datapermission;

import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.google.common.collect.Sets;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminUserDataPermissionRule implements DataPermissionRule {
    @Override
    public Set<String> getTableNames() {
        return Sets.newHashSet("system_users");
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        assert userId != null;
        if (tableName.equals("system_users") && !userId.equals(1L)) {
            return new NotEqualsTo(MyBatisUtils.buildColumn(tableName, tableAlias, "id"), new LongValue(1));
        }
        return null;
    }
}
