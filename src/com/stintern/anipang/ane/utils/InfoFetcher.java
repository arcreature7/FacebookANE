package com.stintern.anipang.ane.utils;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.stintern.anipang.ane.ANEExtension;
import com.stintern.anipang.ane.R;
import com.stintern.anipang.ane.fb.MainActivity;

public class InfoFetcher {

	private static final String TAG = InfoFetcher.class.getSimpleName();

	private MainActivity _mainActivity;
    private ImageLoadAsyncTask _imageLoadAsyncTask;
	
	public void fetchUserInformation(MainActivity mainActivity){

		Log.i(TAG, "fetchUserInformation Start");
		
		_mainActivity = mainActivity;
		((ANEApplication)_mainActivity.getApplication()).setLoggedIn(true);
		
		final Session session = Session.getActiveSession();	
		if (session != null && session.isOpened()) {

			
			Request friendsRequest = Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {
	
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					FacebookRequestError error = response.getError();
					if (error != null) {
						Log.e(TAG, error.toString());
						handleError(_mainActivity, error, true);
					} else if (session == Session.getActiveSession()) {

						((ANEApplication)_mainActivity.getApplication()).setFriends(users);
					}
				}
			});
			
			Bundle params = new Bundle();
			params.putString("fields", "name,first_name,last_name");
			friendsRequest.setParameters(params);
			
			// 사용자의 정보를 받아옴
			Request meRequest = Request.newMeRequest(session, new Request.GraphUserCallback() {
				
				@Override
				public void onCompleted(GraphUser user, Response response) {
					FacebookRequestError error = response.getError();
					if (error != null) {
						Log.e(TAG, error.toString());
						handleError(_mainActivity, error, true);
					} else if (session == Session.getActiveSession()) {
						// 사용자의 정보를 저장
						((ANEApplication)_mainActivity.getApplication()).setCurrentUser(user);
				        
	                    //saveUserToParse(user, session);
					}
				}
			});
			RequestBatch requestBatch = new RequestBatch(friendsRequest, meRequest);
			requestBatch.addCallback(new RequestBatch.Callback() {
	
				@Override
				public void onBatchCompleted(RequestBatch batch) {
					if ( ((ANEApplication)_mainActivity.getApplication()).getCurrentUser() != null &&
						 ((ANEApplication)_mainActivity.getApplication()).getFriends() != null ) {
						
						// 사용자의 이름을 Air 로 전송
						String name = ((ANEApplication)_mainActivity.getApplication()).getCurrentUser().getName();
						if(name != null)
						{
							ANEExtension.aneContext.dispatchStatusEventAsync("userName", name);
						}

						// 사용자의 사진을 Air 로 보냄
			            String id = ((ANEApplication)_mainActivity.getApplication()).getCurrentUser().getId();
				        _imageLoadAsyncTask = new ImageLoadAsyncTask();
				        _imageLoadAsyncTask.execute(id);
			            			            
					} else {
						//fbController.showError(fbController.getString(R.string.error_fetching_profile), true);
					}
				}
			});
			
			// 모든 Request 를 비동기 실행
			requestBatch.executeAsync();
		}
    }
	
	public class ImageLoadAsyncTask extends AsyncTask<String, Void, Bitmap> {
		 
        protected void onPreExecute() {
            super.onPreExecute();
        }
         
        protected Bitmap doInBackground(String... params) {
        	
        	Bitmap bmp = null;
	        
        	int iconWidth = 256;
			HttpGet httpRequest = new HttpGet(URI.create("https://graph.facebook.com/" + params[0] + "/picture?width=" + iconWidth + "&height=" + iconWidth) );
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
			try {
				response = (HttpResponse) httpclient.execute(httpRequest);
	            HttpEntity entity = response.getEntity();
	            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
	            bmp = BitmapFactory.decodeStream(bufHttpEntity.getContent());
	            httpRequest.abort();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
			return bmp;
        }
         
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
             
            if(result != null){
            	_mainActivity.sendImageToAir(result);
            }
             
        }
         
        protected void onCancelled() {
            super.onCancelled();
        }
         
    }
	
	private void saveUserToParse(GraphUser fbUser, Session session) {
		
		ParseFacebookUtils.logIn(fbUser.getId(), session.getAccessToken(), 
				session.getExpirationDate(), new LogInCallback() {
			
			@Override
			public void done(ParseUser parseUser, ParseException err) {                   
				if (parseUser != null) {
					// The user has been saved to Parse.
					if (parseUser.isNew()) {
						// This user was created during this session with Facebook Login.                       
						Log.d(TAG, "ParseUser created.");

						// Call saveInventory() which will save data to Parse if connected. 
						//ANEApplication app = ((ANEApplication)getApplication());
						//app.saveInventory();
					} else {
						Log.d(TAG, "User exists in Parse. Pull their values: " + parseUser);

						// This user existed before. Call loadInventory() which has logic
						// to check Parse if connected.
						//ANEApplication app = ((ANEApplication)getApplication());
						//app.loadInventory();
					}

					//loadInventoryFragment();
				} else {
					// The user wasn't saved. Check the exception.
					Log.d(TAG, "User was not saved to Parse: " + err.getMessage());
				}
			}
		});
	}

	
	
	private void handleError(MainActivity mainActivity, FacebookRequestError error, boolean logout) {
        if (error == null) {
            Log.e(TAG, mainActivity.getString(R.string.error_dialog_default_text));
        }
        else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                	Log.e(TAG,"AUTHENTICATION_RETRY");
                	
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                	Log.e(TAG,"AUTHENTICATION_REOPEN_SESSION");
                	
                    break;

                case PERMISSION:
                	Log.e(TAG,"PERMISSION");

                    break;

                case SERVER:
                case THROTTLING:
                	Log.e(TAG,"SERVER, THROTTLING");

                    break;

                case BAD_REQUEST:
                	Log.e(TAG,"BAD_REQUEST");

                    break;

                case CLIENT:
                	Log.e(TAG,"CLIENT");

                    break;
                    
                case OTHER:
                default:
                	Log.e(TAG,"OTHER");
                	
                    break;
            }
        }
    }
	
}
