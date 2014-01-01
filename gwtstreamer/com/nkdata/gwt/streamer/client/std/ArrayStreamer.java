package com.nkdata.gwt.streamer.client.std;

import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;
import com.nkdata.gwt.streamer.client.Streamer;

/**
 * Used to serialize multidimensional and typed arrays 
 */
public abstract class ArrayStreamer extends Streamer 
{
	@Override
	public void writeObject( Object obj, WriteContext ctx, Writer out )
	{
		ctx.addRef( obj );
		writeObjectData( (Object[]) obj, ctx, out );
	}
	
	
	@Override
	public Object readObject( ReadContext ctx, Reader in )
	{
		int length = in.readInt();
		Object[] obj = createObjectArrayInstance(length);
		ctx.addRef( obj );
		readObjectData( obj, ctx, in );
		return obj;
	}
	
	
	protected void writeObjectData( Object[] obj, WriteContext ctx, Writer out )
	{
		out.writeInt( obj.length );
		
		for ( Object o : obj )
			Streamer.get().writeObject( o, ctx, out );
	}

	
	protected void readObjectData( Object[] obj, ReadContext ctx, Reader in )
	{
		for ( int i = 0; i < obj.length; i++ ) {
			obj[i] = Streamer.get().readObject( ctx, in );
		}
	}
	
	
	/** Create new object array instance */
	protected abstract Object[] createObjectArrayInstance( int length );
}
