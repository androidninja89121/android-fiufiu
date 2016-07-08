package com.gallasinternet.fifiu.main;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pkmmte.view.CircularImageView;
import com.gallasinternet.fifiu.MainActivity;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.global.Global;

import java.util.Locale;

/**
 * Created by Administrator on 9/11/2015.
 */
public class SuggestedMatchFragment extends Fragment implements View.OnClickListener{
//    RelativeLayout rlyButtonChatMe, rlyButtonFindMore;
    Button btnMesssageMe, btnKeepLooking;
    CircularImageView imgvMe, imgvOther;
    TextView tvMatch1, tvMatch2, tvBtnChat, tvHeader;
    ImageLoader mImageLoaderOther = null, mImageLoaderMe = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
//        View v = inflater.inflate(R.layout.fragment_suggested_match, null);
        View v = inflater.inflate(R.layout.temp_your_match, null);

//        rlyButtonChatMe = (RelativeLayout)v.findViewById(R.id.rlyButtonChatMe);
//        rlyButtonFindMore = (RelativeLayout)v.findViewById(R.id.rlyButtonFindMore);
        btnMesssageMe = (Button)v.findViewById(R.id.buttonMessage);
        btnKeepLooking = (Button)v.findViewById(R.id.buttonKeepLooking);
//        imgvMe = (CircularImageView)v.findViewById(R.id.imgvCurrentUser);
//        imgvOther = (CircularImageView)v.findViewById(R.id.imgvTargetUser);
        imgvMe = (CircularImageView)v.findViewById(R.id.imgvLeftNavPhoto);
        imgvOther = (CircularImageView)v.findViewById(R.id.imgvRight);
        tvMatch1 = (TextView)v.findViewById(R.id.textViewMatch);
        tvMatch2 = (TextView)v.findViewById(R.id.textView24);
//        tvBtnChat = (TextView)v.findViewById(R.id.tvButtonChatMe);
        tvHeader = (TextView)v.findViewById(R.id.textViewAppLogo);

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(),"black_jack.ttf");
        tvHeader.setTypeface(type);

//        rlyButtonChatMe.setOnClickListener(this);
//        rlyButtonFindMore.setOnClickListener(this);
        btnMesssageMe.setOnClickListener(this);
        btnKeepLooking.setOnClickListener(this);

        setUI();
        return v;
    }

    void setUI(){

        ((MainActivity)getActivity()).setTitle(getResources().getString(R.string.title_newmatch));

        if(Global.otherInfo.bmpPhoto == null){
            mImageLoaderOther = ImageLoader.getInstance();
            mImageLoaderOther.loadImage(Global.otherInfo.strPhotoURL, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imgvOther.setImageBitmap(bitmap);
                    Global.otherInfo.bmpPhoto = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            imgvOther.setImageBitmap(Global.otherInfo.bmpPhoto);
        }

        if(Global.myInfo.bmpPhoto == null){
            mImageLoaderMe = ImageLoader.getInstance();
            mImageLoaderMe.loadImage(Global.myInfo.strPhotoURL, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imgvMe.setImageBitmap(bitmap);
                    Global.myInfo.bmpPhoto = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            imgvMe.setImageBitmap(Global.myInfo.bmpPhoto);
        }

        String strFormat1 = getResources().getString(R.string.text_newmatch1);
        String strNewMatch1 = String.format(Locale.getDefault(), strFormat1, Global.otherInfo.strUserName);
        String strFormat2 = getResources().getString(R.string.text_newmatch2);
        String strNewMatch2 = String.format(Locale.getDefault(), strFormat2, Global.otherInfo.strUserName, Global.otherInfo.strUserName);

        tvMatch1.setText(strNewMatch1);
        tvMatch2.setText(strNewMatch2);

        String strFormatButton = getResources().getString(R.string.text_newmatch_buttonchat);
        String strButtonChat = String.format(Locale.getDefault(), strFormatButton, Global.otherInfo.strUserName);

        btnMesssageMe.setText(strButtonChat);
//        tvBtnChat.setText(strButtonChat);
    }

    @Override
    public void onClick(View view) {
//        if(view == rlyButtonChatMe){
        Global.stackContainerID.push(Global.nContainerID);
        Global.stackUserInfo.push(Global.otherInfo);

        if(view == btnMesssageMe){
            ((MainActivity)getActivity()).setupContainer(3);
        }else{
            ((MainActivity)getActivity()).setupContainer(1);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mImageLoaderMe != null) mImageLoaderMe.stop();
        if(mImageLoaderOther != null) mImageLoaderOther.stop();
    }
}