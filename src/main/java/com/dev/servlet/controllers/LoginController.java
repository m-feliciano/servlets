package com.dev.servlet.controllers;

import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.interfaces.Controller;
import com.dev.servlet.interfaces.IHttpResponse;
import com.dev.servlet.interfaces.RequestMapping;
import com.dev.servlet.model.LoginModel;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.CryptoUtils;
import com.dev.servlet.utils.PropertiesUtil;
import lombok.NoArgsConstructor;

import javax.inject.Inject;


@NoArgsConstructor
@Controller(path = "/login")
public final class LoginController extends BaseController<User, Long> {

    private static final String HOMEPAGE = PropertiesUtil.getProperty("homepage");
    private static final String FORWARD_PAGES_FORM_LOGIN_JSP = "forward:pages/formLogin.jsp";
    private static final String FORWARD_PAGES_USER_FORM_CREATE_USER_JSP = "forward:pages/user/formCreateUser.jsp";

    @Inject
    public LoginController(LoginModel userModel) {
        super(userModel);
    }

    private LoginModel getModel() {
        return (LoginModel) super.getBaseModel();
    }

    /**
     * Forward to register page
     *
     * @return {@link IHttpResponse}
     */
    @RequestMapping(value = "/registerPage", method = "GET")
    public IHttpResponse<String> forwardRegister() {
        return HttpResponse.ofNext(FORWARD_PAGES_USER_FORM_CREATE_USER_JSP);
    }

    /**
     * Login user.
     *
     * @param request {@link Request}
     * @return {@link IHttpResponse} with the updated user
     */
    @RequestMapping(value = "/form", method = "GET")
    public IHttpResponse<String> form(Request request) {

        if (CryptoUtils.verifyToken(request.getToken())) {
            return HttpResponse.ofNext("redirect:/view/" + HOMEPAGE);
        }

        return HttpResponse.ofNext(FORWARD_PAGES_FORM_LOGIN_JSP);
    }

    /**
     * Login user.
     *
     * @param request {@link Request}
     * @return {@link IHttpResponse} with the updated user
     * @throws ServiceException if the user is not found
     */
    @RequestMapping(value = "/login", method = "POST")
    public IHttpResponse<UserDTO> login(Request request) throws ServiceException {
        UserDTO user = getModel().login(request);
        // OK
        return super.buildHttpResponse(200, user, "redirect:/view/" + HOMEPAGE);
    }

    /**
     * Logout user.
     *
     * @param request {@link Request}
     * @return {@link IHttpResponse} with the updated user
     */
    @RequestMapping(value = "/logout")
    public IHttpResponse<String> logout(Request request) {
        getModel().logout(request);

        return HttpResponse.ofNext(FORWARD_PAGES_FORM_LOGIN_JSP);
    }
}
