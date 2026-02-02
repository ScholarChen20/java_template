package com.example.yoyo_data.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private List<String> roles;

    private List<String> permissions;

    private String remark;
}