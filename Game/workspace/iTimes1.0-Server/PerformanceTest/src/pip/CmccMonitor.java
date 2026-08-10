package pip;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import com.pip.wulin.server.io.UWAPApp;
import com.pip.wulin.server.io.UWAPConnection;
import com.pip.wulin.server.io.UWAPData;
import com.pip.wulin.server.io.UWAPDataListener;
import com.pip.wulin.server.io.UWAPSegment;
import com.pip.wulin.server.io.UWAPSocketSConnection;


public class CmccMonitor extends Thread implements UWAPDataListener, UWAPApp{
    UWAPConnection connection;
    boolean connected;
    boolean logined;
    boolean userLogined;
    boolean error;

    public void registerSession(UWAPConnection conn){
        connection = conn;
        connection.addDataListener(this);
        connected = true;
    }

    public boolean onGotData(UWAPConnection conn, UWAPData data[], int serialNum, int requestId) throws Exception{
        for(int i = 0; i < data.length; i++)
            process(data[i]);

        return false;
    }

    private void process(UWAPData data) throws Exception{
        switch(data.getAppType()){
            case 78:
                logined = true;
                System.out.println("登录成功");
                break;
            case -1:
                data.readByte();
                String str = data.readString();
                System.out.println(str);
                if(str.equals("已经到达最大登录数量")){
                    userLogined = true;
                    break;
                }
                error = true;
                break;
            case 8:
                userLogined = true;
                System.out.println("登录用户成功");
                break;
        }
    }

    public void onSignal(UWAPConnection uwapconnection, int i, String s){
    }

    public void run(){
        boolean noError = true;
        while(true){
            System.out.println(new Date().toString());
            try{
                checkDispatcherThreeTimes();
                checkServerThreeTimes("221.179.216.49", 29999, 101);
                System.out.println("服务器全部正常");
                System.out.println();

                if(!noError){
                    noError = true;
                    sendSMS2("13264355981", "服务器恢复正常。");
                    sendSMS2("13501105162", "服务器恢复正常。");
                    sendSMS2("13910564907", "服务器恢复正常。");
                    sendSMS2("13910193521", "服务器恢复正常。");
                    sendSMS2("13701304464", "服务器恢复正常。");
                }
            }catch(Exception e){
                e.printStackTrace();
                if(noError){
                    if(e instanceof MonitorException){
                        sendSMS2("13264355981", e.getMessage());
                        sendSMS2("13501105162", e.getMessage());
                        sendSMS2("13910564907", e.getMessage());
                        sendSMS2("13910193521", e.getMessage());
                        sendSMS2("13701304464", e.getMessage());
                    }else{
                        sendSMS2("13264355981", "服务器状态异常，原因未知。");
                        sendSMS2("13501105162", "服务器状态异常，原因未知。");
                        sendSMS2("13910564907", "服务器状态异常，原因未知。");
                        sendSMS2("13910193521", "服务器状态异常，原因未知。");
                        sendSMS2("13701304464", "服务器状态异常，原因未知。");
                    }
                    noError = false;
                }
            }
            try{
                Thread.sleep(120000L);
            }catch(Exception e){
            }
        }
    }

    private void checkDispatcherThreeTimes() throws Exception{
        int remainRetry = 3;
        while(true){
            try{
                checkDispatcher();
                break;
            }catch(Exception e){
                e.printStackTrace();
                if(remainRetry > 0){
                    remainRetry--;
                    System.out.println("重试第" + (3 - remainRetry) + "次");
                    try{
                        Thread.sleep(3000);
                    }catch(Exception e1){
                    }
                }else{
                    throw e;
                }
            }
        }
    }

    private void checkServerThreeTimes(String host, int port, int num) throws MonitorException{
        int remainRetry = 3;
        while(true){
            try{
                checkServer(host, port, "test" + num, "cmcc");
                break;
            }catch(MonitorException e){
                e.printStackTrace();
                if(remainRetry > 0){
                    remainRetry--;
                    System.out.println("重试第" + (3 - remainRetry) + "次");
                    try{
                        Thread.sleep(3000);
                    }catch(Exception e1){
                    }
                    num += 20;
                }else{
                    throw e;
                }
            }
        }
    }

    private void checkServer(String host, int port, String user, String pass) throws MonitorException{
        try{
            System.out.println("测试：" + host + ":" + port);

            connection = null;
            connected = false;
            logined = false;
            userLogined = false;
            error = false;
            long startTime = System.currentTimeMillis();

            // 建立连接
            Socket socket = new Socket(host, port);
            new UWAPSocketSConnection(socket, this);
            while(!connected){
                Thread.sleep(100);
                if(System.currentTimeMillis() > startTime + 60000L){
                    throw new Exception();
                }
            }

            // 登录
            UWAPSegment seg = new UWAPSegment(77);
            seg.write(user);
            seg.write(pass);
            seg.write("NK-6600");
            seg.write("1.22.0-15005000");
            connection.write(seg, -1);
            while(!logined){
                Thread.sleep(100);
                if(System.currentTimeMillis() > startTime + 60000L){
                    throw new Exception();
                }
            }

            // 登录用户
            seg = new UWAPSegment(7);
            seg.write(user);
            connection.write(seg, -1);
            while(!userLogined){
                Thread.sleep(100);
                if(System.currentTimeMillis() > startTime + 60000L){
                    throw new Exception();
                }
            }
        }catch(Exception e){
            if(e instanceof MonitorException){
                throw (MonitorException)e;
            }
            throw new MonitorException(host + ":" + port + "测试失败");
        }finally{
            // 关闭连接
            if(connection != null){
                connection.close();
            }
        }
    }

    private void sendSMS(String phone, String msg){
        msg = "幻想监控:" + msg;
        HttpURLConnection conn = null;
        try{
            URL url = new URL("http://mbox.sina.com.cn/wll/recommend.php?mobile=" + phone + "&title=" + URLEncoder.encode(msg, "GBK") + "&url=" + URLEncoder.encode("wap.pipfit.com", "GBK"));
            conn = (HttpURLConnection)url.openConnection();
            conn.getResponseCode();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(conn != null){
                    conn.disconnect();
                }
            }catch(Exception e){
            }
        }
    }

    private void sendSMS2(String phone, String msg){
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd-HH:mm");
        msg = "移动监控[" + df.format(new Date()) + "]:" + msg;
        HttpURLConnection conn = null;
        try{
            URL url = new URL("http://211.144.155.130/smsclientinterface/send.asp");
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=GBK");
            String postContent = "circle=zsmz&pwd=123456&mobile=" + phone + "&message=" + URLEncoder.encode(msg, "GBK");
            ;
            OutputStream os = conn.getOutputStream();
            os.write(postContent.getBytes("GBK"));
            os.close();
            conn.getResponseCode();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(conn != null){
                    conn.disconnect();
                }
            }catch(Exception e){
            }
        }
    }

    private void checkDispatcher() throws MonitorException, IOException{
        HttpURLConnection conn = null;
        InputStream is = null;
        try{
            URL url = new URL("http://221.179.216.49:30002/itimesipd/serverlist?type=all&format=short");
            conn = (HttpURLConnection)url.openConnection();
            conn.getResponseCode();
            is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String[] serverNames = new String[]{
                            "布莱尼斯1线"
            };
            boolean[] flags = new boolean[serverNames.length];
            String line;
            while((line = br.readLine()) != null){
                for(int i = 0; i < serverNames.length; i++){
                    if(line.indexOf(serverNames[i]) >= 0){
                        flags[i] = true;
                        break;
                    }
                }
            }
            for(int i = 0; i < serverNames.length; i++){
                if(!flags[i]){
                    throw new MonitorException(serverNames[i] + "从分配器消失");
                }
            }
        }catch(MonitorException me){
            throw me;
        }catch(Exception e){
            throw new MonitorException("访问分配器失败");
        }finally{
            if(is != null){
                is.close();
            }
            if(conn != null){
                conn.disconnect();
            }
        }
    }
}
