package com.love.order.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.love.order.entity.Dish;
import com.love.order.entity.Order;
import com.love.order.entity.OrderItem;
import com.love.order.entity.User;
import com.love.order.handler.OrderWebSocketHandler;
import com.love.order.mapper.DishMapper;
import com.love.order.mapper.OrderItemMapper;
import com.love.order.mapper.OrderMapper;
import com.love.order.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final UserMapper userMapper;
    private final OrderWebSocketHandler wsHandler;

    /** 提交订单 */
    @Transactional
    public Order createOrder(Long userId, List<Map<String, Object>> items, String remark, String loveNote) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("菜品不能为空");
        }

        BigDecimal total = BigDecimal.ZERO;
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("pending");
        order.setRemark(remark);
        order.setLoveNote(loveNote);
        orderMapper.insert(order);

        for (Map<String, Object> item : items) {
            Long dishId = ((Number) item.get("dishId")).longValue();
            int quantity = ((Number) item.get("quantity")).intValue();

            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) throw new IllegalArgumentException("菜品不存在: " + dishId);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setDishId(dishId);
            orderItem.setDishName(dish.getName());
            orderItem.setDishImage(dish.getImage());
            orderItem.setPrice(dish.getPrice());
            orderItem.setQuantity(quantity);
            orderItemMapper.insert(orderItem);

            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalAmount(total);
        orderMapper.updateById(order);

        // 通知情侣对方
        notifyPartner(userId, "你有一条新订单，快去厨房看看~");

        return order;
    }

    /** 获取订单列表（含订单项） */
    public List<Order> getOrders(Long userId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        if (status != null && !status.isBlank()) {
            wrapper.eq(Order::getStatus, status);
        }
        List<Order> orders = orderMapper.selectList(wrapper);
        // 填充订单项
        for (Order order : orders) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            // items will be serialized via @JsonIgnore or a VO — we'll handle in controller
        }
        return orders;
    }

    /** 订单详情 */
    public Order getOrderDetail(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    /** 更新订单状态 */
    public void updateStatus(Long orderId, String status) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        orderMapper.updateById(order);

        // 通知下单人
        Order dbOrder = orderMapper.selectById(orderId);
        if (dbOrder != null) {
            String msg = switch (status) {
                case "confirmed" -> "你的订单已被确认，厨师开始准备啦~";
                case "cooking" -> "你的菜品正在烹饪中🔥";
                case "finished" -> "你的菜品已经做好啦，快来吃吧~";
                case "cancelled" -> "你的订单已被取消";
                default -> "订单状态已更新: " + status;
            };
            notifyUser(dbOrder.getUserId(), msg);
        }
    }

    /** 获取订单项 */
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
    }

    /** 推送消息给情侣对方 */
    private void notifyPartner(Long userId, String msg) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getPartnerId() != null) {
            wsHandler.sendToUser(user.getPartnerId(),
                    "{\"type\":\"order\",\"message\":\"" + msg + "\"}");
        }
    }

    /** 推送消息给指定用户 */
    private void notifyUser(Long userId, String msg) {
        wsHandler.sendToUser(userId,
                "{\"type\":\"order_status\",\"message\":\"" + msg + "\"}");
    }
}
