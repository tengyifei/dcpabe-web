package com.nkdata.gwt.streamer.client;


public interface StreamFactory {
	public interface Writer {
		public String toString();
		public byte[] toByte();
		public String toRawString();
		public byte[] toRawByte();
		public void writeInt( int val );
		public void writeLong( long val );
		public void writeShort( short val );
		public void writeByte( byte val );
		public void writeChar( char val );
		public void writeBoolean( boolean val );
		public void writeDouble( double val );
		public void writeFloat( float val );
		public void writeString( String val );
		public void writeByteArray( byte[] val);
		public String toString(int prefix);
		public String toRawString(int prefix);
	}
	
	
	public interface Reader {
		public boolean hasMore();
		public int readInt();
		public long readLong();
		public short readShort();
		public byte readByte();
		public char readChar();
		public boolean readBoolean();
		public double readDouble();
		public float readFloat();
		public String readString();
		public void readByteArray( byte[] val);
	}
	
	
	Writer createWriter();
	
	Reader createReader( String str );
	Reader createReader( byte[] data );
	Reader createReader( String str, int toSkip);
	
	Reader createReaderRaw( String str );
	Reader createReaderRaw( byte[] data );
	Reader createReaderRaw( String str, int toSkip );
}
