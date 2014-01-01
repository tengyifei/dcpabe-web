package com.nkdata.gwt.streamer.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Base91Util {

	public static String encode(byte[] buf){
		return encode(buf, 0);
	}
	
	public static String encode(byte[] buf, int prefix){
		int s;
		byte[] ibuf = new byte[53248];
		byte[] obuf = new byte[65536];
		basE91 b91 = new basE91();
		ByteArrayInputStream is = new ByteArrayInputStream(buf);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			while ((s = is.read(ibuf)) > 0) {
				s = b91.encode(ibuf, s, obuf);
				os.write(obuf, 0, s);
			}
		} catch (IOException e) {
			return null;
		}
		s = b91.encEnd(obuf);
		os.write(obuf, 0, s);
		
		//problem: (var-args!!)
		
		/*
		 * public static native String valueOf(char[] x) -{
    // Trick: fromCharCode is a vararg method, so we can use apply() to pass the
    // entire input in one shot.
    return String.fromCharCode.apply(null, x);
  }-;
		 * */
		
		//fix:
		
		StringBuilder tmp = new StringBuilder();
		if (prefix!=0) {
			String prefix_str = ((Integer)prefix).toString();
			if (prefix_str.length()==10)
				tmp.append(prefix_str);
		}
		byte[] tmp_byte = os.toByteArray_Fast_Read();
		for (int i=0; i<tmp_byte.length; i+=100000){
			tmp.append(new String(tmp_byte, i, Math.min(100000, tmp_byte.length-i)));
		}
		
		return tmp.toString();
	}
	
	public static byte[] decode(String str) {
		return decode(str.getBytes(), 0);
	}
	
	public static byte[] decode(byte[] data) {
		return decode(data, 0);
	}

	private static byte[] decode(byte[] data, int toSkip) {
		int s;
		byte[] ibuf = new byte[65536];
		byte[] obuf = new byte[57344];
		basE91 b91 = new basE91();
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		if (toSkip!=0){
			byte[] tmp = new byte[toSkip];
			try {
				is.read(tmp);
			} catch (IOException e1) {
				return null;
			}
		}
		
		try {
			while ((s = is.read(ibuf)) > 0) {
				s = b91.decode(ibuf, s, obuf);
				os.write(obuf, 0, s);
			}
		} catch (IOException e) {
			return null;
		}
		s = b91.decEnd(obuf);
		os.write(obuf, 0, s);
		return os.toByteArray_Fast_Read();
	}

	public static byte[] decode(String str, int toSkip) {
		return decode(str.getBytes(), toSkip);
	}

	public static byte[] encode_byte(byte[] buf) {
		int s;
		byte[] ibuf = new byte[53248];
		byte[] obuf = new byte[65536];
		basE91 b91 = new basE91();
		ByteArrayInputStream is = new ByteArrayInputStream(buf);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			while ((s = is.read(ibuf)) > 0) {
				s = b91.encode(ibuf, s, obuf);
				os.write(obuf, 0, s);
			}
		} catch (IOException e) {
			return null;
		}
		s = b91.encEnd(obuf);
		os.write(obuf, 0, s);
		
		return os.toByteArray_Fast_Read();
	}
}
