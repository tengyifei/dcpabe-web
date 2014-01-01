package com.dcpabe.web.client;

import com.google.gwt.core.client.JavaScriptObject;

public class DoubleJS extends JavaScriptObject {
	protected DoubleJS() {
	}

	public static final native DoubleJS create(double num) /*-{
		return { "n" : num};
  	}-*/;
	
	public final native double getNum() /*-{
		return this.n;
  	}-*/;
}

