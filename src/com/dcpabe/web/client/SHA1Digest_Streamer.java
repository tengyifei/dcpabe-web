package com.dcpabe.web.client;

import org.bouncycastle.crypto.digests.SHA1Digest;

import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public class SHA1Digest_Streamer extends Streamer {

	@Override
	public void writeObject( Object object, WriteContext ctx, Writer out )
	{
		ctx.addRef( object );
	}
	
	@Override
	public Object readObject( ReadContext ctx, Reader in )
	{
		SHA1Digest obj = new SHA1Digest();
		ctx.addRef( obj );
		return obj;
	}
}
