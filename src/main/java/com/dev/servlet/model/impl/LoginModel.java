package com.dev.servlet.model.impl;

import com.dev.servlet.dto.TransferObject;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.UserMapper;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.util.CacheUtil;
import com.dev.servlet.util.CryptoUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

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

    private UserModel userBusiness;

    @Inject
    public void setUserBusiness(UserModel userBusiness) {
        this.userBusiness = userBusiness;
    }

    @Override
    protected Class<? extends TransferObject<Long>> getTransferClass() {
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

        user = userBusiness.findByLoginAndPassword(user)
                .orElseThrow(() -> new ServiceException(HttpServletResponse.SC_UNAUTHORIZED, "Invalid login or password"));

        var userDTO = UserMapper.full(user);
        String jwtToken = CryptoUtils.generateJWTToken(userDTO);
        userDTO.setToken(jwtToken);
        return userDTO;
    }

    /**
     * Logout.
     *
     * @param request {@linkplain Request}
     */
    public void logout(Request request) {
        log.trace("");

        CacheUtil.clearAll(request.token());
    }
}
