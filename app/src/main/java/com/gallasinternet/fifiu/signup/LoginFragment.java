package com.gallasinternet.fifiu.signup;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.SignupActivity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 8/13/2015.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    Button btnBack, btnLogin;
    EditText etxtUsername, etxtPassword;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_login, null);

        btnLogin = (Button)v.findViewById(R.id.btnLogin);
        btnBack = (Button)v.findViewById(R.id.btnBack);
        etxtUsername = (EditText)v.findViewById(R.id.etxtUsername);
        etxtPassword = (EditText)v.findViewById(R.id.etxtPassword);

        if(Global.pref.getBoolean(Constants.prefKeyLoggedIn, false)){
            etxtUsername.setText(Global.pref.getString(Constants.prefKeyUserEmail, ""));
            etxtPassword.setText(Global.pref.getString(Constants.prefKeyUserPswd, ""));
        }

        btnLogin.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        SignupActivity rootActivity = (SignupActivity) getActivity();

        if (view == btnBack) {
            Commons.dismissProgressHUD();
            rootActivity.setupContainer(0);
        }

        if (view == btnLogin) {
            String strUsername = etxtUsername.getText().toString();
            String strPswd = etxtPassword.getText().toString();

            if (strUsername.length() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_user_name), Toast.LENGTH_SHORT).show();
                return;
            }

            if (strPswd.length() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_password), Toast.LENGTH_SHORT).show();
                return;
            }

            final String regId = GCMRegistrar.getRegistrationId(getActivity());

            if(regId.equals("")){
                GCMRegistrar.register(getActivity(), Constants.SENDER_ID);
                Toast.makeText(getActivity(), getResources().getString(R.string.text_reg_id_not_ready), Toast.LENGTH_SHORT).show();
                return;
            }

            if ( !rootActivity.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                rootActivity.buildAlertMessageNoGps();
                return;
            }

            Commons.showProgressHUD(getActivity(), getResources().getString(R.string.text_login) + "...");

            LoginTask loginTask = new LoginTask(strUsername, strPswd, regId);

            loginTask.execute();
        }
    }



    public class LoginTask extends AsyncTask<Void, Void, String> {

        String mEmail, mPswd, mRegID;

        LoginTask(String email, String pswd, String regID) {
            mEmail = email;
            mPswd = pswd;
            mRegID = regID;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_LOGIN);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyEmail, mEmail));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyPassword, mPswd));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyDevRegID, mRegID));

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
                Commons.dismissProgressHUD();
                Toast.makeText(getActivity(), arrTmp[1], Toast.LENGTH_SHORT).show();
            }else{
                SharedPreferences.Editor editor = Global.pref.edit();

                editor.putString(Constants.prefKeyUserEmail, mEmail);
                editor.putString(Constants.prefKeyUserPswd, mPswd);
                editor.putString(Constants.prefKeyRegID, mRegID);
                editor.putBoolean(Constants.prefKeyLoggedIn, true);

                editor.commit();

                Global.myInfo = new UserInfo(result);
                ((SignupActivity)getActivity()).contactTask();
            }
        }
    }
}
