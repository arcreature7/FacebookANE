package com.stintern.anipang.ane.utils;

import java.util.List;

import android.app.Application;

import com.facebook.model.GraphUser;

public class ANEApplication extends Application {

	private boolean _loggedIn = false;
	
	private GraphUser _currentUser;
	private List<GraphUser> _friendList;

	public boolean isLoggedIn() {
		return _loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		_loggedIn = loggedIn;
	}
	
	public GraphUser getCurrentUser() {
		return _currentUser;
	}

	public void setCurrentUser(GraphUser currentUser) {
		_currentUser = currentUser;
	}
	
	public List<GraphUser> getFriends() {
		return _friendList;
	}
	public void setFriends(List<GraphUser> friends) {
		_friendList = friends;
	}
	
}
