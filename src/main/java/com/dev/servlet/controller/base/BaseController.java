package com.dev.servlet.controller.base;

import com.dev.servlet.domain.transfer.records.KeyPair;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.domain.transfer.response.IServletResponse;
import com.dev.servlet.core.annotation.Controller;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseController extends BaseRouterController {

    protected static final String LIST = "list";

    @Setter(AccessLevel.PROTECTED)
    private String webService;

    protected BaseController() {
        this.webService = webServiceFromClass(this.getClass());
    }

    private static String webServiceFromClass(Class<?> clazz) {
        return clazz.getAnnotation(Controller.class).value();
    }

    protected String redirectTo(String context) {
        return getNext("redirect:/api/v1/{webService}/{context}", context);
    }

    protected String redirectTo(Long id) {
        return redirectTo(LIST).concat("/" + id);
    }

    protected String forwardTo(String page) {
        return getNext("forward:pages/{webService}/{context}.jsp", page);
    }

    private String getNext(String next, String context) {
        String replace = next.replace("{webService}", this.webService);
        replace = replace.replace("{context}", context);
        return replace;
    }

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

    protected <U> IHttpResponse<U> newHttpResponse(int status, U response, String nextPath) {
        return HttpResponse.<U>newBuilder().statusCode(status).body(response).next(nextPath).build();
    }

    protected <U> IHttpResponse<U> newHttpResponse(int status, String nextPath) {
        return HttpResponse.<U>newBuilder().statusCode(status).next(nextPath).build();
    }

    protected <U> IHttpResponse<U> okHttpResponse(U response, String nextPath) {
        return newHttpResponse(200, response, nextPath);
    }
}
