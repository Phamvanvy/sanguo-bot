package com.pip.itimes.admin;



import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoSession;
import org.apache.mina.util.NewThreadExecutor;

import com.pip.itimes.net.Packet;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.Session;
import com.pip.itimes.net.SessionHandler;
import com.pip.itimes.net.SessionRegistry;
import com.pip.itimes.net.UWAPConnector;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;



/**
 * @author Jeffrey
 * @version 1.0
 */
public class Main extends JFrame implements KeyListener{

    private JTextArea taContent;
    private JTextField tfCommand;
    private ClientSession cSession;
    private String help = "无";
    private Vector<String> commandCache;

    private static final String[][] SERVERIP = {
			    	{
			            			"119.147.16.47", "8866", "QQ5区"
					},{
                                    "211.100.18.94", "8864", "测试服务器94"
                    }, {
                                    "192.168.0.54", "2264", "测试服务器54"
                    }, {
                                    "218.206.80.185", "8864", "正式服务器1区"
                    }, {
                                    "218.206.80.185", "8865", "正式服务器2区"
                    }, {
                                    "192.168.0.82", "8864", "leo的机器"
                    }, {
                                    "218.206.80.186", "9964", "测试服务器186"
                    }
    };

    private static final int SERVERID = 0;

    public Main(){
        super("iTimes Admin (" + SERVERIP[SERVERID][2] + " : " + SERVERIP[SERVERID][0] + ")");
        taContent = new JTextArea();
        tfCommand = new JTextField();
        taContent.setEditable(false);
        taContent.setWrapStyleWord(true);
        taContent.setLineWrap(true);
        tfCommand.addKeyListener(this);
        getContentPane().add(new JScrollPane(taContent), BorderLayout.CENTER);
        getContentPane().add(tfCommand, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        tfCommand.requestFocus();
        commandCache = new Vector<String>();
        setSize(800, 600);
        loadHelp();
        SessionRegistry registry = new SessionRegistry();
        IoConnector connector = new UWAPConnector(2, new NewThreadExecutor());
//        connector.connect(new InetSocketAddress("192.168.0.70", 6664), new ClientHandler(registry));
//        connector.connect(new InetSocketAddress("192.168.0.53", 6664), new ClientHandler(registry));
//        connector.connect(new InetSocketAddress("218.206.80.185", 8864), new ClientHandler(registry));
//        connector.connect(new InetSocketAddress("218.206.80.186", 8864), new ClientHandler(registry));
//        connector.connect(new InetSocketAddress("192.168.0.54", 2264), new ClientHandler(registry));
//        connector.connect(new InetSocketAddress("192.168.0.15", 8864), new ClientHandler(registry));
//         connector.connect(new InetSocketAddress("218.206.80.186", 9964), new ClientHandler(registry));

        connector.connect(new InetSocketAddress(SERVERIP[SERVERID][0], Integer.parseInt(SERVERIP[SERVERID][1])), new ClientHandler(registry));
//
    }

    private void loadHelp(){
        help = "login <name> <password> 登陆\n" +
               "show <playername>  显示玩家信息\n" +
               "who <mapId> 显示场景中的所有玩家\n" +
               "kick <playerId> 将玩家踢下线\n" +
               "online 查询在线玩家数量\n" +
               "mute <playerId> <time>  将玩家禁言 time的单位为秒\n" +
               "reload  重载关卡信息\n" +
               "shutdown 关闭服务器\n" +
               "modify <playerId> <property> <value> 修改玩家属性\n" +
               "delete <playerId> <itemId> <value> 删除玩家物品\n" +
               "add    <playerId> <itemId> <value> 增加玩家物品\n" +
               "systemsay  <destId> <value> <message>  发送信息{destId -1(世界) -2(场景) -6(圈) -7(系统)}\n" +
               "forbid <playerId>  封号 \n" +
               "battlefield <操作指令（start、stop）> <战场ID> <被踢出后多长时间不可进入战场（分钟）> <关闭报名时间（分钟）> <战场关闭时间（分钟）>\n" +
               "exp <当前级别> <升级到的级别>";
    }

    public static void main(String[] args){
        Main main = new Main();
        main.show();
    }

    public void keyTyped(KeyEvent e){
        String command = tfCommand.getText();
        
        if(command.length() != 0 && e.getKeyChar() == '\n'){
            tfCommand.setText("");
            if(command.equals("clear")){
                taContent.setText("");
            }else if(command.equals("help")){
                taContent.append(help);
            }
            else if(command.startsWith("batch")){
                taContent.setText("");
                sendBatchCommand(command.substring(6));
            }
            else if(command.startsWith("exp")){
                taContent.append(calculateExp(command));
            }else{
                sendCommand(command);
            }
            
            if(commandCache.contains(command)){
                commandCache.remove(command);
            }
            
            commandCache.add(command);
        }
    }

    public String calculateExp(String command){
        String tmp = command;

        int idx = tmp.indexOf(" ");
        tmp = tmp.substring(idx + 1);

        idx = tmp.indexOf(" ");
        int from = Integer.parseInt(tmp.substring(0, idx));
        tmp = tmp.substring(idx + 1);

        int end = Integer.parseInt(tmp);

        int a = 0;

        for(int i=from ;i<end;i++){
            a += EXP[i];
        }

        return "从" + from + "级" + "升级到" + end + "级" + "需要把经验设为" + a + "\n";
    }

    private void sendBatchCommand(String command){
        UWAPSegment seg = new UWAPSegment((byte)229);
        seg.writeString(command);
        cSession.write(seg);
    }

    private void sendCommand(String command){
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_COMMAND);
        seg.writeString(command);
        cSession.write(seg);
    }

    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() != KeyEvent.VK_DOWN && e.getKeyCode() != KeyEvent.VK_UP){
            return;
        }
        
        String currrentCommand = tfCommand.getText();
        
        if(e.getKeyCode() == KeyEvent.VK_DOWN){
            if(currrentCommand.trim().length() == 0){
                return;
            }else{
                boolean flag = true;
                
                for(int i = 0; i < commandCache.size() - 1; i++){
                    if(currrentCommand.equals(commandCache.elementAt(i))){
                        tfCommand.setText(commandCache.elementAt(i + 1));
                        flag = false;
                        
                        break;
                    }
                }
                
                if(flag){
                    tfCommand.setText("");
                }
            }
        }else if(e.getKeyCode() == KeyEvent.VK_UP){
            if(currrentCommand.trim().length() == 0){
                if(commandCache.size() > 0){
                    tfCommand.setText(commandCache.lastElement());
                }
                
                return;
            }else{
                for(int i = 1; i < commandCache.size(); i++){
                    if(currrentCommand.equals(commandCache.elementAt(i))){
                        tfCommand.setText(commandCache.elementAt(i - 1));
                        
                        break;
                    }
                }
            }
        }
    }

    public void keyReleased(KeyEvent e){
    }

    class ClientHandler extends SessionHandler{

        public ClientHandler(SessionRegistry registry){
            super(registry);
        }

        public Session createSession(IoSession session){
            cSession = new ClientSession(session);
            return cSession;
        }

    }

    class ClientSession extends Session{

        public ClientSession(IoSession session){
            super(session);
        }

        public void created(){

        }

        public void idle(IdleStatus status){

        }

        public void closed(){
            System.out.println("closed");
        }

        public void handle(Packet packet){
            UWAPData data = packet.datas[0];
            if(data.getAppType() == ServerConstants.ADMIN_COMMAND){
                try{
                    String s = data.readString();
                    taContent.append(s);
                }catch(IllegalAccessException ex){
                }
            }
        }

        public void opened(){
            System.out.println("opened");
        }

    }

    private static final int[] EXP ={
        10,
        24,
        62,
        136,
        258,
        440,
        694,
        1032,
        1466,
        2008,
        2136,
        2598,
        3081,
        3572,
        4055,
        5330,
        6884,
        8754,
        10981,
        13607,
        16530,
        20304,
        24342,
        27656,
        31258,
        35160,
        39374,
        43912,
        48786,
        54008,
        59590,
        65544,
        71882,
        78616,
        85758,
        93320,
        101314,
        109752,
        118646,
        128008,
        137850,
        148184,
        159022,
        170376,
        182258,
        194680,
        207654,
        221192,
        235306,
        250008,
        265310,
        281224,
        297762,
        314936,
        332758,
        351240,
        370394,
        390232,
        410766,
        432008,
        453970,
        517771,
        616745,
        724288,
        840792,
        966656,
        1102289,
        1248107,
        1404533,
        1572000,
        1750948,
        1941824,
        2145085,
        2361195,
        2590625,
        2833856,
        3091376,
        3363680,
        3651273,
        3954667,
        4274381,
        4610944,
        4964892,
        5336768,
        6299838,
        7363827,
        8535188,
        9820608,
        11227020,
        12761600,
        14431778,
        16245235,
        18209914,
        20334016,
        22626013,
        25094643,
        27748923,
        30598144,
        99999999,
        100000000
    };
}
