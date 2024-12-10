package com.dev.servlet.business;

import com.dev.servlet.controllers.UserController;
import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.interfaces.ResourceMapping;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.mapper.UserMapper;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.enums.PerfilEnum;
import com.dev.servlet.pojo.enums.StatusEnum;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import com.dev.servlet.utils.CacheUtil;
import com.dev.servlet.utils.CryptoUtils;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * The type User business.
 * <p>
 * This class is responsible for the user business logic.
 *
 * @see BaseBusiness
 */
@NoArgsConstructor
@Singleton
@ResourcePath("user")
public class UserBusiness extends BaseBusiness<User, Long, UserDTO> {

    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String USER = "user";
    public static final String TOKEN = "token";

    @Inject
    public UserBusiness(UserController controller) {
        super(controller);
        this.mapper = new UserMapper();
    }

    @Override
    protected UserController getController() {
        return (UserController) super.getController();
    }

    /**
     * Redirect to Edit user.
     *
     * @param request {@link Request}
     * @return {@link Response}
     */
    @ResourceMapping("registerUser")
    public Response register(Request request) throws ServiceException {
        LOGGER.trace("");

        /// Both passwords are encrypted already
        var password = request.getParameter(PASSWORD);
        var confirmPassword = request.getParameter("confirmPassword");

        if (password == null || !password.equals(confirmPassword)) {
            Response.Data data = Response.Data.of("error", "Passwords do not match.");
            Response response = Response.of(data);
            return response.next(super.forwardTo("formCreateUser"));
        }

        String email = request.getRequiredParameter(LOGIN).toLowerCase();
        User user = new User();
        user.setLogin(email);
        user = this.find(user);

        if (user != null) {
            return Response.ofError(HttpServletResponse.SC_FORBIDDEN, "Login already in use.");
        }

        User userRegister = new User();
        userRegister.setLogin(email);
        userRegister.setPassword(password);  // already encrypted
        userRegister.setImgUrl(request.getParameter("imgUrl"));
        userRegister.setStatus(StatusEnum.ACTIVE.getValue());
        userRegister.setPerfis(List.of(PerfilEnum.DEFAULT.getCode()));

        super.save(userRegister);

        Response response = new Response(HttpServletResponse.SC_CREATED);
        response.data("info", "Success");
        return response.next(FORWARD_PAGES_FORM_LOGIN);
    }

    /**
     * Update user.
     *
     * @param request {@link Request}
     * @return {@link Response}
     */
    @ResourceMapping(UPDATE)
    public Response update(Request request, String token) throws ServiceException {
        LOGGER.trace("");

        User userToken = getUser(token);

        if (!userToken.getId().equals(Long.parseLong(request.getEntityId()))) {
            return Response.ofError(HttpServletResponse.SC_FORBIDDEN, NOT_FOUND);
        }

        String email = request.getRequiredParameter(LOGIN).toLowerCase();
        if (this.getController().isEmailAlreadyInUse(email, userToken.getId())) {
            String error = "Email already in use.";
            return Response.ofError(HttpServletResponse.SC_FORBIDDEN, error);
        }

        User user = new User(userToken.getId());
        user.setPerfis(userToken.getPerfis());
        user.setLogin(email.toLowerCase());
        user.setImgUrl(request.getParameter("imgUrl"));
        user.setPassword(request.getRequiredParameter(PASSWORD));
        user.setStatus(StatusEnum.ACTIVE.getValue());
        user = super.update(user);

        // TODO update JSON user config

        // the roles may have changed, so we need to clear the cache and generate a new token
        CacheUtil.clearAll(token);

        String jwtToken = CryptoUtils.generateJWTToken(user);

        UserDTO userDTO = super.fromEntity(user);
        Response response = Response.of(
                new Response.Data().add(TOKEN, jwtToken).add(USER, userDTO)
        );

        return response.next(redirectTo(userToken.getId()));
    }

    /**
     * List user by session.
     *
     * @param request {@link Request}
     * @return {@link Response}
     */
    @ResourceMapping(LIST)
    public Response list(Request request, String token) {
        LOGGER.trace("");

        if (request.getEntityId() == null) {
            return super.responseEntityNotFound(null);
        }

        User user = getUser(token);
        if (!user.getId().equals(Long.parseLong(request.getEntityId()))) {
            return Response.ofError(HttpServletResponse.SC_FORBIDDEN, NOT_FOUND);
        }

        user = super.findById(user.getId());

        UserDTO userDTO = super.fromEntity(user);
        Response response = Response.of(Response.Data.of("user", userDTO));

        return response.next(super.forwardTo("formListUser"));
    }

    /**
     * Delete one
     *
     * @param request {@link Request}
     * @return {@link Response}
     */
    @ResourceMapping(DELETE)
    public Response delete(Request request, String token) {
        LOGGER.trace("");

        User user = getUser(token);
        if (!user.getId().equals(Long.parseLong(request.getEntityId()))) {
            return Response.ofError(HttpServletResponse.SC_FORBIDDEN, NOT_FOUND);
        }

        super.delete(user);
        CacheUtil.clearAll(token);

        Response response = new Response(HttpServletResponse.SC_NO_CONTENT);
        return response.next(super.forwardTo("formLogin"));
    }

    /**
     * Find user.
     *
     * @param user {@link Request}
     * @return {@link Optional} of {@link User}
     */
    public Optional<User> findByLoginAndPassword(User user) {
        user = super.find(user);
        return Optional.ofNullable(user);
    }
}
