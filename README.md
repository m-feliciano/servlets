# Full-Stack Java EE Web Application

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/m-feliciano/servlets)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/license/mit)
[![Java](https://img.shields.io/badge/java-17-blue)](https://adoptopenjdk.net/)

> **Java EE web application with a modular MVC architecture, security, caching, and comprehensive automated testing.**

---

## Overview

This project is a production-grade Java EE web application, designed with extensibility and maintainability in mind. 
It uses modern Java features, follows best practices, and demonstrates a layered architecture with separation of concerns.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Endpoints](#endpoints)
- [Development Patterns](#development-patterns)
- [FAQ](#faq)

---

## Features

- Authentication and authorization with JWT
- Security filters (XSS protection, password encryption)
- Custom data validation via annotations
- Pagination, sorting, and search
- Caching mechanism for user sessions
- Unit and integration tests (JUnit, Mockito)
- Structured logging (SLF4J)
- Extensible, layered MVC architecture

---

## Tech Stack

- Java 17
- Java EE (Servlet/JSP API)
- Hibernate/JPA
- Tomcat 9
- PostgreSQL
- JUnit 5, Mockito
- Lombok, SLF4J

---

## Project Structure

```plaintext
servlet/
  ├── Auth/
  ├── controller/
  ├── adapter
  ├── dto/
  ├── exception/
  ├── mapper/
  ├── model/
  ├── persistence/
  ├── util/
  ├── validator/
  ├── resources/
  │   ├── META-INF/
  │   ├── mockito-extensions/
  │   └── webapp/
  └── test/
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.x
- PostgreSQL
- Tomcat 9

### Setup

1. **Clone the repository**
   ```sh
   git clone https://github.com/m-feliciano/servlets.git
   cd servlets
   ```

2. **Configure the database**
   - Create a PostgreSQL database.
   - Update `resources/META-INF/persistence.xml` with your credentials.
   - Run the SQL scripts in `resources/META-INF/sql`.

3. **Build the project**
   ```sh
   mvn clean install
   ```

4. **Deploy to Tomcat**
   - Copy the generated WAR file to Tomcat's `webapps` directory.
   - Start Tomcat and access the app at `http://localhost:8080/api/v1/login/form`.

---

## Configuration

- Database connection: `resources/META-INF/persistence.xml`
- Application settings: `app.properties`
- SQL scripts: `resources/META-INF/sql`

---

## Endpoints

All endpoints follow the pattern: `/api/v{version}/{resource}/{action}`

### Example: Product Endpoints

| Method | Endpoint                      | Description           |
|--------|-------------------------------|-----------------------|
| GET    | /api/v1/product/list          | List all products     |
| GET    | /api/v1/product/list/{id}     | Product details       |
| POST   | /api/v1/product/create        | Create product        |
| POST   | /api/v1/product/update/{id}   | Update product        |

> See [Endpoints by Controller](#endpoints-by-controller) for the full list.

---

## Development Patterns

- **MVC**: Controllers delegate business logic to models and DAOs.
- **Dependency Injection (CDI)**: Promotes loose coupling and testability.
- **Custom Validation**: Use annotations for input validation.
- **Security**: XSS filters and encrypted passwords.
- **Caching**: Per-user in-memory cache for performance.
- **Pagination & Sorting**: Generic support for large datasets.
- **Testing**: Extensive unit and integration tests.
- **Logging**: Structured logs via SLF4J.
- **Generics**: Use generics for type safety in all layers.

## FAQ

**How do I create a new service?**
- Implement DTO, Model, DAO, and Controller classes following the existing patterns.
- Annotate controller methods with `@RequestMapping` and use custom validators as needed.


That's it! Here's a quick example:

```java
// 1. DTO
public class ExampleDTO extends TransferObject<Long> { }

// 2. Entity
public class Example extends Identifier<Long> { }

// 3. DAO
public class ExampleDAO extends BaseDAO<Example, Long> { }

// 4. Model
public class ExampleModel extends BaseModel<Example, Long> { }

// 5. Controller
@Controller(path = "/example")
public class ExampleController extends BaseController<Example, Long> {

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public IHttpResponse<Void> create(Request request, ExempleModel model) {
        // Do stuff...
    }
}
```

**How do I customize configuration?**
- Edit `app.properties` for cache, rate limits, and other settings.

---

## Endpoints by Controller

### ProductController

| Method | Endpoint                      | Auth | Notes                |
|--------|-------------------------------|------|----------------------|
| GET    | /api/v1/product/list          | Yes  | List all products    |
| GET    | /api/v1/product/list/{id}     | Yes  | Product details      |
| POST   | /api/v1/product/create        | Yes  | Create product       |
| POST   | /api/v1/product/update/{id}   | Yes  | Update product       |
| POST   | /api/v1/product/delete/{id}   | Yes  | Delete product       |
| GET    | /api/v1/product/new           | Yes  | New product form     |
| GET    | /api/v1/product/edit/{id}     | Yes  | Edit product form    |

### UserController

| Method | Endpoint                  | Auth      | Notes                  |
|--------|---------------------------|-----------|------------------------|
| POST   | /api/v1/user/update/{id}  | Yes       | Update user            |
| POST   | /api/v1/user/delete/{id}  | Admin     | Delete user (admin)    |
| POST   | /api/v1/user/registerUser | No        | Register new user      |
| GET    | /api/v1/user/list/{id}    | Yes       | User details           |

### LoginController

| Method | Endpoint                   | Auth | Notes             |
|--------|----------------------------|------|-------------------|
| GET    | /api/v1/login/registerPage | No   | Registration form |
| GET    | /api/v1/login/form         | No   | Login form        |
| POST   | /api/v1/login/login        | No   | Perform login     |
| POST   | /api/v1/login/logout       | Yes  | Perform logout    |

### InventoryController

| Method | Endpoint                      | Auth | Notes             |
|--------|-------------------------------|------|-------------------|
| GET    | /api/v1/inventory/list        | Yes  | List all items    |
| GET    | /api/v1/inventory/list/{id}   | Yes  | Item details      |
| POST   | /api/v1/inventory/create      | Yes  | Create item       |
| POST   | /api/v1/inventory/update/{id} | Yes  | Update item       |
| POST   | /api/v1/inventory/delete/{id} | Yes  | Delete item       |
| GET    | /api/v1/inventory/new         | Yes  | New item form     |
| GET    | /api/v1/inventory/edit/{id}   | Yes  | Edit item form    |

### CategoryController

| Method | Endpoint                     | Auth | Notes                |
|--------|------------------------------|------|----------------------|
| GET    | /api/v1/category/list        | Yes  | List all categories  |
| GET    | /api/v1/category/list/{id}   | Yes  | Category details     |
| POST   | /api/v1/category/create      | Yes  | Create category      |
| POST   | /api/v1/category/update/{id} | Yes  | Update category      |
| POST   | /api/v1/category/delete/{id} | Yes  | Delete category      |
| GET    | /api/v1/category/new         | Yes  | New category form    |
| GET    | /api/v1/category/edit/{id}   | Yes  | Edit category form   |

---

## Example: Controller Implementation

```java
@Controller(path = "/product")
public final class ProductController extends BaseController<Product, Long> {

   @RequestMapping(
           value = "/update/{id}",
           method = RequestMethod.POST,
           validators = {
                   @Validator(values = "id", constraints = {
                           @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                   }),
                   @Validator(values = "description", constraints = {
                           @Constraints(minLength = 5, maxLength = 255, message = "Description must be between {0} and {1} characters")
                   })
           })
   public IHttpResponse<Void> update(Request request, ProductModel model) throws ServiceException {
      ProductDTO product = model.update(request);
      // No Content
      return newHttpResponse(204, redirectTo(product.getId()));
   }
}
```

---

## Example: Register User Endpoint

```java
@RequestMapping(
    value = "/registerUser",
    method = RequestMethod.POST,
    apiVersion = "v2", // API versioning
    requestAuth = false,
    validators = {
        @Validator(values = "login", constraints = {
            @Constraints(isEmail = true, message = "Login must be a valid email")
        }),
        @Validator(values = {"password", "confirmPassword"},
            constraints = {
                @Constraints(minLength = 5, message = "Password must have at least {0} characters"),
                @Constraints(maxLength = 30, message = "Password must have at most {0} characters"),
            }),
    })
public IHttpResponse<Void> register(Request request, UserModel model) { // the model is injected by the framework
    model.register(request);
    return newHttpResponse(201, "redirect:/api/v1/login/form"); // Created
}
```

---

## Example: Delete User Endpoint

```java
@RequestMapping(
    value = "/delete/{id}",
    method = RequestMethod.POST,
    roles = { PerfilEnum.ADMIN },
    validators = {
        @Validator(values = "id", constraints = {
            @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
        })
    })
public IHttpResponse<Void> delete(Request request, UserModel model) {
    model.delete(request);
    return HttpResponse.<Void>ok().next(forwardTo("formLogin")).build();
}
```

---

## Layouts

### Home

![App product list page](./images/homepage.png)

### Product

![App product list page](./images/product-list.png)

---

## Notes

This project began as a learning exercise in Java EE, Servlets/JSP, and JPA, and has evolved to incorporate modern Java features and best practices. 
The frontend can be further improved by consolidating JSP files and leveraging JSTL for dynamic rendering.

[Back to top](#full-stack-java-ee-web-application)
