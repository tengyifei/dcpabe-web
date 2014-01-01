package com.dcpabe.web.client;

import com.nkdata.gwt.streamer.client.Streamable;


public class SecretKey implements Streamable {
	private static final long serialVersionUID = 1L;
	private byte[] ai;
	private byte[] yi;
	
	public SecretKey(){}
	
	public SecretKey(byte[] ai, byte[] yi) {
		this.ai = ai;
		this.yi = yi;
	}

	public byte[] getAi() {
		return ai;
	}

	public byte[] getYi() {
		return yi;
	}
}
