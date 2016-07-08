package com.gallasinternet.fifiu.global;

import android.content.SharedPreferences;

import com.gallasinternet.fifiu.MainActivity;
import com.gallasinternet.fifiu.SignupActivity;
import com.gallasinternet.fifiu.main.ChatFragment;
import com.gallasinternet.fifiu.model.MessageInfo;
import com.gallasinternet.fifiu.model.UserInfo;
import com.gallasinternet.fifiu.utils.ProgressHUD;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Administrator on 8/14/2015.
 */
public class Global {
    public static ProgressHUD progressHUD;

    public static String strCity;
    public static String strCountry;
    public static double dblLat;
    public static double dblLng;
    public static SharedPreferences pref;
    public static UserInfo myInfo;
    public static UserInfo otherInfo;
    public static ArrayList<UserInfo> listContacts = new ArrayList<UserInfo>();
    public static ArrayList<MessageInfo> listMessages = new ArrayList<MessageInfo>();
    public static Stack<Integer> stackContainerID = new Stack<Integer>();
    public static Stack<UserInfo> stackUserInfo = new Stack<UserInfo>();
    public static SignupActivity signupActivity = null;
    public static MainActivity mainActivity = null;
    public static ChatFragment chatFragment = null;
    public static int nContainerID;
}

