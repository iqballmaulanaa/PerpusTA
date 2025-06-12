package com.perpussapp.perpusapp.Model;

public class CoversationModel {
    private int isAdmin,messageType;
    private String key, message  ;
    private long time;

    public CoversationModel() {
    }

    public CoversationModel(int isAdmin, int messageType, String key, String message, long time) {
        this.isAdmin = isAdmin;
        this.messageType = messageType;
        this.key = key;
        this.message = message;
        this.time = time;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
