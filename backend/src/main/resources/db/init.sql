-- ==========================================
-- 情侣私房菜点餐小程序 数据库初始化脚本
-- PostgreSQL
-- ==========================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(128),
    open_id VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    role VARCHAR(20) DEFAULT 'girlfriend',
    kitchen_name VARCHAR(100) DEFAULT '我们的厨房',
    signature VARCHAR(200),
    partner_id BIGINT,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 兼容旧库：如果表已存在，补齐账号登录字段
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS username VARCHAR(50);
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS password_hash VARCHAR(128);
CREATE UNIQUE INDEX IF NOT EXISTS idx_t_user_username ON t_user(username) WHERE username IS NOT NULL;

-- 菜品分类
CREATE TABLE IF NOT EXISTS t_dish_category (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0,
    icon VARCHAR(50),
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 菜品
CREATE TABLE IF NOT EXISTS t_dish (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    image VARCHAR(500),
    price DECIMAL(10,2) NOT NULL DEFAULT 0,
    rating INT DEFAULT 3,
    description VARCHAR(500),
    spicy_level VARCHAR(20) DEFAULT 'none',
    on_shelf BOOLEAN DEFAULT TRUE,
    sort INT DEFAULT 0,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    total_amount DECIMAL(10,2) DEFAULT 0,
    remark VARCHAR(500),
    love_note VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单项
CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    dish_name VARCHAR(100),
    dish_image VARCHAR(500),
    price DECIMAL(10,2),
    quantity INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 种子数据 ==========
-- demo 账号：
--   chef / 123456    厨师方
--   lover / 123456   点餐方
-- 密码使用 SHA-256，MVP 阶段便于部署演示；正式上线建议改 BCrypt。
INSERT INTO t_user (id, username, password_hash, nickname, role, kitchen_name, signature, partner_id, avatar) VALUES
(1001, 'chef', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '阿鱼', 'chef', '阿鱼的暖心厨房', '今晚也想把你喂得开心一点 💗', 1002, '/static/images/default-avatar.png'),
(1002, 'lover', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '小乖', 'girlfriend', '我们的约会餐桌', '想吃你做的饭，也想和你一起吃饭', 1001, '/static/images/default-avatar.png')
ON CONFLICT (id) DO UPDATE SET
    username = EXCLUDED.username,
    password_hash = EXCLUDED.password_hash,
    nickname = EXCLUDED.nickname,
    role = EXCLUDED.role,
    kitchen_name = EXCLUDED.kitchen_name,
    signature = EXCLUDED.signature,
    partner_id = EXCLUDED.partner_id,
    avatar = EXCLUDED.avatar;

INSERT INTO t_dish_category (id, name, sort, icon) VALUES
(2, '荤菜', 2, '🥩'),
(3, '蔬菜', 3, '🥬'),
(4, '热汤', 4, '🍲'),
(5, '主食', 5, '🍚'),
(6, '小炒', 6, '🍳'),
(7, '饮品', 7, '🥤')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, sort = EXCLUDED.sort, icon = EXCLUDED.icon;

INSERT INTO t_dish (id, name, category_id, price, rating, description, spicy_level, on_shelf, sort) VALUES
(1, '红烧排骨', 2, 48.00, 5, '秘制酱料慢炖2小时，骨酥肉烂，适合被认真投喂的夜晚', 'mild', TRUE, 1),
(2, '清炒时蔬', 3, 22.00, 4, '当季新鲜蔬菜，清脆爽口，给今天补一点轻盈', 'none', TRUE, 2),
(3, '番茄牛腩汤', 4, 38.00, 5, '浓郁番茄汤底，牛腩软烂入味，一碗热汤像拥抱', 'none', TRUE, 3),
(4, '蛋炒饭', 5, 18.00, 4, '粒粒分明，金黄诱人，简单但很安心', 'none', TRUE, 4),
(5, '青椒肉丝', 6, 28.00, 4, '经典家常小炒，下饭神器，适合一起追剧', 'mild', TRUE, 5),
(6, '冰镇柠檬水', 7, 12.00, 5, '鲜榨柠檬，清凉解暑，给你一点甜甜的清爽', 'none', TRUE, 6),
(7, '回锅肉', 2, 38.00, 5, '四川经典，肥而不腻，今天就奖励一下自己', 'hot', TRUE, 7),
(8, '蒜蓉西兰花', 3, 20.00, 4, '蒜香浓郁，营养健康，是爱你的绿色信号', 'none', TRUE, 8),
(9, '可乐鸡翅', 2, 36.00, 5, '甜咸刚好，鸡翅软嫩，像一句没说出口的想你', 'none', TRUE, 9),
(10, '银耳雪梨羹', 7, 16.00, 5, '清润温柔，饭后一起慢慢喝', 'none', TRUE, 10)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    category_id = EXCLUDED.category_id,
    price = EXCLUDED.price,
    rating = EXCLUDED.rating,
    description = EXCLUDED.description,
    spicy_level = EXCLUDED.spicy_level,
    on_shelf = EXCLUDED.on_shelf,
    sort = EXCLUDED.sort;
