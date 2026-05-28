package cn.iocoder.yudao.module.system.framework.datapermission;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * {@link MenuDataPermissionRule} 的单元测试
 */
class MenuDataPermissionRuleTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MenuDataPermissionRule rule;

    @Mock
    private PermissionService permissionService;

    @Test
    public void testGetExpression_unrestricted() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            // mock 方法
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(1L);
            when(permissionService.getUserMenuScopeByUserId(1L)).thenReturn(null);

            // 调用，并断言
            assertNull(rule.getExpression("system_menu", new Alias("m")));
        }
    }

    @Test
    public void testGetExpression_managerScope() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            // mock 方法
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(145L);
            when(permissionService.getUserMenuScopeByUserId(145L)).thenReturn(CollUtil.newLinkedHashSet(10L, 20L));

            // 调用，并断言
            Expression expression = rule.getExpression("system_menu", new Alias("m"));
            assertEquals("m.id IN (10, 20)", expression.toString());
        }
    }

    @Test
    public void testGetExpression_emptyManagerScope() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            // mock 方法
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(145L);
            when(permissionService.getUserMenuScopeByUserId(145L)).thenReturn(Collections.emptySet());

            // 调用，并断言
            Expression expression = rule.getExpression("system_menu", new Alias("m"));
            assertEquals("null = null", expression.toString());
        }
    }

}
