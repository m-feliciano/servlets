package com.dev.servlet.core.mapper;


import com.dev.servlet.application.dto.UserDTO;
import com.dev.servlet.domain.model.pojo.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static UserDTO full(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .login(user.getLogin())
                .imgUrl(user.getImgUrl())
                .perfis(user.getPerfis())
                .config(user.getConfig())
                .build();
    }

    public static UserDTO onlyId(User user) {
        if (user == null) return null;
        return UserDTO.builder().id(user.getId()).build();
    }

    public static User onlyId(UserDTO user) {
        if (user == null) return null;

        return new User(user.getId());
    }

    public static User full(UserDTO dto) {
        if (dto == null) return null;
        User user = new User(dto.getId());
        user.setLogin(dto.getLogin());
        user.setImgUrl(dto.getImgUrl());
        user.setPerfis(dto.getPerfis());
        user.setConfig(dto.getConfig());
        user.setPassword(dto.getPassword());
        return user;
    }
}

