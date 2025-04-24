package com.dev.servlet.domain.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
