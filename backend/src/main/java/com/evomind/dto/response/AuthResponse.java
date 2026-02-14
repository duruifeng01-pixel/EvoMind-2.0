package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;
    private Boolean isNewUser;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String phone;
        private String nickname;
        private String avatar;
    }
}
