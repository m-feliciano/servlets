package com.dev.servlet.infrastructure.security.wrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class SecurityRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String[]> modifiedParameters;
    public SecurityRequestWrapper(HttpServletRequest request, String password, String confirmPassword) {
        super(request);
        modifiedParameters = new HashMap<>(request.getParameterMap());
        if (password != null) {
            modifiedParameters.put("password", new String[]{password});
        }
        if (confirmPassword != null) {
            modifiedParameters.put("confirmPassword", new String[]{confirmPassword});
        }
    }

    @Override
    public String getParameter(String name) {
        String[] values = modifiedParameters.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return modifiedParameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return modifiedParameters;
    }
}
