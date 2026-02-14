package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 200)
    private String avatar;

    @Column(name = "wechat_openid", length = 100)
    private String wechatOpenid;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private Boolean enabled = true;
}
