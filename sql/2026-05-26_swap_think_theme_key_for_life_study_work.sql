UPDATE thought
SET theme_key = CASE theme_key
  WHEN 'blue' THEN 'cyan'
  WHEN 'cyan' THEN 'green'
  WHEN 'green' THEN 'blue'
  ELSE theme_key
END;

