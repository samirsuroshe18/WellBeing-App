package com.example.wellbeing.models;

public class TaskModel {
    String _id, title, description, mediaType, taskReference, userId, userName, pofilePicture, timeToComplete;

    public TaskModel() {

    }

    public TaskModel(String _id, String title, String description, String mediaType, String taskReference, String userId, String userName, String pofilePicture, String timeToComplete) {
        this._id = _id;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.taskReference = taskReference;
        this.userId = userId;
        this.userName = userName;
        this.pofilePicture = pofilePicture;
        this.timeToComplete = timeToComplete;
    }

    public String get_id() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getTaskReference() {
        return taskReference;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPofilePicture() {
        return pofilePicture;
    }

    public String getTimeToComplete() {
        return timeToComplete;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setTaskReference(String taskReference) {
        this.taskReference = taskReference;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPofilePicture(String pofilePicture) {
        this.pofilePicture = pofilePicture;
    }

    public void setTimeToComplete(String timeToComplete) {
        this.timeToComplete = timeToComplete;
    }
}
