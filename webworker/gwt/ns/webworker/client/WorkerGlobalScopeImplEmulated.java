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

package gwt.ns.webworker.client;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * This class emulates the global scope of a Worker, including passing messages
 * to the outside context via a proxy. Additional logic is in
 * {@link WorkerImplProxy}. The split is necessitated (currently) by the fact
 * that the internal scope interface is largely the same as that of the outside
 * Worker handle. Wherever possible this class mimics native functionality,
 * with emulation work handled by the proxy.
 */
public class WorkerGlobalScopeImplEmulated implements WorkerGlobalScope {
	private MessageHandler insideMessageHandler;
	private WorkerImplProxy outsideProxy;
	
	// flag for terminated state
	private boolean terminated = false;
	
	public WorkerGlobalScopeImplEmulated(WorkerImplProxy proxy) {
		outsideProxy = proxy;
	}
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#close()
	 */
	@Override
	public void close() {
		// see WorkerImplProxy.terminate()
		if (!terminated) {
			terminated = true;
			
			if (outsideProxy != null)
				outsideProxy.terminate();
			
			// break references
			outsideProxy = null;
			insideMessageHandler = null;
		}
	}
	
	@Override
	public native WorkerLocation getLocation() /*-{
		return $wnd.location;
	}-*/;

	@Override
	public void importScript(String url) {
		// TODO: emulated importScripts
	}
	
	@Override
	public void postMessage(int prefix, String message) {
		if (terminated) // guard for termination
			return;
		
		// TODO: full emulation of MessageEvent
		MessageEvent event = MessageEvent.createEmulated(message);
		
		// pass to outside scope
		outsideProxy.onMessage(event);
	}

	@Override
	public void setErrorHandler(ErrorHandler handler) {
		// no-op unless reason to pass errors
	}

	@Override
	public void setMessageHandler(MessageHandler messageHandler) {
		if (!terminated)
			insideMessageHandler = messageHandler;
	}
	
	
	/**
	 * Pass a message into Worker scope.
	 * 
	 * @param message
	 */
	protected void onMessage(MessageEvent event) {
		if (terminated)	//guarded for termination
			return;
		
		if (insideMessageHandler != null)
			insideMessageHandler.onMessage(event);
	}

	@Override
	public void postMessage(int prefix, ArrayBuffer ab) {
		throw new IllegalArgumentException("Not supported yet!");
	}

	@Override
	public void transferMessage(int prefix, ArrayBuffer message_array) {
		throw new IllegalArgumentException("Not supported yet!");
	}

	@Override
	public void postMessage(int type) {
		throw new IllegalArgumentException("Not supported yet!");
	}
}
