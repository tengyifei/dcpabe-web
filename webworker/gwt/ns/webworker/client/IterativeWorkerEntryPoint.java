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

import com.google.gwt.user.client.Timer;

/**
 * A base class for Workers which need to repeatedly execute a method at a
 * certain (possibly variable) rate. This class terminates its own timer on
 * close.
 * 
 * TODO: generalize for more types of use
 */
public abstract class IterativeWorkerEntryPoint extends WorkerEntryPoint {
	private Timer t;
	private boolean terminate = false;

	// TODO: be able to restart timer?
	
	/**
	 * This method is called repeatedly until Worker is closed, terminated, or
	 * method returns a negative int. System will schedule (though not
	 * necessarily execute due to the nature of the event queue) the next call
	 * to execute() in the returned number of milliseconds.
	 * 
	 * <p>If a negative number is returned, execute() is not called again, but
	 * Worker still exists. Call {@link #close()} to shut down Worker.</p>
	 * 
	 * <p>Work done within execute() will not be performed in a Web Worker on
	 * platforms that don't offer that feature. Execution time should therefore
	 * be limited in duration so the application stays responsive.</p>
	 * 
	 * @return The number of milliseconds in which to call execute() again, a
	 * negative value if finished.
	 */
	public abstract int execute();
	
	@Override
	public void onModuleLoad() {
		super.onModuleLoad();

		// TODO: something more lightweight than Timer? what overhead
		// does it bring? (cancel() each time newly scheduled, etc)
		t = new Timer() {
			@Override
			public void run() {
				int newdelay = execute();
				
				// must be at least 1 for IE
				newdelay = (newdelay == 0) ? 1 : newdelay;
				
				if (!terminate && newdelay > 0) {
					t.schedule(newdelay);
				} // else finished
			}
		};
		t.schedule(1);
	}

	@Override
	protected void onModuleClose() {
		super.onModuleClose();

		terminate = true;
		t.cancel();
	}
}
