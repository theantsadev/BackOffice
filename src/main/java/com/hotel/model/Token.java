package com.hotel.model;

import java.sql.Timestamp;

public class Token {
    private int id;
    private String token;
    private Timestamp dateHeureExpiration;

    public Token() {
    }

    public Token(int id, String token, Timestamp dateHeureExpiration) {
        this.id = id;
        this.token = token;
        this.dateHeureExpiration = dateHeureExpiration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getDateHeureExpiration() {
        return dateHeureExpiration;
    }

    public void setDateHeureExpiration(Timestamp dateHeureExpiration) {
        this.dateHeureExpiration = dateHeureExpiration;
    }

    public boolean isExpired() {
        return dateHeureExpiration != null && dateHeureExpiration.before(new Timestamp(System.currentTimeMillis()));
    }
}
