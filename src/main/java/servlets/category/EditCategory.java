package servlets.category;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EditCategory extends BaseCategory {

    /**
     * Execute.
     *
     * @param req  the req
     * @param resp the resp
     * @return the string
     */
    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("doGET editing a category");
        if (!this.validate(req, resp)) {
            return "forward:pages/not-found.jsp";
        }

        req.setAttribute("category", controller.findById(Long.parseLong(req.getParameter("id"))));
        return "forward:pages/category/formUpdateCategory.jsp";
    }

}