-- Monthly IB backend performance indexes.
-- Run during off-peak hours. The statements are idempotent for MySQL by
-- checking information_schema before each CREATE INDEX.

SET @schema_name = DATABASE();

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'tutorings'
      AND index_name = 'idx_tutorings_date_time'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_tutorings_date_time ON tutorings (`date`, `hour`, `minute`)',
    'SELECT ''idx_tutorings_date_time already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'tutorings'
      AND index_name = 'idx_tutorings_user_status'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_tutorings_user_status ON tutorings (request_user_id, tutoring_status)',
    'SELECT ''idx_tutorings_user_status already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'tutorings'
      AND index_name = 'idx_tutorings_created'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_tutorings_created ON tutorings (create_at)',
    'SELECT ''idx_tutorings_created already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'questions'
      AND index_name = 'idx_questions_author_created'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_questions_author_created ON questions (author_id, create_at)',
    'SELECT ''idx_questions_author_created already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'questions'
      AND index_name = 'idx_questions_status_created'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_questions_status_created ON questions (question_status, create_at)',
    'SELECT ''idx_questions_status_created already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'subscribe_user'
      AND index_name = 'idx_subscribe_user_user_status_created'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_subscribe_user_user_status_created ON subscribe_user (user_id, subscribe_status, create_at)',
    'SELECT ''idx_subscribe_user_user_status_created already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'boards'
      AND index_name = 'idx_boards_author_created'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_boards_author_created ON boards (author_id, create_at)',
    'SELECT ''idx_boards_author_created already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'board_files'
      AND index_name = 'idx_board_files_board'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_board_files_board ON board_files (board_id)',
    'SELECT ''idx_board_files_board already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'board_replys'
      AND index_name = 'idx_board_replys_board'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_board_replys_board ON board_replys (board_id)',
    'SELECT ''idx_board_replys_board already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'user_image'
      AND index_name = 'idx_user_image_user'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_user_image_user ON user_image (user_id)',
    'SELECT ''idx_user_image_user already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
