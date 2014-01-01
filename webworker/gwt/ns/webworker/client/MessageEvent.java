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

// This code originated in part from code from SpeedTracer, r3
// http://code.google.com/p/speedtracer/source/detail?r=3

package gwt.ns.webworker.client;

import org.vectomatic.arrays.ArrayBuffer;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * An event used for message passing with a Web Worker. Contents of message
 * can be accessed by {@link #getData()}.
 */
public class MessageEvent extends JavaScriptObject {
	/**
	 * Create a simple emulated "MessageEvent" that will work with this
	 * overlay.
	 * 
	 * TODO: need full emulated implementation of MessageEvent
	 * 
	 * @param message Payload of event
	 * @return New emulated MessageEvent
	 */
	public final static native MessageEvent createEmulated(String message) /*-{
		return { "data" : message };
	}-*/;
	
	protected MessageEvent() {
		// required protected constructor for JavaScriptObject
	}
	
	/**
	 * @see {@link Worker#postMessage(String)}
	 * 
	 * @return The passed message
	 */
	public final native String getData() /*-{
		return this.data.data;
	}-*/;
	
	public final native ArrayBuffer getData_array() /*-{
		return this.data.data;
	}-*/;

	public final native String getLastEventId() /*-{
		return this.lastEventId;
	}-*/;

	public final native String getOrigin() /*-{
		return this.origin;
	}-*/;

	public final native String getSource() /*-{
		return this.source;
	}-*/;

	public final native int getPrefix() /*-{
		return this.data.prefix;
	}-*/;
	
	public final native int getKind() /*-{
		return this.data.kind;
	}-*/;
}