package com.example.wellbeing.models;

public class CommentModel {
    String _id, userName, userProfile, content, time;

    public CommentModel(String _id, String userName, String userProfile, String content, String time) {
        this._id = _id;
        this.userName = userName;
        this.userProfile = userProfile;
        this.content = content;
        this.time = time;
    }

    public String get_id() {
        return _id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
