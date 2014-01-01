/*
 * Copyright 2010 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gwt.ns.webworker.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * Extend this class to serve as the entry point of a Worker module. On
 * platforms which support the functionality, the module will be compiled into a
 * native Worker script, while the rest will fall back to an asynchronous
 * emulation of the same.
 * 
 * <p>See documentation for more information.<p>
 * 
 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/'>Current WHATWG Web Worker Draft Spec</a>
 */
public abstract class WorkerEntryPoint implements EntryPoint {
	WorkerGlobalScope selfImpl = new WorkerGlobalScopeImplNative();
	
	/**
	 * @see {@link WorkerGlobalScope#close()}
	 */
	public final void close() {
		getGlobalScope().close();
	}
	
	/**
	 * Retrieve a reference to the global scope of this Worker.
	 * 
	 * @return A reference to the global scope.
	 */
	public WorkerGlobalScope getGlobalScope() {
		return selfImpl;
	}
	
	/**
	 * @see {@link WorkerGlobalScope#getLocation()}
	 */
	public final WorkerLocation getLocation() {
		return getGlobalScope().getLocation();
	}
	
	/**
	 * @see {@link WorkerGlobalScope#importScript(String)}
	 */
	public final void importScript(String url) {
		getGlobalScope().importScript(url);
	}
	
	@Override
	public void onModuleLoad() {
		// base WorkerEntryPoint just passes startup to subclass
		onWorkerLoad();
	}
	
	/**
	 * Hook to allow emulated Workers to shutdown gracefully. Message passing
	 * has already been shutdown and previous events discarded.
	 */
	protected void onModuleClose() {
		onWorkerClose();
	}
	
	/**
	 * The Worker entry point method, called automatically by loading a module
	 * which contains a subclass of a Worker entry point. Subclasses will
	 * generally implement this method (not override {@link #onModuleLoad()}).
	 */
	public abstract void onWorkerLoad();
	
	/* TODO: the ways in which an emulated worker can linger need to be
	 * enumerated and either guarded or (as a last resort) the user warned.
	 * this seems closely aligned with detecting and denying DOM use within
	 * Workers
	 */	
	/**
	 * Called when this Worker has {@link #close()}ed itself or has been
	 * terminated from the calling context. An emulated Worker cannot be
	 * manually shutdown and garbage collected if references to it remain, so
	 * subclasses should implement this method to remove any running
	 * machinery (e.g. timer, xhr) which would leak memory and CPU cycles.
	 * 
	 * <p>Note that by the time this method is called,
	 * {@link #postMessage(String)} will be a no-op and all still-queued
	 * messages will have been discarded, so there is no (standard-compliant)
	 * way of communicating the results of additional work. This is consistent
	 * with the behavior of native Workers.<p>
	 */
	public abstract void onWorkerClose();
	
	/**
	 * @see {@link WorkerGlobalScope#postMessage(String)}
	 */
	public final void postMessage(Integer prefix, String message) {
		getGlobalScope().postMessage(prefix, message);
	}
	
	public final void postMessage(Integer prefix, ArrayBuffer ab){
		getGlobalScope().postMessage(prefix, ab);
	}
	
	public final void transferMessage(Integer prefix, ArrayBuffer ab){
		getGlobalScope().transferMessage(prefix, ab);
	}

	/**
	 * @see {@link WorkerGlobalScope#setErrorHandler(ErrorHandler)}
	 */
	protected final void setErrorHandler(ErrorHandler errorHandler) {
		getGlobalScope().setErrorHandler(errorHandler);
	}
	
	/**
	 * @see {@link WorkerGlobalScope#setMessageHandler(MessageHandler)}
	 */
	protected final void setMessageHandler(MessageHandler messageHandler) {
		getGlobalScope().setMessageHandler(messageHandler);
	}
}
