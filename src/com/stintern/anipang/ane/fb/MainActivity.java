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
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.stintern.anipang.ane.ANEExtension;
import com.stintern.anipang.ane.utils.ANEApplication;
import com.stintern.anipang.ane.utils.InfoFetcher;
import com.stintern.anipang.ane.utils.Resources;

public class MainActivity extends Activity {
    
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions, user_friends");
	
    private InfoFetcher 	_infoFetcher;
    private ANEApplication 	_aneApplication;
    
    private int _callType;

    // 초대 관련
    private WebDialog dialog = null;
    private String dialogAction = null;
    private Bundle dialogParams = null;
    
    //Facebook API
	private UiLifecycleHelper _uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        setContentView( getResourceId("activity_main", "layout") );
        
        _callType = getIntent().getIntExtra(Resources.INTENT_TYPE, 0);

        //Session 을 고나리할 UILifecycleHelper 객체 생성
        _uiHelper = new UiLifecycleHelper(this, statusCallback);
        _uiHelper.onCreate(savedInstanceState);

        _aneApplication = (ANEApplication)getApplication();
        
        // 사용자의 정보를 가져오는 유틸리티 객체 생성
        _infoFetcher = new InfoFetcher();
        
        // Facebook 연동을 위한 준비과정
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
	    
        Log.i(TAG, "Parse Start");
	    Parse.initialize( getApplicationContext(), getString(getResourceId("app_id", "string")), getString(getResourceId("app_secret", "string")) );
	    ParseFacebookUtils.initialize(getString( getResourceId("app_id", "string")) );
        
	    // 세션을 확인
	    Log.i(TAG, "check session in onCreate");
        Session session = Session.getActiveSession();        
        if (session == null) {
        	Log.i(TAG, "session == null");
            if (savedInstanceState != null) {
            	Log.i(TAG, "savedInstanceState != null");
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            
            if (session == null) {
            	Log.i(TAG, "new session");
                session = new Session(this);
            }
            
            // 세션 설정을 설정하고 Facebook Login 창을 띄움
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {

            	Log.i(TAG, "openForRead");
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
            else
            {
            	Log.i(TAG, "login");
            	login();
            }
        }
        else
        {
        	Log.i(TAG, "session == null -> else");
//            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
//            session.requestNewPublishPermissions(newPermissionsRequest);
        	switch(_callType)
        	{
        	case Resources.GET_USER_INFO:
            	Log.i(TAG, "GET_USER_INFO");
            	// 사용자의 정보를 가져옴
                loadUserInformation();
                break;
                
        	case Resources.INVITE_FRIENDS:
            	inviteFriends();
            	break;

        	case Resources.SHARE_APP:
            	shareApp();
            	break;
        	}
        	
        }
	}

	/**
	 * 현재 세션을 확인하고 Open 되어 있으면 사용자의 정보를 가져옵니다.
	 */
	private void loadUserInformation() {
    	Log.i(TAG, "loadUserInformation");
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
    	Log.i(TAG, "login");
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
        	Log.i(TAG, "!session.isOpened() && !session.isClosed()");
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
        	Log.i(TAG, "else in login()");
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void logout() {
    	Log.i(TAG, "logout");
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }
    
    private void inviteFriends(){
    	Bundle params = new Bundle();
        params.putString("message", "Anipang!!! " + /*application.getScore() +*/ " 같이 하자!!");
        showDialogWithoutNotificationBar("apprequests", params);
    }
    
    private void showDialogWithoutNotificationBar(String action, Bundle params){
    	dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).
    	        setOnCompleteListener(new WebDialog.OnCompleteListener() {
    	        @Override
    	        public void onComplete(Bundle values, FacebookException error) {
    	            if (error != null && !(error instanceof FacebookOperationCanceledException)) {
    	            	Log.e(TAG, getString( getResourceId("network_error", "string") ));
    	            }
    	            dialog = null;
    	            dialogAction = null;
    	            dialogParams = null;
    	        }
    	    }).build();

    	    Window dialog_window = dialog.getWindow();
    	    dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
    	        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    	    dialogAction = action;
    	    dialogParams = params;

    	    dialog.show();
    }
    
    private void shareApp(){

    	FacebookDialog.ShareDialogBuilder builder = new FacebookDialog.ShareDialogBuilder(this)
        	.setLink(Resources.SHARE_GAME_LINK)
        	.setName(Resources.SHARE_GAME_NAME);
    	
		if (builder.canPresent()) {
		    builder.build().present();
		}
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	Log.i(TAG, "SessionStatusCallback");
        	if( _callType == Resources.GET_USER_INFO )
        	{
                if (session.isOpened()) {
                	
                	Log.i(TAG, "loadUserInformation_isOpened");
                	
                	// 로그인이 되어있을 경우 정보를 가져옴
                	if( !_aneApplication.isLoggedIn() )
                	{
                    	// 사용자의 정보를 가져옴
                		_infoFetcher.fetchUserInformation(MainActivity.this);
                	}
                }
        	}
            	
        }
    }
    
	public void sendImageToAir(Bitmap bmp)
	{
    	Log.i(TAG, "sendImageToAir");
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
    	Log.i(TAG, "onStart");
		super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	protected void onStop() {
    	Log.i(TAG, "onStop");
		super.onStop();
		
		_uiHelper.onStop();
	}

	@Override
	protected void onDestroy() {
    	Log.i(TAG, "onDestroy");
		super.onDestroy();
		_uiHelper.onDestroy();
	}

	@Override
	protected void onPause() {
    	Log.i(TAG, "onPause");
		super.onPause();
		_uiHelper.onPause();
	}

	@Override
	protected void onResume() {
    	Log.i(TAG, "onResume");
		super.onResume();
		_uiHelper.onResume();
	}
}
