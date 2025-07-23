package br.edu.ifba.inf008.shell.model;

import java.time.LocalDateTime;

public class User {

    private int userId;
    private String name;
    private String email;
    private LocalDateTime registeredAt;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
