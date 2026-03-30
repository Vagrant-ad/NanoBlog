package com.vagrant.nanoblog.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;  // 用户名
    private String password;  // 明文密码
    private String nickname;  // 昵称
    private String email;     // 邮箱
    private Long roleId;     // 角色ID
}
