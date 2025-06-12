package com.perpussapp.perpusapp.Model;

public class ListChat  implements Comparable<ListChat> {
    private String nis, lastMessage;
    private  long time;
    private  int messageType;

    public ListChat() {
    }

    public ListChat(String nis, String lastMessage, long time, int messageType) {
        this.nis = nis;
        this.lastMessage = lastMessage;
        this.time = time;
        this.messageType = messageType;
    }

    public String getNis() {
        return nis;
    }

    public void setNis(String nis) {
        this.nis = nis;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }


    @Override
    public int compareTo(ListChat o) {
        return Long.compare(this.getTime(), o.getTime());
    }
}
