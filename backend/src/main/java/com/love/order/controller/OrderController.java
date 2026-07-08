package com.love.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.love.order.common.R;
import com.love.order.entity.Order;
import com.love.order.entity.OrderItem;
import com.love.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 提交订单 */
    @PostMapping("/create")
    public R<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        String remark = (String) body.get("remark");
        String loveNote = (String) body.get("loveNote");

        Order order = orderService.createOrder(userId, items, remark, loveNote);
        List<OrderItem> orderItems = orderService.getOrderItems(order.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("userId", order.getUserId());
        result.put("status", order.getStatus());
        result.put("totalAmount", order.getTotalAmount());
        result.put("remark", order.getRemark());
        result.put("loveNote", order.getLoveNote());
        result.put("items", orderItems.stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("dishId", item.getDishId());
            m.put("dishName", item.getDishName());
            m.put("dishImage", item.getDishImage());
            m.put("price", item.getPrice());
            m.put("quantity", item.getQuantity());
            return m;
        }).toList());
        result.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : null);
        return R.ok(result);
    }

    /** 获取订单列表 */
    @GetMapping("/list")
    public R<List<Map<String, Object>>> getOrders(@RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Order> orders = orderService.getOrders(userId, status);

        List<Map<String, Object>> result = orders.stream().map(order -> {
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
            m.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString().substring(0, 16) : "");
            return m;
        }).toList();

        return R.ok(result);
    }

    /** 订单详情 */
    @GetMapping("/{orderId}")
    public R<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderService.getOrderDetail(orderId);
        if (order == null) return R.fail("订单不存在");

        List<OrderItem> items = orderService.getOrderItems(orderId);
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
        m.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString().substring(0, 16) : "");
        return R.ok(m);
    }

    /** 更新订单状态 */
    @PutMapping("/{orderId}/status")
    public R<Void> updateStatus(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        orderService.updateStatus(orderId, status);
        return R.ok();
    }
}
