package servlets.user;

import com.mchange.util.AssertException;
import controllers.UserController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.Action;
import servlets.utils.RequestValidation;
import utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class BaseUser implements Action, RequestValidation {

    protected final Logger logger = LoggerFactory.getLogger(BaseUser.class);
    private final EntityManager em = JPAUtil.getEntityManager();
    protected final UserController controller = new UserController(em);

    /**
     * Execute.
     *
     * @param req  the req
     * @param resp the resp
     * @return the string
     */

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) {
        logger.error("Error: BaseUser.execute() is not implemented");
        throw new AssertException("This method should be overridden");
    }

    @Override
    public boolean validate(HttpServletRequest req, HttpServletResponse resp) {
        if (Objects.isNull(req.getParameter("id"))) {
            logger.warn("Error: Id can't be null");
            req.setAttribute("error", "User not found");
            return false;
        }
        return true;
    }
}