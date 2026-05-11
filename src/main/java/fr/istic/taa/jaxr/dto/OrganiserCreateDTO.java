package fr.istic.taa.jaxr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class OrganiserCreateDTO {
    @NotNull @NotEmpty
    private String name;

    @NotNull @NotEmpty
    private String firsname;

    @NotNull @NotEmpty
    private String email;

    @NotNull @NotEmpty
    private String password;

    public String getFirsname() {
        return firsname;
    }

    public void setFirsname(String firsname) {
        this.firsname = firsname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
