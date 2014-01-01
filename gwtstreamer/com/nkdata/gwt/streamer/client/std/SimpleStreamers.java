package com.nkdata.gwt.streamer.client.std;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public class SimpleStreamers {
	public static class IntegerStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeInt( (Integer) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readInt();
		}
	}

	
	public static class ShortStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeShort( (Short) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readShort();
		}
	}

	
	public static class ByteStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeByte( (Byte) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readByte();
		}
	}

	
	public static class LongStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeLong( (Long) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readLong();
		}
	}

	
	public static class DoubleStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeDouble( (Double) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readDouble();
		}
	}

	
	public static class FloatStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeFloat( (Float) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readFloat();
		}
	}

	
	public static class CharStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeChar( (Character) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readChar();
		}
	}

	
	public static class BooleanStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out ) {
			out.writeBoolean( (Boolean) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			return in.readBoolean();
		}
	}

	
	public static class StringStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out )
		{
			ctx.addRef( obj );
			out.writeString( (String) obj );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			String s = in.readString();
			ctx.addRef( s );
			return s;
		}
	}

	
	public static class DateStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out )
		{
			out.writeLong( ((Date) obj).getTime() );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			long time = in.readLong();
			return new Date( time );
		}
	}
	
	
	public static class BigIntegerStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out )
		{
			out.writeString( ((BigInteger) obj).toString() );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			String s = in.readString();
			return new BigInteger( s );
		}
	}
	
	
	public static class BigDecimalStreamer extends Streamer {
		@Override
		public void writeObject( Object obj, WriteContext ctx, Writer out )
		{
			out.writeString( ((BigDecimal) obj).toString() );
		}
		
		@Override
		public Object readObject( ReadContext ctx, Reader in )
		{
			String s = in.readString();
			return new BigDecimal( s );
		}
	}
}
