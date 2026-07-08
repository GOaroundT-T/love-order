-- ==========================================
-- 情侣私房菜点餐小程序 数据库初始化脚本
-- PostgreSQL
-- ==========================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY,
    open_id VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    role VARCHAR(20) DEFAULT 'girlfriend',
    kitchen_name VARCHAR(100) DEFAULT '我的厨房',
    signature VARCHAR(200),
    partner_id BIGINT,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

INSERT INTO t_dish_category (id, name, sort, icon) VALUES
(1, '全部', 1, '🍽️'),
(2, '荤菜', 2, '🥩'),
(3, '蔬菜', 3, '🥬'),
(4, '热汤', 4, '🍲'),
(5, '主食', 5, '🍚'),
(6, '小炒', 6, '🍳'),
(7, '饮品', 7, '🥤')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_dish (id, name, category_id, price, rating, description, spicy_level) VALUES
(1, '红烧排骨', 2, 48.00, 5, '秘制酱料慢炖2小时，骨酥肉烂', 'mild'),
(2, '清炒时蔬', 3, 22.00, 4, '当季新鲜蔬菜，清脆爽口', 'none'),
(3, '番茄牛腩汤', 4, 38.00, 5, '浓郁番茄汤底，牛腩软烂入味', 'none'),
(4, '蛋炒饭', 5, 18.00, 4, '粒粒分明，金黄诱人', 'none'),
(5, '青椒肉丝', 6, 28.00, 4, '经典家常小炒，下饭神器', 'mild'),
(6, '冰镇柠檬水', 7, 12.00, 5, '鲜榨柠檬，清凉解暑', 'none'),
(7, '回锅肉', 2, 38.00, 5, '四川经典，肥而不腻', 'hot'),
(8, '蒜蓉西兰花', 3, 20.00, 4, '蒜香浓郁，营养健康', 'none')
ON CONFLICT (id) DO NOTHING;
