package top.aiolife.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.aiolife.system.mapper.ISysMenuMapper;
import top.aiolife.system.pojo.entity.SysMenuEntity;
import top.aiolife.system.pojo.vo.MenuAdminVO;
import top.aiolife.system.pojo.vo.MenuRouteVO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 菜单服务测试，验证后端菜单路由排序字段注入与管理端原始数据返回。
 *
 * @author Ethan
 * @date 2026-06-02
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private ISysMenuMapper sysMenuMapper;

    @Test
    void shouldInjectSortAsRouteMetaOrderWhenMetaHasNoOrder() {
        SysMenuEntity menu = menu(100L, 0L, "foodRecord", "/my-hub/food-record", 5,
                "{\"title\":\"美食\",\"icon\":\"mdi:food-fork-drink\"}");
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(menu));

        List<MenuRouteVO> routes = service().getAccessibleMenuTree(List.of("admin"));

        assertEquals(1, routes.size());
        assertEquals("美食", routes.get(0).getMeta().get("title"));
        assertEquals("mdi:food-fork-drink", routes.get(0).getMeta().get("icon"));
        assertEquals(5, routes.get(0).getMeta().get("order"));
    }

    @Test
    void shouldOverrideExistingRouteMetaOrderWithSort() {
        SysMenuEntity menu = menu(101L, 0L, "dashboard", "/dashboard", 2,
                "{\"title\":\"仪表盘\",\"order\":99}");
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(menu));

        List<MenuRouteVO> routes = service().getAccessibleMenuTree(List.of("admin"));

        assertEquals(2, routes.get(0).getMeta().get("order"));
    }

    @Test
    void shouldCreateRouteMetaOrderWhenMetaIsBlankOrInvalid() {
        SysMenuEntity blankMetaMenu = menu(102L, 0L, "blank", "/blank", 3, null);
        SysMenuEntity invalidMetaMenu = menu(103L, 0L, "invalid", "/invalid", null, "{invalid-json");
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(blankMetaMenu, invalidMetaMenu));

        List<MenuRouteVO> routes = service().getAccessibleMenuTree(List.of("admin"));
        MenuRouteVO blankRoute = findRoute(routes, "blank");
        MenuRouteVO invalidRoute = findRoute(routes, "invalid");

        assertNotNull(blankRoute.getMeta());
        assertEquals(3, blankRoute.getMeta().get("order"));
        assertNotNull(invalidRoute.getMeta());
        assertEquals(0, invalidRoute.getMeta().get("order"));
    }

    @Test
    void shouldKeepSiblingRoutesSortedBySortThenId() {
        SysMenuEntity idLater = menu(200L, 0L, "idLater", "/id-later", 1, "{\"title\":\"later\"}");
        SysMenuEntity higherSort = menu(100L, 0L, "higherSort", "/higher-sort", 2, "{\"title\":\"higher\"}");
        SysMenuEntity idEarlier = menu(100L, 0L, "idEarlier", "/id-earlier", 1, "{\"title\":\"earlier\"}");
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(higherSort, idLater, idEarlier));

        List<MenuRouteVO> routes = service().getAccessibleMenuTree(List.of("admin"));

        assertEquals("idEarlier", routes.get(0).getName());
        assertEquals("idLater", routes.get(1).getName());
        assertEquals("higherSort", routes.get(2).getName());
    }

    @Test
    void shouldNotInjectRuntimeOrderIntoAdminTreeMeta() {
        SysMenuEntity menu = menu(104L, 0L, "foodRecord", "/my-hub/food-record", 5,
                "{\"title\":\"美食\"}");
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(menu));

        List<MenuAdminVO> adminTree = service().getAdminMenuTree();

        assertEquals("美食", adminTree.get(0).getMeta().get("title"));
        assertTrue(!adminTree.get(0).getMeta().containsKey("order"));
        assertEquals(5, adminTree.get(0).getSort());
    }

    private MenuServiceImpl service() {
        return new MenuServiceImpl(sysMenuMapper, new ObjectMapper());
    }

    private MenuRouteVO findRoute(List<MenuRouteVO> routes, String name) {
        return routes.stream()
                .filter(route -> name.equals(route.getName()))
                .findFirst()
                .orElseThrow();
    }

    private SysMenuEntity menu(Long id, Long parentId, String name, String path, Integer sort, String meta) {
        SysMenuEntity entity = new SysMenuEntity();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setName(name);
        entity.setPath(path);
        entity.setComponent("dashboard/home/index");
        entity.setMeta(meta);
        entity.setSort(sort);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        return entity;
    }
}
