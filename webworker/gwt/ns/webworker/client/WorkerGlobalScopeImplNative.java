/*
 * Copyright 2010 Brendan Kenny
 * Copyright 2009 Google Inc.
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

// This code is largely adapted from code from SpeedTracer, r3
// http://code.google.com/p/speedtracer/source/detail?r=3

package gwt.ns.webworker.client;

import com.dcpabe.web.client.IntJS;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * Wrapper for WorkerGlobalScope for platforms that natively support Workers.
 */
public class WorkerGlobalScopeImplNative implements WorkerGlobalScope {
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#close()
	 */
	public final native void close() /*-{
		$self.close();
	}-*/;
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#getLocation()
	 */
	public final native WorkerLocation getLocation() /*-{
		return $self.location;
	}-*/;

	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#importScript(java.lang.String)
	 */
	public final native void importScript(String url) /*-{
		$self.importScripts(url);
	}-*/;

	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#importScripts(com.google.gwt.core.client.JsArrayString)
	 */
	public final native void importScripts(JsArrayString urls) /*-{
		$self.importScripts.apply(null, urls);
	}-*/;

	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#importScripts(java.lang.String[])
	 */
	public final void importScripts(String[] urls) {
		JsArrayString jsUrls = JsArrayString.createArray().cast();
		for (int i = 0, l = urls.length; i < l; ++i) {
			jsUrls.set(i, urls[i]);
		}
		importScripts(jsUrls);
	}
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#postMessage(java.lang.String)
	 */
	public final native void postMessage(String message) /*-{
		$self.postMessage(message);
	}-*/;

	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#setErrorHandler(gwt.ns.webworker.client.ErrorHandler)
	 */
	// Note: no UncaughtExceptionHandler since this code will never run in dev mode
	public final native void setErrorHandler(ErrorHandler handler) /*-{
		$self.onerror = function(event) {
			handler.@gwt.ns.webworker.client.ErrorHandler::onError(Lgwt/ns/webworker/client/ErrorEvent;)(event);
		}
	}-*/;
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.WorkerGlobalScope#setMessageHandler(gwt.ns.webworker.client.MessageHandler)
	 */
	// Note: no UncaughtExceptionHandler since this code will never run in dev mode
	public final native void setMessageHandler(MessageHandler messageHandler) /*-{
	    $self.onmessage = function(event) {
			messageHandler.@gwt.ns.webworker.client.MessageHandler::onMessage(Lgwt/ns/webworker/client/MessageEvent;)(event);
		}
	}-*/;
	
	public final native void postMessage(int type, String message) /*-{
		$self.postMessage({ "data" : message , "prefix" : type, "kind" : 1});
	}-*/;
	
	public final native void postMessage(int type) /*-{
		$self.postMessage({ "data" : "" , "prefix" : type, "kind" : 1});
	}-*/;

	public final native void transferMessage(int prefix, ArrayBuffer message_array) /*-{
		$self.postMessage({ "data" : message_array , "prefix" : prefix, "kind" : 0}, [message_array]);
	}-*/;

	public final native void postMessage(int prefix, ArrayBuffer message_array) /*-{
		$self.postMessage({ "data" : message_array , "prefix" : prefix, "kind" : 0});
	}-*/;
	
}
