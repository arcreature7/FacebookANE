package com.stintern.anipang.ane;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class ANEExtension implements FREExtension {

	public static ANEContext aneContext = null;
	
	@Override
	public FREContext createContext(String arg0) {
		aneContext = new ANEContext(); 
		return aneContext;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

}
