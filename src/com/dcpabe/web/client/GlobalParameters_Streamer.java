package com.dcpabe.web.client;

import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public final class GlobalParameters_Streamer extends Streamer {

	@Override
	public void writeObject( Object obj, WriteContext ctx, Writer out )
	{
		ctx.addRef( obj );
		GlobalParameters param = (GlobalParameters) obj;
		get().writeObject(param.curveParams, ctx, out);
		get().writeObject(param.g1.toBytes(), ctx, out);
		get().writeObject(param.eg1g1_preprocess, ctx, out, true);
		get().writeObject(param.g1_preprocess, ctx, out, true);
	}

	@Override
	public Object readObject( ReadContext ctx, Reader in )
	{
		GlobalParameters param = new GlobalParameters();
		ctx.addRef( param );
		param.curveParams = (CurveParameters) get().readObject( ctx, in );
		Pairing pairing = PairingFactory.getPairing(param.curveParams);
		param.g1 = pairing.getG1().newElement();
		param.g1.setFromBytes((byte[]) get().readObject( ctx, in ));
		param.g1 = param.g1.getImmutable();
		param.eg1g1_preprocess = (byte[]) get().readObject( ctx, in );
		param.g1_preprocess = (byte[]) get().readObject( ctx, in );
		
		return param;
	}
	    
}
