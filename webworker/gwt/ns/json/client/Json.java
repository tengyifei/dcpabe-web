/*
 * Copyright 2009 Brendan Kenny
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

package gwt.ns.json.client;

import gwt.ns.json.client.impl.JsonImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;

/**
 * A simple class to provide safe methods for serializing to and parsing from
 * JSON strings. Other, more full featured implementations exist
 * (<a href='http://code.google.com/p/gwt-rpc-plus/'>gwt-rpc-plus</a> and
 * perhaps {@link JsonUtils} in the future) but this has few moving parts and
 * relies only on the reference eval() implementation (and the many eyes on it)
 * to keep parsing safe.<br><br>
 * 
 * Defers to native browser implementation, if it exists, or Douglas
 * Crockford's json2.js, if not.<br><br>
 * 
 * TODO: optional replacer/reviver parameters? Native impl slightly less wide
 * spread (e.g. > FX 3.5.4), but emulated impl can cover it.
 */
public class Json {
	static final JsonImpl impl = GWT.create(JsonImpl.class);
	
	/**
	 * Serializes a {@link JavaScriptObject} to a JSON string. Optional
	 * parameters are omitted.
	 * 
	 * @param jso The object to serialize
	 * @return The JSON string
	 */
	public static final String strigify(JavaScriptObject jso) {
		return impl.strigify(jso);
	}
	
	/**
	 * This method parses a JSON string to produce a {@link JavaScriptObject}.
	 * Optional parameters are omitted.
	 * 
	 * @param <T> The type of JavaScriptObject that should be returned 
	 * @param json The JSON string to parse 
	 * @return The evaluated object
	 */
	public static final <T extends JavaScriptObject> T parse(String json) {
		return impl.parse(json);
	};
}
