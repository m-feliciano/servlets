package com.dev.servlet.presentation.controller.base;

import com.dev.servlet.application.transfer.records.KeyPair;
import com.dev.servlet.application.transfer.response.HttpResponse;
import com.dev.servlet.application.transfer.response.IHttpResponse;
import com.dev.servlet.application.transfer.response.IServletResponse;
import com.dev.servlet.core.annotation.Controller;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Base Controller for the application
 *
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseController extends BaseRouterController {

    // Common paths
    private static final String FORWARD_TO = "forward:pages/{webService}/{context}.jsp"; // forward:pages/product/formCreateProduct.jsp
    private static final String REDIRECT_TO = "redirect:/api/v1/{webService}/{context}";
    protected static final String LIST = "list";

    @Setter(AccessLevel.PROTECTED)
    private String webService;

    protected BaseController() {
        this.webService = webServiceFromClass(this.getClass());
    }

    private static String webServiceFromClass(Class<?> clazz) {
        return clazz.getAnnotation(Controller.class).value();
    }

    /**
     * Redirect to the path
     *
     * @param context
     * @return - the next path
     */
    protected String redirectTo(String context) {
        return getNext(REDIRECT_TO, context);
    }

    /**
     * Redirect to list entity
     *
     * @param id - the entity id
     * @return - the next path
     */
    protected String redirectTo(Long id) {
        return redirectTo("list").concat("/" + id);
    }

    /**
     * Forward to the path
     *
     * @param page
     */
    protected String forwardTo(String page) {
        return getNext(FORWARD_TO, page);
    }

    /**
     * Get the next path
     *
     * @param webService
     * @param context
     */
    private String getNext(String webService, String context) {
        String replace = webService.replace("{webService}", this.webService);
        replace = replace.replace("{context}", context);
        return replace;
    }

    /**
     * Build the {@linkplain IServletResponse} object
     *
     * @param response {@linkplain Set} of {@linkplain KeyPair} - the response data
     * @param next     the next path
     */
    protected IServletResponse newServletResponse(Set<KeyPair> response, String next) {
        return new IServletResponse() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public Set<KeyPair> body() {
                return response;
            }

            @Override
            public String next() {
                return next;
            }
        };
    }

    /**
     * Build the {@linkplain IServletResponse} object
     *
     * @param response - the response data
     * @param nextPath the next path
     * @param <U>      the response type
     */
    protected <U> IHttpResponse<U> newHttpResponse(int status, U response, String nextPath) {
        return HttpResponse.<U>newBuilder().statusCode(status).body(response).next(nextPath).build();
    }

    /**
     * Build the {@linkplain IServletResponse} object
     *
     * @param nextPath the next path
     * @param <U>      the response type
     */
    protected <U> IHttpResponse<U> newHttpResponse(int status, String nextPath) {
        return HttpResponse.<U>newBuilder().statusCode(status).next(nextPath).build();
    }

    protected <U> IHttpResponse<U> okHttpResponse(U response, String nextPath) {
        return newHttpResponse(200, response, nextPath);
    }
}
