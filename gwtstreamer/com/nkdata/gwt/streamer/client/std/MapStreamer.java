package com.nkdata.gwt.streamer.client.std;

import java.util.Map;

import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public abstract class MapStreamer extends Streamer {
	@Override
	public void writeObject( Object obj, WriteContext ctx, Writer out )
	{
		ctx.addRef( obj );
		writeObjectData( (Map<?,?>) obj, ctx, out );
	}
	
	
	@Override
	public Object readObject( ReadContext ctx, Reader in )
	{
		int length = in.readInt();
		Map<Object,Object> obj = createMapInstance(length);
		ctx.addRef( obj );
		readObjectData( obj, ctx, in, length );
		return obj;
	}
	
	
	protected void writeObjectData( Map<?,?> obj, WriteContext ctx, Writer out )
	{
		out.writeInt( obj.size() );
		
		for ( Map.Entry<?, ?> o : obj.entrySet() ) {
			Streamer.get().writeObject( o.getKey(), ctx, out );
			Streamer.get().writeObject( o.getValue(), ctx, out );
		}
	}

	
	protected void readObjectData( Map<Object,Object> obj, ReadContext ctx, Reader in, int length )
	{
		for ( int i = 0; i < length; i++ ) {
			obj.put( get().readObject( ctx, in ), get().readObject(ctx, in) );
		}
	}
	
	protected abstract Map<Object,Object> createMapInstance( int length );
}
