package com.dcpabe.web.client;

import gwt.ns.webworker.client.MessageEvent;
import gwt.ns.webworker.client.MessageHandler;
import gwt.ns.webworker.client.WorkerEntryPoint;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;

import java.util.Collection;
import java.util.Vector;

import org.vectomatic.arrays.ArrayBuffer;

import com.dcpabe.web.client.ac.AccessStructure;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.typedarrays.client.ArrayBufferNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.nkdata.gwt.streamer.client.Streamer;
import com.nkdata.gwt.streamer.client.impl.Base91Util;

public class SchedulingWorker extends WorkerEntryPoint {
	GlobalParameters gp = null;
	
	//customized 10-byte long strings as message prefixes
	public static final int POPULATE_PARAM=1234365789;
	public static final int CHILD_ENC_FIN=1254290785;
	public static final int CHILD_DEC_FIN=1018764265;
	public static final int GEN_AUTH=1953040821;
	public static final int GEN_POL=1483945967;
	public static final int GEN_PRIV=1778848233;
	public static final int START_ENC=1756672982;
	public static final int START_DEC=2113960293;
	public static final int UPDATE_PARAM=1265895353;
	public static final int UPDATE_CORENUM=1224443387;

	public static final int POPULATE_PARAM_DONE=1334687744;
	public static final int UPDATE_PARAM_DONE=2132050377;
	public static final int DEFAULT_MESSAGE=1461315598;
	public static final int FIN_ENC=1743226582;
	public static final int FIN_DEC=2132433693;
	public static final int GEN_AUTH_FIN=1542743846;
	public static final int GEN_POL_FIN=1435875320;
	public static final int GEN_PRIV_FIN=1668392012;

	public static final int SCHEDULE_ENC = 1723654936;
	public static final int SCHEDULE_DEC = 1335468892;
	public static final int PRE_SCHEDULE = 1423540901;
	public static final int SET_THREAD_ID = 2065493309;
	
	public static final int CHILD_SET_GP = 1236547903;
	public static final int PROPAGATE_PK = 2008443255;
	
	public static final int DLD_AUTHORITY = 1119944565;
	public static final int DLD_PRIVATE = 2139753344;
	public static final int LOAD_AUTHORITY = 2112243556;
	public static final int LOAD_PRIVATE = 1111333324;
	public static final int DLD_GLOBAL = 1237584901;
	
	AuthorityKeys ak = null;
	AttributeGen attgen=new AttributeGen();
	PersonalKeys user = null;

	private Ciphertext ct;
	private Encryption_passData[] data_array;
	private int currentEncIndex;

	int numCores = 2;
	Message m;
	byte[][] C1;
	byte[][] C2;
	byte[][] C3;

	protected String pol;
	boolean[] finished = new boolean[Main.numCores_max];

	private boolean encrypt_finished = false;
	
	long start=0;
	
    public void onWorkerClose() {
    	gp = null;
    }
    
    public void onWorkerLoad() {
    	Streaming.registerStreamer();
    	
    	final SchedulingWorker parent_worker = this;
    	for (int i=0; i<Main.numCores_max; i++)
    		finished[i]=false;
    	
    	MessageHandler msghdl = new MessageHandler(){
    		@Override
    		public void onMessage(MessageEvent event) {

    			if (event.getKind()==0){
    				ArrayBuffer ab = event.getData_array();
					byte[] data = new byte[ab.getByteLength()];
					Int8ArrayNative ia = Int8ArrayNative.create((com.google.gwt.typedarrays.shared.ArrayBuffer) ab);
					for (int i=0; i<data.length; i++){
						data[i]=ia.get(i);
					}
					
    				switch(event.getPrefix()){
    				case UPDATE_PARAM:
    					gp = (GlobalParameters)Streamer.get().fromBytesRaw(data);
        				postMsg("Received new GP!");
        				postMsg(UPDATE_PARAM_DONE);
    					break;
    				case PROPAGATE_PK:
    				case LOAD_AUTHORITY:
    					ak = (AuthorityKeys) Streamer.get().fromBytesRaw(data);
        				postMessage(GEN_AUTH_FIN, ak.getAttributes());
    					break;
    				case LOAD_PRIVATE:
    					user = (PersonalKeys) Streamer.get().fromBytesRaw(data);
        				StringBuilder sb = new StringBuilder();
        				Collection<String> tmp = user.getAttributes();
        				for (String str:tmp){
        					sb.append(str);
        					sb.append(" ");
        				}
        				postMessage(GEN_PRIV_FIN, sb.toString());
        				break;
    				case CHILD_ENC_FIN:
	    				Encryption_returnData result = 
	    					(Encryption_returnData) Streamer.get().fromBytesRaw(data);
	    				postMsg("\tThread "+((Integer)(result.thread_id)).toString()+": Finished iteration "+((Integer)(result.index)).toString());
	    				C1[result.index]=result.c1;
	    				C2[result.index]=result.c2;
	    				C3[result.index]=result.c3;
	    				finished[result.thread_id]=true;
	    				scheduleChild(result.thread_id);
	    				break;
    				default:
    					postMsg("Wrong message!");
        				break;
    				}
    			}else{
	    			String message = event.getData();
	    			int message_prefix = event.getPrefix();
	    			
	    			switch (message_prefix){
	    			case POPULATE_PARAM:
	    				postMsg("Populating param...");
	    				DefaultCurveParameters cp = new DefaultCurveParameters();
	    				cp.load(message);
	    				gp = new GlobalParameters();
	    				gp.setCurveParams(cp);
	    				gp = DCPABE.populateGlobalParameters(gp);
	    				postMsg("Done!");
	    				postMsg(POPULATE_PARAM_DONE);
	    				break;
	    			case GEN_PRIV:
	    				generate_user_key(message);
	    				break;
	    			case GEN_AUTH:
	    				generate_auth(Integer.parseInt(message));
	    				break;
	    			case GEN_POL:
	    				generate_pol(Integer.parseInt(message));
	    				break;
	    			case CHILD_DEC_FIN:
	    				
	    				break;
	    			case START_ENC:
	    				Scheduler.get().scheduleDeferred(new ScheduledCommand(){
							@Override
							public void execute() {
								if (gp==null){
									postMsg("Load GlobalParam first!");
									postMsg(FIN_ENC);
									return;
								}
								if (ak==null){
									postMsg("Load Authority Keys first!");
									postMsg(FIN_ENC);
									return;
								}
								start=System.currentTimeMillis();
								m = new Message();
			    				PublicKeys pks = new PublicKeys();
			    				pks.subscribeAuthority(ak.getPublicKeys());
			    				AccessStructure arho = AccessStructure.buildFromPolicy(pol);
			    				Ciphertext tmpct = DCPABE.encrypt(m, arho, gp, pks, parent_worker, numCores);
			    				if (tmpct!=null){
			    					Encrypt_fin(tmpct, true);
			    				}
			    				postMsg("Original Message: "+Base91Util.encode( m.m ));
							}
	    				});
	    				break;
	    			case START_DEC:
	    				start=System.currentTimeMillis();
	    				if (ct==null){
	    					postMsg("No message to decrypt!");
	    					postMsg(FIN_DEC);
	    					break;
	    				}
	    				Message m_dec=DCPABE.decrypt(ct, user, gp);
	    				if (m_dec!=null){
		    				StringBuilder sb = new StringBuilder();
		    				sb.append(Base91Util.encode(m_dec.m));
		    				postMsg("Decrypted Message: "+sb.toString());
		    				postMsg(FIN_DEC);
		    				outputtime();
	    				}else{
	    					postMsg("Decryption failed");
	    					postMsg(FIN_DEC);
	    				}
	    				break;
	    			case UPDATE_CORENUM:
	    				numCores = Integer.parseInt(message);
	    				postMsg("Active threads: "+numCores);
	    				break;
	    			case DLD_AUTHORITY:
	    				postMessage(DLD_AUTHORITY, Streamer.get().toBytesRaw(ak));
	    				break;
	    			case DLD_PRIVATE:
	    				postMessage(DLD_PRIVATE, Streamer.get().toBytesRaw(user));
	    				break;
	    			case DLD_GLOBAL:
	    				postMessage(DLD_GLOBAL, Streamer.get().toBytesRaw(gp));
	    			default:
	    				postMsg("Wrong message!");
	    				break;
	    			}
    			}
    		}
    	};
    	
    	setMessageHandler(msghdl);
    }

	protected void postMessage(int prefix, byte[] bytesRaw) {
		ArrayBufferNative ab = ArrayBufferNative.create(bytesRaw.length);
		Int8ArrayNative ia = Int8ArrayNative.create(ab);
		ia.set(bytesRaw);
		transferMessage(prefix, ab);
	}

	protected void generate_user_key(String message){
    	postMsg("Generate private keys...");
    	String[] user_attr = message.split(" ");
    	user = new PersonalKeys("User");
    	for (String i : user_attr){
    		user.addKey(DCPABE.keyGen("User", 
					i, 
					ak.getSecretKeys().get(i), 
					gp));
    		postMsg("\tAttribute "+i+" processed");
    	}
    	postMsg(GEN_PRIV_FIN);
    }

	protected void generate_pol(int att_num) {
		Vector<String> formula_group = attgen.gen(att_num, att_num, 1);
		String [] formula_array = new String[1];
		formula_group.toArray(formula_array);
		pol = formula_array[0];
		postMsg(GEN_POL_FIN,formula_array[0]);
	}

	protected void generate_auth(int att_num) {
		Vector<String> formula_group = attgen.gen(att_num, att_num, 1);
		String [] formula_array = new String[1];
		formula_group.toArray(formula_array);
		
		postMsg("authoritySetup...");
		String[] attributes = (String [])attgen.backup_attrlist.toArray(new String[]{""});
		ak = DCPABE.authoritySetup("defaultAuthority", 
				gp, 
				attributes);
		
		//postMessage(Streamer.get().toString(ak, PROPAGATE_PK));
		byte[] ak_serialized = Streamer.get().toBytesRaw(ak);
		ArrayBufferNative ab = ArrayBufferNative.create(ak_serialized.length);
		Int8ArrayNative ia = Int8ArrayNative.create(ab);
		ia.set(ak_serialized);
		transferMessage(PROPAGATE_PK, ab);
		
		StringBuilder sb = new StringBuilder();
		for (String i:attributes){
			sb.append(i);
			sb.append(" ");
		}
		
		pol = formula_array[0];
		postMsg(GEN_AUTH_FIN,sb.toString());
		postMsg(GEN_POL_FIN,formula_array[0]);
	}

	public void postMsg(String str){
    	postMessage(DEFAULT_MESSAGE, str);
    }
    public void postMsg(int type, String str){
    	postMessage(type,str);
    }
    public void postMsg(int str){
    	postMessage(str, "");
    }

	public void setEncryptionData(Encryption_passData[] data_array) {
		this.data_array = data_array;
	}

	public void setCt(Ciphertext ct) {
		this.ct = ct;
	}

	public void startEncInternalLoop() {
		encrypt_finished=false;
		for (int i=0; i<numCores; i++)
    		finished[i]=true;
		currentEncIndex = 0;
		C1 = new byte[data_array.length][];
		C2 = new byte[data_array.length][];
		C3 = new byte[data_array.length][];
		scheduleChild tmp = new scheduleChild();
		tmp.curr=0;
		tmp.numCores=numCores;
		Scheduler.get().scheduleIncremental(tmp);
	}
	
	class scheduleChild implements RepeatingCommand{
		public int curr=0;
		public int numCores=0;

		@Override
		public boolean execute() {
			if (curr<numCores){
				if (!scheduleChild(curr)) return false;
				curr++;
				if (curr>=numCores) return false; else return true;
			}
			return false;
		}
	}

	private void Encrypt_fin(Ciphertext ct, boolean direct) {
		if (!direct){
			for (int i=0; i<data_array.length; i++){
				ct.setC1(C1[i]);
				ct.setC2(C2[i]);
				ct.setC3(C3[i]);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(Base91Util.encode(ct.getC0()));
		postMsg("Encrypted Message: "+sb.toString());
		postMsg(FIN_ENC);
		outputtime();
	}

	private void outputtime() {
		long end=System.currentTimeMillis();
		postMsg("Time elapsed: "+(((double)end-(double)start)/1000.0)+"s\n");
	}

	private boolean scheduleChild(int iCore) {
		if (currentEncIndex >= data_array.length){
			boolean done = true;
			for (int i=0; i < numCores; i++){
				if (!finished[i]) {done = false; break;}
			}
			if (done){
				if (!encrypt_finished){
					encrypt_finished=true;
					Scheduler.get().scheduleDeferred(new ScheduledCommand(){
						@Override
						public void execute() {
							Encrypt_fin(ct, false);
						}
					});
				}
			}
			return false;
		}
		
		postMsg(PRE_SCHEDULE, ((Integer)iCore).toString());
		postMessage(SCHEDULE_ENC, Streamer.get().toBytesRaw(data_array[currentEncIndex]));

		finished[iCore]=false;
		currentEncIndex++;
		return true;
	}
}