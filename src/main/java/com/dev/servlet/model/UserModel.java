package com.dev.servlet.model;

import com.dev.servlet.dao.UserDAO;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.mapper.UserMapper;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.enums.PerfilEnum;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.CacheUtil;
import lombok.NoArgsConstructor;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * The type User business.
 * <p>
 * This class is responsible for the user business logic.
 *
 * @see BaseModel
 */
@Model
@NoArgsConstructor
public class UserModel extends BaseModel<User, Long> {

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String USER_NOT_FOUND = "User not found.";
    private static final String CONFIRM_PASSWORD = "confirmPassword";

    @Inject
    public UserModel(UserDAO userDAO) {
        super(userDAO);
    }

    public UserDAO getDAO() {
        return (UserDAO) super.getBaseDAO();
    }

    @Override
    protected Class<? extends Identifier<Long>> getTransferClass() {
        return UserDTO.class;
    }

    @Override
    protected User toEntity(Object object) {
        return UserMapper.full((UserDTO) object);
    }

    /**
     * Find user.
     *
     * @param email
     * @param id
     * @return if the email is already in use
     */
    public boolean isEmailAlreadyInUse(String email, Long id) {
        User user = new User();
        user.setLogin(email);
        user = this.find(user);

        return user != null && !user.getId().equals(id);
    }

    /**
     * Redirect to Edit user.
     *
     * @param request {@link Request}
     * @return {@link UserDTO} the user
     */
    public UserDTO register(Request request) throws ServiceException {
        LOGGER.trace("");

        /// Both passwords are encrypted already
        var password = request.getRequiredParameter(PASSWORD);
        var confirmPassword = request.getRequiredParameter(CONFIRM_PASSWORD);

        if (password == null || !password.equals(confirmPassword)) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Passwords do not match.");
        }

        String email = request.getRequiredParameter(LOGIN).toLowerCase();
        User user = new User();
        user.setLogin(email);
        user = this.find(user);

        if (user != null) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Login already in use.");
        }

        User userRegister = new User();
        userRegister.setLogin(email);
        userRegister.setPassword(password);  // already encrypted
        userRegister.setImgUrl(request.getParameter("imgUrl"));
        userRegister.setStatus(StatusEnum.ACTIVE.getValue());
        userRegister.setPerfis(List.of(PerfilEnum.DEFAULT.getCode()));

        super.save(userRegister);

        return UserMapper.full(userRegister);
    }

    /**
     * Update user.
     *
     * @param request {@link Request}
     * @return {@link UserDTO} the user
     */
    public UserDTO update(Request request) throws ServiceException {
        LOGGER.trace("");

        User userToken = getUser(request.getToken());

        if (!userToken.getId().equals(Long.parseLong(request.getEntityId()))) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, USER_NOT_FOUND);
        }

        String email = request.getRequiredParameter(LOGIN).toLowerCase();
        if (this.isEmailAlreadyInUse(email, userToken.getId())) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Email already in use.");
        }

        User user = new User(userToken.getId());
        user.setPerfis(userToken.getPerfis());
        user.setLogin(email.toLowerCase());
        user.setImgUrl(request.getParameter("imgUrl"));
        user.setPassword(request.getRequiredParameter(PASSWORD));
        user.setStatus(StatusEnum.ACTIVE.getValue());
        user = super.update(user);

        return UserMapper.full(user);
    }

    /**
     * List user by session.
     *
     * @param request {@link Request}
     * @return {@link HttpResponse}
     */
    public UserDTO findById(Request request) throws ServiceException {
        LOGGER.trace("");

        if (request.getEntityId() == null) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "User # null not found.");
        }

        User user = getUser(request.getToken());
        if (!user.getId().equals(Long.parseLong(request.getEntityId()))) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, USER_NOT_FOUND);
        }

        user = super.findById(user.getId());

        return UserMapper.full(user);
    }

    /**
     * Delete user.
     *
     * @param request {@link Request}
     */
    public void delete(Request request) throws ServiceException {
        LOGGER.trace("");

        User user = getUser(request.getToken());
        if (!user.getId().equals(Long.parseLong(request.getEntityId()))) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, USER_NOT_FOUND);
        }

        super.delete(user);
        CacheUtil.clearAll(request.getToken());
    }

    /**
     * Find user.
     *
     * @param user {@link Request}
     * @return {@link Optional} of {@link User}
     */
    public Optional<User> findByLoginAndPassword(User user) {
        return Optional.ofNullable(super.find(user));
    }
}
