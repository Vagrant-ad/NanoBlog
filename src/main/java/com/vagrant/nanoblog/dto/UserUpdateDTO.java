package com.vagrant.nanoblog.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String email;

}
