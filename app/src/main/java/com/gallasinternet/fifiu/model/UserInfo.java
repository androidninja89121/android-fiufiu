package com.gallasinternet.fifiu.model;

import android.graphics.Bitmap;

import com.gallasinternet.fifiu.global.Constants;

/**
 * Created by Administrator on 8/18/2015.
 */
public class UserInfo {
    public String          strUserID;
    public String          strUserName;
    public String          strPhotoURL;
    public String          strBirthDate;
    public String          strSex;
    public String          strSexLookingFor;
    public Bitmap          bmpPhoto;
    public String          strAge;//for push

    public UserInfo(String strData) {
        super();
        String arrTmp[] = strData.split("\\|");
        String[] arrVal = new String[5];

        for(int i = 0; i < 5; i ++){
            String arrOther[] = arrTmp[i + 1].split(":", 2);
            arrVal[i] = arrOther[1];
        }

        this.strUserID = arrVal[0];
        this.strUserName = arrVal[1];
        this.strPhotoURL = Constants.URL_PHOTO + strUserID + ".png";
        this.strBirthDate = arrVal[2];
        this.strSex = arrVal[3];
        this.strSexLookingFor = arrVal[4];
        this.bmpPhoto = null;
        this.strAge = "";
    }

    public UserInfo(){
        super();
        this.strUserID = "";
        this.strUserName = "";
        this.strPhotoURL = "";
        this.strBirthDate = "";
        this.strSex = "";
        this.strSexLookingFor = "";
        this.bmpPhoto = null;
        this.strAge = "";
    }
}
