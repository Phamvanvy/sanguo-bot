package pip.gm.cmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import pip.gm.fw.AbstractClient;
import pip.gm.fw.Auth;
import pip.gm.fw.Command;
import pip.gm.fw.Controller;
import pip.gm.fw.IMessage;
/**
 * 通用的脚本命令
 */
public class CmdScript extends Command {
    public boolean exec(String cmd, AbstractClient aworld, String []s) throws Exception {
        if (s != null && s.length == 2) {
            if (cmd != null) {
                if (isCommand(aworld.auth, cmd)) {
                	aworld.onMessage(IMessage.MSG_TYPE_LOG, "Batch start：" + s[1], null);
                    execScript(aworld, aworld.con, s[1]);
                    aworld.onMessage(IMessage.MSG_TYPE_LOG, "Finished：" + s[1], null);
                }
            }
        }
        return false;
    }
    public void execScript(AbstractClient world, Controller controller, String file) {
        StringBuffer buf = new StringBuffer();
        File f = new File(file);
        if (f.exists() && !f.isDirectory() && f.canRead()) {
            FileInputStream fin = null;
            byte[]bb = new byte[1024];
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                fin = new FileInputStream(f);
                while (true) {
                    int k = fin.read(bb);
                    if (k < 0) {
                        break;
                    }
                    bout.write(bb, 0, k);
                }
            } catch (Exception ex) {
            } finally {
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException ex1) {
                    }
                }
            }
            bb = bout.toByteArray();
            try {
                bout.close();
            } catch (IOException ex2) {
            }
            String s = new String(bb);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\n' || c == '\r') {
                    processCommand(buf.toString().trim(), world, controller);
                    try{
                        //进行100ms的延时处理，以防止命令间隔时间果断造成处理丢失。
                        Thread.sleep(100);
                    }catch(Exception e){
                    }
                    buf.setLength(0);
                } else {
                    buf.append(c);
                }
            }
            processCommand(buf.toString().trim(), world, controller);
        } else {
            world.onMessage(IMessage.MSG_TYPE_SYSTEM, "File not exist:" + f.getAbsolutePath(), null);
        }

    }
    public void processCommand(String cmdLine, AbstractClient world, Controller controller) {
        if (cmdLine == null || cmdLine.trim().length() == 0 || cmdLine.startsWith("#")) {
            return;
        }
        world.onMessage(IMessage.MSG_TYPE_LOG, cmdLine, null);
        String s[] = pip.util.StringUtil.splitLines(cmdLine);
        controller.processCommand(s);
    }
    public long getAuth() {
    	return 0;
    }
    public String getCommand(Auth auth) {
        return "run";
    }
    public String getName(Auth auth) {
        return "Execute batch script";
    }
    public String getDescription(Auth auth) {
        return "  /run ＜script file＞  ";
    }

}
