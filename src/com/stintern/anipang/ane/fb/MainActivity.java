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

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.stintern.anipang.ane.ANEExtension;
import com.stintern.anipang.ane.utils.ANEApplication;
import com.stintern.anipang.ane.utils.InfoFetcher;

public class MainActivity extends Activity {
    
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions, user_friends");
	
    private InfoFetcher 	_infoFetcher;
    private ANEApplication 	_aneApplication; 
    
    //Facebook API
	private UiLifecycleHelper _uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView( getResourceId("activity_main", "layout") );

        //Session 을 고나리할 UILifecycleHelper 객체 생성
        _uiHelper = new UiLifecycleHelper(this, statusCallback);
        _uiHelper.onCreate(savedInstanceState);

        _aneApplication = (ANEApplication)getApplication();
        
        // 사용자의 정보를 가져오는 유틸리티 객체 생성
        _infoFetcher = new InfoFetcher();
        
        // Facebook 연동을 위한 준비과정
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
	    
	    Parse.initialize( getApplicationContext(), getString(getResourceId("app_id", "string")), getString(getResourceId("app_secret", "string")) );
	    ParseFacebookUtils.initialize(getString( getResourceId("app_id", "string")) );
        
	    // 세션을 확인
        Session session = Session.getActiveSession();        
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            
            if (session == null) {
                session = new Session(this);
            }
            
            // 세션 설정을 설정하고 Facebook Login 창을 띄움
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
            else
            {
            	login();
            }
        }
        else
        {
//            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
//            session.requestNewPublishPermissions(newPermissionsRequest);
            
        	// 사용자의 정보를 가져옴
            loadUserInformation();
        }
	}

	/**
	 * 현재 세션을 확인하고 Open 되어 있으면 사용자의 정보를 가져옵니다.
	 */
	private void loadUserInformation() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
        	
        	Log.i(TAG, "loadUserInformation_isOpened");
        	
        	// 로그인이 되어있을 경우 정보를 가져옴
        	if( !_aneApplication.isLoggedIn() )
        	{

            	Log.i(TAG, "before fetchUserInformation");
            	
        		_infoFetcher.fetchUserInformation(this);
        	}
        } 
        else
        {
        	Log.i(TAG, "loadUserInformation_else");
        	login();
        }
    }

    private void login() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void logout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            loadUserInformation();
        }
    }
    
	public void sendImageToAir(Bitmap bmp)
	{
		// Bitmap 파일을 String 으로 변환
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, stream);
		byte[] imageByteArray = stream.toByteArray();
		
		String encodedString = Base64.encodeToString(imageByteArray, Base64.NO_WRAP);
		Log.i(TAG, encodedString);
		
		if( ANEExtension.aneContext == null )
		{
			Log.i(TAG, "aneContext is null");
		}
		else
		{
			// Air Application 으로 변환한 String 값을 보냄
			ANEExtension.aneContext.dispatchStatusEventAsync("userImage", encodedString);
		}
	}
	
	private int getResourceId(String name, String type){
		return getResources().getIdentifier(name, type, this.getPackageName());
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
