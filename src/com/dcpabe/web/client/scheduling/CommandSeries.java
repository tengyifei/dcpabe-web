package com.dcpabe.web.client.scheduling;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class CommandSeries {
	boolean running = false;
	InternalCommandChain command;
	InternalCommandChain command_end;
	
	public void appendCommand(boolean repeat, Object cmd){
		InternalCommandChain tmp = new InternalCommandChain(repeat);
		if (repeat)
			tmp.cmd_repeat = (RepeatingCommand) cmd;
		else
			tmp.cmd_normal = (ScheduledCommand) cmd;
		
		if (command==null){
			command = tmp;
			command_end = command;
		}else{
			command_end.next = tmp;
			command_end = command_end.next;
		}
	}
	
	public boolean executeCommands(){
		if (running || (command==null)) return false;
		running = true;
		
		if (command.repeat){
			CustomRepeatingCommand repeatcmd = new CustomRepeatingCommand();
			repeatcmd.cmd=command;
			Scheduler.get().scheduleIncremental(repeatcmd);
		}else{
			CustomScheduledCommand normalcmd = new CustomScheduledCommand();
			normalcmd.cmd=command;
			Scheduler.get().scheduleDeferred(normalcmd);
		}
		return true;
	}
	
	public class InternalCommandChain {
		public RepeatingCommand cmd_repeat;
		public ScheduledCommand cmd_normal;
		public final boolean repeat;
		public InternalCommandChain(boolean repeat){
			this.repeat = repeat;
		}
		public InternalCommandChain next=null;
	}
	
	public abstract class CustomCommandBase{
		public InternalCommandChain cmd;
		
		void goNext(){
			if (cmd.next!=null){
				if (cmd.next.repeat){
					CustomRepeatingCommand tmp = new CustomRepeatingCommand();
					tmp.cmd = cmd.next;
					Scheduler.get().scheduleIncremental(tmp);
				}else{
					CustomScheduledCommand tmp = new CustomScheduledCommand();
					tmp.cmd = cmd.next;
					Scheduler.get().scheduleDeferred(tmp);
				}
			}else{
				running = false;
			}
		}
	}
	
	public class CustomScheduledCommand extends CustomCommandBase implements ScheduledCommand{
		@Override
		public void execute() {
			cmd.cmd_normal.execute();
			goNext();
		}
	}
	
	public class CustomRepeatingCommand extends CustomCommandBase implements RepeatingCommand{
		@Override
		public boolean execute() {
			boolean more = cmd.cmd_repeat.execute();
			if (more)	
				return true;
			else{
				goNext();
				return false;
			}
		}
	}
}
