package com.dev.servlet.domain.service.internal;

import com.dev.servlet.domain.service.ILoginService;
import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.UserMapper;
import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.domain.model.User;
import com.dev.servlet.domain.service.IUserService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@NoArgsConstructor
@Singleton
public class LoginServiceImpl implements ILoginService {
    @Override
    public UserDTO login(Request request, IUserService userService) throws ServiceException {
        log.trace("");
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        User user = userService.findByLoginAndPassword(login, password).orElse(null);
        if (user == null) return null;
        UserDTO dto = UserMapper.full(user);
        dto.setToken(CryptoUtils.generateJwtToken(dto));
        return dto;
    }
    @Override
    public void logout(Request request) {
        log.trace("");
        CacheUtils.clearAll(request.getToken());
    }
}
