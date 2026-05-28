package cn.iocoder.yudao.module.system.convert.auth;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;
import static org.junit.jupiter.api.Assertions.*;

class AuthConvertTest {

    @Test
    void testConvert_separateVisibleMenusAndPermissions() {
        AdminUserDO user = new AdminUserDO().setId(145L).setUsername("manager");
        MenuDO studentMenu = new MenuDO().setId(10L).setParentId(ID_ROOT).setName("学生管理")
                .setType(MenuTypeEnum.MENU.getType()).setSort(1);
        MenuDO memberUserQueryButton = new MenuDO().setId(20L).setParentId(999L).setName("会员查询")
                .setType(MenuTypeEnum.BUTTON.getType()).setPermission("member:user:query").setSort(2);

        AuthPermissionInfoRespVO result = AuthConvert.INSTANCE.convert(user, Collections.emptyList(),
                ListUtil.toList(studentMenu), ListUtil.toList(studentMenu, memberUserQueryButton));

        assertTrue(result.getPermissions().contains("member:user:query"));
        assertEquals(1, result.getMenus().size());
        assertEquals("学生管理", result.getMenus().get(0).getName());
    }

}
