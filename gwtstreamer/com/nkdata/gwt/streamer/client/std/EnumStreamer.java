package com.nkdata.gwt.streamer.client.std;

import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;


public abstract class EnumStreamer extends Streamer {
	@Override
	public void writeObject(Object obj, WriteContext ctx, Writer out)
	{
		Enum<?> e = (Enum<?>) obj;
		out.writeInt( e.ordinal() );
	}
	
	@Override
	public Object readObject(ReadContext ctx, Reader in) {
		int val = in.readInt();
		return getEnumValueOf( val );
	}
	
	
	protected abstract Enum<?> getEnumValueOf( int value );
}
