package com.dev.servlet.controllers;

import com.dev.servlet.dao.UserDAO;
import com.dev.servlet.pojo.User;
import lombok.NoArgsConstructor;

import javax.inject.Inject;


@NoArgsConstructor
public final class UserController extends BaseController<User, Long> {

    @Inject
    public UserController(UserDAO userDAO) {
        super(userDAO);
    }

    @Override
    protected UserDAO getBaseDAO() {
        return (UserDAO) super.getBaseDAO();
    }

    public boolean isEmailAlreadyInUse(String email, Long id) {
        return this.getBaseDAO().isEmailAlreadyInUse(email, id);
    }
}
