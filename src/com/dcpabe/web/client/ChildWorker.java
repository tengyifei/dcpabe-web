package com.dcpabe.web.client;

import org.vectomatic.arrays.ArrayBuffer;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import com.google.gwt.typedarrays.client.ArrayBufferNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.nkdata.gwt.streamer.client.Streamer;

import gwt.ns.webworker.client.MessageEvent;
import gwt.ns.webworker.client.MessageHandler;
import gwt.ns.webworker.client.WorkerEntryPoint;

public class ChildWorker extends WorkerEntryPoint  {

	int id=-1;
	Pairing pairing;
	GlobalParameters GP;
	PublicKeys pks;
	
	AbstractElementPowPreProcessing_Fast eg1g1_preprocess;
	AbstractElementPowPreProcessing_Fast g1_preprocess;
	
	@Override
	public void onWorkerLoad() {
		
		Streaming.registerStreamer();
		
		setMessageHandler(new MessageHandler(){
			@Override
			public void onMessage(MessageEvent event) {
				
				if (event.getKind()==0){
					ArrayBuffer ab = event.getData_array();
					byte[] data1 = new byte[ab.getByteLength()];
					Int8ArrayNative ia = Int8ArrayNative.create((com.google.gwt.typedarrays.shared.ArrayBuffer) ab);
					for (int i=0; i<data1.length; i++){
						data1[i]=ia.get(i);
					}
					switch (event.getPrefix()){
					case SchedulingWorker.UPDATE_PARAM:
					case SchedulingWorker.CHILD_SET_GP:
						GP = (GlobalParameters)Streamer.get().fromBytesRaw(data1);
						pairing = PairingFactory.getPairing(GP.getCurveParams());
						eg1g1_preprocess = new AbstractElementPowPreProcessing_Fast(
										pairing.getGT(),
										GP.geteg1g1_preprocess());
						
						g1_preprocess = new AbstractElementPowPreProcessing_Fast(
										pairing.getG1(),
										GP.getg1_preprocess());
						break;
					case SchedulingWorker.PROPAGATE_PK:
						pks = new PublicKeys();
	    				pks.subscribeAuthority(
	    						((AuthorityKeys)Streamer.get().fromBytesRaw(data1))
	    						.getPublicKeys());
	    				postMsg("Thread "+id+" received.");
						break;
					case SchedulingWorker.SCHEDULE_ENC:
						Encryption_passData data = 
								(Encryption_passData) Streamer.get().fromBytesRaw(data1);
						postMsg("\tThread "+id+": Processing iteration "+((Integer)data.index).toString());
						Encryption_returnData result = DCPABE.internal(data, GP, pairing, pks, eg1g1_preprocess, g1_preprocess);
						result.thread_id = id;
						postMessage(SchedulingWorker.CHILD_ENC_FIN, Streamer.get().toBytesRaw(result));
						break;
					default:
						postMsg("Child: Wrong message!");
					}
				}else{
					int message_prefix = event.getPrefix();
					String message = event.getData();
					switch (message_prefix){
					case SchedulingWorker.SET_THREAD_ID:
						id=Integer.parseInt(message);
						break;
					default:
						postMsg("Child: unknwon command: "+message_prefix);
						break;
					}
				}
			}
		});
	}
	
	protected void postMessage(int prefix, byte[] bytesRaw) {
		ArrayBufferNative ab = ArrayBufferNative.create(bytesRaw.length);
		Int8ArrayNative ia = Int8ArrayNative.create(ab);
		ia.set(bytesRaw);
		transferMessage(prefix, ab);
	}

	public void postMsg(String str){
    	postMessage(SchedulingWorker.DEFAULT_MESSAGE, str);
    }
	
	@Override
	public void onWorkerClose() {
	}

}
