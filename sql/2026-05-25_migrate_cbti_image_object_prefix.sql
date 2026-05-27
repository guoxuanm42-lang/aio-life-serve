-- CBTI 人格图片对象键前缀迁移
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 将 cbti_personality.image_object 从历史前缀（如 images/cbti/characters/SUDO.png）迁移为方案A前缀（cbti/SUDO.png）
-- 2) 保留文件名与扩展名，仅替换前缀目录

UPDATE `cbti_personality`
SET `image_object` = CONCAT('cbti/', SUBSTRING_INDEX(`image_object`, '/', -1))
WHERE `image_object` IS NOT NULL
  AND `image_object` <> ''
  AND `image_object` NOT LIKE 'cbti/%';

