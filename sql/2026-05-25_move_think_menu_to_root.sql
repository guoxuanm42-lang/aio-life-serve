-- 闪念菜单提升为一级菜单（从“记录”分组中移出）
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 将 sys_menu 中“闪念”(id=1503) 从 parent_id=1500（记录）提升为 parent_id=0（一级菜单）
-- 2) 将菜单 path 从 /my-hub/think 改为 /think（避免继续归类到 /my-hub 分组）
-- 3) 调整 sort 与 meta.order，保证在侧边栏位置稳定（紧跟“记录”之后）

UPDATE `sys_menu`
SET
  `parent_id` = 0,
  `path` = '/think',
  `sort` = 4,
  `meta` = JSON_SET(
    COALESCE(`meta`, JSON_OBJECT()),
    '$.title', '闪念',
    '$.icon', 'mdi:lightbulb-on-outline',
    '$.order', 4,
    '$.backTop', false,
    '$.keepAlive', true
  )
WHERE `id` = 1503;

