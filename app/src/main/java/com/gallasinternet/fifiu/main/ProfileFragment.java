package com.gallasinternet.fifiu.main;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.global.Commons;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;

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
 * Created by Administrator on 8/21/2015.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{
    Spinner spinnerSexLookingFor;
    SwitchButton switchLike, switchMessage;
    Button btnSave;
    SaveProfileTask mSaveProfileTask = null;
    private String[] arrGender = {"M", "F"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_profile, null);

        spinnerSexLookingFor = (Spinner)v.findViewById(R.id.spinnerSexLookingFor);
        switchLike = (SwitchButton)v.findViewById(R.id.switchLike);
        switchMessage = (SwitchButton)v.findViewById(R.id.switchMessage);
        btnSave = (Button)v.findViewById(R.id.btnProfileSave);

        switchLike.setChecked(true);
        switchMessage.setChecked(true);

        btnSave.setOnClickListener(this);

        initSpinner();

        String strLookingFor = Global.pref.getString(Constants.prefKeyLookingFor, "M");
        boolean boolLike = Global.pref.getBoolean(Constants.prefKeyNotifyLike, true);
        boolean boolMessage = Global.pref.getBoolean(Constants.prefKeyNotifyMessage, true);

        if(strLookingFor.equals("M")) spinnerSexLookingFor.setSelection(0); else spinnerSexLookingFor.setSelection(1);
        if(boolLike) switchLike.setChecked(true); else switchLike.setChecked(false);
        if(boolMessage) switchMessage.setChecked(true); else switchMessage.setChecked(false);

        return v;
    }

    void initSpinner(){
        ArrayAdapter<String> adapterLookingFor = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount(); // you dont display last item. It is used as hint.
            }
        };

        adapterLookingFor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterLookingFor.add(getResources().getString(R.string.text_male));
        adapterLookingFor.add(getResources().getString(R.string.text_female));

        spinnerSexLookingFor.setAdapter(adapterLookingFor);
        spinnerSexLookingFor.setSelection(0);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mSaveProfileTask != null && mSaveProfileTask.getStatus() != AsyncTask.Status.FINISHED) {
            mSaveProfileTask.cancel(true);
        }
    }

    @Override
    public void onClick(View view) {
        if(view == btnSave){
            Commons.showProgressHUD(getActivity(), getResources().getString(R.string.text_saving) + "...");

            int nPos = spinnerSexLookingFor.getSelectedItemPosition();

            String strLooking = arrGender[nPos];

            strLooking = strLooking.substring(0, 1);

            SharedPreferences.Editor editor = Global.pref.edit();

            editor.putBoolean(Constants.prefKeyNotifyLike, switchLike.isChecked());
            editor.putBoolean(Constants.prefKeyNotifyMessage, switchMessage.isChecked());

            editor.commit();

            mSaveProfileTask = new SaveProfileTask(strLooking, switchLike.isChecked(), switchMessage.isChecked());
            mSaveProfileTask.execute();
        }
    }

    public class SaveProfileTask extends AsyncTask<Void, Void, String> {

        String mSexLooking,mLike, mMessage;

        SaveProfileTask(String sexLooking, boolean like, boolean message) {
            mSexLooking = sexLooking;
            mLike = like ? "1":"0";
            mMessage = message ? "1":"0";
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_SET_PROFILE);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyID, Global.myInfo.strUserID));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeySexoProcura, mSexLooking));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyNotifyLike, mLike));
                nameValuePairs.add(new BasicNameValuePair(Constants.uKeyNotifyMsg, mMessage));

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
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }


}
