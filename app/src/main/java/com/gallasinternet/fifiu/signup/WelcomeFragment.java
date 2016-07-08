package com.gallasinternet.fifiu.signup;

import android.app.Fragment;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.gallasinternet.fifiu.R;
import com.gallasinternet.fifiu.SignupActivity;
import com.gallasinternet.fifiu.global.Constants;

/**
 * Created by Administrator on 8/13/2015.
 */
public class WelcomeFragment extends Fragment implements View.OnClickListener {
    Button btnSignup, btnLogin, btnFacebook, btnGooglePlus;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_welcome, null);

        btnSignup = (Button)v.findViewById(R.id.btnSignup);
        btnLogin = (Button)v.findViewById(R.id.btnLogin);
        btnFacebook = (Button)v.findViewById(R.id.btnFacebook);
        btnGooglePlus = (Button)v.findViewById(R.id.btnGooglePlus);

        btnLogin.setOnClickListener(this);
        btnSignup.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
        btnGooglePlus.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        SignupActivity rootActivity = (SignupActivity)getActivity();

        if(view == btnLogin){
            rootActivity.setupContainer(1);
        }

        if(view == btnSignup){
            rootActivity.setupContainer(2);
        }

        if(view == btnFacebook){
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

            rootActivity.loginWithFacebook();
        }

        if(view == btnGooglePlus){
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

            rootActivity.loginWithGooglePuls();
        }
    }


}
