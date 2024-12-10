package servlets.auth;

import com.dev.servlet.filter.wrappers.SecurityRequestWrapper;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityRequestWrapperTest {

    private SecurityRequestWrapper wrapper;
    private final String encryptedPassword = "encryptedPassword";

    @Before
    public void setUp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("password", new String[]{"originalPassword"});
        parameterMap.put("confirmPassword", new String[]{"originalConfirmPassword"});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("password")).thenReturn("originalPassword");
        when(request.getParameter("confirmPassword")).thenReturn("originalConfirmPassword");

        wrapper = new SecurityRequestWrapper(request, encryptedPassword, encryptedPassword);
    }

    @Test
    public void testGetParameter() {
        assertEquals(encryptedPassword, wrapper.getParameter("password"));
        assertEquals(encryptedPassword, wrapper.getParameter("confirmPassword"));
    }

    @Test
    public void testGetParameterValues() {
        assertEquals(encryptedPassword, wrapper.getParameterValues("password")[0]);
        assertEquals(encryptedPassword, wrapper.getParameterValues("confirmPassword")[0]);
    }

    @Test
    public void testGetParameterMap() {
        Map<String, String[]> parameterMap = wrapper.getParameterMap();
        assertEquals(encryptedPassword, parameterMap.get("password")[0]);
        assertEquals(encryptedPassword, parameterMap.get("confirmPassword")[0]);
    }
}