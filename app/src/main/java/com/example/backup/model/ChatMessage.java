package com.example.backup.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    private String text;
    private boolean isUser;
    private String time;
    private Movie suggestedMovie;

    public ChatMessage(String text, boolean isUser, String time) {
        this.text = text;
        this.isUser = isUser;
        this.time = time;
    }

    public ChatMessage(String text, boolean isUser, String time, Movie suggestedMovie) {
        this.text = text;
        this.isUser = isUser;
        this.time = time;
        this.suggestedMovie = suggestedMovie;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Movie getSuggestedMovie() {
        return suggestedMovie;
    }

    public void setSuggestedMovie(Movie suggestedMovie) {
        this.suggestedMovie = suggestedMovie;
    }
}
