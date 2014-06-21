package com.stintern.anipang.ane.functions;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.stintern.anipang.ane.ANEExtension;

public class InviteFriends implements FREFunction {

	@Override
	public FREObject call(FREContext context, FREObject[] arg1) {

		ANEExtension.aneContext.inviteFriends();
		
		return null;
	}

}
