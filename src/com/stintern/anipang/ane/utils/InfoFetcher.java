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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.stintern.anipang.ane.R;
import com.stintern.anipang.ane.fb.MainActivity;

public class InfoFetcher {
	
	private static final String TAG = InfoFetcher.class.getSimpleName();
	
	public void fetchUserInformation(final MainActivity mainActivity){

		Log.i(TAG, "fetchUserInformation Start");
		final Session session = Session.getActiveSession();	
		if (session != null && session.isOpened()) {
			// Get the user's list of friends
			Request friendsRequest = Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {
	
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					FacebookRequestError error = response.getError();
					if (error != null) {
						Log.e(TAG, error.toString());
						handleError(mainActivity, error, true);
					} else if (session == Session.getActiveSession()) {
						// Set the friends attribute
						((ANEApplication)mainActivity.getApplication()).setFriends(users);
					}
				}
			});
			Bundle params = new Bundle();
			params.putString("fields", "name,first_name,last_name");
			friendsRequest.setParameters(params);
			
//			Request req = new Request(session, "/me/photos", null, HttpMethod.GET, new Request.Callback() {
//				
//				@Override
//				public void onCompleted(Response response) {
//					GraphObject obj = response.getGraphObject();
//					obj.getInnerJSONObject();
//				}
//			});
			
			HttpGet httpRequest = new HttpGet(URI.create("https://graph.facebook.com/krbod/picture") );
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
			try {
				response = (HttpResponse) httpclient.execute(httpRequest);
	            HttpEntity entity = response.getEntity();
	            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
	            Bitmap bmap = BitmapFactory.decodeStream(bufHttpEntity.getContent());
	            httpRequest.abort();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			new Request(session, "https://graph.facebook.com/krbod/picture", null, HttpMethod.GET, new Request.Callback() {
//				
//				@Override
//				public void onCompleted(Response response) {
//					GraphObject obj = response.getGraphObject();
//					obj.getInnerJSONObject();
//					
//				}
//			}).executeAsync();
			
			// Get current logged in user information
			Request meRequest = Request.newMeRequest(session, new Request.GraphUserCallback() {
				
				@Override
				public void onCompleted(GraphUser user, Response response) {
					FacebookRequestError error = response.getError();
					if (error != null) {
						Log.e(TAG, error.toString());
						handleError(mainActivity, error, true);
					} else if (session == Session.getActiveSession()) {
						// Set the currentFBUser attribute
						((ANEApplication)mainActivity.getApplication()).setCurrentUser(user);
						
						// Now save the user into Parse.
	                    saveUserToParse(user, session);
					}
				}
			});
			
			// Create a RequestBatch and add a callback once the batch of requests completes
			RequestBatch requestBatch = new RequestBatch(friendsRequest, meRequest);
			requestBatch.addCallback(new RequestBatch.Callback() {
	
				@Override
				public void onBatchCompleted(RequestBatch batch) {
					if ( ((ANEApplication)mainActivity.getApplication()).getCurrentUser() != null &&
						 ((ANEApplication)mainActivity.getApplication()).getFriends() != null ) {

						mainActivity.setImage();
			            
			            String name = ((ANEApplication)mainActivity.getApplication()).getCurrentUser().getFirstName();
			            
			            Log.i(TAG, name);

					} else {
						//fbController.showError(fbController.getString(R.string.error_fetching_profile), true);
					}
				}
			});
			
			// Execute the batch of requests asynchronously
			requestBatch.executeAsync();
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
        DialogInterface.OnClickListener listener = null;
        String dialogBody = null;

        if (error == null) {
            dialogBody = mainActivity.getString(R.string.error_dialog_default_text);
        }
        else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                	Log.e(TAG,"AUTHENTICATION_RETRY");
//                    // tell the user what happened by getting the message id, and
//                    // retry the operation later
//                    String userAction = (error.shouldNotifyUser()) ? "" :
//                            getString(error.getUserActionMessageId());
//                    dialogBody = getString(R.string.error_authentication_retry, userAction);
//                    listener = new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
//                            startActivity(intent);
//                        }
//                    };
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                	Log.e(TAG,"AUTHENTICATION_REOPEN_SESSION");
//                    // close the session and reopen it.
//                    dialogBody = getString(R.string.error_authentication_reopen);
//                    listener = new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Session session = Session.getActiveSession();
//                            if (session != null && !session.isClosed()) {
//                                session.closeAndClearTokenInformation();
//                            }
//                        }
//                    };
                    break;

                case PERMISSION:
                	Log.e(TAG,"PERMISSION");
//                    // request the publish permission
//                    dialogBody = getString(R.string.error_permission);
//                    listener = new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                        	if (fragments[HOME] != null) {
//                        		((HomeFragment) fragments[HOME]).setPendingPost(true);
//                        		((HomeFragment) fragments[HOME]).requestPublishPermissions(Session.getActiveSession());
//                        	}
//                        }
//                    };
                    break;

                case SERVER:
                case THROTTLING:
                	Log.e(TAG,"SERVER, THROTTLING");
//                    // this is usually temporary, don't clear the fields, and
//                    // ask the user to try again
//                    dialogBody = getString(R.string.error_server);
                    break;
//
                case BAD_REQUEST:
                	Log.e(TAG,"BAD_REQUEST");
//                    // this is likely a coding error, ask the user to file a bug
//                    dialogBody = getString(R.string.error_bad_request, error.getErrorMessage());
                    break;

                case CLIENT:
                	Log.e(TAG,"CLIENT");
//                	// this is likely an IO error, so tell the user they have a network issue
                	dialogBody = mainActivity.getString(R.string.network_error);
                    break;
                    
                case OTHER:
                default:
                	Log.e(TAG,"OTHER");
//                    // an unknown issue occurred, this could be a code error, or
//                    // a server side issue, log the issue, and either ask the
//                    // user to retry, or file a bug
//                    dialogBody = getString(R.string.error_unknown, error.getErrorMessage());
                    break;
            }
        }
//
        new AlertDialog.Builder(mainActivity)
                .setPositiveButton(R.string.error_dialog_button_text, listener)
                .setTitle(R.string.error_dialog_title)
                .setMessage(dialogBody)
                .show();
//        
//        if (logout) {
//        	logout();
        
    }
    		
    		
    		
}
