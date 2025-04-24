package com.dev.servlet.controller;
import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.domain.model.enums.RequestMethod;
import com.dev.servlet.domain.model.enums.RoleType;
import com.dev.servlet.domain.service.IUserService;
import com.dev.servlet.controller.base.BaseController;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import javax.inject.Inject;
import javax.inject.Singleton;

@NoArgsConstructor
@Singleton
@Controller("user")
public class UserController extends BaseController {

    @Inject
    private IUserService userService;

    @RequestMapping(
            value = "/update/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    }),
                    @Validator(values = "login", constraints = {
                            @Constraints(isEmail = true, message = "Login must be a valid email")
                    }),
                    @Validator(values = "password", constraints = {
                            @Constraints(minLength = 5, maxLength = 30, message = "Password length must be between {0} and {1} characters")
                    })
            })
    @SneakyThrows
    public IHttpResponse<UserDTO> update(Request request) {
        UserDTO user = userService.update(request);
        return newHttpResponse(204, user, redirectTo(user.getId()));
    }

    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            roles = RoleType.ADMIN,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<Void> delete(Request request) {
        userService.delete(request);
        return HttpResponse.<Void>next(forwardTo("formLogin")).build();
    }

    @RequestMapping(
            value = "/registerUser",
            method = RequestMethod.POST,
            requestAuth = false,
            validators = {
                    @Validator(values = "login", constraints = {
                            @Constraints(isEmail = true, message = "Login must be a valid email")
                    }),
                    @Validator(values = {"password", "confirmPassword"},
                            constraints = {
                                    @Constraints(minLength = 5, message = "Password must have at least {0} characters"),
                                    @Constraints(maxLength = 30, message = "Password must have at most {0} characters"),
                            }),
            })
    @SneakyThrows
    public IHttpResponse<Void> register(Request request) {
        userService.register(request);
        return newHttpResponse(201, "redirect:/api/v1/login/form");
    }

    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<UserDTO> list(Request request) {
        UserDTO user = userService.getById(request);
        return okHttpResponse(user, forwardTo("formListUser"));
    }
}
