package com.dev.servlet.controller;

import com.dev.servlet.controller.base.BaseController;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.impl.LoginModel;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.enums.RequestMethod;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.RequestMapping;
import com.dev.servlet.util.CryptoUtils;
import com.dev.servlet.util.PropertiesUtil;
import com.dev.servlet.validator.Constraints;
import com.dev.servlet.validator.Validator;
import lombok.NoArgsConstructor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;


@NoArgsConstructor
@Controller(path = "/login")
public final class LoginController extends BaseController<User, Long> {

    private String homepage;
    private static final String FORWARD_PAGES_FORM_LOGIN_JSP = "forward:pages/formLogin.jsp";
    private static final String FORWARD_PAGES_USER_FORM_CREATE_USER_JSP = "forward:pages/user/formCreateUser.jsp";

    @Inject
    public LoginController(LoginModel userModel) {
        super(userModel);
    }

    @PostConstruct
    public void init() {
        homepage = PropertiesUtil.getProperty("homepage");
    }

    private LoginModel getModel() {
        return (LoginModel) super.getBaseModel();
    }

    /**
     * Forward to register page
     *
     * @return {@linkplain IHttpResponse}
     */
    @RequestMapping(value = "/registerPage", requestAuth = false)
    public IHttpResponse<String> forwardRegister() {
        return HttpResponseImpl.<String>ok().next(FORWARD_PAGES_USER_FORM_CREATE_USER_JSP).build();
    }

    /**
     * Login user.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with the updated user
     */
    @RequestMapping(value = "/form", requestAuth = false)
    public IHttpResponse<String> form(Request request) {
        String next;
        if (CryptoUtils.verifyToken(request.token())) {
            next = "redirect:/" + homepage;
        } else {
            next = FORWARD_PAGES_FORM_LOGIN_JSP;
        }

        return HttpResponseImpl.<String>ok().next(next).build();
    }

    /**
     * Login user.
     *
     * @param request {@linkplain Request}
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
    public IHttpResponse<UserDTO> login(Request request) throws ServiceException {
        LoginModel model = getModel();
        UserDTO user = model.login(request);
        // OK
        return super.okHttpResponse(user, "redirect:/" + homepage);
    }

    /**
     * Logout user.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} with the updated user
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public IHttpResponse<String> logout(Request request) {
        LoginModel model = getModel();
        model.logout(request);

        return HttpResponseImpl.<String>ok().next(FORWARD_PAGES_FORM_LOGIN_JSP).build();
    }
}
