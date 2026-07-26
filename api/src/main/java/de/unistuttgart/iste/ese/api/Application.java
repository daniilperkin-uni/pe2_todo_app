package de.unistuttgart.iste.ese.api;

import jakarta.annotation.Nonnull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Application {

    /**
     * Startet die Spring Boot Anwendung
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Ermöglicht Cross-Origin Resource Sharing (CORS)
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Erlaubt CORS-Anfragen für alle Ressourcen und HTTP-Methoden vom Frontend
             */
            @Override
            public void addCorsMappings(@Nonnull CorsRegistry registry) {
                // allow CORS requests for all resources and HTTP methods from the frontend origin
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedOrigins("http://localhost:5173", "http://localhost:80")
                        .allowCredentials(true);
            }
        };
    }
}
