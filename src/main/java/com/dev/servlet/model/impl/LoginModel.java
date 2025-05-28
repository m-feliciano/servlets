package com.dev.servlet.model.impl;

import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.UserMapper;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.util.CryptoUtils;
import com.dev.servlet.util.CacheUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.Optional;

import static com.dev.servlet.util.ThrowableUtils.throwIfTrue;

/**
 * The type Login business.
 * <p>
 * This class is responsible for the login business logic.
 *
 * @apiNote This class provides no controller
 * @see BaseModel
 * @since 1.0
 */
@Slf4j
@Setter
@NoArgsConstructor
@Model
public class LoginModel extends BaseModel<User, Long> {

    private UserModel userModel;

    @Inject
    public LoginModel(UserModel userModel) {
        this.userModel = userModel;
    }

    @Override
    protected Class<UserDTO> getTransferClass() {
        return UserDTO.class;
    }

    @Override
    protected User toEntity(Object object) {
        return UserMapper.full((UserDTO) object);
    }

    /**
     * Login.
     *
     * @param request {@linkplain Request}
     * @return the next path
     */
    public UserDTO login(Request request) throws ServiceException {
        log.trace("");

        User user = new User(
                request.getParameter("login"),
                request.getParameter("password")
        );

        Optional<User> optional = userModel.findByLoginAndPassword(user);
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
