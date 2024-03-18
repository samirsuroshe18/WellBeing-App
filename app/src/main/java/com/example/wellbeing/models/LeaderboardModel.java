package com.example.wellbeing.models;

public class LeaderboardModel {
    String _id, userName, profilePicture, wellpoints, rankNo;

    public LeaderboardModel() {
    }

    public LeaderboardModel(String _id, String userName, String profilePicture, String wellpoints, String rankNo) {
        this._id = _id;
        this.userName = userName;
        this.profilePicture = profilePicture;
        this.wellpoints = wellpoints;
        this.rankNo = rankNo;
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

    public void setWellpoints(String wellpoints) {
        this.wellpoints = wellpoints;
    }

    public void setRankNo(String rankNo) {
        this.rankNo = rankNo;
    }

    public String get_id() {
        return _id;
    }

    public String getUserName() {
        return userName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getWellpoints() {
        return wellpoints;
    }

    public String getRankNo() {
        return rankNo;
    }
}
