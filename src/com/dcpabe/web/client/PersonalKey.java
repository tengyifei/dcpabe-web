package com.dcpabe.web.client;

import com.nkdata.gwt.streamer.client.Streamable;


public class PersonalKey implements Streamable {
	private static final long serialVersionUID = 1L;
	private String attribute;
	private byte[] key;
	private byte[] preprocess;
	
	private PersonalKey(){}
	
	public PersonalKey(String attribute, byte[] key) {
		this.attribute = attribute;
		this.key = key;
	}

	public String getAttribute() {
		return attribute;
	}

	public byte[] getKey() {
		return key;
	}
	
	public byte[] getKeyPreprocessed(){
		return preprocess;
	}
	public void setKeyPreprocessed(byte[] preprocess){
		this.preprocess=preprocess;
	}
}
