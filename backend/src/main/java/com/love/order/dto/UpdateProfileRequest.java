package com.love.order.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String avatar;
    private String kitchenName;
    private String signature;
}
