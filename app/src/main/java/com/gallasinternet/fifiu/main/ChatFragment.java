package com.gallasinternet.fifiu.main;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pkmmte.view.CircularImageView;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.model.MessageInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 8/27/2015.
 */
public class ChatFragment extends Fragment implements View.OnClickListener{
    TextView tvName;
    Button btnSend;
    CircularImageView imgvPhoto;
    LinearLayout lyContent;
    EditText etxtChatMessage;
    ScrollView scrollChat;
    AdView mAdView;
    AdRequest mAdRequest;
    GetChatHistory mGetChatHistory = null;
    SendChatMessage mSendChatMessage = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_chat, null);

        tvName = (TextView)v.findViewById(R.id.tvChatName);
        btnSend = (Button)v.findViewById(R.id.btnSend);
        imgvPhoto = (CircularImageView)v.findViewById(R.id.imgvChatPhoto);
        lyContent = (LinearLayout)v.findViewById(R.id.lyMsgContent);
        etxtChatMessage = (EditText)v.findViewById(R.id.etxtMsg);
        scrollChat = (ScrollView)v.findViewById(R.id.scrollViewChat);

        btnSend.setOnClickListener(this);

        if(Global.otherInfo.bmpPhoto == null){
            ImageLoader.getInstance().loadImage(Global.otherInfo.strPhotoURL, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imgvPhoto.setImageBitmap(bitmap);
                    Global.otherInfo.bmpPhoto = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            imgvPhoto.setImageBitmap(Global.otherInfo.bmpPhoto);
        }

        tvName.setText(Global.otherInfo.strUserName);

        loadData();

        mAdView = (AdView) v.findViewById(R.id.adView);
        mAdRequest = new AdRequest.Builder()
                .addTestDevice("lLHXH8P+ZweGyA6yzLsxV8iijoA=")
                .build();
        mAdView.loadAd(mAdRequest);
        return v;
    }

    public void loadData(){
        String strUserIDFrom = Global.myInfo.strUserID;
        String strUserIDTo = Global.otherInfo.strUserID;

        Global.chatFragment = this;

        mGetChatHistory = new GetChatHistory(strUserIDFrom, strUserIDTo);
        mGetChatHistory.execute();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mGetChatHistory != null && mGetChatHistory.getStatus() != AsyncTask.Status.FINISHED) {
            mGetChatHistory.cancel(true);
        }

        if(mSendChatMessage != null && mSendChatMessage.getStatus() != AsyncTask.Status.FINISHED) {
            mSendChatMessage.cancel(true);
        }
    }

    @Override
    public void onClick(View view) {
        if(view == btnSend){
            String strMsg = etxtChatMessage.getText().toString();
            if(strMsg.length() < 1) return;
            mSendChatMessage = new SendChatMessage(Global.myInfo.strUserID, Global.otherInfo.strUserID, strMsg);
            mSendChatMessage.execute();
            etxtChatMessage.setText("");
        }
    }

    void getChatHistory(String strResult){
        Global.listMessages.clear();

        String[] arrRecord = strResult.split("<br>");

        for(int i = 1; i < arrRecord.length; i ++){
            String strSentence = arrRecord[i];
            String[] arrFields = strSentence.split("\\|");
            String[] arrVal = new String[10];

            for(int j = 0; j < arrFields.length; j ++) {
                String strFields = arrFields[j];
                int nPos = strFields.indexOf(":");
                arrVal[j] = strFields.substring(nPos + 1);
            }

            MessageInfo messageInfo = new MessageInfo();
            messageInfo.strText = arrVal[8];
            messageInfo.strDate = arrVal[0];
            messageInfo.strTime = arrVal[1];
            messageInfo.strUserIDFrom = arrVal[2];
            messageInfo.strUserIDTo = arrVal[5];

            Global.listMessages.add(messageInfo);
        }
    }

    void displayData(){
        lyContent.removeAllViews();
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        if(Global.listMessages.size() == 0){
            TextView tv = new TextView(getActivity());
            tv.setText(getResources().getString(R.string.text_no_chat_history));
            lyContent.addView(tv);
            return;
        }

        for(int i = 0; i < Global.listMessages.size(); i ++){
            MessageInfo messageInfo = Global.listMessages.get(i);
            String strUserIDFrom = messageInfo.strUserIDFrom;
            final boolean isMe = Global.myInfo.strUserID.equals(strUserIDFrom);

            RelativeLayout newCell = (RelativeLayout)layoutInflater.inflate(R.layout.cell_message_other, null);

            if(isMe){
                newCell = (RelativeLayout)layoutInflater.inflate(R.layout.cell_message_me, null);
            }

            final CircularImageView imgvPhotoOther = (CircularImageView)newCell.findViewById(R.id.imgvMsgMePhoto);
            TextView tvContent = (TextView)newCell.findViewById(R.id.tvMsgMeContent);
            TextView tvDate = (TextView)newCell.findViewById(R.id.tvMsgDate);

            tvContent.setText(messageInfo.strText);
            tvDate.setText(messageInfo.strDate + " " + messageInfo.strTime);

            String strPhotoURL = (isMe)? Global.myInfo.strPhotoURL: Global.otherInfo.strPhotoURL;
            Bitmap bmpPhoto = (isMe)? Global.myInfo.bmpPhoto: Global.otherInfo.bmpPhoto;

            if(bmpPhoto == null){
                ImageLoader.getInstance().loadImage(strPhotoURL, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        imgvPhotoOther.setImageBitmap(bitmap);

                        if(isMe){
                            Global.myInfo.bmpPhoto = bitmap;
                        }else{
                            Global.otherInfo.bmpPhoto = bitmap;
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }else{
                imgvPhotoOther.setImageBitmap(bmpPhoto);
            }

            lyContent.addView(newCell);
        }

        scrollChat.post(new Runnable() {
            @Override
            public void run() {
                scrollChat.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public class GetChatHistory extends AsyncTask<Void, Void, String> {

        String mUserIDFrom, mUserIDTo;

        GetChatHistory(String strUserIDFrom, String strUserIDTo) {
            mUserIDFrom = strUserIDFrom;
            mUserIDTo = strUserIDTo;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_GET_CHAT_HISTORY);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyUserIDFrom, mUserIDFrom));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyUserIDTo, mUserIDTo));

                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);

                HttpEntity entity = response.getEntity();
                String responseText = EntityUtils.toString(entity);

                return responseText;
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getChatHistory(result);
            displayData();
        }
    }

    public class SendChatMessage extends AsyncTask<Void, Void, String> {

        String mUserIDFrom, mUserIDTo, mChatMsg;

        SendChatMessage(String strUserIDFrom, String strUserIDTo, String strChatMsg) {
            mUserIDFrom = strUserIDFrom;
            mUserIDTo = strUserIDTo;
            mChatMsg = strChatMsg;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_SEND_CHAT_MESSAGE);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyUserIDFrom, mUserIDFrom));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyUserIDTo, mUserIDTo));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyText, mChatMsg));

                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);

                HttpEntity entity = response.getEntity();
                String responseText = EntityUtils.toString(entity);

                return responseText;
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            loadData();
        }
    }
}
