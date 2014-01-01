package com.nkdata.gwt.streamer.client.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.nkdata.gwt.streamer.client.Streamable;
import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamerException;

/** 
 * This is a client-side implementation of the class.
 */
public class StreamerInternal
{
	public static Streamer createRootStreamer() {
		return GWT.create( Streamer.class );
	}
	
	/**
	 * @return always null. In client mode all streamers must be generated and registered statically 
	 */
	public static Streamer createStreamerFor( String className )
	{
		return null;
	}


	public static String urlEncode( String s ) {
		return URL.encodeQueryString( s );
	}

	
	public static String urlDecode( String s ) {
		return URL.decodeQueryString( s );
	}
}
