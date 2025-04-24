package com.dev.servlet.core.builder;

import com.dev.servlet.domain.transfer.records.KeyPair;
import com.dev.servlet.domain.transfer.records.Query;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.util.URIUtils;
import lombok.Builder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Builder(builderClassName = "RequestCreator", builderMethodName = "newBuilder")
public class RequestBuilder {
    private final HttpServletRequest httpServletRequest;
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
            return Request.builder()
                    .endpoint(endpoint).method(method).body(body).token(token).query(query).retry(retry)
                    .build();
        }

        private String token(HttpServletRequest request) {
            return (String) request.getSession().getAttribute("token");
        }

        private void setUp() {
            if (id != null) {
                endpoint = endpoint.substring(0, endpoint.lastIndexOf("/"));
                endpoint = endpoint.concat("/{id}");
                addBody(new KeyPair("id", id));
            }
        }

        private void addBody(KeyPair id) {
            if (body == null) {
                body = new ArrayList<>();
            }
            body.add(id);
        }
    }
}
