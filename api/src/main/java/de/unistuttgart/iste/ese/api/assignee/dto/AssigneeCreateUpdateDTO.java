package de.unistuttgart.iste.ese.api.assignee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern; // <-- WICHTIG: Import hier geändert!

public class AssigneeCreateUpdateDTO {
    @NotBlank(message = "Prename must not be blank")
    private String prename;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    @Pattern(regexp = "^.*@(stud\\.|iste\\.|ipvs\\.|sec\\.)?uni-stuttgart\\.de$", message = "Email must be a @uni-stuttgart.de, @stud.uni-stuttgart.de, @iste.uni-stuttgart.de, @ipvs.uni-stuttgart.de or @sec.uni-stuttgart.de address")
    private String email;

    // Getter und Setter

    /**
     * Gibt den Vornamen des Zugewiesenen zurück
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Setzt den Vornamen des Zugewiesenen
     */
    public void setPrename(String prename) {
        this.prename = prename;
    }

    /**
     * Gibt den Nachnamen des Zugewiesenen zurück
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Nachnamen des Zugewiesenen
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt die E-Mail-Adresse des Zugewiesenen zurück
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setzt die E-Mail-Adresse des Zugewiesenen
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
