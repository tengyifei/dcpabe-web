package com.nkdata.gwt.streamer.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import com.nkdata.gwt.streamer.client.StreamFactory;
import com.nkdata.gwt.streamer.client.StreamerException;


public class Base91StreamFactory implements StreamFactory 
{
	public static class Base91Writer implements com.nkdata.gwt.streamer.client.StreamFactory.Writer
	{
		private ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		
		public String toString() {
			byte[] buf = out.toByteArray_Fast_Read();
			return Base91Util.encode( buf );
		}
		
		public void writeInt( int v ) {
	        out.write((v >>> 24) & 0xFF);
	        out.write((v >>> 16) & 0xFF);
	        out.write((v >>>  8) & 0xFF);
	        out.write((v >>>  0) & 0xFF);
		}

		public void writeLong( long v ) {
	        out.write(((int)(v >>> 56) & 0xFF));
	        out.write(((int)(v >>> 48) & 0xFF));
	        out.write(((int)(v >>> 40) & 0xFF));
	        out.write(((int)(v >>> 32) & 0xFF));
	        out.write(((int)(v >>> 24) & 0xFF));
	        out.write(((int)(v >>> 16) & 0xFF));
	        out.write(((int)(v >>>  8) & 0xFF));
	        out.write(((int)(v >>>  0) & 0xFF));
		}
		
		public void writeShort( short v ) {
	        out.write((v >>> 8) & 0xFF);
	        out.write((v >>> 0) & 0xFF);
		}
		
		public void writeByte( byte v ) {
			out.write(v);
		}
		
		public void writeChar( char v ) {
	        out.write((v >>> 8) & 0xFF);
	        out.write((v >>> 0) & 0xFF);
		}
		
		public void writeBoolean( boolean v ) {
			out.write(v ? 1 : 0);
		}
		
		public void writeDouble( double val ) {
			writeString( Double.toString( val ) );
		}
		
		public void writeFloat( float val ) {
			writeString( Double.toString( val ) );
		}
		
		/** String will be encoded and may contain any character */
		public void writeString( String val ) {
			try {
				byte[] buf = val.getBytes("UTF-8");
				writeInt(buf.length);
				
				out.write(buf);
			} catch ( UnsupportedEncodingException e ) {
				throw new StreamerException( e );
			}
		}

		@Override
		public void writeByteArray(byte[] val) {
			out.write(val);
		}

		@Override
		public byte[] toByte() {
			return Base91Util.encode_byte( out.toByteArray_Fast_Read() );
		}

		@Override
		public String toRawString() {
			//fix for var-arg issue
			StringBuilder tmp = new StringBuilder();
			byte[] tmp_byte = out.toByteArray_Fast_Read();
			for (int i=0; i<tmp_byte.length; i+=100000){
				tmp.append(new String(tmp_byte, i, Math.min(100000, tmp_byte.length-i)));
			}
			return tmp.toString();
		}

		@Override
		public byte[] toRawByte() {
			return out.toByteArray();
		}

		@Override
		public String toString(int prefix) {
			byte[] buf = out.toByteArray_Fast_Read();
			return Base91Util.encode( buf, prefix );
		}

		@Override
		public String toRawString(int prefix) {
			StringBuilder tmp = new StringBuilder();
			if (prefix!=0) {
				String prefix_str = ((Integer)prefix).toString();
				if (prefix_str.length()==10)
					tmp.append(prefix_str);
			}
			byte[] tmp_byte = out.toByteArray_Fast_Read();
			for (int i=0; i<tmp_byte.length; i+=100000){
				tmp.append(new String(tmp_byte, i, Math.min(100000, tmp_byte.length-i)));
			}
			return tmp.toString();
		}
	
	}

	public static class Base91Reader implements Reader
	{
		private ByteArrayInputStream in;
		
		public Base91Reader( final String str ) {
			this(str, false);
		}
		
		public Base91Reader( final byte[] data){
			this(data, false);
		}
		
		public Base91Reader(String str, int toSkip) {
			this(str, toSkip, false);
		}

		public Base91Reader(byte[] data, boolean raw) {
			if (raw){
				in = new ByteArrayInputStream( data );
			}else{
				byte[] buf = Base91Util.decode( data );
				in = new ByteArrayInputStream( buf );
			}
		}

		public Base91Reader(String str, boolean raw) {
			if (raw){
				in = new ByteArrayInputStream( str.getBytes() );
			}else{
				byte[] buf = Base91Util.decode( str );
				in = new ByteArrayInputStream( buf );
			}
		}

		public Base91Reader(String str, int toSkip, boolean raw) {
			if (raw){
				in = new ByteArrayInputStream( str.getBytes() );
				if (toSkip!=0){
					byte[] tmp = new byte[toSkip];
					try {
						in.read(tmp);
					} catch (IOException e1) {
					}
				}
			}else{
				byte[] buf = Base91Util.decode( str, toSkip );
				in = new ByteArrayInputStream( buf );
			}
		}

		public boolean hasMore() {
			return in.hasMore();
		}

		public int readInt()
		{
	        int ch1 = in.read();
	        int ch2 = in.read();
	        int ch3 = in.read();
	        int ch4 = in.read();
	        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
		}

		public long readLong() {
	        int ch1 = in.read();
	        int ch2 = in.read();
	        int ch3 = in.read();
	        int ch4 = in.read();
	        int ch5 = in.read();
	        int ch6 = in.read();
	        int ch7 = in.read();
	        int ch8 = in.read();
	        return ((ch1 << 56) + (ch2 << 48) + (ch3 << 40) + (ch4 << 32) + (ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8 << 0));
		}
		
		public short readShort() {
	        int ch1 = in.read();
	        int ch2 = in.read();
	        return (short)((ch1 << 8) + (ch2 << 0));
		}

		
		public byte readByte() {
			int ch = in.read();
			return (byte)(ch);
		}
		
		public char readChar() {
	        int ch1 = in.read();
	        int ch2 = in.read();
	        return (char)((ch1 << 8) + (ch2 << 0));
		}
		
		public boolean readBoolean() {
			int ch = in.read();
			return (ch != 0);
		}
		
		public double readDouble() {
			String s = readString();
			
			try {
				return Double.parseDouble( s );
			} catch ( NumberFormatException ex ) {
				throw new StreamerException( ex );
			}
		}
		
		public float readFloat() {
			String s = readString();
			
			try {
				return Float.parseFloat( s );
			} catch ( NumberFormatException ex ) {
				throw new StreamerException( ex );
			}
		}
		
		/** String will be encoded and may contain any character */
		public String readString() {
			int l = readInt();
			byte[] buf = new byte[l];
			try {
				in.read(buf);
			} catch (IOException e1) {
				throw new StreamerException( e1 );
			}
			try {
				return new String( buf, "UTF-8" );
			} catch ( UnsupportedEncodingException e ) {
				throw new StreamerException( e );
			}
		}


		@Override
		public void readByteArray(byte[] val) {
			try {
				in.read(val);
			} catch (IOException e) {
			}
		}
		
	}
	
	@Override
	public Writer createWriter() {
		return new Base91Writer();
	}

	@Override
	public Reader createReader(String str) {
		return new Base91Reader( str );
	}

	@Override
	public Reader createReaderRaw(byte[] data) {
		return new Base91Reader( data, true );
	}

	@Override
	public Reader createReader(String str, int toSkip) {
		return new Base91Reader( str, toSkip );
	}

	@Override
	public Reader createReader(byte[] data) {
		return new Base91Reader( data );
	}

	@Override
	public Reader createReaderRaw(String str) {
		return new Base91Reader( str, true );
	}

	@Override
	public Reader createReaderRaw(String str, int toSkip) {
		return new Base91Reader( str, toSkip, true );
	}

}
