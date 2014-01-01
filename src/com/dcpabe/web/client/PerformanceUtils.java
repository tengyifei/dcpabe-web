package com.dcpabe.web.client;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;

import com.dcpabe.web.client.GlobalParameters;

public class PerformanceUtils {
	
	public final static int TEST_PERIOD_MILLIS = 23000;
	
	//default performance index on a computer, used to normalize test results
	public final static double DEFAULT_PERFORMANCE = 18.3881064;
	
	//reject, instead of using mod p, to give even distribution of results
	public static BigInteger random(BigInteger p) {
	    SecureRandom rnd = new SecureRandom();
	    
	    //however, if the random number is unfortunately consistently larger than p, 
	    //will fallback to mod solution for to avoid performance loss
	    for (int cnt=0; cnt<3; cnt++) {
	        BigInteger num = new BigInteger(p.bitLength(), rnd);
	        if (num.compareTo(p) <= 0)
	            return num;
	    }
	    
	    return new BigInteger(p.bitLength(), rnd).mod(p.add(BigInteger.ONE));
	}
	
	//determine CPU integer performance
	public static double getPerformanceIndex(GlobalParameters gp){
		long count = 0;
		
		Pairing pairing = PairingFactory.getPairing(gp.getCurveParams());
		
		long start = System.currentTimeMillis();
		long elapsed;

		Element e = pairing.getG1().newRandomElement().getImmutable();
		do {
			BigInteger bgint = BigInteger.valueOf(2).pow(237).subtract(
								random(BigInteger.valueOf(2).pow(8)));
			e.pow(bgint);
			count++;
			elapsed = System.currentTimeMillis() - start;
		} while (elapsed < TEST_PERIOD_MILLIS);
		
		return count*1000.0/(double)elapsed;
	}
	
}
