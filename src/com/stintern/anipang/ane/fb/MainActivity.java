package com.stintern.anipang.ane.fb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;

public class MainActivity extends Activity {

    private Button buttonLoginLogout;
    
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
        
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
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
            buttonLoginLogout.setText(getResources().getIdentifier("btn_logout", "string", this.getPackageName()));
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
        } else {
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
