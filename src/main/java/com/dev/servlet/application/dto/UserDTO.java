package com.dev.servlet.application.dto;

import com.dev.servlet.domain.model.pojo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO for {@linkplain User}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO extends DataTransferObject<Long> {
    private Long id;
    private String login;
    private String password;
    private String imgUrl;
    private List<Long> perfis;
    private String config;
    private String token;
}

