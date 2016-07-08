package com.gallasinternet.fifiu;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gcm.GCMBaseIntentService;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.model.UserInfo;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
import java.util.Locale;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
    private final static int NOTIFICATION_ID = 412434;

    public GCMIntentService() {
        super(Constants.SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
    }

    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString(Constants.EXTRA_MESSAGE);

        if(message == null) return;

        String[] arrFields = message.split("\\|");
        String[] arrVal = new String[10];

        arrVal[0] = arrFields[0];

        for(int j = 1; j < arrFields.length; j ++) {
            String strFields = arrFields[j];
            int nPos = strFields.indexOf(":");
            arrVal[j] = strFields.substring(nPos + 1);
        }

        String strMsg = "";
        String strUserID = arrVal[1];
        String strFullName = arrVal[2];
        String strPhotoURL = arrVal[3];
        String strAge = arrVal[4];

        if(arrVal[0].equals(Constants.nKeyChat)){
            strMsg = arrVal[2] + " " + getResources().getString(R.string.text_says) +": " + arrVal[3];
            if(Global.pref.getBoolean(Constants.prefKeyNotifyMessage, false)) generateNotification(context, strUserID, strFullName, "", "", Constants.nKeyChat, strMsg);
            if(Global.chatFragment != null) Global.chatFragment.loadData();
        }

        if(arrVal[0].equals(Constants.nKeyLike)){
            strMsg = String.format(Locale.getDefault(), getResources().getString(R.string.push_newlike), strFullName);
            if(Global.pref.getBoolean(Constants.prefKeyNotifyLike, false)) generateNotification(context, strUserID, strFullName, strPhotoURL, strAge, Constants.nKeyLike, strMsg);
            if(Global.mainActivity != null) new GetContacts(Global.myInfo.strUserID).execute();
        }

        if(arrVal[0].equals(Constants.nKeyMatch)){
            if(Global.mainActivity != null){
                new GetContacts(Global.myInfo.strUserID).execute();

                Global.otherInfo = new UserInfo();
                Global.otherInfo.strUserID = strUserID;
                Global.otherInfo.strUserName = strFullName;
                Global.otherInfo.strPhotoURL = strPhotoURL;
                Global.otherInfo.bmpPhoto = null;

                ImageLoader.getInstance().loadImage(Global.otherInfo.strPhotoURL, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        Global.otherInfo.bmpPhoto = bitmap;
                        Global.mainActivity.setupContainer(4);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });

            }else{
                strMsg = String.format(Locale.getDefault(), getResources().getString(R.string.push_newmatch), strFullName);
                if(Global.pref.getBoolean(Constants.prefKeyNotifyLike, false)) generateNotification(context, strUserID, strFullName, strPhotoURL,strAge, Constants.nKeyMatch, strMsg);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
//        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String userid, String fullname, String url, String age, String mode, String message) {
        int icon = R.drawable.ic_notify;
        int requestID = (int) System.currentTimeMillis();
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        
        String title = context.getString(R.string.app_name);
        
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.putExtra(Constants.extraKeyUserID, userid);
        notificationIntent.putExtra(Constants.extraKeyFullName, fullname);
        notificationIntent.putExtra(Constants.extraKeyPhotoURL, url);
        notificationIntent.putExtra(Constants.extraKeyAge, age);
        notificationIntent.putExtra(Constants.extraKeyMode, mode);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message).setAutoCancel(true);

        mBuilder.setContentIntent(intent);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        notificationManager.notify(requestID, mBuilder.build());
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
        }
    }

}
