package com.gallasinternet.fifiu.main;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.model.GraphLocation;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pkmmte.view.CircularImageView;
import com.gallasinternet.fifiu.MainActivity;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.model.UserInfo;

/**
 * Created by Administrator on 8/26/2015.
 */
public class ContactsFragment extends Fragment {
    LinearLayout lyContacts;
    RelativeLayout rlyNoContacts;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_contacts, null);

        lyContacts = (LinearLayout)v.findViewById(R.id.lyContacts);
        rlyNoContacts = (RelativeLayout)v.findViewById(R.id.rlyNoContacts);

        if(Global.listContacts.size() == 0){
            rlyNoContacts.setVisibility(View.VISIBLE);
            ((MainActivity)getActivity()).setTitle(getResources().getString(R.string.title_contacts_no_matches));
        }else{
            rlyNoContacts.setVisibility(View.INVISIBLE);
            ((MainActivity)getActivity()).setTitle(getResources().getString(R.string.title_contacts));
        }

        displayContacts();
        return v;
    }

    void displayContacts(){
        lyContacts.removeAllViews();
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        for(int i = 0; i < Global.listContacts.size(); i ++){
            final UserInfo userInfo = Global.listContacts.get(i);

            RelativeLayout newCell = (RelativeLayout)layoutInflater.inflate(R.layout.contacts_item, null);
            final CircularImageView imgvPhoto = (CircularImageView)newCell.findViewById(R.id.imgvContactPhoto);
            Button btnAction = (Button)newCell.findViewById(R.id.btnContactAction);
            TextView tvName = (TextView)newCell.findViewById(R.id.tvContactName);
            TextView tvDescription = (TextView)newCell.findViewById(R.id.tvContactDescription);

            tvName.setText(userInfo.strUserName);

            if(userInfo.bmpPhoto == null){
                ImageLoader.getInstance().loadImage(userInfo.strPhotoURL, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        imgvPhoto.setImageBitmap(bitmap);
                        userInfo.bmpPhoto = bitmap;
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }else{
                imgvPhoto.setImageBitmap(userInfo.bmpPhoto);
            }

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            newCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Global.stackContainerID.push(Global.nContainerID);
                    Global.stackUserInfo.push(Global.otherInfo);

                    Global.otherInfo = userInfo;
                    ((MainActivity) getActivity()).setupContainer(3);
                }
            });

            lyContacts.addView(newCell);
        }
    }
}
