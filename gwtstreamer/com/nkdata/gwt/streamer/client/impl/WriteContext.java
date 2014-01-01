package com.nkdata.gwt.streamer.client.impl;

import java.util.IdentityHashMap;
import java.util.Map;

public class WriteContext {
	private Map<Object,Integer> refs = new IdentityHashMap<Object,Integer>( 50 );
	
	public Integer getRefIdx( Object obj ) {
		return refs.get( obj );
	}
	
	public void addRef( Object obj ) {
		Integer old = refs.put( obj, refs.size() );
		
		if ( old != null ) {
			refs.put( obj, old );
			throw new IllegalStateException( "Reference to object already exists: "+obj );
		}
	}
}
