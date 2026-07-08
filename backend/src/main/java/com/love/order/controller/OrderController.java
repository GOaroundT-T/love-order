package com.love.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.love.order.common.R;
import com.love.order.dto.CreateOrderRequest;
import com.love.order.dto.UpdateOrderStatusRequest;
import com.love.order.entity.Order;
import com.love.order.entity.OrderItem;
import com.love.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 提交订单 */
    @PostMapping("/create")
    public R<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderRequest body) {
        Long userId = StpUtil.getLoginIdAsLong();
        Order order = orderService.createOrder(userId, body.getItems(), body.getRemark(), body.getLoveNote());
        return R.ok(toOrderMap(order));
    }

    /** 获取订单列表 */
    @GetMapping("/list")
    public R<List<Map<String, Object>>> getOrders(@RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Order> orders = orderService.getOrders(userId, status);
        return R.ok(orders.stream().map(this::toOrderMap).toList());
    }

    /** 订单详情 */
    @GetMapping("/{orderId}")
    public R<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Order order = orderService.getOrderDetail(userId, orderId);
        if (order == null) return R.fail("订单不存在");
        return R.ok(toOrderMap(order));
    }

    /** 更新订单状态 */
    @PutMapping("/{orderId}/status")
    public R<Void> updateStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest body) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.updateStatus(userId, orderId, body.getStatus());
        return R.ok();
    }

    private Map<String, Object> toOrderMap(Order order) {
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", order.getId());
        m.put("userId", order.getUserId());
        m.put("status", order.getStatus());
        m.put("totalAmount", order.getTotalAmount());
        m.put("remark", order.getRemark());
        m.put("loveNote", order.getLoveNote());
        m.put("items", items.stream().map(item -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("dishId", item.getDishId());
            im.put("dishName", item.getDishName());
            im.put("dishImage", item.getDishImage());
            im.put("price", item.getPrice());
            im.put("quantity", item.getQuantity());
            return im;
        }).toList());
        m.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString().replace('T', ' ').substring(0, 16) : "");
        return m;
    }
}
