package it.unisa.dia.gas.plaf.jpbc.pairing;

import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public final class DefaultCurveParameters_Streamer extends Streamer {
	
    @Override
    public void writeObject( Object obj, WriteContext ctx, Writer out ) {
        ctx.addRef( obj );
        DefaultCurveParameters mo = (DefaultCurveParameters) obj;
        out.writeString(mo.toString());
    }

    @Override
    public Object readObject( ReadContext ctx, Reader in )
    {
    	DefaultCurveParameters mo = new DefaultCurveParameters();
        ctx.addRef( mo );
        mo.load(in.readString());
        return mo;
    }

}
