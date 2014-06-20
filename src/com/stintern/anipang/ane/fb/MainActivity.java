package com.stintern.anipang.ane.fb;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.ProfilePictureView;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.stintern.anipang.ane.utils.ANEApplication;
import com.stintern.anipang.ane.utils.InfoFetcher;

public class MainActivity extends Activity {
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions, user_photos");
    
	private static final String TAG = MainActivity.class.getSimpleName();
	
    private Button buttonLoginLogout;
    
    private InfoFetcher 	_infoFetcher;
    private ANEApplication 	_aneApplication; 
    
	private UiLifecycleHelper _uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(
        	getResources().getIdentifier("activity_main", "layout", this.getPackageName())
        	);
        
        _uiHelper = new UiLifecycleHelper(this, statusCallback);
        _uiHelper.onCreate(savedInstanceState);

        buttonLoginLogout = (Button)findViewById(getResources().getIdentifier("buttonLoginLogout", "id", this.getPackageName()));
        _aneApplication = (ANEApplication)getApplication();
        
        _infoFetcher = new InfoFetcher();
        
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        
        int appID = getResources().getIdentifier("app_id", "string", this.getPackageName());
	    int secretKey = getResources().getIdentifier("app_secret", "string", this.getPackageName());
	    Parse.initialize(getApplicationContext(), getString(appID), getString(secretKey));
	    ParseFacebookUtils.initialize(getString(appID));
        
        Session session = Session.getActiveSession();
		Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
        session.requestNewPublishPermissions(newPermissionsRequest);
        
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }

        updateView();
	}

	private void updateView() {
        Session session = Session.getActiveSession();
        
        
        if (session.isOpened()) {
        	
        	// 로그인이 되었을 경우 정보를 가져옴
        	if( !_aneApplication.isLoggedIn() )
        	{
        		_infoFetcher.fetchUserInformation(this);
        	}
        	buttonLoginLogout.setText(getResources().getIdentifier("btn_logout", "string", this.getPackageName()));
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
        } 
        else {
            
        	buttonLoginLogout.setText(getResources().getIdentifier("btn_login", "string", this.getPackageName()));
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });
        }
    }

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void onClickLogout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
        }
    }
    
    
	public void setImage()
	{
		ProfilePictureView image = new ProfilePictureView(this);

		image = (ProfilePictureView)findViewById(getResources().getIdentifier("profilepic", "id", this.getPackageName()) );
		image.setProfileId(_aneApplication.getCurrentUser().getId());
		image.setCropped(true);
		image.setDrawingCacheEnabled(true);

		Bitmap bitmap = image.getDrawingCache();
		
		// Bitmap 파일을 String 으로 변환
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, stream);
		byte[] imageByteArray = stream.toByteArray();
		
		String encodedString = Base64.encodeToString(imageByteArray, Base64.NO_WRAP);
		Log.i(TAG, encodedString);

		
//		Log.i(TAG, "이미지 변환 완료");
//		if( ANEExtension.aneContext == null )
//		{
//			Log.i(TAG, "aneContext is null");
//		}
//		else
//		{
//			// Air Application 으로 변환한 String 값을 보냄
//			ANEExtension.aneContext.dispatchStatusEventAsync("userImage", encodedString);
//		}
		
		finish();
	}
    
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		_uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		_uiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		_uiHelper.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_uiHelper.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		_uiHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		_uiHelper.onResume();
	}
}
