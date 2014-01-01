package com.nkdata.gwt.streamer.client.std;

import java.util.ArrayList;
import java.util.List;

import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;
import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamerException;

/**
 * Structure streamer. Generator creates extended version of this for every class.
 * @author akuranov
 */
public abstract class StructStreamer extends Streamer
{
	@Override
	public void writeObject( Object obj, WriteContext ctx, Writer out )
	{
		ctx.addRef( obj );
		writeObjectData( obj, ctx, out );
	}
	
	
	@Override
	public Object readObject( ReadContext ctx, Reader in )
	{
		Object obj = createObjectInstance();
		ctx.addRef( obj );
		readObjectData( obj, ctx, in );
		return obj;
	}
	
	
	protected void writeObjectData( Object obj, WriteContext ctx, Writer out )
	{
		StructStreamer streamer = getSuperclassStreamer();
		
		if ( streamer != null )
			// write inherited fields
			streamer.writeObjectData( obj, ctx, out );
		
		final List<Object> values = getValues( obj );
		
		for ( Object o : values )
			Streamer.get().writeObject( o, ctx, out );
	}

	
	protected void readObjectData( Object obj, ReadContext ctx, Reader in )
	{
		StructStreamer streamer = getSuperclassStreamer();
		
		if ( streamer != null )
			streamer.readObjectData( obj, ctx, in );
		
		final int fieldNum = getFieldNum();
		final List<Object> values = new ArrayList<Object>(fieldNum);
		
		for ( int i = 0; i < fieldNum; i++ ) {
			values.add( Streamer.get().readObject( ctx, in ) );
		}
		
		setValues( obj, values );
	}
	
	
	/** 
	 * Get a superclass streamer. If null class is a top of hierarchy.
	 * Note: if a superclass does not implements Streamable interface 
	 * function will return null and current streamer must serialize all
	 * fields including inherited.
	 */
	protected StructStreamer getSuperclassStreamer()
	{
		Class<?> superClass = getTargetClass().getSuperclass();
		
		if ( superClass != null ) {
			Streamer st = Streamer.get( superClass );
			
			if ( st != null && !(st instanceof StructStreamer) )
				throw new StreamerException( "Streamer for class is not an instance of StructStreamer: "+superClass.getName() );
			
			return (StructStreamer) st;
		}
		
		return null;
	}
	
	
	/** Get target class this streamer operates with. */
	protected abstract Class<?> getTargetClass();
	
	/** Get number of fields. */
	protected abstract int getFieldNum();
	
	/** Create new obect instance */
	protected abstract Object createObjectInstance();
	
	/** Get field values. Fields must be ordered in alphanumeric order */
	protected abstract List<Object> getValues( Object obj );

	/** Set field values. Fields must be ordered in alphanumeric order */
	protected abstract void setValues( Object obj, List<Object> values );
}
