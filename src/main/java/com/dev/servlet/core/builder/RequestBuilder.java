package com.dev.servlet.core.builder;

import com.dev.servlet.model.pojo.records.KeyPair;
import com.dev.servlet.model.pojo.records.Query;
import com.dev.servlet.model.pojo.records.Request;
import com.dev.servlet.util.URIUtils;
import lombok.Builder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create a {@linkplain Request} from the {@linkplain HttpServletRequest}.
 *
 * @since 1.4.0
 */
@Builder(builderClassName = "RequestCreator", builderMethodName = "newBuilder")
public class RequestBuilder {

    private final HttpServletRequest httpServletRequest;

    /**
     * This build method will be used to create the {@linkplain  Request} from the {@linkplain HttpServletRequest}.
     */
    public static class RequestCreator {
        private String endpoint;
        private String method;
        private List<KeyPair> body;
        private String token;
        private String id;
        private Query query;
        private int retry;

        public RequestCreator endpoint() {
            this.endpoint = httpServletRequest.getServletPath();
            return this;
        }

        public RequestCreator method() {
            this.method = httpServletRequest.getMethod();
            return this;
        }

        public RequestCreator body() {
            this.body = new ArrayList<>(URIUtils.getParameters(httpServletRequest));
            return this;
        }

        public RequestCreator token() {
            this.token = token(httpServletRequest);
            return this;
        }

        public RequestCreator id() {
            this.id = URIUtils.getResourceId(httpServletRequest);
            return this;
        }

        public RequestCreator query() {
            this.query = URIUtils.getQuery(httpServletRequest);
            return this;
        }

        public RequestCreator retry(int retry) {
            this.retry = retry;
            return this;
        }

        public RequestCreator complete() {
            return this.endpoint().method().body().token().id().query();
        }

        public Request build() {
            this.setUp();

            return Request.of(endpoint, method, body, token, query, retry);
        }

        private String token(HttpServletRequest request) {
            return (String) request.getSession().getAttribute("token");
        }

        private void setUp() {
            if (id != null) {
                // Here we're going to remove the last part of the endpoint
                // Example: /api/v1/products/1 become /api/v1/products/{id}
                endpoint = endpoint.substring(0, endpoint.lastIndexOf("/"));
                endpoint = endpoint.concat("/{id}");

                // then we add the id to the body
                addBody(new KeyPair("id", id));
            }

            // TODO: validations
        }

        private void addBody(KeyPair id) {
            if (body == null) {
                body = new ArrayList<>();
            }
            body.add(id);
        }
    }
}