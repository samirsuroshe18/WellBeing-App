package com.example.wellbeing.models;

import android.net.Uri;

public class PostModel {
    String _id, userProfile, userName, createdAt, description, media, mediaType, taskId, duration;
    int totalLikes, totalDislikes, totalComments;
    public PostModel() {

    }

    public PostModel(String _id, String userProfile, String userName, String createdAt, String description, String media, int totalLikes, int totalDislikes, int totalComments, String mediaType, String taskId, String duration) {
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
        this.taskId = taskId;
        this.duration = duration;
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

    public int getTotalLikes() {
        return totalLikes;
    }

    public int getTotalDislikes() {
        return totalDislikes;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getDuration() {
        return duration;
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

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public void setTotalDislikes(int totalDislikes) {
        this.totalDislikes = totalDislikes;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
