package fr.istic.taa.jaxr.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class ConcertCreateDTO {
    @NotNull
    private Long organiserId;

    @NotNull
    @NotEmpty
    private String lieu;

    @NotNull
    @Positive
    private Long capacite;

    private String description;

    @NotNull
    private LocalDateTime dateTime;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer popularite;

    // region Generated code
    public Integer getPopularite() {
        return popularite;
    }

    public void setPopularite(Integer popularite) {
        this.popularite = popularite;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }


    public Long getCapacite() {
        return capacite;
    }

    public void setCapacite(Long capacite) {
        this.capacite = capacite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrganisateurId() {
        return organiserId;
    }

    public void setOrganisateurId(Long organisateurId) {
        this.organiserId = organisateurId;
    }
    // endregion
}