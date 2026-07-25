package de.unistuttgart.iste.ese.api.assignee;

import ch.qos.logback.classic.Logger;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Repräsentiert einen Zuständigen in der Datenbank
 */
@Entity
@Table(name = "assignees")
public class Assignee {

    // Eindeutige ID des Zuständigen
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vorname des Zuständigen
    @NotBlank(message = "Prename must not be blank")
    @Column(nullable = false)
    private String prename;

    // Nachname des Zuständigen
    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false)
    private String name;

    // E-Mail-Adresse des Zuständigen
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;
    private Logger logger;

    // Getter und Setter
    /**
     * Gibt die ID zurück
     */
    public Long getId() {
        return id;
    }

    /**
     * Setzt die ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gibt den Vornamen zurück
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Setzt den Vornamen
     */
    public void setPrename(String prename) {
        this.prename = prename;
    }

    /**
     * Gibt den Nachnamen zurück
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Nachnamen
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt die E-Mail-Adresse zurück
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setzt die E-Mail-Adresse
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
