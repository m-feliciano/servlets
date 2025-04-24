package com.dev.servlet.controller;
import com.dev.servlet.domain.transfer.dto.CategoryDTO;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.transfer.response.HttpResponse;
import com.dev.servlet.domain.transfer.response.IHttpResponse;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.annotation.Controller;
import com.dev.servlet.core.annotation.RequestMapping;
import com.dev.servlet.core.annotation.Validator;
import com.dev.servlet.domain.model.enums.RequestMethod;
import com.dev.servlet.domain.service.ICategoryService;
import com.dev.servlet.controller.base.BaseController;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@NoArgsConstructor
@Singleton
@Controller("category")
public class CategoryController extends BaseController {
    @Inject
    private ICategoryService categoryService;

    @RequestMapping(value = "/new")
    public IHttpResponse<Void> forwardRegister() {
        String next = forwardTo("formCreateCategory");
        return HttpResponse.<Void>next(next).build();
    }

    @RequestMapping(
            value = "/delete/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<Void> delete(Request request) {
        categoryService.delete(request);
        return HttpResponse.<Void>next(redirectTo(LIST)).build();
    }

    @RequestMapping(
            value = "/edit/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<CategoryDTO> edit(Request request) {
        CategoryDTO category = categoryService.getById(request);
        return okHttpResponse(category, forwardTo("formUpdateCategory"));
    }

    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "name", constraints = {
                            @Constraints(minLength = 3, maxLength = 50, message = "Name must be between {0} and {1} characters")
                    })
            })
    @SneakyThrows
    public IHttpResponse<Void> register(Request request) {
        CategoryDTO category = categoryService.register(request);
        return newHttpResponse(201, redirectTo(category.getId()));
    }

    @RequestMapping(
            value = "/update/{id}",
            method = RequestMethod.POST,
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    }),
                    @Validator(values = "name", constraints = {
                            @Constraints(minLength = 5, maxLength = 50, message = "Name must be between {0} and {1} characters"),
                    })
            })
    @SneakyThrows
    public IHttpResponse<Void> update(Request request) {
        CategoryDTO category = categoryService.update(request);
        return newHttpResponse(204, redirectTo(category.getId()));
    }

    @RequestMapping(value = "/list")
    public IHttpResponse<Collection<CategoryDTO>> list(Request request) {
        Collection<CategoryDTO> categories = categoryService.list(request);
        return okHttpResponse(categories, forwardTo("listCategories"));
    }

    @RequestMapping(
            value = "/list/{id}",
            validators = {
                    @Validator(values = "id", constraints = {
                            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                    })
            })
    @SneakyThrows
    public IHttpResponse<CategoryDTO> listById(Request request) {
        CategoryDTO category = categoryService.getById(request);
        return okHttpResponse(category, forwardTo("formListCategory"));
    }
}
