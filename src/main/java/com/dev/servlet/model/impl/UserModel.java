package com.dev.servlet.model.impl;

import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.mapper.UserMapper;
import com.dev.servlet.model.impl.base.BaseModel;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.enums.RoleType;
import com.dev.servlet.model.pojo.enums.Status;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.persistence.dao.UserDAO;
import com.dev.servlet.util.CacheUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@Model
@NoArgsConstructor
public class UserModel extends BaseModel<User, Long> {

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String CONFIRM_PASSWORD = "confirmPassword";

    @Inject
    public UserModel(UserDAO userDAO) {
        super(userDAO);
    }

    @Override
    protected Class<UserDTO> getTransferClass() {
        return UserDTO.class;
    }

    @Override
    protected User toEntity(Object object) {
        return UserMapper.full((UserDTO) object);
    }

    @Override
    protected User getEntity(Request request) {
        return getUser(request.token());
    }

    /**
     * Find user.
     *
     * @param email
     * @param candidate
     * @return if the email is already in use
     */
    public boolean isEmailAvailable(String email, User candidate) {
        User user = new User();
        user.setLogin(email);
        user = this.find(user);

        if (user != null) {
            return user.getId().equals(candidate.getId());
        }

        return true;
    }

    /**
     * Redirect to Edit user.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain UserDTO} the user
     */
    public UserDTO register(Request request) throws ServiceException {
        log.trace("");

        /// Both passwords are encrypted already
        var password = request.getParameter(PASSWORD);
        var confirmPassword = request.getParameter(CONFIRM_PASSWORD);

        if (password == null || !password.equals(confirmPassword)) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Passwords do not match.");
        }

        User user = new User(request.getParameter(LOGIN).toLowerCase());
        user = this.find(user);

        if (user != null) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Login already in use.");
        }

        User newUser = new User();
        newUser.setLogin(request.getParameter(LOGIN).toLowerCase());
        newUser.setPassword(password);  // already encrypted
        newUser.setImgUrl(request.getParameter("imgUrl"));
        newUser.setStatus(Status.ACTIVE.getValue());
        newUser.setPerfis(List.of(RoleType.DEFAULT.getCode()));

        super.save(newUser);

        return UserMapper.full(newUser);
    }

    /**
     * Update user.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain UserDTO} the user
     */
    public UserDTO update(Request request) throws ServiceException {
        log.trace("");

        User fromToken = getEntity(request);
        validadeUserRequest(request, fromToken);

        String email = request.getParameter(LOGIN).toLowerCase();
        if (!this.isEmailAvailable(email, fromToken)) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Email already in use.");
        }

        User user = new User(fromToken.getId());
        user.setPerfis(fromToken.getPerfis());
        user.setLogin(email);
        user.setImgUrl(request.getParameter("imgUrl"));
        user.setPassword(request.getParameter(PASSWORD));
        user.setStatus(Status.ACTIVE.getValue());
        user = super.update(user);

        return UserMapper.full(user);
    }

    /**
     * List user by session.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain HttpResponseImpl}
     */
    public UserDTO findById(Request request) throws ServiceException {
        log.trace("");

        User fromToken = getEntity(request);
        validadeUserRequest(request, fromToken);

        User user = super.findById(fromToken.getId());
        return UserMapper.full(user);
    }

    /**
     * Delete user.
     *
     * @param request {@linkplain Request}
     */
    public void delete(Request request) throws ServiceException {
        log.trace("");

        User fromToken = this.getEntity(request);
        validadeUserRequest(request, fromToken);

        super.delete(fromToken);
        CacheUtil.clearAll(request.token());
    }

    /**
     * Validate if the user is the same as the one in the request.
     *
     * @param request
     * @param fromToken
     * @throws ServiceException
     */
    private static void validadeUserRequest(Request request, User fromToken) throws ServiceException {
        long requestId = Long.parseLong(request.id());

        if (!fromToken.getId().equals(requestId)) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "User not found.");
        }
    }

    /**
     * Find user.
     *
     * @param user {@linkplain Request}
     * @return {@linkplain Optional} of {@linkplain User}
     */
    public Optional<User> findByLoginAndPassword(User user) {
        return Optional.ofNullable(super.find(user));
    }
}
