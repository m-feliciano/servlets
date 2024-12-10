package com.dev.servlet.controllers;

import com.dev.servlet.dto.ServiceException;
import com.dev.servlet.dto.UserDTO;
import com.dev.servlet.interfaces.Controller;
import com.dev.servlet.interfaces.IHttpResponse;
import com.dev.servlet.interfaces.RequestMapping;
import com.dev.servlet.model.UserModel;
import com.dev.servlet.pojo.User;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.Request;
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
     * @param request {@link Request}
     * @return {@link IHttpResponse} with the updated user
     * @throws ServiceException if the user is not found
     */
    @RequestMapping(value = UPDATE, method = "POST")
    public IHttpResponse<Void> update(Request request) throws ServiceException {
        UserDTO user = this.getModel().update(request);
        // OK
        return super.buildHttpResponse(200, null, super.redirectTo(user.getId()));
    }

    /**
     * Delete user.
     *
     * @param request {@link Request}
     * @return {@link IHttpResponse} with no content {@link Void}
     */
    @RequestMapping(value = DELETE, method = "POST")
    public IHttpResponse<Void> delete(Request request) throws ServiceException {
        this.getModel().delete(request);

        return HttpResponse.ofNext(super.forwardTo("formLogin"));
    }

    /**
     * Create user.
     *
     * @param request {@link Request}
     * @return {@link IHttpResponse}
     */
    @RequestMapping(value = "/registerUser", method = "POST")
    public IHttpResponse<Void> register(Request request) throws ServiceException {
        UserDTO user = this.getModel().register(request);
        // Created
        return super.buildHttpResponse(201, null, super.forwardTo("formLogin"));
    }

    /**
     * List the user by id.
     *
     * @param request {@link Request}
     * @return {@link IHttpResponse} of {@link UserDTO}
     */
    @RequestMapping(value = "/{id}", method = "GET")
    public IHttpResponse<UserDTO> listById(Request request) throws ServiceException {
        UserDTO user = this.getModel().findById(request);
        // OK
        return super.buildHttpResponse(200, user, super.forwardTo("formListUser"));
    }
}
