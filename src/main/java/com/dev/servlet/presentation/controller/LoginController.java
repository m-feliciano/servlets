package com.dev.servlet.presentation.controller;


import com.dev.servlet.application.dto.UserDTO;
import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.application.dto.response.HttpResponse;
import com.dev.servlet.application.dto.response.IHttpResponse;
import com.dev.servlet.application.service.LoginService;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.Property;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.model.pojo.enums.RequestMethod;
import com.dev.servlet.domain.service.UserService;
import com.dev.servlet.presentation.controller.base.BaseController;
import lombok.NoArgsConstructor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.dev.servlet.core.util.CryptoUtils.isValidToken;

@NoArgsConstructor
@Singleton
@Controller("login")
public class LoginController extends BaseController<User, Long> {

    private static final String FORWARD_PAGES_FORM_LOGIN_JSP = "forward:pages/formLogin.jsp";
    private static final String FORWARD_PAGES_USER_FORM_CREATE_USER_JSP = "forward:pages/user/formCreateUser.jsp";

    private LoginService loginService;
    private UserService userService;

    @Inject
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
    }

    /**
     * Forward to register page
     *
     * @return {@linkplain HttpResponse}
     */
    @RequestMapping(value = "/registerPage", requestAuth = false)
    public IHttpResponse<String> forwardRegister() {
        return HttpResponse.<String>ok().next(FORWARD_PAGES_USER_FORM_CREATE_USER_JSP).build();
    }

    /**
     * Login user.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with the updated user
     */
    @RequestMapping(value = "/form", requestAuth = false)
    public IHttpResponse<String> form(Request request, @Property("homepage") String homepage) {
        String next;
        if (isValidToken(request.token())) {
            next = "redirect:/" + homepage;
        } else {
            next = FORWARD_PAGES_FORM_LOGIN_JSP;
        }

        return HttpResponse.<String>ok().next(next).build();
    }

    /**
     * Login user.
     *
     * @param request      {@linkplain Request}
     * @return {@linkplain IHttpResponse} with the updated user
     * @throws ServiceException if the user is not found
     */
    @RequestMapping(
            value = "/login",
            method = RequestMethod.POST,
            requestAuth = false,
            validators = {
                    @Validator(values = "login", constraints = {
                            @Constraints(isEmail = true, message = "Email must be valid"),
                    }),
                    @Validator(values = "password", constraints = {
                            @Constraints(minLength = 5, maxLength = 30, message = "Password must have at least {0} characters")
                    })
            })
    public IHttpResponse<UserDTO> login(Request request, @Property("homepage") String homepage) throws ServiceException {

        UserDTO user = loginService.login(request, userService);
        return okHttpResponse(user, "redirect:/" + homepage);
    }

    /**
     * Logout user.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with the updated user
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public IHttpResponse<String> logout(Request request) {
        loginService.logout(request);
        return HttpResponse.<String>ok().next(FORWARD_PAGES_FORM_LOGIN_JSP).build();
    }
}

