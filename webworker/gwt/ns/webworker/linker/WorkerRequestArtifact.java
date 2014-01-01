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

import java.util.HashMap;

import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.impl.StandardLinkerContext;

/**
 * A request for the compilation of a Worker module. Contains the logic used to
 * determine the proper output directory for the resulting script and the
 * context-dependent relative URL to locate it.
 */
public class WorkerRequestArtifact extends Artifact<WorkerRequestArtifact> {
	private static final long serialVersionUID = 6156556088389056672L;
	
	/**
	 * The file extension of worker scripts
	 */
	public static final String WORKER_EXTENSION = ".cache.js";
	/**
	 * The root subdirectory of worker scripts within compilation output
	 */
	public static final String WORKER_SUBDIR = "workerjs";
	/**
	 * The normal URL path-element separator: "/"
	 */
	public static final String URL_SEPARATOR = "/";
	
	private static final String PLACEHOLDER_STRONG_NAME = "___WORKERSTRONGNAMEPLACEHOLDE___";
	private static final int STRONG_NAME_LEN = 32;
	
	protected String canonicalName;
	protected String shortName;
	
	public HashMap<String, String> perm_map_filename;
	
	/**
	 * @return A placeholder strong name for search and replace in compiled js 
	 */
	public static String getPlaceholderStrongName() {
		// protect against what would be a subtle bug
		assert (PLACEHOLDER_STRONG_NAME.length() == STRONG_NAME_LEN)
				: "Placeholder String length altered. Length: "
					+ PLACEHOLDER_STRONG_NAME.length();
		
		return PLACEHOLDER_STRONG_NAME;
	}
	
	
	// TODO: better nomenclature than "canonical name" and "name"
	public WorkerRequestArtifact(String moduleCanonicalName, String moduleName) {
		super(StandardLinkerContext.class);	// TODO: is this correct?
		
		canonicalName = moduleCanonicalName;
		shortName = moduleName;
	}

	/**
	 * @return The full name of the module file to be compiled into a Worker
	 */
	public String getCanonicalName() {
		return canonicalName;
	}
	
	/**
	 * @return The name of the module to be compiled (possibly overridden by,
	 *   e.g. a rename-to rule in the module file)
	 */
	public String getName() {
		return shortName;
	}
	
	/**
	 * Convenience method. Returns
	 * {@link #getRelativePath(String, String)} based on
	 * {@link #getPlaceholderStrongName()} and {@link #URL_SEPARATOR}.
	 * 
	 * @return A URL suitable for use as a placeholder.
	 */
	public String getRelativePlaceholderUrl() {
		return getRelativePath(getPlaceholderStrongName(), URL_SEPARATOR);
	}
	
	
	/**
	 * Returns a recursion-level-appropriate relative path to the Worker script
	 * which will result from this request. 
	 * 
	 * @param strongName Cache-guarding strong subdirectory name
	 * @param separator Name-separator character (e.g. {@link #URL_SEPARATOR})
	 * @return Path to Worker script, relative to script being compiled
	 * 
	 * @see {@link #getRelativeDirectoryPath(String, String)}
	 * @see {@link #getScriptFileName()}
	 */
	public String getRelativePath(String strongName, String separator) {
		String path = getRelativeDirectoryPath(strongName, separator);
		if (!path.isEmpty())
			path += separator;
		path += getScriptFileName();
		
		return path;
	}
	
	/**
	 * Returns a recursion-level-appropriate relative path to eventual worker
	 * directory. For the primary EntryPoint, it includes the workerjs and
	 * strongName subdirectories. For workers created within
	 * workers (i.e. within a recursive compile), the requested Worker will be
	 * in the same directory, so the directory path is an empty string.
	 * 
	 * @param strongName Cache-guarding strong subdirectory name
	 * @param separator Name-separator character (e.g. {@link #URL_SEPARATOR})
	 * @return Path to Worker script dir, relative to script being compiled
	 * 
	 * @see {@link #getPlaceholderStrongName()} for a placeholder strong name
	 * @see {@link #URL_SEPARATOR} for the normal URL separator
	 */
	public String getRelativeDirectoryPath(String strongName, String separator) {
		String path = "";
		
		if (!WorkerCompiler.isRecursed()) {
			path = WORKER_SUBDIR + separator + strongName;
		}
		
		return path;
	}
	
	/**
	 * @return The name of the Worker javascript file to be created
	 */
	public String getScriptFileName() {
		return getName() + WORKER_EXTENSION;
	}
	
	@Override
	public int hashCode() {
		// the same module should result in a single Worker request
		return getCanonicalName().hashCode();
	}
	
	@Override
	protected int compareToComparableArtifact(WorkerRequestArtifact o) {
		return getCanonicalName().compareTo(o.getCanonicalName());
	}

	@Override
	protected Class<WorkerRequestArtifact> getComparableArtifactType() {
		return WorkerRequestArtifact.class;
	}


	public String getRelativePath_Strong(String scriptDirStrongName,
			String separator, String real_name) {
		String path = getRelativeDirectoryPath(scriptDirStrongName, separator);
		if (!path.isEmpty())
			path += separator;
		path += real_name;
		
		return path;
	}

}
