package com.gallasinternet.fifiu;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.gallasinternet.fifiu.global.Commons;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.model.UserInfo;
import com.gallasinternet.fifiu.signup.LoginFragment;
import com.gallasinternet.fifiu.signup.SignupFragment;
import com.gallasinternet.fifiu.signup.WelcomeFragment;
import com.gallasinternet.fifiu.utils.CustomActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 8/13/2015.
 */
public class SignupActivity extends CustomActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    public LocationManager locationManager = null;
    LocationListener locationListener = null;
    UiLifecycleHelper uiHelper;
    LoginButton btnFacebookLogin;
    String strFBID, strFBImgURL, strFBName, strFBEmail, strFBGender, getStrFBGenderLookingFor;
    GoogleApiClient mGoogleApiClient;
    ConnectionResult mConnectionResult;
    boolean mIntentInProgress;
    boolean signedInUser;
    boolean isGooglePlusButtonClicked = false;
    boolean isFacebookButtonClicked =false;

    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        Global.mainActivity = null;
        Global.signupActivity = this;
        Global.chatFragment = null;

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();

        btnFacebookLogin = new LoginButton(this);
        btnFacebookLogin.setReadPermissions(Arrays.asList("email", "public_profile"));
        Session.getActiveSession().closeAndClearTokenInformation();

        initGPS();
        setupContainer(0);

        registerReceiver(mHandleMessageReceiver, new IntentFilter(Constants.DISPLAY_MESSAGE_ACTION));

        final String regId = GCMRegistrar.getRegistrationId(this);
        if(regId.equals("")){
            GCMRegistrar.register(this, Constants.SENDER_ID);
        }
    }

    public void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.text_gpsrequired))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.button_gpsrequired_config), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getResources().getString(R.string.button_gpsrequired_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(Constants.EXTRA_MESSAGE);
             }
    };

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }


    public void loginWithGooglePuls(){
        isGooglePlusButtonClicked = true;
        googlePlusLogin();
    }

    public void loginWithFacebook(){
        isFacebookButtonClicked = true;
        btnFacebookLogin.performClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    signedInUser = false;
                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {
            if(sessionState.isOpened()){

                if(!isFacebookButtonClicked){
                    session.closeAndClearTokenInformation();
                    return;
                }

                Request me = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser graphUser, Response response) {
                        if (graphUser != null) {
                            strFBID = graphUser.getId();
                            strFBEmail = graphUser.getProperty("email").toString();
                            strFBImgURL = "https://graph.facebook.com/" + strFBID + "/picture?width=800&height=800";
                            strFBName = graphUser.getName();
                            strFBGender = graphUser.asMap().get("gender").toString();

                            if(strFBGender.equals("M") || strFBGender.equals("male")){
                                strFBGender = "M";
                                getStrFBGenderLookingFor = "F";
                            }else{
                                strFBGender = "F";
                                getStrFBGenderLookingFor = "M";
                            }

                            new LoginWithSocialTask().execute();
                        }
                    }
                });

                Bundle params = me.getParameters();
                params.putString("fields", "email,name,birthday,gender");
                me.setParameters(params);
                me.executeAsync();

            }else if(sessionState.isClosed()){
            }
        }
    };

    public void setupContainer(int nIdx)
    {
        switch (nIdx) {
            case 0:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new WelcomeFragment()).commit();
                break;
            case 1:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new LoginFragment()).commit();
                break;
            case 2:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new SignupFragment()).commit();
                break;
        }
    }

    public void gotoMain()
    {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void initGPS(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void onTest(){
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                strFBName = currentPerson.getDisplayName();
                strFBImgURL = currentPerson.getImage().getUrl();
                strFBEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
                strFBID = currentPerson.getId();

                strFBGender = "M";
                getStrFBGenderLookingFor = "F";

                Commons.showProgressHUD(SignupActivity.this, getResources().getString(R.string.text_login) + "...");

                new LoginWithSocialTask().execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        signedInUser = false;

        if(!isGooglePlusButtonClicked){
            googlePlusLogout();
            return;
        }

        onTest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (signedInUser) {
                resolveSignInError();
            }
        }
    }

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            Global.dblLat = loc.getLatitude();
            Global.dblLng = loc.getLongitude();

    /*----------to get City-Name from coordinates ------------- */
            String cityName=null;
            String countryName = null;

            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());

                cityName=addresses.get(0).getLocality();
                countryName = addresses.get(0).getCountryName();

                Global.strCity = cityName;
                Global.strCountry = countryName;

                SharedPreferences.Editor editor = Global.pref.edit();

                editor.putString(Constants.prefKeyCity, cityName);
                editor.putString(Constants.prefKeyCountry, countryName);
                editor.putFloat(Constants.prefKeyLat, (float)Global.dblLat);
                editor.putFloat(Constants.prefKeyLng, (float)Global.dblLng);

                editor.commit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }


        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    public void contactTask(){
        new GetContacts(Global.myInfo.strUserID).execute();
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

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    public class LoginWithSocialTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_LOGIN);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyFGToken, strFBID));

                String regId = GCMRegistrar.getRegistrationId(SignupActivity.this);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyDevRegID, regId));

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
                new SignupWithSocialTask().execute();

            }else{
                Commons.dismissProgressHUD();
                Global.myInfo = new UserInfo(result);
                contactTask();
            }
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
            new GetProfileTask().execute();
        }
    }

    void processProfile(String strResult){
        String arrTmp[] = strResult.split("\\|");
        String[] arrVal = new String[3];

        for(int i = 0; i < 3; i ++){
            String arrOther[] = arrTmp[i + 1].split(":");
            arrVal[i] = arrOther[1];
        }

        SharedPreferences.Editor editor = Global.pref.edit();

        editor.putString(Constants.prefKeyLookingFor, arrVal[0]);
        editor.putBoolean(Constants.prefKeyNotifyLike, arrVal[1].equals("1"));
        editor.putBoolean(Constants.prefKeyNotifyMessage, arrVal[2].equals("1"));

        editor.commit();
    }

    public class GetProfileTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_GET_PROFILE);
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
            Commons.dismissProgressHUD();
            String arrTmp[] = result.split("\\|");

            if(arrTmp[0].equals(Constants.ERROR)){
                Toast.makeText(getBaseContext(), arrTmp[1], Toast.LENGTH_SHORT).show();
            }else{
                processProfile(result);
                gotoMain();
            }
        }
    }


    public class SignupWithSocialTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(strFBImgURL).getContent());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                ByteArrayBody bab = new ByteArrayBody(byte_arr, "profile.png");

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                reqEntity.addPart(Constants.uKeyPhoto, bab);
                reqEntity.addPart(Constants.uKeyName, new StringBody(strFBName));
                reqEntity.addPart(Constants.uKeyEmail, new StringBody(strFBEmail));
                reqEntity.addPart(Constants.uKeyFGToken, new StringBody(strFBID));
                reqEntity.addPart(Constants.uKeySex, new StringBody(strFBGender));
                reqEntity.addPart(Constants.uKeySexLookFor, new StringBody(getStrFBGenderLookingFor));
                reqEntity.addPart(Constants.uKeyGPS, new StringBody(Global.dblLat + ", " + Global.dblLng));

                HttpPost post = new HttpPost(Constants.URL_SIGNUP);

                post.setEntity(reqEntity);
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
            Commons.dismissProgressHUD();

            String arrTmp[] = result.split("\\|");

            if(arrTmp[0].equals(Constants.ERROR)){
                Toast.makeText(getBaseContext(), arrTmp[1], Toast.LENGTH_SHORT).show();
            }else{
                String arrOther[] = arrTmp[1].split(":");
                Global.myInfo = new UserInfo();

                Global.myInfo.strUserID = arrOther[1];
                Global.myInfo.strUserName = strFBName;
                Global.myInfo.strPhotoURL = Constants.URL_PHOTO + arrOther[1] + ".png";
                Global.myInfo.strBirthDate = "0000-00-00";
                Global.myInfo.strSex = strFBGender;
                Global.myInfo.strSexLookingFor = getStrFBGenderLookingFor;

                SharedPreferences.Editor editor = Global.pref.edit();

                editor.putBoolean(Constants.prefKeyNotifyLike, true);
                editor.putBoolean(Constants.prefKeyNotifyMessage, true);
                editor.putString(Constants.prefKeyLookingFor, getStrFBGenderLookingFor);

                editor.commit();

                gotoMain();
            }
        }
    }
}
