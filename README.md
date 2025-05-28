# Full-Stack Java Web Application

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/m-feliciano/servlets)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/license/mit)
[![Java](https://img.shields.io/badge/java-17-blue)](https://adoptopenjdk.net/)

> **A complete Java EE web application with MVC architecture, authentication, security, caching, automated tests, and best practice examples.**

---

## Demo

![App home page](./images/homepage.png)

---

## Table of Contents
- [About the Project](#about-the-project)
- [Main Features](#main-features)
- [Tech Stack](#tech-stack)
- [How to Run Locally](#how-to-run-locally)
- [Dev Section and Endpoints](#url-design-and-endpoints)
- [Patterns and Architecture](#patterns-and-architecture)
- [How to Contribute](#how-to-contribute)
- [License](#license)

---

## About the Project

A complete web system for management.
Includes authentication, authorization, XSS security, password encryption,
caching, pagination, sorting, automated tests, and extensible architecture.

---

## Main Features
- Authentication and authorization with session control
- Security filters (XSS, password encryption)
- Data validation with custom annotations
- Pagination, sorting, and search
- In-memory cache per user
- Unit and integration tests (JUnit, Mockito)
- Structured logging
- Layered and highly extensible architecture

---

## Tech Stack
- **Java 17**
- **Servlet/JSP API**
- **Hibernate/JPA**
- **Tomcat 9**
- **PostgreSQL**
- **JUnit 5, Mockito**
- **Lombok, SLF4J**

---

## How to Run Locally

1. **Clone the repository:**
   ```sh
   git clone https://github.com/m-feliciano/servlets.git
   cd servlets
   ```
2. **Configure the database:**
   - Create a PostgreSQL database and adjust `resources/META-INF/persistence.xml`.
   - Run the scripts in `resources/META-INF/sql`.
3. **Build the project:**
   ```sh
   mvn clean install
   ```
4. **Deploy to Tomcat:**
   - Copy the generated WAR to the `webapps` folder of Tomcat.
   - Start Tomcat and access `http://localhost:8080/api/v1/login/form`.

---

## Dev Section and Endpoints

- URLs follow the pattern: `/api/v1/{resource}/{action}`
- Supports pagination, sorting, and search via query params
- API versioning

### Endpoints Table (Product Example)
| Method | Endpoint                      | Description                |
|--------|-------------------------------|----------------------------|
| GET    | /api/v1/product/list          | List all products          |
| GET    | /api/v1/product/list/{id}     | Product details            |
| POST   | /api/v1/product/create        | Create a new product       |
| POST   | /api/v1/product/update/{id}   | Update a product           |
| POST   | /api/v1/product/delete/{id}   | Delete a product           |

> See the section [Endpoints by Controller](#endpoints-by-controller) for the full list of endpoints.

---

## Patterns and Architecture
- **MVC**: Clear separation between Controller, Model, DAO, DTO
- **Dependency Injection (CDI)**
- **Custom validation with annotations**
- **Security filters (XSS, encryption)**
- **In-memory cache per user**
- **Generic pagination and sorting**
- **Automated tests (JUnit, Mockito)**
- **Structured logging (SLF4J)**

---

## How to Contribute
1. Fork this repository
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: my feature'`
4. Push to your branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more details.

---

## FAQ

**1. How to create a new service?**
- Create DTO, Model, DAO, and Controller following the pattern of existing examples.
- Annotate methods with `@RequestMapping` and use custom validation.

**2. How to customize configuration?**
- Edit `app.properties` to change cache, rate limit, etc.

---

## URL Components:
- **`{context}`**: Application context path (e.g., `https://your-domain.com/api`).
- **`{version}`**: API version (e.g., `v1`).
- **`{path}`**: Controller path (e.g., `product`).
- **`{service}`**: Specific service or action (e.g., `list`).
- **`{query}`**: Optional query parameters (e.g., `?page=1&limit=5`).

### Examples:

#### GET Requests:
- `/api/v1/product/list` - List all products.
- `/api/v1/product/list/{id}` - Get product by ID.

#### POST Requests:
- `/api/v1/product/update/{id}` - Update a product.
- `/api/v1/product/delete/{id}` - Delete a product.

### Query Parameters:
- **Sorting**: `sort=<field>&order=<asc|desc>` (e.g., `sort=id&order=asc`).
- **Pagination**: `page=<page>&limit=<size>` (e.g., `page=1&limit=5`).
- **Search**: `q=<query>&k=<field>` (e.g., `q=macbook&k=name`).

### Notes:
- Default values for query parameters can be configured in the `app.properties` file.
- API versioning is included in the URL but not mapped to controllers. The default version is `v1`.

## Endpoints by Controller

### ProductController
| Method | Endpoint                      | Authentication | Notes                        |
|--------|-------------------------------|----------------|------------------------------|
| GET    | /api/v1/product/list          | Yes            | List all products            |
| GET    | /api/v1/product/list/{id}     | Yes            | Product details              |
| POST   | /api/v1/product/create        | Yes            | Create a new product         |
| POST   | /api/v1/product/update/{id}   | Yes            | Update a product             |
| POST   | /api/v1/product/delete/{id}   | Yes            | Delete a product             |
| GET    | /api/v1/product/new           | Yes            | New product form             |
| GET    | /api/v1/product/edit/{id}     | Yes            | Edit product form            |

### UserController
| Method | Endpoint                  | Authentication | Notes                        |
|--------|---------------------------|----------------|------------------------------|
| POST   | /api/v1/user/update/{id}  | Yes            | Update user                  |
| POST   | /api/v1/user/delete/{id}  | Yes (admin)    | Delete user (admin only)     |
| POST   | /api/v1/user/registerUser | No             | Register new user            |
| GET    | /api/v1/user/list/{id}    | Yes            | User details                 |

### LoginController
| Method | Endpoint                   | Authentication | Notes                        |
|--------|----------------------------|----------------|------------------------------|
| GET    | /api/v1/login/registerPage | No             | Registration form            |
| GET    | /api/v1/login/form         | No             | Login form                   |
| POST   | /api/v1/login/login        | No             | Perform login                |
| POST   | /api/v1/login/logout       | Yes            | Perform logout               |

### InventoryController
| Method | Endpoint                      | Authentication | Notes                        |
|--------|-------------------------------|----------------|------------------------------|
| GET    | /api/v1/inventory/list        | Yes            | List all items               |
| GET    | /api/v1/inventory/list/{id}   | Yes            | Item details                 |
| POST   | /api/v1/inventory/create      | Yes            | Create new item              |
| POST   | /api/v1/inventory/update/{id} | Yes            | Update item                  |
| POST   | /api/v1/inventory/delete/{id} | Yes            | Delete item                  |
| GET    | /api/v1/inventory/new         | Yes            | New item form                |
| GET    | /api/v1/inventory/edit/{id}   | Yes            | Edit item form               |

### CategoryController
| Method | Endpoint                     | Authentication | Notes                        |
|--------|------------------------------|----------------|------------------------------|
| GET    | /api/v1/category/list        | Yes            | List all categories          |
| GET    | /api/v1/category/list/{id}   | Yes            | Category details             |
| POST   | /api/v1/category/create      | Yes            | Create new category          |
| POST   | /api/v1/category/update/{id} | Yes            | Update category              |
| POST   | /api/v1/category/delete/{id} | Yes            | Delete category              |
| GET    | /api/v1/category/new         | Yes            | New category form            |
| GET    | /api/v1/category/edit/{id}   | Yes            | Edit category form           |

#### Example of controller

```java

// Example of a controller
   @Controller(path = "/product")
   public final class ProductController extends BaseController<Product, Long> {
   
      @RequestMapping(value = "/list")
      public IServletResponse list(Request request) {
         ProductModel model = this.getModel();
         Product filter = model.getEntity(request);

         request.query().getPageRequest().setFilter(filter);

         IPageable<Product> pageable = model.getAllPageable(request.query().getPageRequest());

         Set<KeyPair> container = new HashSet<>();
         container.add(new KeyPair("pageable", pageable));

         if (pageable.getContent().iterator().hasNext()) {
            BigDecimal totalPrice = model.calculateTotalPriceFor(filter);
            container.add(new KeyPair("totalPrice", totalPrice));
         }

         Collection<CategoryDTO> categories = categoryController.list(request.withToken()).body();
         container.add(new KeyPair("categories", categories));

         String next = super.forwardTo("listProducts");
         return super.newServletResponse(container, next);
      }
   }
```

#### Endpoint register user

```java
    // POST ap1/v2/user/registerUser
   @RequestMapping(
           value = "/registerUser",
           method = RequestMethod.POST,
           apiVersion = "v2",
           requestAuth = false,
           validators = {
                   @Validator(values = "login", constraints = {
                           @Constraints(isEmail = true, message = "Login must be a valid email")
                   }),
                   @Validator(values = {"password", "confirmPassword"},
                           constraints = {
   //                                @Constraints(minLength = 5, maxLength = 30, message = "Password must be between {0} and {1} characters")
                                   @Constraints(minLength = 5, message = "Password must have at least {0} characters"),
                                   @Constraints(maxLength = 30, message = "Password must have at most {0} characters"),
                           }),
           })
   public IHttpResponse<Void> register(Request request) {
      UserModel model = this.getModel();
      model.register(request);
      return super.newHttpResponse(201, "redirect:/api/v1/login/form"); // Created
   }
```

#### Endpoint delete user (Only admin)

```java
   // POST /user/delete/{id}
   @RequestMapping(
           value = "/delete/{id}",
           method = RequestMethod.POST,
           roles = { 
                   PerfilEnum.ADMIN
           },
           validators = {
                   @Validator(values = "id", constraints = {
                           @Constraints(min = 1, message = "ID must be greater than or equal to {0}")
                   })
           })
   public IHttpResponse<Void> delete(Request request) {
      UserModel model = this.getModel();
      model.delete(request);

      String next = super.forwardTo("formLogin");
      return HttpResponse.<Void>ok().next(next).build();
   }
```

#### Complex Test 

```java
   // ProductControllerTest.java
    @Test
    @DisplayName(
            "Test listProducts method to retrieve a list of products. " +
            "It should return a 200 status code and the expected response.")
    @SuppressWarnings("unchecked")
    void testListProducts() {
        // Setup
        Product filterMock = new Product("prod", "desc", null);
        when(productModel.getEntity(any())).thenReturn(filterMock);
    
        var categories = List.of(new CategoryDTO());
        var categoryResponse = HttpResponseImpl.<Collection<CategoryDTO>>newBuilder().body(categories).build();
        when(categoryController.list(any())).thenReturn(categoryResponse);
    
        var products = List.of(
                new Product("prod1", "desc1", BigDecimal.valueOf(50)),
                new Product("prod2", "desc2", BigDecimal.valueOf(50))
        );
    
        var pageableMock = PageableImpl.<Product>builder()
                .content(products)
                .currentPage(1)
                .pageSize(2)
                .sort(Sort.by("id").ascending())
                .build();
    
        when(productModel.getAllPageable(any())).thenReturn(pageableMock);
        when(productModel.calculateTotalPriceFor(any())).thenReturn(BigDecimal.valueOf(100));
    
        // Execution
        IServletResponse response = productController.list(request);
    
        // Verification
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    
        // Verify pageable content
        var pageable = (IPageable<Product>) response.body().stream()
                .filter(pair -> "pageable".equals(pair.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Pageable not found"))
                .value();
    
        long counter = StreamSupport.stream(pageable.getContent().spliterator(), false).count();
        assertEquals(2, counter);
    
        // Verify total price
        BigDecimal totalPrice = (BigDecimal) response.body().stream()
                .filter(pair -> "totalPrice".equals(pair.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Total price not found"))
                .value();
    
        assertEquals(BigDecimal.valueOf(100), totalPrice);
    
        // Verify categories
        var responseCategories = (Collection<CategoryDTO>) response.body().stream()
                .filter(pair -> "categories".equals(pair.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Categories not found"))
                .value();
        assertEquals(categories, responseCategories);
    
        // Verify interactions
        verify(productModel, times(1)).getEntity(request);
        verify(productModel, times(1)).getAllPageable(any());
        verify(productModel, times(1)).calculateTotalPriceFor(filterMock);
        verify(categoryController, times(1)).list(any());
    }
```

### Creating a new Service
```java
   // 1. DTO
   public class ExampleDTO { ... }
   
   // 2. Entidade
   public class Example extends Identifier<Long> { ... }
   
   // 3. DAO
   public class ExampleDAO extends BaseDAO<Example, Long> { ... }
   
   // 4. Model
   public class ExampleModel extends BaseModel<Example, Long> { ... }
   
   // 5. Controller
   @Controller(path = "/example")
   public class ExampleController extends BaseController<Example, Long> {
       @RequestMapping(value = "/create", method = RequestMethod.POST)
       public IHttpResponse<Void> create(Request request) { ... }
       // other methods...
   }
```


## Folder Structure

```plaintext
servlet
   ├───Auth
   │   └───wrapper
   ├───controller
   │   └───base
   ├───core
   │   ├───builder
   │   ├───impl
   │   ├───interceptor
   │   └───listener
   ├───dto
   ├───exception
   ├───mapper
   ├───model
   │   ├───base
   │   ├───impl
   │   ├───pojo
   │   │   ├───domain
   │   │   ├───enums
   │   │   └───records
   │   └───shared
   ├───persistence
   │   ├───dao
   │   │   └───base
   │   └───impl
   ├───util
   └───validator

   resources
      │   ├───META-INF
      │   │   └───sql
      │   └───mockito-extensions
      └───webapp
          ├───assets
          │   └───images
          ├───css
          ├───js
          ├───META-INF
          ├───web
          │   └───WEB-INF
          └───WEB-INF
              ├───fragments
              ├───routes
              └───view
                  ├───components
                  │   └───buttons
                  └───pages
                      ├───category
                      ├───inventory
                      ├───product
                      └───user
   test
     └───java
         └───servlets
             ├───auth
             ├───controllers
             ├───core
             └───utils
```

## Some layouts

### Product

#### `/product/list/{id}`

![App product list page](./images/product-list.png)

### Info Page

[comment]: <> (Found on the web, author unknown)
![Error](./images/cat_404.gif)


## Setup Instructions

1. Clone the repository:
    ```sh
    git clone https://github.com/m-feliciano/servlets.git
    ```
2. Navigate to the project directory:
    ```sh
    cd servlets
    ```
3. Build the project using Maven:
    ```sh
    mvn clean install
    ```
4. Create a new database in PostgreSQL:
    ```docker
   ## create network
    docker network create -d bridge <network-name>
    
    ## run container (example)
    docker run --name <container-name> \
    --network=<network-name> -p 5432:5432 \
    -e "POSTGRES_USER=<user>" \
    -e "POSTGRES_PASSWORD=<password>" \
    -d postgres
    
    ## exec into container
    docker exec -it <container-name> psql -U postgres
    ## create table
    
    ## etc
    # The scripts to create the database are in the `resources/META-INF/sql` folder.
    # The database connection is set in the `resources/META-INF/persistence.xml` file.
    ```

5. Setting up the database:
    - Run the scripts in the `resources/META-INF/sql` folder to create the tables and insert initial data.
    - Update the `persistence.xml` file with your database credentials.
    - Update the `app.properties` file as needed.
      <br><br>
6. Deploy the application to Tomcat:
    - Install Tomcat 9 on your machine.
    - Copy the generated WAR file to the Tomcat `webapps` directory.
    - Start the Tomcat server.
      <br><br>
7. Usage Instructions
    - Access the application at `<server>/<context-path>` (e.g., `http://localhost:8080/api/v1/login/form`).

## Notes

***Note***: This project was initially created years ago to learn Java EE, core Servlet/JSP, and JPA. It has been
updated to incorporate the latest Java features and best practices.

There is a lot of room for improvement,
like refactoring the frontend joining the files into a single one using `JSP fragments`,
and `JSTL` to render the content dynamically.

[Back to top](#full-stack-java-web-application)
