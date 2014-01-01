/*
 * Copyright 2010 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gwt.ns.webworker.linker;

import gwt.ns.webworker.rebind.WorkerFactoryGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.StatementRanges;
import com.google.gwt.core.ext.linker.SymbolData;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.impl.StandardCompilationResult;
import com.google.gwt.dev.Permutation;
import com.google.gwt.dev.util.Util;

import com.google.gwt.dev.cfg.StaticPropertyOracle;
import com.google.gwt.dev.jjs.PermutationResult;

/**
 * Linker partner to {@link WorkerFactoryGenerator}. All Worker modules
 * requested via the artifact set are compiled and the actual paths to the
 * scripts, strongly named based on the contents of the scripts themselves, are
 * inserted into native-worker supporting permutations.
 * 
 * <p>Note that this Linker can be invoked within a recursive compilation
 * by a Worker that needs to spawn a sub-Worker. In this case, the relative URL
 * contains no strong name (since it will share the Worker's directory) and the
 * request can be passed up to the original Linker, thus preventing repeated
 * compilations and unbounded recursion.</p>
 */
@LinkerOrder(Order.POST)
public class WorkerCompilationLinker extends AbstractLinker {

	@Override
	public String getDescription() {
		return "Worker Compiler";
	}

	@Override
	public ArtifactSet link(TreeLogger logger, LinkerContext context,
			ArtifactSet artifacts) throws UnableToCompleteException {
		
		ArtifactSet toReturn = new ArtifactSet(artifacts);
		
		// get set of requests for insideWorker compilations from artifacts
		SortedSet<WorkerRequestArtifact> workerRequests =
			toReturn.find(WorkerRequestArtifact.class);
		if (workerRequests.size() == 0) {
			logger.log(TreeLogger.SPAM, "No Worker compilations requested. No action taken.");
			return toReturn; // early exit, sorry
		}
		
		// compile all requested workers
		// if this is a recursive call, requests were passed up to parent so
		// returned value is null
		SortedMap<WorkerRequestArtifact, HashMap<String,Tuple<String,String>>> workerScripts =
			WorkerCompiler.run(logger, workerRequests);
		
		// if they exist, deal with compiled scripts:
		if (workerScripts != null && workerScripts.size() != 0) {
			// directory strong name from all scripts
			@SuppressWarnings("unchecked")
			HashMap<String,Tuple<String,String>>[] perm_maps = workerScripts.values().toArray(new HashMap[0]);
			int scriptnum=0;
			for (HashMap<String,Tuple<String,String>> perm_map : perm_maps)
				scriptnum+=perm_map.size();
			String[] JSs = new String[scriptnum];
			int i=0;
			for (HashMap<String,Tuple<String,String>> perm_map : perm_maps){
				for (Map.Entry<String, Tuple<String,String>>perm : perm_map.entrySet()){
					JSs[i]=perm.getValue().x;
					i++;
				}
			}
			byte[][] bytejs = Util.getBytes(JSs);
			String scriptDirStrongName = Util.computeStrongName(bytejs);
			
			// emit worker scripts
			for (Map.Entry<WorkerRequestArtifact, HashMap<String,Tuple<String,String>>> script : workerScripts.entrySet()) {
				WorkerRequestArtifact req = script.getKey();
				HashMap<String,Tuple<String,String>> perms = script.getValue();
				for (Map.Entry<String, Tuple<String,String>>perm : perms.entrySet()){
					toReturn.add(emitString(logger, perm.getValue().x,
							req.getRelativePath_Strong(scriptDirStrongName,
							File.separator, perm.getValue().y)));
				}
			}
			
			// get the set of current compilation results
			SortedSet<CompilationResult> compResults =
				toReturn.find(CompilationResult.class);
			
			SortedSet<SyntheticArtifact> synResults =
					toReturn.find(SyntheticArtifact.class);
			
			/*for (SyntheticArtifact a : synResults){
					logger.log(TreeLogger.INFO, "Artifact: "+a.toString());
			}*/
			
			/*
			 * Reading the js from and writing it to a new CompilationResult is
			 * expensive (possibly disk cached), so read once and write only if
			 * altered.
			 */
			for (CompilationResult compRes : compResults) {
				// assume all need modification
				// TODO: rule out emulated permutations via properties
				//logger.log(TreeLogger.INFO, "Artifact size = "+toReturn.size());
				//if (!toReturn.remove(compRes)) throw new UnableToCompleteException();
				//logger.log(TreeLogger.INFO, "Artifact size = "+toReturn.size());
				CompilationResult altered = replacePlaceholder(logger, compRes,
						WorkerRequestArtifact.getPlaceholderStrongName(),
						scriptDirStrongName);
				//if (!toReturn.add(altered)) throw new UnableToCompleteException();
				
				for (SyntheticArtifact a : synResults){
					if (a.toString().length()<32) continue;
					if (a.toString().substring(0, 32).equals(compRes.getStrongName()) && a.toString().contains("cache.js")){
						//logger.log(TreeLogger.INFO, "Remove: "+a.toString());
						if (!toReturn.remove(a)) throw new UnableToCompleteException();

						try {
							byte[] buffer = new byte[a.getContents(logger).available()];
							a.getContents(logger).read(buffer);
							String string = new String(buffer);
							StringBuffer bufferstring = new StringBuffer(string);
							if (replaceAll(bufferstring, WorkerRequestArtifact.getPlaceholderStrongName(), scriptDirStrongName)){
								boolean out = false;
								String useragent="";
								for (SortedMap<SelectionProperty, String> map : compRes.getPropertyMap()) {
						    	      for (Map.Entry<SelectionProperty, String> entry : map.entrySet()) {
						    	        if (entry.getKey().getName().contentEquals("user.agent")){
						    	        	useragent = entry.getValue();
						    	        	out = true;
						    	        	break;
						    	        }
						    	      }
						    	      if (out) break;
						    	}
								for (Map.Entry<WorkerRequestArtifact, HashMap<String,Tuple<String,String>>> script : workerScripts.entrySet()) {
			    					WorkerRequestArtifact req = script.getKey();
			    					HashMap<String,Tuple<String,String>> perms = script.getValue();
			    					Tuple<String,String> object = perms.get(useragent);
			    					String workername = object.y.substring(0, object.y.lastIndexOf("_"));
			    					replaceAll(bufferstring, workername+".cache.js", object.y);
			    				}
								
								toReturn.add(emitString(logger, bufferstring.toString(), compRes.getStrongName()+".cache.js"));
							}else{
								toReturn.add(a);
							}
						} catch (IOException e) {
							e.printStackTrace();
							throw new UnableToCompleteException();
						}
						
					}

				}
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Searches for all instances of a placeholder String in a
	 * CompilationResult. If found, they are replaced as specified and a
	 * new CompilationResult, with a newly generated strong name, is returned.
	 * If no occurrences were found, the original CompilationResult is
	 * returned.
	 * 
	 * @param logger
	 * @param result CompilationResult to process
	 * @param placeholder String to be replaced
	 * @param replacement String to insert in place of placeholder
	 * 
	 * @return A CompilationResult suitable for emitting
	 */
	public CompilationResult replacePlaceholder(TreeLogger logger,
			CompilationResult result, String placeholder, String replacement) {
		
		boolean needsMod = false;
		
		String[] js = result.getJavaScript();
		StringBuffer[] jsbuf = new StringBuffer[js.length];
		for (int i = 0; i < jsbuf.length; i++) {
			jsbuf[i] = new StringBuffer(js[i]);
		}
			
		// search and replace in all fragments
		for (StringBuffer fragment : jsbuf) {
			needsMod |= replaceAll(fragment, placeholder, replacement);
		}
		
		// by default, returning unaltered result
		CompilationResult toReturn = result;
		
		// code has been altered, need to create new CompilationResult
		if (needsMod) {
			logger.log(TreeLogger.SPAM, "Compilation permutation "
					+ result.getPermutationId() + " being modified.");
			
			// new js for compilation result
			byte[][] newcode = new byte[jsbuf.length][];
			for (int i = 0; i < jsbuf.length; i++) {
				newcode[i] = Util.getBytes(jsbuf[i].toString());
			}
			
			// new strong name
			//String strongName = Util.computeStrongName(newcode);
			
			// same symbolMap copied over since none altered
			// symbolMap a little more complicated
			// can only get deserialized version, need to reserialize
			// code from com.google.gwt.dev.jjs.JavaToJavaScriptCompiler
			// TODO: turns out this can get pretty bad. Workaround?
			SymbolData[] symbolMap = result.getSymbolMap();
			byte[] serializedSymbolMap;
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Util.writeObjectToStream(baos, (Object) symbolMap);
				serializedSymbolMap = baos.toByteArray();
			} catch (IOException e) {
				// should still never happen
				logger.log(TreeLogger.ERROR, "IOException while reserializing "
						+ "SymbolMap.");
				throw new RuntimeException("Should never happen with in-memory stream",
						e);
			}
			
			CustomPermutationResult cpr = new CustomPermutationResult();
			cpr.perm = new Permutation(result.getPermutationId(), (StaticPropertyOracle)null);
			cpr.JS = newcode;
			cpr.statementranges = result.getStatementRanges();
			cpr.symmap = serializedSymbolMap;
			
			StandardCompilationResult altered =
				new StandardCompilationResult(cpr);
			
			// need to copy permutation properties to new result
			for (Map<SelectionProperty, String> propertyMap : result.getPropertyMap()) {
				altered.addSelectionPermutation(propertyMap);
			}
			
			logger.log(TreeLogger.INFO, "Compilation permuation "
					+ toReturn.getPermutationId()
					+ " : "
					+ toReturn.getStrongName()
					+ " altered to include path to worker script(s).");
			
			toReturn = altered;
			
			/*for (int i = 0; i < jsbuf.length; i++) {
				logger.log(TreeLogger.INFO, toReturn.getJavaScript()[i]);
			}*/
			
		}/*else{
			logger.log(TreeLogger.INFO, "Compilation permuation "
					+ toReturn.getPermutationId()
					+ " : "
					+ toReturn.getStrongName()
					+ " not altered.");
		}*/
		
		return toReturn;
	}
	
	class CustomPermutationResult implements PermutationResult{

		/**
		 * 
		 */
		private static final long serialVersionUID = -2889147049407568954L;
		public byte[][] JS;
		public Permutation perm;
		public StatementRanges[] statementranges;
		public byte[] symmap;

		@Override
		public void addArtifacts(Collection<? extends Artifact<?>> newArtifacts) {}
		@Override
		public ArtifactSet getArtifacts() {return null;}

		@Override
		public byte[][] getJs() {
			return JS;
		}

		@Override
		public Permutation getPermutation() {
			return perm;
		}

		@Override
		public byte[] getSerializedSymbolMap() {
			return symmap;
		}

		@Override
		public StatementRanges[] getStatementRanges() {
			return statementranges;
		}
		
	}
	
	
	/**
	 * Searches StringBuffer for all occurrences of search and replaces them
	 * with replace.
	 * 
	 * @param buf StringBuffer to search
	 * @param search Substring to search for
	 * @param replace Replacement String if search is found
	 * 
	 * @return True if substring search was found.
	 */
	protected static boolean replaceAll(StringBuffer buf, String search,
			String replace) {
		int len = search.length();
		boolean searchFound = false;
		for (int pos = buf.indexOf(search); pos >= 0; pos = buf.indexOf(search,
				pos + 1)) {
			buf.replace(pos, pos + len, replace);
			searchFound = true;
		}
		
		return searchFound;
	}
}
