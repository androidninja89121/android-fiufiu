package com.gallasinternet.fifiu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pkmmte.view.CircularImageView;
import com.gallasinternet.fifiu.adapter.LeftNavAdapter;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.main.ChatFragment;
import com.gallasinternet.fifiu.main.ContactsFragment;
import com.gallasinternet.fifiu.main.MatchFragment;
import com.gallasinternet.fifiu.main.ProfileFragment;
import com.gallasinternet.fifiu.main.SuggestedMatchFragment;
import com.gallasinternet.fifiu.model.MenuInfo;
import com.gallasinternet.fifiu.model.UserInfo;
import com.gallasinternet.fifiu.utils.CustomActivity;

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
import java.util.Stack;

/**
 * Created by Administrator on 8/13/2015.
 */
public class MainActivity extends CustomActivity{

    public DrawerLayout drawerLayout;
    public ListView drawerLeft;
    private ActionBarDrawerToggle drawerToggle;
    public boolean leftBarOpened = false;
    Button btnHome;
    TextView tvHeaderTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHome = (Button)findViewById(R.id.btnHome);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(drawerLeft);

                btnHome.setX(800);
            }
        });

        tvHeaderTitle = (TextView)findViewById(R.id.txtHeaderTitle);

        Global.otherInfo = null;
        Global.stackContainerID = new Stack<Integer>();
        Global.stackUserInfo = new Stack<UserInfo>();

        onNewIntent(getIntent());
    }

    public void setTitle(String strTitle){
        tvHeaderTitle.setText(strTitle);
    }

    @Override
    public void onNewIntent(Intent intent){
        Bundle bundle = intent.getExtras();

        if(bundle == null){
            Global.mainActivity = this;
            Global.signupActivity = null;
            Global.chatFragment = null;

            setupDrawer();

            SharedPreferences.Editor editor = Global.pref.edit();

            editor.putString(Constants.prefKeyUserID, Global.myInfo.strUserID);
            editor.putString(Constants.prefKeyUserName, Global.myInfo.strUserName);
            editor.putString(Constants.prefKeyPhotoURL, Global.myInfo.strPhotoURL);

            editor.commit();

            setupContainer(1);
        }else{
            String strUserID = bundle.getString(Constants.extraKeyUserID, "");
            String strFullName = bundle.getString(Constants.extraKeyFullName, "");
            String strPhotoURL = bundle.getString(Constants.extraKeyPhotoURL, "");
            String strAge = bundle.getString(Constants.extraKeyAge, "");
            String strMode = bundle.getString(Constants.extraKeyMode, "");

            Global.otherInfo = new UserInfo();

            Global.otherInfo.strUserID = strUserID;
            Global.otherInfo.strUserName = strFullName;
            Global.otherInfo.strPhotoURL = strPhotoURL;
            Global.otherInfo.bmpPhoto = null;
            Global.otherInfo.strAge = strAge;

            if(Global.mainActivity == null){
                Global.mainActivity = this;
                Global.signupActivity = null;
                Global.chatFragment = null;

                Global.myInfo = new UserInfo();

                Global.myInfo.strUserID = Global.pref.getString(Constants.prefKeyUserID, "");
                Global.myInfo.strUserName = Global.pref.getString(Constants.prefKeyUserName, "");
                Global.myInfo.strPhotoURL = Global.pref.getString(Constants.prefKeyPhotoURL, "");

                new GetContacts(Global.myInfo.strUserID).execute();
            }else {

                if (strMode.equals(Constants.nKeyChat)) {
                    setupContainer(3);
                }else if(strMode.equals(Constants.nKeyLike)){
                    setupContainer(1);
                }else if(strMode.equals(Constants.nKeyMatch)){
                    setupContainer(4);
                }

            }
        }
    }



    private void setupDrawer()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.home_icon, R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view)
            {
                invalidateOptionsMenu();
                btnHome.setX(50);

                leftBarOpened = false;
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                leftBarOpened = true;
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.closeDrawers();

        setupLeftNavDrawer();
    }

    public void setupContainer(int nIdx)
    {
        Global.nContainerID = nIdx;
        Global.chatFragment = null;
        switch (nIdx) {
            case 0:
                tvHeaderTitle.setText(getResources().getString(R.string.title_contacts));
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new ContactsFragment()).commit();
                break;
            case 1:
                tvHeaderTitle.setText(getResources().getString(R.string.title_matches));
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new MatchFragment()).commit();
                break;
            case 2:
                tvHeaderTitle.setText(getResources().getString(R.string.title_profile));
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new ProfileFragment()).commit();
                break;
            case 3:
                tvHeaderTitle.setText(getResources().getString(R.string.title_chat));
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new ChatFragment()).commit();
                break;
            case 4:
//                tvHeaderTitle.setText(getResources().getString(R.string.title_newmatch));
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new SuggestedMatchFragment()).commit();
                break;
        }
    }

    private void setupLeftNavDrawer()
    {
        drawerLeft = (ListView) findViewById(R.id.left_drawer);

        View header = getLayoutInflater().inflate(R.layout.left_nav_header, null);

        final CircularImageView imgvPhoto = (CircularImageView)header.findViewById(R.id.imgvLeftNavPhoto);
        TextView tvUserName = (TextView)header.findViewById(R.id.tvLeftNavUserName);

        tvUserName.setText(Global.myInfo.strUserName);

        if(Global.myInfo.bmpPhoto == null){
            ImageLoader.getInstance().loadImage(Global.myInfo.strPhotoURL, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imgvPhoto.setImageBitmap(bitmap);
                    Global.myInfo.bmpPhoto = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else{
            imgvPhoto.setImageBitmap(Global.myInfo.bmpPhoto);
        }

        drawerLeft.addHeaderView(header);
        drawerLeft.setHeaderDividersEnabled(false);

        final ArrayList<MenuInfo> arrMenu = new ArrayList<MenuInfo>();

        arrMenu.add(new MenuInfo(getResources().getString(R.string.title_contacts), R.drawable.ic_message, Global.listContacts.size()));
        arrMenu.add(new MenuInfo(getResources().getString(R.string.title_matches), R.drawable.ic_matches));
        arrMenu.add(new MenuInfo(getResources().getString(R.string.title_profile), R.drawable.ic_profile));

        final LeftNavAdapter adp = new LeftNavAdapter(this, arrMenu);
        drawerLeft.setAdapter(adp);
        drawerLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View arg1, int arg2,
                                    long arg3)
            {
                if (arg2 == 0)
                    return;

                if(Global.nContainerID != arg2 - 1){
                    Global.stackContainerID.push(Global.nContainerID);
                    Global.stackUserInfo.push(Global.otherInfo);
                }

                Global.otherInfo = null;
                setupContainer(arg2 - 1);

                drawerLayout.closeDrawers();
                adp.notifyDataSetChanged();
                // drawerLayout.closeDrawers();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(Global.stackContainerID.empty()){
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
        }else{
            int nIdx = (int)Global.stackContainerID.pop();
            Global.otherInfo = Global.stackUserInfo.pop();

            setupContainer(nIdx);
        }
    }

    void getContacts(String strResult){
        Global.listContacts.clear();
        String[] arrTmp = strResult.split("<br>");

        for(int i = 0; i < arrTmp.length; i ++){
            String strTmp = arrTmp[i];

            if(strTmp.length() == 0) continue;

            String[] arrOne = strTmp.split("\\|");

            String[] arrVal = new String[3];

            for(int j = 0; j < 2; j ++){
                String arrTwo[] = arrOne[j].split(":");
                arrVal[j] = arrTwo[1];
            }

            arrVal[2] = arrOne[2].substring(6);

            UserInfo userInfo = new UserInfo();

            userInfo.strUserID = arrVal[0];
            userInfo.strUserName = arrVal[1];
            userInfo.strPhotoURL = arrVal[2];

            Global.listContacts.add(userInfo);
        }
    }

    public class GetContacts extends AsyncTask<Void, Void, String> {

        String mUserID;

        GetContacts(String strUserID) {
            mUserID = strUserID;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_GET_CONTACTS);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyID, mUserID));

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
            getContacts(result);
            setupDrawer();
            setupContainer(3);
        }
    }
}
