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
                // allow CORS requests for all resources and HTTP methods from the frontend origins;
                // the Vite dev server runs on 5173, browsers send the bare host for port 80
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173",
                                "http://localhost", "http://127.0.0.1")
                        .allowCredentials(true);
            }
        };
    }
}
