package com.dev.servlet.controllers;

import com.dev.servlet.interfaces.Controller;
import com.dev.servlet.interfaces.IHttpResponse;
import com.dev.servlet.interfaces.IServletResponse;
import com.dev.servlet.model.BaseModel;
import com.dev.servlet.pojo.Identifier;
import com.dev.servlet.pojo.records.HttpResponse;
import com.dev.servlet.pojo.records.KeyPair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Base Controller for the application
 *
 * @param <T> the entity extends {@link Identifier} of {@link K}
 * @param <K> the entity id
 * @implNote You should extend this class and provide a Model specialization, which extends {@link BaseModel}.
 * @see BaseModel
 */
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class BaseController<T extends Identifier<K>, K> {

    // Common actions
    protected static final String LIST = "/";
    protected static final String CREATE = "/create"; // Create resource
    protected static final String UPDATE = "/update";
    protected static final String DELETE = "/delete";

    // Redirect actions
    protected static final String NEW = "/new";
    protected static final String EDIT = "/edit";

    // Common paths
    private static final String FORWARD_TO = "forward:pages{webService}/{context}.jsp"; // forward:pages/product/formCreateProduct.jsp
    private static final String REDIRECT_TO = "redirect:/view{webService}{context}"; // redirect:/view/product/?id=1

    private BaseModel<T, K> baseModel;
    private String webService;

    protected BaseController(BaseModel<T, K> baseModel) {
        this.baseModel = baseModel;
        this.webService = this.getClass().getAnnotation(Controller.class).path();
    }

    /**
     * Redirect to the path
     *
     * @param context {@link String}
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
    protected String redirectTo(K id) {
        return redirectTo("/?id=").concat(String.valueOf(id));
    }

    /**
     * Forward to the path
     *
     * @param page {@link String}
     * @return
     */
    protected String forwardTo(String page) {
        return getNext(FORWARD_TO, page);
    }

    /**
     * Get the next path
     *
     * @param webService {@link String}
     * @param context    {@link String}
     * @return {@link String}
     */
    private String getNext(String webService, String context) {
        String replace = webService.replace("{webService}", this.webService);
        replace = replace.replace("{context}", context);
        return replace;
    }

    /**
     * Build the {@link IServletResponse} object
     *
     * @param response {@link Set} of {@link KeyPair} - the response data
     * @param next     the next path
     */
    protected IServletResponse getServletResponseOf(Set<KeyPair> response, String next) {
        return new IServletResponse() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public Set<KeyPair> getResponse() {
                return response;
            }

            @Override
            public String getNext() {
                return next;
            }
        };
    }

    /**
     * Build the {@link IServletResponse} object
     *
     * @param response - the response data
     * @param nextPath the next path
     * @param <U>      the response type
     */
    protected <U> IHttpResponse<U> buildHttpResponse(int status, U response, String nextPath) {
        return HttpResponse.<U>builder().status(status).response(response).next(nextPath).build();
    }
}
