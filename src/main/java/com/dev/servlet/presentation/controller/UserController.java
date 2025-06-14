package com.dev.servlet.presentation.controller;


import com.dev.servlet.application.dto.UserDTO;
import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.application.dto.response.HttpResponse;
import com.dev.servlet.application.dto.response.IHttpResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.model.pojo.enums.RequestMethod;
import com.dev.servlet.domain.model.pojo.enums.RoleType;
import com.dev.servlet.domain.service.UserService;
import com.dev.servlet.presentation.controller.base.BaseController;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;

@NoArgsConstructor
@Singleton
@Controller("user")
public class UserController extends BaseController<User, Long> {

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Update user.
     *
     * @param request     {@linkplain Request}
     * @return {@linkplain IHttpResponse} with the updated user
     * @throws ServiceException if the user is not found
     */
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
    public IHttpResponse<UserDTO> update(Request request) throws ServiceException {
        UserDTO user = userService.update(request);
        // OK
        return newHttpResponse(204, user, redirectTo(user.getId()));
    }

    /**
     * Delete user.
     *
     * @param request     {@linkplain Request}
     * @return {@linkplain IHttpResponse} with no content {@linkplain Void}
     */
    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            roles = RoleType.ADMIN,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<Void> delete(Request request) throws ServiceException {
        userService.delete(request);
        return HttpResponse.<Void>ok().next(forwardTo("formLogin")).build();
    }

    /**
     * Create user.
     *
     * @param request     {@linkplain Request}
     * @return {@linkplain IHttpResponse}
     */
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
    public IHttpResponse<Void> register(Request request) throws ServiceException {
        userService.register(request);
        // Created
        return newHttpResponse(201, "redirect:/api/v1/login/form");
    }

    /**
     * List the user by id.
     *
     * @param request     {@linkplain Request}
     * @return {@linkplain IHttpResponse} of {@linkplain UserDTO}
     */
    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<UserDTO> list(Request request) throws ServiceException {
        UserDTO user = userService.findById(request);
        // OK
        return okHttpResponse(user, forwardTo("formListUser"));
    }
}
