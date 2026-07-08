package com.love.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @Valid
    @NotEmpty(message = "菜品不能为空")
    private List<OrderItemRequest> items;

    private String remark;

    private String loveNote;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "菜品不能为空")
        private Long dishId;

        @Min(value = 1, message = "菜品数量至少为1")
        private Integer quantity;
    }
}
