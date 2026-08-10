package pip.gm.fw;

import java.util.*;

import cwu.util.sort.SortAgent;
import cwu.util.sort.*;
import pip.util.StringUtil;
/**
 * 异步执行命令的控制器.
 */
public class Controller implements Runnable, pip.util.ui.RichConsole.ConsoleActionListener {
	protected ArrayList<String[]> cmds = new ArrayList<String[]>();
	protected HashMap<String,Command> commandmaps = new HashMap<String,Command>();

	public static String helpMsg = "Usage: /help command [Display detail of command]\n";

	public AbstractClient world;
	private long lastCommandTime;  // 控制批量执行时的间隔

	public Controller(AbstractClient f) {
		world = f;
		new Thread(this).start();
	}
	public Command getCommand(String name) {
		return commandmaps.get(name);
	}
	public void run() {
		while (world.con == this) {
			String[] s = null;
			synchronized (cmds) {
				if (cmds.size() > 0) {
					s = cmds.remove(0);
				} else {
					try {
						cmds.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
			if (s != null) {
				if (lastCommandTime + 2000 > System.currentTimeMillis()) {
					try {
						Thread.sleep(lastCommandTime + 2000 - System.currentTimeMillis());
					} catch (Exception e) {
					}
				}
				execCommand(s);
				lastCommandTime = System.currentTimeMillis();
			}
		}
	}

	public void addCommand(String s) {
		try {
			Class kls = Class.forName(s);
			if (kls != null) {
				Command cmd = (Command) kls.newInstance();
				String command = cmd.getCommand(Auth.getRootAuth());
				if (command != null) {
					commandmaps.put(command, cmd);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void addCommand(Command cmd) {
		String command = cmd.getCommand(Auth.getRootAuth());
		if (command != null) {
			commandmaps.put(command, cmd);
		}
	}

	public void delCommand(String s) {
		commandmaps.remove(s);
	}

	public String getCommandList(Auth auth) {
		StringBuffer buf = new StringBuffer();
		for (Command cmd: commandmaps.values()) {
			String cmdId = cmd.getCommand(auth);
			String cmdName = cmd.getName(auth);
			if (cmdId != null) {
				buf.append("\n <action title=\"Copy ");
				buf.append(StringUtil.formal(cmdName));
				buf.append(" Template\" command=\"set inputText /");
				buf.append(cmdId);
				buf.append(" \">/");
				int cl = cmdId.length();
				buf.append(cmdId);
				if (cl < 10) {
					buf.append("          ".substring(cl));
				}
				buf.append(" :");
				buf.append(StringUtil.formal(cmdName));
				long k = cmd.getAuth();
				if (k != 0) {
					buf.append(" (").append(Auth.getAuth(k).trim()).append(")");
				}
				buf.append("</action>");
			}
		}
		return buf.toString();
	}
	protected void help(String[] s) {
		StringBuffer buf = new StringBuffer();
		buf.append(helpMsg);
		HashMap<String,Command> cmdHelp = new HashMap<String,Command>();
		if (s.length == 1) {
			for (Command cmd: commandmaps.values()) {
				String cmdName = cmd.getCommand(world.auth);
				if (cmdName != null) {
					cmdHelp.put(cmdName, cmd);
				}
			}
		} else {
			String inputCmd = s[1];
			Command oneCommand = commandmaps.get(inputCmd);
			if (oneCommand != null) {
				cmdHelp.put(inputCmd, oneCommand);
			} else {
				for (Command cmd: commandmaps.values()) {
					String cmdName = cmd.getCommand(world.auth);
					if (cmdName != null && cmdName.startsWith(inputCmd)) {
						cmdHelp.put(cmdName, cmd);
					}
				}
			}
		}
		if (cmdHelp.size() == 0) {
			buf.append("No matching commands.");
		} else if (cmdHelp.size() == 1) {
			String cmdName = cmdHelp.keySet().iterator().next();
			Command cmd = cmdHelp.get(cmdName);
			buf.append("\n <action title=\"Copy ");
			buf.append(StringUtil.formal(cmd.getName(world.auth)));
			buf.append(" Template\" command=\"set inputText /");
			buf.append(cmd.getCommand(world.auth));
			buf.append(" \">");
			buf.append(cmd.getCommand(world.auth));
			buf.append("    ");
			buf.append(StringUtil.formal(cmd.getName(world.auth)));
			buf.append("\n");
			buf.append(cmd.getDescription(world.auth));
			buf.append("</action>");
		} else {
			buf.append("Command:");
			String []cmds = new String[cmdHelp.keySet().size()];
			cmdHelp.keySet().toArray(cmds);
			SortAgent.sort(cmds, new CompareAgent(){
				  public int compare(java.lang.Object arg0, java.lang.Object arg1) {
					  return ((String)arg0).compareTo((String)arg1);
				  }
			}, 0);
			for (String cmdName : cmds) {
				Command cmd = cmdHelp.get(cmdName);
				buf.append("\n <action title=\"Copy ");
				buf.append(StringUtil.formal(cmdName));
				buf.append(" Template\" command=\"set inputText /");
				String aS = cmd.getCommand(world.auth);
				buf.append(aS);
				buf.append(" \">/");
				int cl = aS.length();
				buf.append(aS);
				if (cl < 10) {
					buf.append("          ".substring(cl));
				}
				buf.append(" :");
				buf.append(StringUtil.formal(cmd.getName(world.auth)));
				buf.append("</action> ");
				if (world.auth.hasAuth(AuthConstants.root)) {
					long k = cmd.getAuth();
					if (k != 0) {
						buf.append(" (").append(Auth.getAuth(k).trim()).append(")");
					}
				}
			}
		}
		world.onMessage(IMessage.MSG_TYPE_COMMAND, buf.toString(), null); // TODO 加入命令
	}

	/** 添加待执行命令,排队执行 */
	public void processCommand(String[] s) {
		if (s != null && s.length > 0) {
			synchronized (cmds) {
				cmds.add(s);
				cmds.notifyAll();
			}
		}
	}
	/** 添加待执行命令,排队执行 */
	public void processCommand(String s) {
		if (s != null) {
		    processCommand(pip.util.StringUtil.splitLines(s));
		}
	}

	/** 立即执行命令 */
	public void execCommand(String s[]) {
		if (s != null && s.length > 0) {
			String cmd = s[0];
			if (cmd != null) {
				cmd = cmd.trim().toLowerCase();
				if (cmd.length() > 0) {
					if ("help".equals(cmd)) {
						help(s);
					} else {
						Command oneCommand = commandmaps.get(cmd);
						try {
							if (oneCommand == null) {
								world.onMessage(IMessage.MSG_TYPE_LOG, "Unkown command " + cmd + " or invalid argument.", null);
							} else if (oneCommand.exec(cmd, world, s)) {
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
							world.onMessage(IMessage.MSG_TYPE_SYSTEM, "Error：" + e.getMessage(), null);
						}
						world.onMessage(IMessage.MSG_TYPE_SYSTEM, "Warning：command is not handled completely.", null);
					}
				}
			}
		}
	}
}
