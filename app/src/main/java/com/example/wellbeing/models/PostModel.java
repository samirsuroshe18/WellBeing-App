package com.example.wellbeing.models;

public class PostModel {
    String _id, userProfile, userName, createdAt, description, media, totalLikes, totalDislikes, totalComments, mediaType;

    public PostModel(String _id, String userProfile, String userName, String createdAt, String description, String media, String totalLikes, String totalDislikes, String totalComments, String mediaType) {
        this._id = _id;
        this.userProfile = userProfile;
        this.userName = userName;
        this.createdAt = createdAt;
        this.description = description;
        this.media = media;
        this.totalLikes = totalLikes;
        this.totalDislikes = totalDislikes;
        this.totalComments = totalComments;
        this.mediaType = mediaType;
    }

    public String get_id() {
        return _id;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public String getUserName() {
        return userName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public String getMedia() {
        return media;
    }

    public String getTotalLikes() {
        return totalLikes;
    }

    public String getTotalDislikes() {
        return totalDislikes;
    }

    public String getTotalComments() {
        return totalComments;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setTotalLikes(String totalLikes) {
        this.totalLikes = totalLikes;
    }

    public void setTotalDislikes(String totalDislikes) {
        this.totalDislikes = totalDislikes;
    }

    public void setTotalComments(String totalComments) {
        this.totalComments = totalComments;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
