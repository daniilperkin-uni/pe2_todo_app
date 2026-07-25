# Spring Boot RESTful API

This file contains helpful information about the REST API project.

## Maven

IntelliJ offers the "Maven" tool window on the right side.

- Development mode: Choose rest-api -> Plugins -> spring-boot -> spring-boot:run to run and have live reload (on save).
- Create `jar`: Choose rest-api -> Plugins -> spring-boot -> spring-boot:re-package to create `target/rest-api.jar`.

After you run the created JAR file, you should be able to see the implemented resources in your browser (<http://localhost:8080/api/v1/cats>).

Otherwise, you can execute the corresponding tasks using the `mvnw` command line wrapper in a terminal.

For example `./mvnw spring-boot:run` to run the application.

## Documentation and Tutorials

- General references: <https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle>
- Application properties: <https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html>
- Baeldung Spring Boot tutorials: <https://www.baeldung.com/spring-boot>

## Troubleshooting

If there is no table `pe2.cats` reported, stop `spring-boot:run` and re-run that task again.

Also check the `application.properties` file for the correct database connection settings and the correct `spring.jpa.hibernate.ddl-auto` setting.

## Java release version 25 not supported

If you see an error like this when trying to compile the project:

```
Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.0:compile (default-compile) on project api: Fatal error compiling: Error: Releaseversion 25 not supported
```

- Download and install the Java Development Kit (JDK) version 25 (for example [Adoptium](https://adoptium.net/de/temurin/releases)).
- Install it and set the `JAVA_HOME` environment variable to the installation path.
- After logging out and back in, you should be able to run `java -version` in a terminal and see that Java 25 is used.

## Advanced Usage

In case you installed the JDK locally, you can use the Windows Terminal / bash to execute commands:

```bash
# build and package executable --> appears in target/rest-api.jar
./mvnw clean install

# execute tests only
./mvnw test

# generate test coverage report (execute tests first) --> appears in target/site/jacoco/index.html
./mvnw jacoco:report

# build and package executable without running tests
./mvnw clean install -DskipTests

# run the created JAR file
# --> http://localhost:8080/api/v1/cats
java -jar ./target/rest-api.jar

# for development: build and run in live-reload mode (rebuild on save)
# --> http://localhost:8080/api/v1/cats
./mvnw spring-boot:run
```
