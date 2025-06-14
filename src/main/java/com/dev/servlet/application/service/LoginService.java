package com.dev.servlet.application.service;


import com.dev.servlet.application.dto.UserDTO;
import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.UserMapper;
import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.service.UserService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Optional;

import static com.dev.servlet.core.util.ThrowableUtils.throwIfTrue;

/**
 * The type Login business.
 * <p>
 * This class is responsible for the login business logic.
 *
 * @apiNote This class provides no controller
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor
@Singleton
public class LoginService {

    /**
     * Login.
     *
     * @param request {@linkplain Request}
     * @return the next path
     */
    public UserDTO login(Request request, UserService userService) throws ServiceException {
        log.trace("");

        User user = new User(
                request.getParameter("login"),
                request.getParameter("password")
        );

        Optional<User> optional = userService.findByLoginAndPassword(user);
        throwIfTrue(optional.isEmpty(), 401, "Invalid login or password");

        UserDTO dto = UserMapper.full(optional.get());
        dto.setToken(CryptoUtils.generateJwtToken(dto));
        return dto;
    }

    /**
     * Logout.
     *
     * @param request {@linkplain Request}
     */
    public void logout(Request request) {
        log.trace("");
        CacheUtils.clearAll(request.token());
    }
}

