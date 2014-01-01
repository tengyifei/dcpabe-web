package com.nkdata.gwt.streamer.client;

@SuppressWarnings("serial")
public class StreamerException extends RuntimeException
{
	public StreamerException() {
	}

	public StreamerException( Exception ex ) {
		super( ex );
	}

	public StreamerException( String message ) {
		super( message );
	}
	
	public StreamerException( String message, Exception ex ) {
		super( message, ex );
	}
}
