package servlets;

import domain.User;
import org.apache.commons.lang3.time.StopWatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static servlets.base.Base.*;

public class LoginServlet extends BaseLogin {

    /**
     * Execute the action
     *
     * @param req  the req
     * @param resp the resp
     * @return the string
     */
    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getParameter("action") == null) {
            logger.error("Error: action can't be null");
            req.setAttribute(ERROR, "Action can't be null");
        }
        String request = req.getParameter("action");
        switch (request) {
            case "login" -> {
                return login(req, resp);
            }
            case "logout" -> {
                return logout(req, resp);
            }
            case "loginForm" -> {
                return loginForm();
            }
            default -> {
                logger.error("Error: action not found");
                req.setAttribute(ERROR, "Action not found");
            }
        }
        return FORWARD_PAGES_NOT_FOUND_JSP;
    }

    private String loginForm() {
        logger.info("doPOST redirecting to form login");
        return "forward:pages/formLogin.jsp";
    }

    /**
     * Login.
     *
     * @param req
     * @param resp
     * @return the path to the next page
     */
    private String login(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getParameter("success") != null) {
            logger.info("forward to login page");
            return FORWARD_PAGES_FORM_LOGIN_JSP;
        }

        StopWatch sw = new StopWatch();
        sw.start();

        logger.info("Validate user to login");
        User user = controller.findByLogin(req.getParameter(EMAIL));
        if (user == null || !user.equals(user.getLogin(), req.getParameter(PASSWORD))) {
            req.setAttribute("invalid", "User or password invalid.");
            logger.info("User or password invalid.");
            req.setAttribute(EMAIL, req.getParameter(EMAIL));
            sw.stop();
            logger.info("Time to validate user to login: {}ms", sw.getTime());
            return FORWARD_PAGES_FORM_LOGIN_JSP;
        }

        HttpSession session = req.getSession();
        session.setAttribute("userLogged", user.getLogin());
        logger.info("User logged: {}", user.getLogin());

        sw.stop();
        logger.info("Time to login: {}ms", sw.getTime());

        return REDIRECT_PRODUCT_ACTION_LIST_PRODUCTS;
    }

    /**
     * Logout.
     *
     * @param req
     * @param resp
     * @return the path to login page
     */
    private String logout(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("logout: invalidating session");
        HttpSession session = req.getSession();
        session.invalidate();
        logger.info("logout: session is null");
        logger.info("logout: redirecting to form login");
        return FORWARD_PAGES_FORM_LOGIN_JSP;
    }
}