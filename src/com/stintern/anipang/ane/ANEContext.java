package com.stintern.anipang.ane;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.stintern.anipang.ane.fb.MainActivity;
import com.stintern.anipang.ane.functions.GetUserInfo;
import com.stintern.anipang.ane.functions.InviteFriends;
import com.stintern.anipang.ane.utils.Resources;

public class ANEContext extends FREContext {

	@Override
	public void dispose() {
		ANEExtension.aneContext = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions() {
		
		Map<String, FREFunction> map = new HashMap<String, FREFunction>();
        
        map.put("getUserInfo", new GetUserInfo());
        map.put("inviteFriends", new InviteFriends());
        
		return map;
	}
	
	public void getUserInfo()
	{
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.putExtra(Resources.INTENT_TYPE, Resources.GET_USER_INFO);
		getActivity().startActivity(intent);
	}

	public void inviteFriends()
	{
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.putExtra(Resources.INTENT_TYPE, Resources.INVITE_FRIENDS);
		getActivity().startActivity(intent);
	}
}
