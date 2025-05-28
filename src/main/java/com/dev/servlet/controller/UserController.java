package com.dev.servlet.controller;

import com.dev.servlet.controller.base.BaseController;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.exception.ServiceException;
import com.dev.servlet.model.impl.UserModel;
import com.dev.servlet.model.pojo.domain.User;
import com.dev.servlet.model.pojo.enums.RequestMethod;
import com.dev.servlet.model.pojo.enums.RoleType;
import com.dev.servlet.model.pojo.records.HttpResponseImpl;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.core.IHttpResponse;
import com.dev.servlet.core.RequestMapping;
import com.dev.servlet.validator.Constraints;
import com.dev.servlet.validator.Validator;
import lombok.NoArgsConstructor;

import javax.inject.Inject;


@NoArgsConstructor
@Controller(path = "/user")
public final class UserController extends BaseController<User, Long> {

    @Inject
    public UserController(UserModel userModel) {
        super(userModel);
    }

    private UserModel getModel() {
        return (UserModel) super.getBaseModel();
    }

    /**
     * Update user.
     *
     * @param request {@linkplain Request}
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
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        UserModel model = this.getModel();
        UserDTO user = model.update(request);
        // OK
        String next = super.redirectTo(user.getId());
        return super.newHttpResponse(204, next);
    }

    /**
     * Delete user.
     *
     * @param request {@linkplain Request}
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
        UserModel model = this.getModel();
        model.delete(request);

        String next = super.forwardTo("formLogin");
        return HttpResponseImpl.<Void>ok().next(next).build();
    }

    /**
     * Create user.
     *
     * @param request {@linkplain Request}
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
        UserModel model = this.getModel();
        model.register(request);
        // Created
        return super.newHttpResponse(201,  "redirect:/api/v1/login/form");
    }

    /**
     * List the user by id.
     *
     * @param request {@linkplain Request}
     * @return {@linkplain IHttpResponse} of {@linkplain UserDTO}
     */
    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    public IHttpResponse<UserDTO> listById(Request request) throws ServiceException {
        UserModel model = this.getModel();
        UserDTO user = model.findById(request);
        // OK
        return super.okHttpResponse(user, super.forwardTo("formListUser"));
    }
}
