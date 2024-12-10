package com.dev.servlet.builders;

import com.dev.servlet.pojo.records.KeyPair;
import com.dev.servlet.pojo.records.Query;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.URIUtils;
import lombok.Builder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * This class is used to create a {@link Request} from the {@link HttpServletRequest}.
 *
 * @since 1.4.0
 */
@Builder(builderClassName = "RequestCreator", buildMethodName = "create", builderMethodName = "factory")
public class RequestFactory {

    private final HttpServletRequest httpServletRequest;

    /**
     * This build method will be used to create the {@link Request} from the {@link HttpServletRequest}.
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
            this.endpoint = URIUtils.getEndpoint(httpServletRequest);
            return this;
        }

        public RequestCreator method() {
            this.method = httpServletRequest.getMethod();
            return this;
        }

        public RequestCreator body() {
            this.body = URIUtils.getParameters(httpServletRequest);
            return this;
        }

        public RequestCreator token() {
            this.token = getToken(httpServletRequest);
            return this;
        }

        public RequestCreator entityId() {
            this.id = URIUtils.getResourceId(httpServletRequest);
            return this;
        }

        public RequestCreator query() {
            this.query = URIUtils.getQuery(httpServletRequest);
            return this;
        }

        public void validate() {
            if (id != null && method.equalsIgnoreCase("GET")) {
                endpoint = endpoint.concat("/{id}");
            }

            // TODO: validations
        }

        public RequestCreator retry(int retry) {
            this.retry = retry;
            return this;
        }

        public RequestCreator complete() {
            return this.endpoint().method().body().token().entityId().query();
        }

        public Request create() {
            this.validate();

            return Request.builder()
                    .endpoint(endpoint).method(method).body(body).token(token).entityId(id).query(query).retry(retry)
                    .build();
        }

        private String getToken(HttpServletRequest request) {
            return (String) request.getSession().getAttribute("token");
        }
    }
}