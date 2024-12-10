package com.dev.servlet.mapper;

import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.pojo.User;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class UserMapper extends BaseMapper<User, UserDTO> {

    public static UserDTO from(User user) {
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
        return UserDTO.builder().id(user.getId()).login(user.getLogin()).build();
    }

    public static User from(UserDTO dto) {
        if (dto == null) return null;
        User user = new User(dto.getId());
        user.setLogin(dto.getLogin());
        user.setImgUrl(dto.getImgUrl());
        user.setPerfis(dto.getPerfis());
        user.setConfig(dto.getConfig());
        return user;
    }

    @Override
    public UserDTO fromEntity(User object) {
        return from(object);
    }

    @Override
    public User toEntity(UserDTO object) {
        return from(object);
    }
}
