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

import org.vectomatic.arrays.ArrayBuffer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * A class to act as proxy between the Worker interface and an emulated Web
 * Worker. Wherever possible, emulation logic happens here. For example,
 * messages passed between this class and the Worker's
 * {@link WorkerGlobalScopeImplEmulated} are placed on the event queue to
 * simulate the asynchronous Worker message posting process.
 */
public class WorkerImplProxy implements Worker {
	/**
	 * A command to pass a message received from the outside context to the
	 * worker's scope. Discarded if worker has been terminated.
	 */
	class PassMessageIn implements ScheduledCommand {
		MessageEvent message;
		
		public PassMessageIn(MessageEvent event) {
			message = event;
		}
		
		@Override
		public void execute() {
			if (!terminated) {
				workerScope.onMessage(message);
			}
		}
	}
	/**
	 * A command to pass a message from Worker's scope to the 
	 * outsideMessageHandler, if registered. Discarded if Worker terminated.
	 */
	class PassMessageOut implements ScheduledCommand {
		MessageEvent message;
		
		public PassMessageOut(MessageEvent event) {
			message = event;
		}
		
		@Override
		public void execute() {
			if (!terminated && outsideMessageHandler != null) {
				outsideMessageHandler.onMessage(message);
			}
		}
	}
	
	/**
	 * Asynchronously start the worker
	 */
	class RunWorkerCommand implements ScheduledCommand {
		@Override
		public void execute() {
			// it's unlikely, but since terminate() is sync, may be called by
			// the time this command is run (e.g. if UI events beat timeout
			// events).
			if (!terminated) {
				entryPoint.onModuleLoad();
				loaded = true;
			}
		}
	}
	/**
	 * Asynchronously call worker's close hook
	 */
	class CloseWorkerCommand implements ScheduledCommand {
		// intentionally keep reference
		WorkerEntryPoint entryPoint;
		
		public CloseWorkerCommand(WorkerEntryPoint entryPoint) {
			this.entryPoint = entryPoint;
		}

		@Override
		public void execute() {
			// called after run cmd, but don't run if never loaded
			if (loaded) {
				entryPoint.onModuleClose();
			}
		}
	}
	
	private WorkerEntryPoint entryPoint;
	private boolean loaded = false;
	private MessageHandler outsideMessageHandler;
	private boolean terminated = false;
	private WorkerGlobalScopeImplEmulated workerScope;
	
	/**
	 * Creates a running emulated Worker from entryPoint 
	 */
	public WorkerImplProxy(WorkerEntryPoint entryPoint) {
		workerScope = new WorkerGlobalScopeImplEmulated(this);
		this.entryPoint = entryPoint;
		
		// TODO: ugly. Workable for now but need to fix
		// TODO: **much more than ugly, makes all worker scope calls within
		//  emulated worker virtual in browsers that need most efficient impl**
		this.entryPoint.selfImpl = workerScope;
		
		// async start.
		// RunWorkerCommand enqueued before all others
		enqueueCommand(new RunWorkerCommand());
	}
	
	public void postMessage(String message) {
		if (terminated) // discard if terminated.
			return;
		
		// TODO: full emulation of MessageEvent
		MessageEvent event = MessageEvent.createEmulated(message);
		
		// queue a command to send event into worker scope
		PassMessageIn passInCmd = new PassMessageIn(event);
		enqueueCommand(passInCmd);
	}
	
	@Override
	public void setErrorHandler(ErrorHandler handler) {
		// no-op without reason to add error passing
		// TODO: is there a need for or even a source of error events?
	}

	@Override
	public void setMessageHandler(MessageHandler messageHandler) {
		if (!terminated)
			outsideMessageHandler = messageHandler;
	}
	
	@Override
	public void terminate() {
		/* 
		 * Attempt to halt worker.
		 * spec algorithms are slightly different for terminate() vs close()
		 * (immediate halt vs pause for possible task completion, then halt),
		 * but should be close enough for emulation.
		 * 
		 * Repeated calls have no effect.
		 */
		if (!terminated) {
			terminated = true;
			
			// shutdown scope
			// Note:humorous recursion here, but harmless since single threaded
			workerScope.close();
			enqueueCommand(new CloseWorkerCommand(entryPoint));
			
			// sever references
			workerScope = null;
			entryPoint = null;
			outsideMessageHandler = null;
		}
	}
	
	/**
	 * Receives message events from emulated Worker. Queue command to pass
	 * event to outside messageHandler.
	 */
	protected void onMessage(MessageEvent event) {
		if (terminated) // discard if terminated
			return;
		
		PassMessageOut passOutCmd = new PassMessageOut(event);
		enqueueCommand(passOutCmd);
	}
	
	/**
	 * Add a scheduled command to the end of the event queue.
	 * 
	 * TODO: how best to fit into GWT's event system?
	 * other command queues (eg scheduleDeferred) add way too much overhead
	 * for something called so often. performance is still poor in dev mode
	 * due to switch to/from JSNI, though.
	 * 
	 * @param cmd Command to put on event queue
	 */
	private void enqueueCommand(ScheduledCommand cmd) {
		if (GWT.isScript()) {
			nativeEnqueueCommand(cmd);
		} else {
			hostedEnqueueCommand(cmd);
		}
	}
	
	/**
	 * Put...somewhere for best hosted mode performance
	 */
	private void hostedEnqueueCommand(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
		//nativeEnqueueCommand(cmd); // TODO
	}
	
	/**
	 * Put at end of native event queue.
	 */
	private native void nativeEnqueueCommand(ScheduledCommand cmd) /*-{
		setTimeout(function(){
			cmd.@com.google.gwt.core.client.Scheduler.ScheduledCommand::execute()();
  		}, 1);
	}-*/;
	
	@Override
	public final void postMessage(int type, String message) {
		postMessage(((Integer)type).toString()+message);
	}

	@Override
	public void postMessage(int prefix, ArrayBuffer message_array) {
		throw new IllegalArgumentException("Not supported yet!!");
	}

	@Override
	public void postMessage(int prefix) {
		throw new IllegalArgumentException("Not supported yet!!");
	}

	@Override
	public void transferMessage(int prefix, ArrayBuffer ab) {
		throw new IllegalArgumentException("Not supported yet!!");
	}
}
