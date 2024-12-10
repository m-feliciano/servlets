package com.dev.servlet.builders;

import com.dev.servlet.pojo.records.KeyPair;
import com.dev.servlet.pojo.records.Query;
import com.dev.servlet.pojo.records.Request;
import com.dev.servlet.utils.URIUtils;
import lombok.Builder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * This class is used to build the {@link Request} object.
 *
 * @author marcelo.feliciano
 * @since 1.4.0
 */
@Builder(builderClassName = "RequestBuilderBuilder")
public class RequestBuilder {

    private final HttpServletRequest httpServletRequest;

    /**
     * This build method will be used to create the {@link Request} from the {@link HttpServletRequest}.
     *
     * @author marcelo.feliciano
     */
    public static class RequestBuilderBuilder {
        public Request build() {
            String endpoint = URIUtils.getEndpoint(httpServletRequest);
            String resourceId = URIUtils.getResourceId(httpServletRequest);
            Query query = URIUtils.getQuery(httpServletRequest);
            List<KeyPair> parameters = URIUtils.getParameters(httpServletRequest);

            return Request.builder()
                    .endpoint(endpoint)
                    .entityId(resourceId)
                    .query(query)
                    .parameters(parameters)
                    .build();
        }
    }
}