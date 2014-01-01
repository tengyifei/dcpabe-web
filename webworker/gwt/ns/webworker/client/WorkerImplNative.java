/*
 * Copyright 2009 Brendan Kenny
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

import org.vectomatic.arrays.ArrayBuffer;

import com.dcpabe.web.client.IntJS;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * An implementation of {@link Worker} for platforms supporting Web Workers.
 */
public class WorkerImplNative extends JavaScriptObject implements Worker {
	/**
	 * Create a Web Worker from the script located at the passed URL
	 * 
	 * @param url URL of insideWorker script, relative to calling script's URL
	 * @return The created insideWorker
	 */
	public static native WorkerImplNative create(String url) /*-{
		return new Worker("main/"+url);
  	}-*/;
	
	/**
	 * Takes care of reporting exceptions to the console in hosted mode.
	 * 
	 * @param listener the listener object to call back.
	 * @param port argument from the callback.
	 */
	private static void onErrorImpl(ErrorHandler errorHandler, ErrorEvent event) {
		UncaughtExceptionHandler ueh = GWT.getUncaughtExceptionHandler();
		if (ueh != null) {
			try {
				errorHandler.onError(event);
			} catch (Exception ex) {
				ueh.onUncaughtException(ex);
			}
		} else {
			errorHandler.onError(event);
		}
	}

	/**
	 * Takes care of reporting exceptions to the console in hosted mode.
	 * 
	 * @param listener the listener object to call back.
	 * @param port argument from the callback.
	 */
	private static void onMessageImpl(MessageHandler messageHandler,
			MessageEvent event) {
		UncaughtExceptionHandler ueh = GWT.getUncaughtExceptionHandler();
		if (ueh != null) {
			try {
				messageHandler.onMessage(event);
			} catch (Exception ex) {
				ueh.onUncaughtException(ex);
			}
		} else {
			messageHandler.onMessage(event);
		}
	}

	protected WorkerImplNative() {
		// constructors must be protected in JavaScriptObject overlays.
	}

	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.Worker#setErrorHandler(gwt.ns.webworker.client.ErrorHandler)
	 */
	public final native void setErrorHandler(ErrorHandler handler) /*-{
		this.onerror = function(event) {
			@gwt.ns.webworker.client.WorkerImplNative::onErrorImpl(Lgwt/ns/webworker/client/ErrorHandler;Lgwt/ns/webworker/client/ErrorEvent;)(handler, event);
		}
	}-*/;
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.Worker#setMessageHandler(gwt.ns.webworker.client.MessageHandler)
	 */
	public final native void setMessageHandler(MessageHandler messageHandler) /*-{
		this.onmessage = function(event) {
			@gwt.ns.webworker.client.WorkerImplNative::onMessageImpl(Lgwt/ns/webworker/client/MessageHandler;Lgwt/ns/webworker/client/MessageEvent;)(messageHandler, event);
		}
	}-*/;
	
	/* (non-Javadoc)
	 * @see gwt.ns.webworker.client.Worker#terminate()
	 */
	public final native void terminate() /*-{
		this.terminate();
	}-*/;

	public final native void postMessage(int type, String message) /*-{
		this.postMessage({ "data" : message, "prefix" : type, "kind" : 1});
	}-*/;
	
	public final native void postMessage(int type) /*-{
		this.postMessage({ "data" : "", "prefix" : type, "kind" : 1});
	}-*/;

	public final native void transferMessage(int prefix, ArrayBuffer message_array) /*-{
		this.postMessage({ "data" : message_array, "prefix" : prefix, "kind" : 0}, [message_array]);
	}-*/;

	public final native void postMessage(int prefix, ArrayBuffer message_array) /*-{
		this.postMessage({ "data" : message_array, "prefix" : prefix, "kind" : 0});
	}-*/;
	
}
