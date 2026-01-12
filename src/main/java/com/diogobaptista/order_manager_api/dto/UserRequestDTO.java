package com.diogobaptista.order_manager_api.dto;

import javax.validation.constraints.NotNull;

public class UserRequestDTO {

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Email is required")
    private String email;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String mail) {
        this.email = mail;
    }
}
