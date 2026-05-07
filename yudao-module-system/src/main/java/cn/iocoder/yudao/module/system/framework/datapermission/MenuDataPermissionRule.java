package cn.iocoder.yudao.module.system.framework.datapermission;

import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.google.common.collect.Sets;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class MenuDataPermissionRule implements DataPermissionRule {

    @Override
    public Set<String> getTableNames() {
        return Sets.newHashSet("system_menu");
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId != null && tableName.equals("system_menu") && !userId.equals(1L)) {
            ExpressionList<LongValue> right = new ExpressionList<>(Arrays.asList(
                    new LongValue(102),
                    new LongValue(1254),
                    new LongValue(2159),
                    new LongValue(2160),
                    new LongValue(1224),
                    new LongValue(105),
                    new LongValue(1261),
                    new LongValue(115),
                    new LongValue(1070),
                    new LongValue(1255),
                    new LongValue(114),
                    new LongValue(116),
                    new LongValue(1083),
                    new LongValue(2525),
                    new LongValue(2559),
                    new LongValue(2551),
                    new LongValue(2161),
                    new LongValue(1185),
                    new LongValue(2262),
                    new LongValue(2345),
                    new LongValue(2365),
                    new LongValue(2209),
                    new LongValue(2303),
                    new LongValue(2310),
                    new LongValue(2808),
                    new LongValue(2390),
                    new LongValue(2084),
                    new LongValue(2397),
                    new LongValue(2785),
                    new LongValue(2758),
                    new LongValue(4000)
                    ));
            // 构造 IN
            InExpression in = new InExpression();
            in.setLeftExpression(MyBatisUtils.buildColumn(tableName, tableAlias, "id"));
            in.setRightExpression(new ParenthesedExpressionList<>(right));
            in.setNot(true);
            return in;
        }
        return null;
    }
}
