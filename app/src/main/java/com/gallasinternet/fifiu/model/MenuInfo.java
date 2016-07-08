package com.gallasinternet.fifiu.model;

/**
 * Created by Administrator on 8/19/2015.
 */
public class MenuInfo {
    private String title;
    private int image;
    private int notifys;

    public MenuInfo(String title, int image)
    {
        this.title = title;
        this.image = image;
        this.notifys = -1;
    }

    public MenuInfo(String title, int image, int notifys){
        this.title = title;
        this.image = image;
        this.notifys = notifys;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getNotifys() {
        return notifys;
    }

    public void setNotifys(int notifys) {
        this.notifys = notifys;
    }
}
