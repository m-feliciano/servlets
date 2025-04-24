package com.dev.servlet.core.validator;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.util.CollectionUtils;
import com.dev.servlet.core.util.CryptoUtils;
import com.dev.servlet.core.util.EndpointParser;
import com.dev.servlet.domain.model.User;
import com.dev.servlet.domain.model.enums.RoleType;
import lombok.NoArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import static com.dev.servlet.core.util.CryptoUtils.isValidToken;
import static com.dev.servlet.core.util.ThrowableUtils.throwServiceError;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RequestValidator {
    public static void validate(EndpointParser endpoint, RequestMapping mapping, Request request) throws ServiceException {
        validateMethod(request.getMethod(), mapping);
        validateAuth(request.getToken(), mapping);
        validateRoles(mapping.roles(), request);
        validateApiVersion(endpoint.getApiVersion(), mapping);
        validateConstraints(mapping.validators(), request);
    }

    private static void validateRoles(RoleType[] roles, Request request) throws ServiceException {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        User user = CryptoUtils.getUser(request.getToken());
        for (RoleType role : roles) {
            boolean userDoesntHavePermission = !user.hasRole(role);
            if (userDoesntHavePermission) {
                throwServiceError(403, "User does not have permission to access this endpoint");
            }
        }
    }

    private static void validateApiVersion(String apiVersion, RequestMapping mapping) throws ServiceException {
        if (!mapping.apiVersion().equals(apiVersion)) {
            throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "API Not implemented");
        }
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
        if (!mapping.method().getMethod().equals(method)) {
            throwServiceError(405, "Method not allowed.");
        }
    }

    private static void validateAuth(String token, RequestMapping mapping) throws ServiceException {
        if (mapping.requestAuth() && !isValidToken(token)) {
            throwServiceError(401, "Authentication required.");
        }
    }
}
