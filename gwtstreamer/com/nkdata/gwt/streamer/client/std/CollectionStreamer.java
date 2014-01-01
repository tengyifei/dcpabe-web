package com.nkdata.gwt.streamer.client.std;

import java.util.Collection;

import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public abstract class CollectionStreamer extends Streamer
{
	@Override
	public void writeObject( Object obj, WriteContext ctx, Writer out )
	{
		ctx.addRef( obj );
		writeObjectData( (Collection<?>) obj, ctx, out );
	}
	
	
	@Override
	public Object readObject( ReadContext ctx, Reader in )
	{
		int length = in.readInt();
		Collection<Object> obj = createCollectionInstance(length);
		ctx.addRef( obj );
		readObjectData( obj, ctx, in, length );
		return obj;
	}
	
	
	protected void writeObjectData( Collection<?> obj, WriteContext ctx, Writer out )
	{
		out.writeInt( obj.size() );
		
		for ( Object o : obj )
			Streamer.get().writeObject( o, ctx, out );
	}

	
	protected void readObjectData( Collection<Object> obj, ReadContext ctx, Reader in, int length )
	{
		for ( int i = 0; i < length; i++ ) {
			obj.add( Streamer.get().readObject( ctx, in ) );
		}
	}
	

	protected abstract Collection<Object> createCollectionInstance( int length );
}
