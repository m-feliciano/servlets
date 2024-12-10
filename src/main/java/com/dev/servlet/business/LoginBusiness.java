package com.dev.servlet.business;

import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.interfaces.ResourceMapping;
import com.dev.servlet.interfaces.ResourcePath;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.pojo.records.Response;
import com.dev.servlet.utils.CacheUtil;
import com.dev.servlet.utils.CryptoUtils;
import com.dev.servlet.utils.PropertiesUtil;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * The type Login business.
 * <p>
 * This class is responsible for the login business logic.
 *
 * @apiNote This class provides no controller
 * @see BaseBusiness
 * @since 1.0
 */
@Setter
@NoArgsConstructor
@Singleton
@ResourcePath("login")
public class LoginBusiness extends BaseBusiness<User, Long, UserDTO> {

    @Inject
    private UserBusiness userBusiness;

    /**
     * Forward
     *
     * @param token
     * @return the next path
     */
    @ResourceMapping("form")
    public Response forwardLogin(String token) {
        LOGGER.trace("");

        if (!CryptoUtils.verifyToken(token)) {
            return Response.ofNext(FORWARD_PAGES_FORM_LOGIN);
        }

        String homepage = PropertiesUtil.getProperty("homepage");
        return Response.ofNext(redirectTo(homepage));
    }

    /**
     * Forward to create
     *
     * @return {@link Response}
     */
    @ResourceMapping("registerPage")
    public Response forwardRegister() {
        LOGGER.trace("");
        return Response.ofNext(FORWARD_CREATE_USER);
    }


    /**
     * Login.
     *
     * @param request {@link Request}
     * @return the next path
     */
    @ResourceMapping("login")
    public Response login(Request request) {
        LOGGER.trace("");

        User user = userBusiness.getEntity(request);

        Optional<User> optional = userBusiness.findByLoginAndPassword(user);
        if (optional.isEmpty()) {
            return Response.ofError(HttpServletResponse.SC_FORBIDDEN, "User or password invalid.");
        }

        user = optional.get();
        String jwtToken = CryptoUtils.generateJWTToken(user);

        var responseData = new Response.Data()
                .add("token", jwtToken)
                .add("user", userBusiness.fromEntity(user));

        String homepage = PropertiesUtil.getProperty("homepage");
        return Response.of(responseData).next("redirect:/view/" + homepage);
    }

    /**
     * Logout.
     *
     * @param token
     * @return {@link Response}
     */
    @ResourceMapping("logout")
    public Response logout(String token) {
        LOGGER.trace("");
        CacheUtil.clearAll(token);

        return Response.ofNext(FORWARD_PAGES_FORM_LOGIN);
    }
}
