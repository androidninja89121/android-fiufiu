package com.gallasinternet.fifiu.signup;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pkmmte.view.CircularImageView;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.SignupActivity;
import com.gallasinternet.fifiu.global.Commons;
import com.gallasinternet.fifiu.global.Constants;
import com.gallasinternet.fifiu.global.Global;
import com.gallasinternet.fifiu.model.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 8/13/2015.
 */
public class SignupFragment extends Fragment implements View.OnClickListener {

    CircularImageView imgvPhoto;
    Button btnSignup, btnBack;
    Spinner spinnerGender, spinnerLookingFor;
    EditText etxtUsername, etxtEmail, etxtPassword;
    Bitmap selectedBmp = null;

    private static final int ACTION_REQUEST_GALLERY = 99;
    private static final int ACTION_REQUEST_CROP = 80;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_signup, null);

        imgvPhoto = (CircularImageView)v.findViewById(R.id.imgvLeftNavPhoto);
        btnSignup = (Button)v.findViewById(R.id.btnSignup);
        btnBack = (Button)v.findViewById(R.id.btnBack);
        spinnerGender = (Spinner)v.findViewById(R.id.spinnerGender);
        spinnerLookingFor = (Spinner)v.findViewById(R.id.spinnerLookingFor);
        etxtUsername = (EditText)v.findViewById(R.id.etxtUsername);
        etxtEmail = (EditText)v.findViewById(R.id.etxtEmail);
        etxtPassword = (EditText)v.findViewById(R.id.etxtPswd);

        initSpinner();

        imgvPhoto.setOnClickListener(this);
        btnSignup.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        return v;
    }

    void initSpinner(){
        ArrayAdapter<String> adapterGender = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }

        };

        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterGender.add("M");
        adapterGender.add("F");
        adapterGender.add(getResources().getString(R.string.text_gender));

        spinnerGender.setAdapter(adapterGender);
        spinnerGender.setSelection(adapterGender.getCount()); //display hint

        ArrayAdapter<String> adapterLookingFor = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }

        };

        adapterLookingFor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterLookingFor.add("M");
        adapterLookingFor.add("F");
        adapterLookingFor.add(getResources().getString(R.string.text_looking_for) + "...");

        spinnerLookingFor.setAdapter(adapterLookingFor);
        spinnerLookingFor.setSelection(adapterLookingFor.getCount()); //display hint
    }

    @Override
    public void onClick(View view) {

        if(view == imgvPhoto){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            Intent chooser = Intent.createChooser(intent, getResources().getString(R.string.text_choose_picture));
            startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
        }

        if(view == btnBack){
            SignupActivity rootActivity = (SignupActivity)getActivity();
            rootActivity.setupContainer(0);
        }

        if(view == btnSignup){
            String strUsername = etxtUsername.getText().toString();
            String strEmail = etxtEmail.getText().toString();
            String strPassword = etxtPassword.getText().toString();
            String strGender = spinnerGender.getSelectedItem().toString();
            String strLookingFor = spinnerLookingFor.getSelectedItem().toString();

            if(selectedBmp == null){
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_photo), Toast.LENGTH_SHORT).show();
                return;
            }

            if(strUsername.length() == 0){
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_user_name), Toast.LENGTH_SHORT).show();
                return;
            }

            if(strEmail.length() == 0){
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_email), Toast.LENGTH_SHORT).show();
                return;
            }

            if(strPassword.length() == 0){
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_password), Toast.LENGTH_SHORT).show();
                return;
            }

            if(!strGender.equals("F") && !strGender.equals("M")){
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_gender), Toast.LENGTH_SHORT).show();
                return;
            }

            if(!strLookingFor.equals("F") && !strLookingFor.equals("M")){
                Toast.makeText(getActivity(), getResources().getString(R.string.text_enter_lookingfor_gender), Toast.LENGTH_SHORT).show();
                return;
            }

            SignupActivity rootActivity = (SignupActivity)getActivity();

            if ( !rootActivity.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                rootActivity.buildAlertMessageNoGps();
                return;
            }

            String strGPS = Global.dblLat + ", " + Global.dblLng;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedBmp.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byte_arr = stream.toByteArray();
            ByteArrayBody bab = new ByteArrayBody(byte_arr, "profile.png");

            try {
                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                reqEntity.addPart(Constants.uKeyPhoto, bab);
                reqEntity.addPart(Constants.uKeyName, new StringBody(strUsername));
                reqEntity.addPart(Constants.uKeyEmail, new StringBody(strEmail));
                reqEntity.addPart(Constants.uKeyPassword, new StringBody(strPassword));
                reqEntity.addPart(Constants.uKeySex, new StringBody(strGender));
                reqEntity.addPart(Constants.uKeySexLookFor, new StringBody(strLookingFor));
                reqEntity.addPart(Constants.uKeyGPS, new StringBody(strGPS));

                Commons.showProgressHUD(getActivity(), getResources().getString(R.string.text_signup) + "...");

                SignupTask signupTask = new SignupTask(reqEntity);
                signupTask.execute();
            } catch (UnsupportedEncodingException e) {
                Log.e(e.getClass().getName(), e.getMessage());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ACTION_REQUEST_GALLERY) {
            try{
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA };
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                performCrop(picturePath);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if (resultCode == Activity.RESULT_OK && requestCode == ACTION_REQUEST_CROP ) {
            Bundle extras = data.getExtras();
            selectedBmp = extras.getParcelable("data");
            // Set The Bitmap Data To ImageView
            imgvPhoto.setImageBitmap(selectedBmp);
            imgvPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private void performCrop(String picUri) {
        try {
            //Start Crop Activity

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            File f = new File(picUri);
            Uri contentUri = Uri.fromFile(f);

            cropIntent.setDataAndType(contentUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, ACTION_REQUEST_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.text_not_support_crop), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public class SignupTask extends AsyncTask<Void, Void, String> {

        MultipartEntity mMultiPartEntity;

        SignupTask(MultipartEntity multipartEntity) {
            mMultiPartEntity = multipartEntity;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpPost post = new HttpPost(Constants.URL_SIGNUP);

                post.setEntity(mMultiPartEntity);
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
                Toast.makeText(getActivity(), arrTmp[1], Toast.LENGTH_SHORT).show();
            }else{
                String arrOther[] = arrTmp[1].split(":");
                Global.myInfo = new UserInfo();

                Global.myInfo.strUserID = arrOther[1];
                Global.myInfo.strUserName = etxtUsername.getText().toString();
                Global.myInfo.strPhotoURL = Constants.URL_PHOTO + arrOther[1] + ".png";
                Global.myInfo.strBirthDate = "0000-00-00";
                Global.myInfo.strSex = spinnerGender.getSelectedItem().toString();
                Global.myInfo.strSexLookingFor = spinnerLookingFor.getSelectedItem().toString();

                SharedPreferences.Editor editor = Global.pref.edit();

                editor.putBoolean(Constants.prefKeyNotifyLike, true);
                editor.putBoolean(Constants.prefKeyNotifyMessage, true);
                editor.putString(Constants.prefKeyLookingFor, Global.myInfo.strSexLookingFor);

                editor.commit();

                ((SignupActivity) getActivity()).gotoMain();
            }
        }
    }
}
