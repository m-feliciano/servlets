package com.dev.servlet.model;

import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.mapper.UserMapper;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.CacheUtil;
import com.dev.servlet.utils.CryptoUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Setter
@NoArgsConstructor
@Model
public class LoginModel extends BaseModel<User, Long> {

    @Inject
    private UserModel userBusiness;

    @Override
    protected Class<? extends Identifier<Long>> getTransferClass() {
        return UserDTO.class;
    }

    @Override
    protected User toEntity(Object object) {
        return UserMapper.full((UserDTO) object);
    }

    /**
     * Login.
     *
     * @param request {@link Request}
     * @return the next path
     */
    public UserDTO login(Request request) throws ServiceException {
        LOGGER.trace("");

        User user = userBusiness.getEntity(request);

        user = userBusiness.findByLoginAndPassword(user).orElseThrow(
                () -> new ServiceException(HttpServletResponse.SC_UNAUTHORIZED, "Invalid login or password"));

        var userDTO = UserMapper.full(user);
        String jwtToken = CryptoUtils.generateJWTToken(userDTO);
        userDTO.setToken(jwtToken);
        return userDTO;
    }

    /**
     * Logout.
     *
     * @param request {@link Request}
     */
    public void logout(Request request) {
        LOGGER.trace("");

        CacheUtil.clearAll(request.getToken());
    }
}
