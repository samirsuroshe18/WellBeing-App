package com.example.wellbeing.models;

public class UserModel {
    String _id, userName, profilePicture, email, wellpoints, task_completed, successRate, rank, createdAt;

    public UserModel() {
    }

    public UserModel(String _id, String userName, String profilePicture, String email, String wellpoints, String task_completed, String successRate, String rank, String createdAt) {
        this._id = _id;
        this.userName = userName;
        this.profilePicture = profilePicture;
        this.email = email;
        this.wellpoints = wellpoints;
        this.task_completed = task_completed;
        this.successRate = successRate;
        this.rank = rank;
        this.createdAt = createdAt;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWellpoints(String wellpoints) {
        this.wellpoints = wellpoints;
    }

    public void setTask_completed(String task_completed) {
        this.task_completed = task_completed;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String get_id() {
        return _id;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getEmail() {
        return email;
    }

    public String getWellpoints() {
        return wellpoints;
    }

    public String getTask_completed() {
        return task_completed;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public String getRank() {
        return rank;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
