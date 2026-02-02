package com.example.yoyo_data.common.dto;

import com.example.yoyo_data.common.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class JwtUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private UserVO user;

    private List<String> roles;

    private List<String> permissions;

    private Long expireTime;

    private String token;
}