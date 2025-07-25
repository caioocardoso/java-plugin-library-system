package br.edu.ifba.inf008.domain.model;

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
}