package edu.fsu.cs.mobile.geochat;

import java.util.Date;

public class ChatMessage {
    private String text;
    private String user;
    private long time;

    public ChatMessage(String text, String user){
        this.text = text;
        this.user = user;
        time = new Date().getTime();
    }

    public ChatMessage()    {}

    public long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public String getUser() {
        return user;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
