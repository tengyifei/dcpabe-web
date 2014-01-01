package com.dcpabe.web.client;

import org.bouncycastle.crypto.digests.SHA1Digest;

import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters_Streamer;

import com.nkdata.gwt.streamer.client.Streamer;

public class Streaming {
	
	public static void registerStreamer(){
		Streamer.registerStreamer( GlobalParameters.class, new GlobalParameters_Streamer() );
		Streamer.registerStreamer( DefaultCurveParameters.class, new DefaultCurveParameters_Streamer() );
		Streamer.registerStreamer( SHA1Digest.class, new SHA1Digest_Streamer() );
	}

}
