package crud.category;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controllers.CategoryController;
import crud.Action;
import entities.Category;
import infra.ConnectionFactory;
import utils.Validate;

public class ListCategory implements Action {

	@Override
	public String doService(HttpServletRequest req, HttpServletResponse resp) {
		System.out.println("doGET listing single category");

		if (Validate.isValid(req, "id")) {
			int id = Integer.parseInt(req.getParameter("id"));
			Connection conn = new ConnectionFactory().getConnection();
			CategoryController controller = new CategoryController(conn);
			Category cat = controller.findById(id);
			if (cat != null) {
				System.out.println(cat);
				req.setAttribute("category", cat);
				return "forward:category/formListCategory.jsp";
			}
		}
		return "forward:notFound.jsp";
	}

}