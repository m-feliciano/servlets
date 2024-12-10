# Full-Stack Java Web Application

This project is a comprehensive Java/JSP web application.
It follows the Model-View-Controller (MVC) architecture and uses the Java EE stack.

## Table of Contents

- [Technology Stack](#tech-stack)
- [URL Design](#url-design)
- [Layout](#Some-layouts)
- [Packages](#packages)
- [Setup Instructions](#setup-instructions)
- [Notes](#notes)

## Tech Stack

- **Java (JDK 17)**: Core programming language.
- **Hibernate (ORM)**: Simplifies database interactions.
- **Tomcat 9 (Server)**: Web server and servlet container.
- **PostgreSQL (Database)**: Open-source relational database management system.
- **Criteria API**: Type-safe way to build database queries.

## URL Design

- `{context}/view/{path}/{service}/{id|query}`

Example:

- `server/view/product/list/1`

## Some layouts

### Home Page

#### `/product/list?page=1&limit=2`

![App home page](./images/homepage.png)

#### Tips:

- *Sorting*: `sort=<field>&order=<asc|desc>&page=<page>&limit=<size>`
- *Searching*: `q=<query>&k=<field>`

Sample URLs:

- `/product/list?page=1&limit=5&sort=id&order=desc`
- `/product/list?q=macbook+pro&k=name`

Default values can be changed in the `app.properties` file.

### Product

#### `/product/list/{id}`

![App product list page](./images/product-list.png)

### Info Page

[comment]: <> (Found on the web, author unknown)
![Error](./images/cat_404.gif)

## Packages

```
───main
│   ├───java
│   │   └───com
│   │       └───dev
│   │           └───servlet
│   │               ├───builders
│   │               ├───business
│   │               │   └───shared
│   │               ├───controllers
│   │               ├───dao
│   │               ├───dto
│   │               ├───filter
│   │               ├───interfaces
│   │               ├───listeners
│   │               ├───mapper
│   │               ├───pojo
│   │               │   ├───enums
│   │               │   └───records
│   │               ├───providers
│   │               ├───transform
│   │               └───utils
│   ├───resources
│   │   └───META-INF
│   │       └───sql
│   └───webapp
│       ├───assets
│       ├───css
│       ├───META-INF
│       ├───web
│       │   └───WEB-INF
│       └───WEB-INF
│           ├───jspf
│           └───view
│               ├───components
│               └───pages
│                   ├───category
│                   ├───inventory
│                   ├───product
│                   └───user
└───test
    └───java
        └───servlets
```

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
   - Access the application at `<server>/view/<context-path>` (e.g., `http://localhost:8080/view/login/form`).

## Notes
***Note***: This project was initially created years ago to learn Java EE, core Servlet/JSP, and JPA. It has been updated to incorporate the latest Java features and best practices.

There is a lot of room for improvement, 
like refactoring the frontend joining the files into a single one using `JSP fragments`, 
and `JSTL` to render the content dynamically.

[Back to top](#full-stack-java-web-application)