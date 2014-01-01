package com.dcpabe.web.client;

import com.google.gwt.core.client.JavaScriptObject;

public class IntJS extends JavaScriptObject {
	protected IntJS() {
	}

	public static final native IntJS create(int num) /*-{
		return { "n" : num};
  	}-*/;
	
	public final native int getNum() /*-{
		return this.n;
  	}-*/;
}

