package com.stintern.anipang.ane;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.stintern.anipang.ane.fb.MainActivity;
import com.stintern.anipang.ane.functions.GetUserInfo;

public class ANEContext extends FREContext {

	@Override
	public void dispose() {
		ANEExtension.aneContext = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions() {
		
		Map<String, FREFunction> map = new HashMap<String, FREFunction>();
        
        map.put("getUserInfo", new GetUserInfo());
        
		return map;
	}
	
	public void getUserInfo()
	{
		Intent intent = new Intent(getActivity(), MainActivity.class);
		getActivity().startActivity(intent);
	}

}
