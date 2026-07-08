package com.love.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BindPartnerRequest {
    @NotNull(message = "请提供对方ID")
    private Long partnerId;
}
