package com.gallasinternet.fifiu.main;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.gallasinternet.fifiu.MainActivity;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.global.Commons;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.model.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Administrator on 8/21/2015.
 */
public class MatchFragment extends Fragment implements View.OnClickListener{
    RelativeLayout rlyGood, rlyBad, rlyPreload;
    ImageView imgvPhoto;
    TextView tvAge, tvName;
    UserInfo mUserInfo;
    AdView mAdView;
    AdRequest mAdRequest;
    GifImageView gifImageView;
    GetNextLikeTask mTaskGetNext = null;
    SetLikeOrDisLikeTask mTaskSetLikeOrDislike = null;
    ImageLoader mImageLoader = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_match, null);

        rlyGood = (RelativeLayout)v.findViewById(R.id.rlyGood);
        rlyBad = (RelativeLayout)v.findViewById(R.id.rlyBad);
        rlyPreload = (RelativeLayout)v.findViewById(R.id.rlyPreload);
        imgvPhoto = (ImageView)v.findViewById(R.id.imgMatchPhoto);
        tvAge = (TextView)v.findViewById(R.id.tvAge);
        tvName = (TextView)v.findViewById(R.id.tvName);
        gifImageView = (GifImageView)v.findViewById(R.id.gifImageView);

        GifDrawable gifFromResource = (GifDrawable)gifImageView.getDrawable();

        gifFromResource.start();

        rlyGood.setOnClickListener(this);
        rlyBad.setOnClickListener(this);

        mUserInfo = new UserInfo();

        if(Global.otherInfo == null){

            getNextLike();
        }else{
            mUserInfo = Global.otherInfo;
            setUI();
        }

        mAdView = (AdView) v.findViewById(R.id.adView);
        mAdRequest = new AdRequest.Builder()
                .addTestDevice("lLHXH8P+ZweGyA6yzLsxV8iijoA=")
                .build();
        mAdView.loadAd(mAdRequest);
        return v;
    }

    void getNextLike(){
        rlyPreload.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.title_finding_matches));

        mTaskGetNext = new GetNextLikeTask();
        mTaskGetNext.execute();
    }

    void getUserData(String strData){
        String arrTmp[] = strData.split("\\|");
        String[] arrVal = new String[4];

        for(int i = 0; i < 4; i ++){
            String arrOther[] = arrTmp[i + 1].split(":", 2);
            arrVal[i] = arrOther[1];
        }

        mUserInfo = new UserInfo();

        mUserInfo.strUserID = arrVal[0];
        mUserInfo.strUserName = arrVal[1];
        mUserInfo.strBirthDate = arrVal[2];
        mUserInfo.strPhotoURL = arrVal[3];
    }

    void setUI(){
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.loadImage(mUserInfo.strPhotoURL, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                rlyPreload.setVisibility(View.INVISIBLE);
                ((MainActivity) getActivity()).setTitle(getResources().getString(R.string.title_matches));
                imgvPhoto.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

        tvName.setText(mUserInfo.strUserName);

        if(mUserInfo.strAge.length() < 1){
            int nAge = 25;

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = format.parse(mUserInfo.strBirthDate);
                nAge = Commons.getDiffYears(date, Calendar.getInstance().getTime());
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            tvAge.setText(nAge + " " + getResources().getString(R.string.text_years));
        }else{
            tvAge.setText(mUserInfo.strAge + " " + getResources().getString(R.string.text_years));
        }
    }

    @Override
    public void onClick(View view) {
        String isLike = "0";
        if(view == rlyGood) isLike = "1";

        mTaskSetLikeOrDislike = new SetLikeOrDisLikeTask(Global.myInfo.strUserID, mUserInfo.strUserID, isLike);
        mTaskSetLikeOrDislike.execute();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mTaskGetNext != null && mTaskGetNext.getStatus() != AsyncTask.Status.FINISHED) {
            mTaskGetNext.cancel(true);
        }

        if(mTaskSetLikeOrDislike != null && mTaskSetLikeOrDislike.getStatus() != AsyncTask.Status.FINISHED) {
            mTaskSetLikeOrDislike.cancel(true);
        }

        if(mImageLoader != null) mImageLoader.stop();
    }

    public class GetNextLikeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_GET_NEXT_LIKE);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyID, Global.myInfo.strUserID));

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

            String arrTmp[] = result.split("\\|");

            if(arrTmp[0].equals(Constants.ERROR)){
                Toast.makeText(getActivity(), arrTmp[1], Toast.LENGTH_SHORT).show();
            }else{
                getUserData(result);
                setUI();
            }
        }
    }

    public class SetLikeOrDisLikeTask extends AsyncTask<Void, Void, String> {

        String mUserID, mTargetUserID, mIsLike;

        SetLikeOrDisLikeTask(String userID, String targetUserID, String isLike) {
            mUserID = userID;
            mTargetUserID = targetUserID;
            mIsLike = isLike;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_SET_LIKE);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyID, mUserID));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyTargetID, mTargetUserID));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyLike, mIsLike));
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
            getNextLike();
        }
    }
}
