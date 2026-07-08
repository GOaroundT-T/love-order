package com.love.order.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.love.order.dto.CreateOrderRequest;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> ALLOWED_STATUS = Set.of("pending", "confirmed", "cooking", "finished", "cancelled");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final UserMapper userMapper;
    private final OrderWebSocketHandler wsHandler;

    /** 提交订单 */
    @Transactional
    public Order createOrder(Long userId, List<CreateOrderRequest.OrderItemRequest> items, String remark, String loveNote) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("菜品不能为空");

        BigDecimal total = BigDecimal.ZERO;
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("pending");
        order.setRemark(remark);
        order.setLoveNote(loveNote);
        orderMapper.insert(order);

        for (CreateOrderRequest.OrderItemRequest item : items) {
            Long dishId = item.getDishId();
            int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
            if (quantity < 1) throw new IllegalArgumentException("菜品数量至少为1");

            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) throw new IllegalArgumentException("菜品不存在: " + dishId);
            if (Boolean.FALSE.equals(dish.getOnShelf())) throw new IllegalArgumentException("菜品已下架: " + dish.getName());

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

        notifyPartner(userId, "你有一条新的爱心点单，快去厨房看看~", order.getId(), order.getStatus());
        return order;
    }

    /** 获取当前用户和情侣之间的订单列表 */
    public List<Order> getOrders(Long userId, String status) {
        User user = userMapper.selectById(userId);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime);

        if (user != null && user.getPartnerId() != null) {
            wrapper.in(Order::getUserId, userId, user.getPartnerId());
        } else {
            wrapper.eq(Order::getUserId, userId);
        }

        if (status != null && !status.isBlank()) wrapper.eq(Order::getStatus, status);
        return orderMapper.selectList(wrapper);
    }

    /** 订单详情 */
    public Order getOrderDetail(Long currentUserId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return null;
        assertCanAccess(currentUserId, order);
        return order;
    }

    /** 更新订单状态 */
    public void updateStatus(Long currentUserId, Long orderId, String status) {
        if (status == null || !ALLOWED_STATUS.contains(status)) throw new IllegalArgumentException("订单状态不正确");

        Order dbOrder = orderMapper.selectById(orderId);
        if (dbOrder == null) throw new IllegalArgumentException("订单不存在");
        assertCanAccess(currentUserId, dbOrder);

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        orderMapper.updateById(order);

        String msg = switch (status) {
            case "confirmed" -> "你的订单已被确认，厨师开始准备啦~";
            case "cooking" -> "你的菜品正在烹饪中🔥";
            case "finished" -> "你的菜品已经做好啦，快来一起吃吧~";
            case "cancelled" -> "这次点单已取消，我们换个想吃的吧";
            default -> "订单状态已更新: " + status;
        };
        notifyUser(dbOrder.getUserId(), msg, orderId, status);
    }

    /** 获取订单项 */
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
    }

    private void assertCanAccess(Long currentUserId, Order order) {
        if (order.getUserId().equals(currentUserId)) return;
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser != null && order.getUserId().equals(currentUser.getPartnerId())) return;
        throw new IllegalArgumentException("无权查看或操作这个订单");
    }

    /** 推送消息给情侣对方 */
    private void notifyPartner(Long userId, String msg, Long orderId, String status) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getPartnerId() != null) {
            wsHandler.sendToUser(user.getPartnerId(), JSONUtil.toJsonStr(Map.of(
                    "type", "order",
                    "message", msg,
                    "orderId", orderId,
                    "status", status
            )));
        }
    }

    /** 推送消息给指定用户 */
    private void notifyUser(Long userId, String msg, Long orderId, String status) {
        wsHandler.sendToUser(userId, JSONUtil.toJsonStr(Map.of(
                "type", "order_status",
                "message", msg,
                "orderId", orderId,
                "status", status
        )));
    }
}
