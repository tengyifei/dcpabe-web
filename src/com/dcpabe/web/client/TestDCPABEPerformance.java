package com.dcpabe.web.client;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;
import com.dcpabe.web.client.PerformanceUtils;
import com.dcpabe.web.client.AuthorityKeys;
import com.dcpabe.web.client.Ciphertext;
import com.dcpabe.web.client.DCPABE;
import com.dcpabe.web.client.GlobalParameters;
import com.dcpabe.web.client.Message;
import com.dcpabe.web.client.PersonalKeys;
import com.dcpabe.web.client.PublicKeys;
import com.dcpabe.web.client.ac.AccessStructure;
import com.dcpabe.web.client.AttributeGen;

public class TestDCPABEPerformance {
	
	public enum TestMode {
		ATTRIBUTE,
		POLICY_LEN,
		CLIENT_ATTR_NUM
	}
	
	static int num_rounds = 5;
	static int num_user_tested = 5;
	static String user_name = "defaultUser";
	
	static double factor;
	
	SchedulingWorker parent_worker=null;

	void Test(GlobalParameters gp, TestMode mode, int min, int max, int defAttr, int defPol, int defClient){
		parent_worker.postMsg("Assessing performance for "+PerformanceUtils.TEST_PERIOD_MILLIS/1000+" seconds...");
		double performance_index=PerformanceUtils.getPerformanceIndex(gp);
		parent_worker.postMsg("Performance Index: " + performance_index);
		
		factor = performance_index/PerformanceUtils.DEFAULT_PERFORMANCE;
		parent_worker.postMsg("Factor: " + (factor*100.0) + "%");
		factor = 1;
		
		switch (mode){
		case ATTRIBUTE:
			for (int i=min; i<=max; i++){
				subTest(i, defPol, defClient, num_rounds, gp);
			}
			break;
		case POLICY_LEN:
			for (int i=min; i<=max; i++){
				subTest(defAttr, i, defClient, num_rounds, gp);
			}
			break;
		case CLIENT_ATTR_NUM:
			for (int i=min; i<=max; i++){
				subTest(defAttr, defPol, i, num_rounds, gp);
			}
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void subTest(int total_attr_num, int attr_pol_num, int client_attr_num, int pass_num, GlobalParameters gp) {
		
		parent_worker.postMsg("Total attribute number="+total_attr_num+", attributes in policy="+attr_pol_num+", client attribute number="+client_attr_num+", number of run="+pass_num);
		
		AttributeGen attgen=new AttributeGen();
		Vector<String> formula_group = attgen.gen(total_attr_num, attr_pol_num, pass_num);
		String [] formula_array = new String[pass_num];		//all boolean formula tested
		formula_group.toArray(formula_array);
		
		double time;
		
		PersonalKeys [] attr_array = new PersonalKeys [num_user_tested];	//each element contains all attributes the user has
		int attr_client=Math.min(client_attr_num, total_attr_num);
		long start, end, oldtime=0, newtime=0;
		
		Vector<String> attr_list;
		SecureRandom rnd = new SecureRandom();
		rnd.setSeed(new Random().nextLong());
		rnd.nextBoolean();
		rnd.setSeed(new Random().nextLong());
		
		parent_worker.postMsg("\tauthoritySetup...");
		//authority setup
		AuthorityKeys ak = DCPABE.authoritySetup("defaultAuthority", 
				gp, 
				(String [])attgen.backup_attrlist.toArray(new String[]{""}));
		
		parent_worker.postMsg("\tgenerating private key...");
		//private key generation
		for (int i=0; i<num_user_tested; i++){
			attr_list = (Vector<String>) attgen.backup_attrlist.clone();
			attr_array[i] = new PersonalKeys(user_name);
			for (int j=0; j<attr_client; j++){
				String tmp=attr_list.remove(rnd.nextInt(attr_list.size()));
				attr_array[i].addKey(DCPABE.keyGen(user_name, 
						tmp, 
						ak.getSecretKeys().get(tmp), 
						gp));
			}
		}
		
		PublicKeys pks = new PublicKeys();
		pks.subscribeAuthority(ak.getPublicKeys());
		
		Ciphertext[] ct = new Ciphertext[pass_num];
		Message[] msg = new Message[pass_num];
		
		parent_worker.postMsg("\tbegin encryption...");
		//test encryption
		do{
			start=System.currentTimeMillis();
			int cnt=0;
			for (String i:formula_array){
				parent_worker.postMsg("\tpolicy: "+i);
				AccessStructure arho = AccessStructure.buildFromPolicy(i);
				Message m = new Message();
				ct[cnt] = DCPABE.encrypt(m, arho, gp, pks, parent_worker, 1);
				msg[cnt] = m;
				cnt++;
			}
			end=System.currentTimeMillis();
			oldtime=newtime;
			newtime=end-start;
			parent_worker.postMsg("\tone iteration");
			
		}while (((double)Math.abs(newtime-oldtime)) / (double)newtime > 0.015);
	
		time=(((double)newtime+(double)oldtime)/2.0/1000.0);
		
		parent_worker.postMsg("\tEncryption Time="+(time/(double)pass_num*factor)+"s");
		System.gc();
		oldtime=0;
		newtime=0;
		
		parent_worker.postMsg("\tbegin decryption...");
		int failed=0;
		//test decryption
		do{
			failed=0;
			start=System.currentTimeMillis();
			for (int i=0; i<pass_num; i++){
				parent_worker.postMsg("\t\tpolicy: "+i);
				for (int j=0; j<num_user_tested; j++){
					StringBuffer sb = new StringBuffer();
					sb.append("\t\t\tattributes: ");
					String[] attr_names = attr_array[j].getAttributes().toArray(new String[0]);
					for (String k:attr_names){
						sb.append(k+", ");
					}
					parent_worker.postMsg(sb.toString());
					Message message=DCPABE.decrypt(ct[i], attr_array[j], gp);
					if (message==null){
						failed++;
					}else if (!Arrays.equals(message.m, msg[i].m)){
							throw new IllegalArgumentException("wrong!!");
					}
				}
			}
			end=System.currentTimeMillis();
			oldtime=newtime;
			newtime=end-start;
			parent_worker.postMsg("\tone iteration");
			
		}while (((double)Math.abs(newtime-oldtime)) / (double)newtime > 0.015);
	
		time=(((double)newtime+(double)oldtime)/2.0/1000.0);
		
		parent_worker.postMsg("\tDecryption Time="
		+(time/(double)pass_num/(double)num_user_tested*factor/(1-((double)failed/(double)num_user_tested/(double)pass_num)))
		+"s, percentage fail="
		+(double)failed/(double)num_user_tested/(double)pass_num*100.0
		+"%");
		
	}
	
}
