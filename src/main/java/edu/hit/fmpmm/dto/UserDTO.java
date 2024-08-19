package edu.hit.fmpmm.dto;

import lombok.Data;

/**
 * 使用系统的用户
 */
@Data
public class UserDTO {
    private Integer id;
    private String phone;
    private String nickname;
    private String password;

    public UserDTO() {
    }

    public UserDTO(Integer id, String phone, String nickname, String password) {
        this.id = id;
        this.phone = phone;
        this.nickname = nickname;
        this.password = password;
    }
}
