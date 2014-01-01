package com.dcpabe.web.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.nkdata.gwt.streamer.client.Streamable;

public class PersonalKeys implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8779381679620478328L;
	private String userID;
	private Map<String, PersonalKey> personalKeys;
	private byte[] preprocess;
	
	public PersonalKeys(String userID) {
		this.userID = userID;
		personalKeys = new HashMap<String, PersonalKey>();
	}
	
	public PersonalKeys(){}
	
	public void addKey(PersonalKey pkey) {
		personalKeys.put(pkey.getAttribute(), pkey);
	}

	public String getUserID() {
		return userID;
	}
	
	public byte[] getUserIDPreprocessed(){
		return preprocess;
	}
	public void setUserIDPreprocessed(byte[] preprocess){
		this.preprocess=preprocess;
	}
	
	public Collection<String> getAttributes() {
		return personalKeys.keySet();
	}
	
	public PersonalKey getKey(String attribute) {
		return personalKeys.get(attribute);
	}
}
