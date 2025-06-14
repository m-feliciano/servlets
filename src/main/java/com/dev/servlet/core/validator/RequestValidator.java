package com.dev.servlet.core.validator;


import com.dev.servlet.application.dto.request.Request;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.CollectionUtils;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.domain.model.pojo.domain.User;
import com.dev.servlet.domain.model.pojo.enums.RoleType;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static com.dev.servlet.core.util.CryptoUtils.isValidToken;
import static com.dev.servlet.core.util.ThrowableUtils.throwIfTrue;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RequestValidator {

    public static final String PERMISSION_ERROR = "User does not have permission to access this endpoint";
    public static final String API_NOT_IMPLEMENTED_ERROR = "API Not implemented";
    public static final String AUTHENTICATION_REQUIRED = "Authentication required.";

    public static void validate(EndpointParser endpoint, RequestMapping mapping, Request request) throws ServiceException {
        validateMethod(request.method(), mapping);
        validateAuth(request.token(), mapping);
        validateRoles(mapping.roles(), request);
        validateApiVersion(endpoint.getApiVersion(), mapping);
        validateConstraints(mapping.validators(), request);
    }

    private static void validateRoles(RoleType[] roles, Request request) throws ServiceException {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }

        User user = CryptoUtils.getUser(request.token());

        for (RoleType role : roles) {
            boolean userDoesntHavePermission = !user.hasRole(role);
            throwIfTrue(userDoesntHavePermission, 403, PERMISSION_ERROR);
        }
    }

    private static void validateApiVersion(String apiVersion, RequestMapping mapping) throws ServiceException {
        boolean shouldThrow = !mapping.apiVersion().equals(apiVersion);
        throwIfTrue(shouldThrow, 400, API_NOT_IMPLEMENTED_ERROR);
    }

    private static void validateConstraints(Validator[] validators, Request request) throws ServiceException {
        List<String> resultErrors = new ArrayList<>();

        for (Validator validator : validators) {
            for (String value : validator.values()) {
                var constraintValidator = new ConstraintValidator(validator.constraints());

                String parameter = request.getParameter(value);

                List<String> errors = constraintValidator.validate(parameter);
                if (!CollectionUtils.isEmpty(errors)) {
                    resultErrors.addAll(errors);
                }
            }
        }

        if (!CollectionUtils.isEmpty(resultErrors)) {
            throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, String.join("\n", resultErrors));
        }
    }

    private static void validateMethod(String method, RequestMapping mapping) throws ServiceException {
        boolean methodNotMatch = !mapping.method().getMethod().equals(method);
        throwIfTrue(methodNotMatch, 405, "Method not allowed. Expected: " + mapping.method() + ", but got: " + method);
    }

    private static void validateAuth(String token, RequestMapping mapping) throws ServiceException {
        boolean tokenInvalid = mapping.requestAuth() && !isValidToken(token);
        throwIfTrue(tokenInvalid, 401, AUTHENTICATION_REQUIRED);
    }
}