package com.example.backup.model;

import java.io.Serializable;

public class Cast implements Serializable {
    private int id;
    private int movieId;
    private String actorName;
    private String characterName;
    private String profilePath;

    public Cast() {}

    public Cast(int id, int movieId, String actorName, String characterName, String profilePath) {
        this.id = id;
        this.movieId = movieId;
        this.actorName = actorName;
        this.characterName = characterName;
        this.profilePath = profilePath;
    }

    public Cast(String actorName, String characterName, String profilePath) {
        this.actorName = actorName;
        this.characterName = characterName;
        this.profilePath = profilePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }
}
