package de.unistuttgart.iste.ese.api.assignee.dto;

public class AssigneeDTO {
    private Long id;
    private String prename;
    private String name;
    private String email;

    // Getter und Setter

    /**
     * Gibt die ID des Zuständigen zurück
     */
    public Long getId() {
        return id;
    }

    /**
     * Setzt die ID des Zuständigen
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gibt den Vornamen des Zuständigen zurück
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Setzt den Vornamen des Zuständigen
     */
    public void setPrename(String prename) {
        this.prename = prename;
    }

    /**
     * Gibt den Nachnamen des Zuständigen zurück
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Nachnamen des Zuständigen
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt die E-Mail-Adresse des Zuständigen zurück
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setzt die E-Mail-Adresse des Zuständigen
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
