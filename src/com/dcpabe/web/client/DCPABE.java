package com.dcpabe.web.client;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingPreProcessing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.util.List;
import java.util.Vector;

import com.dcpabe.web.client.ac.AccessStructure;

public class DCPABE {
	public static GlobalParameters globalSetup(int lambda) {
		GlobalParameters params = new GlobalParameters();
		params.setCurveParams(new TypeA1CurveGenerator(3, lambda).generate());
		params = populateGlobalParameters(params);
		
		return params;
	}
	
	public static GlobalParameters populateGlobalParameters(GlobalParameters gp){
		Pairing pairing = PairingFactory.getPairing(gp.getCurveParams());
		
		gp.setG1(pairing.getG1().newRandomElement().getImmutable());
		
		Element eg1g1 = pairing.pairing(gp.getG1(), gp.getG1()).getImmutable();

		byte[] data = new AbstractElementPowPreProcessing_Fast(
				eg1g1).toBytes();
		gp.set_eg1g1_preprocess(data);
		
		byte[] data_g1 = new AbstractElementPowPreProcessing_Fast(
				gp.getG1()).toBytes();
		gp.setg1_preprocess(data_g1);
		
		return gp;
	}
	
	public static AuthorityKeys authoritySetup(String authorityID, GlobalParameters GP, String ... attributes) {
		AuthorityKeys authorityKeys = new AuthorityKeys(authorityID);
		
		Pairing pairing = PairingFactory.getPairing(GP.getCurveParams());
		Element eg1g1 = pairing.pairing(GP.getG1(), GP.getG1()).getImmutable();
		
		for (String attribute : attributes) {
			Element ai = pairing.getZr().newRandomElement().getImmutable();
			Element yi = pairing.getZr().newRandomElement().getImmutable();
			
			Element G1_yi= GP.getG1().powZn(yi);
			
			byte[] data = new AbstractElementPowPreProcessing_Fast(G1_yi).toBytes();
			
			authorityKeys.getPublicKeys().put(attribute, new PublicKey(
					eg1g1.powZn(ai).toBytes(), 
					G1_yi.toBytes(),
					data)
					);
			
			authorityKeys.getSecretKeys().put(attribute, new SecretKey(ai.toBytes(), yi.toBytes()));
		}
		
		return authorityKeys;
	}

	public static Ciphertext encrypt(Message message, AccessStructure arho, GlobalParameters GP, PublicKeys pks, SchedulingWorker parent_worker, int numCores) {
		
		Ciphertext ct = new Ciphertext();
		
		Pairing pairing = PairingFactory.getPairing(GP.getCurveParams());

		AbstractElementPowPreProcessing_Fast eg1g1_preprocess = 
				new AbstractElementPowPreProcessing_Fast(
						pairing.getGT(),
						GP.geteg1g1_preprocess());
		
		AbstractElementPowPreProcessing_Fast g1_preprocess = 
				new AbstractElementPowPreProcessing_Fast(
						pairing.getG1(),
						GP.getg1_preprocess());
		
		Element M = pairing.getGT().newRandomElement().getImmutable();
		message.m = M.toBytes();
		
		Element s = pairing.getZr().newRandomElement().getImmutable();
		
		Vector<Element> v = new Vector<Element>(arho.getL());
		v.add(s);
		Vector<Element> w = new Vector<Element>();
		w.add(pairing.getZr().newZeroElement().getImmutable());
		
		for (int i = 1; i < arho.getL(); i++) {
			v.add(pairing.getZr().newRandomElement().getImmutable());
			w.add(pairing.getZr().newRandomElement().getImmutable());
		}
		
		ct.setAccessStructure(arho);
		parent_worker.postMsg("\tSet C0...");
		
		ct.setC0(M.mul(eg1g1_preprocess.powZn(s)).toBytes()); // C_0
		
		parent_worker.postMsg("\tInternal loop...");
		
		Element[] wx=new Element[arho.getN()];
		Element[] lambdax=new Element[arho.getN()];
		Element[] c1x2=new Element[arho.getN()];
		
		for (int x = 0; x < arho.getN(); x++) {
			lambdax[x] = dotProduct(arho.getRow(x), v, pairing.getZr().newZeroElement(), pairing);
			wx[x] = dotProduct(arho.getRow(x), w, pairing.getZr().newZeroElement(), pairing);
			c1x2[x] = pairing.getGT().newElement();
			c1x2[x].setFromBytes(pks.getPK(arho.rho(x)).getEg1g1ai());
		}
		
		Encryption_passData[] data_array = new Encryption_passData[arho.getN()];
		for (int x = 0; x < arho.getN(); x++){
			data_array[x] =
				new Encryption_passData(
						wx[x], 
						lambdax[x], 
						c1x2[x], 
						x,
						arho.rho(x)
					);
		}
		
		if (arho.getN()<2 || numCores==1){
			for (int x = 0; x < arho.getN(); x++){
				parent_worker.postMsg("\tProcessing row "+x);
				Encryption_returnData result = internal(data_array[x], GP, pairing, pks, eg1g1_preprocess, g1_preprocess);
				ct.setC1(result.c1);
				ct.setC2(result.c2);
				ct.setC3(result.c3);
			}
			parent_worker.setCt(ct);
			return ct;
		}else{
			parent_worker.postMsg("\tRunning sub-tasks...");
			//let child workers do it
			parent_worker.setEncryptionData(data_array);
			parent_worker.setCt(ct);
			parent_worker.startEncInternalLoop();
			//returns null as haven't finished yet
			return null;
		}
	}
	
	public static Encryption_returnData internal(
			Encryption_passData data, GlobalParameters GP, Pairing pairing, PublicKeys pks, 
			AbstractElementPowPreProcessing_Fast eg1g1_preprocess,
			AbstractElementPowPreProcessing_Fast g1_preprocess){
		Encryption_returnData result = new Encryption_returnData();
		
		AbstractElementPowPreProcessing_Fast preprocess_c3x = 
				new AbstractElementPowPreProcessing_Fast(
						pairing.getG1(),
						pks.getPK(data.pk_id).getG1yi_preprocess());
		
		Element rx = pairing.getZr().newRandomElement().getImmutable();
		Element c1x1 = eg1g1_preprocess.powZn(data.lambdax);
		data.c1x2.powZn(rx);
		
		result.c1=c1x1.mul(data.c1x2).toBytes();
		
		Element GP_rx = g1_preprocess.powZn(rx).getImmutable();
		Element GP_wx = g1_preprocess.powZn(data.wx).getImmutable();
		
		result.c2=GP_rx.toBytes();
		result.c3=preprocess_c3x.powZn(rx).mul(GP_wx).toBytes();
		
		result.index = data.index;	
		return result;
	}

	public static Message decrypt(Ciphertext CT, PersonalKeys pks, GlobalParameters GP) {
		//List<Integer> toUse = CT.getAccessStructure().getIndexesList(pks.getAttributes());
		List<Integer> toUse = CT.getAccessStructure().getIndexesList(pks.getAttributes());
		
		if (null == toUse || toUse.isEmpty()) {
			//throw new IllegalArgumentException("not satisfying");
			return null;
		}
		
		Pairing pairing = PairingFactory.getPairing(GP.getCurveParams());
		
		Element HGID = pairing.getG1().newElement();
		HGID.setFromHash(pks.getUserID().getBytes(), 0, pks.getUserID().getBytes().length);
		HGID = HGID.getImmutable();
		
		PairingPreProcessing eHGID = pairing.pairing(HGID);

		Element t = pairing.getGT().newOneElement();
		
		for (Integer x : toUse) {
			Element c3x = pairing.getG1().newElement();
			c3x.setFromBytes(CT.getC3(x));
			Element p1 = eHGID.pairing(c3x);
			
			PairingPreProcessing eKey = pairing.pairing(
					pks.getKey(CT.getAccessStructure().rho(x))
					.getKeyPreprocessed());
			
			Element c2x = pairing.getG1().newElement();
			c2x.setFromBytes(CT.getC2(x));
			Element p2 = eKey.pairing(c2x);
			
			Element c1x = pairing.getGT().newElement();
			c1x.setFromBytes(CT.getC1(x));
			t.mul(c1x.mul(p1).mul(p2.invert()));
		}
		
		Element c0 = pairing.getGT().newElement();
		c0.setFromBytes(CT.getC0());
		c0.mul(t.invert());
		
		return new Message(c0.toBytes());
	}
	
	public static PersonalKey keyGen(String userID, String attribute, SecretKey sk, GlobalParameters GP) {
		Pairing pairing = PairingFactory.getPairing(GP.getCurveParams());
		
		Element HGID = pairing.getG1().newElement();
		HGID.setFromHash(userID.getBytes(), 0, userID.getBytes().length);
		Element ai = pairing.getZr().newElement();
		ai.setFromBytes(sk.getAi());
		Element yi = pairing.getZr().newElement();
		yi.setFromBytes(sk.getYi());
		
		AbstractElementPowPreProcessing_Fast g1_preprocess = 
				new AbstractElementPowPreProcessing_Fast(
						pairing.getG1(),
						GP.getg1_preprocess());
		Element key = g1_preprocess.powZn(ai).mul(HGID.powZn(yi));
		
		PersonalKey pk = new PersonalKey(attribute, key.toBytes());
		
		pk.setKeyPreprocessed(pairing.pairing(key).toBytes());
		
		return pk;
	}
	
	private static Element dotProduct(Vector<Integer> v1, Vector<Element> v2, Element element, Pairing pairing) {
		if (v1.size() != v2.size()) throw new IllegalArgumentException("different length");
		if (element.isImmutable()) throw new IllegalArgumentException("immutable");
		
		if (!element.isZero())
			element.setToZero();
		
		for (int i = 0; i < v1.size(); i++) {
			Element e = pairing.getZr().newElement();
			
			switch (v1.get(i)) {
			case -1:
				e.setToOne().negate();
				break;
			case 1:
				e.setToOne();
				break;
			case 0:
				e.setToZero();
				break;
			}
			
			element.add(e.mul(v2.get(i).getImmutable()));
		}
		
		return element.getImmutable();
	}

}
