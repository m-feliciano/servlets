package com.dev.servlet.dto;

import com.dev.servlet.pojo.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.dev.servlet.pojo.User}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Identifier<Long>, Serializable {
    private Long id;
    private String login;
    private String password;
    private String imgUrl;
    private List<Long> perfis;
    private String config;
}
