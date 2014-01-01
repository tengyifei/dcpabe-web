package com.dcpabe.web.client;

import it.unisa.dia.gas.jpbc.Element;

import com.nkdata.gwt.streamer.client.Streamable;

public class Encryption_passData implements Streamable{
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private Encryption_passData(){}
	
	public Encryption_passData(
			Element element,
			Element element2,
			Element element3,
			int index2,
			String pk_id2
			) {
		
		wx = element;
		lambdax = element2;
		c1x2 = element3;
		index = index2;
		pk_id = pk_id2;
	}
	
	Element wx;
	Element lambdax;
	Element c1x2;
	int index;
	public String pk_id;
}
