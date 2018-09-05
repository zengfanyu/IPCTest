package com.example.zengfanyu.aildtest.file;

import android.icu.text.PluralRules;

import java.io.Serializable;

/**
 * @author: zengfanyu
 * @Data: 2018/9/4
 * @Description:
 */
public class User implements Serializable {
    private int userId;
    private String userName;
    private boolean isMale;

    public User(int userId, String userName, boolean isMale) {
        this.userId = userId;
        this.userName = userName;
        this.isMale = isMale;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", isMale=" + isMale +
                '}';
    }
}
