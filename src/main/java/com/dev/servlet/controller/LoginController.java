package com.dev.servlet.controller;
import com.dev.servlet.domain.service.ILoginService;
import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.Property;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.domain.model.enums.RequestMethod;
import com.dev.servlet.domain.service.IUserService;
import com.dev.servlet.controller.base.BaseController;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import static com.dev.servlet.core.util.CryptoUtils.isValidToken;

@Slf4j
@NoArgsConstructor
@Singleton
@Controller("login")
public class LoginController extends BaseController {
    private static final String FORWARD_PAGES_FORM_LOGIN_JSP = "forward:pages/formLogin.jsp";
    private static final String FORWARD_PAGES_USER_FORM_CREATE_USER_JSP = "forward:pages/user/formCreateUser.jsp";

    @Inject
    private ILoginService loginService;
    @Inject
    private IUserService userService;

    @RequestMapping(value = "/registerPage", requestAuth = false)
    public IHttpResponse<String> forwardRegister() {
        return HttpResponse.<String>next(FORWARD_PAGES_USER_FORM_CREATE_USER_JSP).build();
    }

    @RequestMapping(value = "/form", requestAuth = false)
    public IHttpResponse<String> form(Request request, @Property("homepage") String homepage) {
        String next;
        if (isValidToken(request.getToken())) {
            next = "redirect:/" + homepage;
        } else {
            next = FORWARD_PAGES_FORM_LOGIN_JSP;
        }
        return HttpResponse.<String>next(next).build();
    }

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
    @SneakyThrows
    public IHttpResponse<UserDTO> login(Request request, @Property("homepage") String homepage) {
        log.info("");
        UserDTO user = loginService.login(request, userService);
        if (user == null) {
            return HttpResponse.<UserDTO>newBuilder()
                    .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Invalid login or password")
                    .reasonText("Unauthorized")
                    .next(FORWARD_PAGES_FORM_LOGIN_JSP)
                    .build();
        }
        return okHttpResponse(user, "redirect:/" + homepage);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public IHttpResponse<String> logout(Request request) {
        loginService.logout(request);
        return HttpResponse.<String>next(FORWARD_PAGES_FORM_LOGIN_JSP).build();
    }
}
