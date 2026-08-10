import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Main implements Runnable{
    public static final String[][] GAMES = new String[][]{
                    {
                                    "明珠三国", "http://218.206.80.185:7070/sanguoipd/serverlist?type=all&format=stat"
                    }, {
                                    "幻想i时代", "http://218.206.80.185:7070/itimesipd2/serverlist?type=all&format=stat"
                    }, {
                                    "武林擂2", "http://218.206.80.185:7070/wulin2ipd/serverlist?type=all&format=stat"
                    }, {
                                    "武林OL", "http://218.206.80.185:7070/wulin2ipd2/serverlist?type=all&format=stat"
                    }, {
                                    "QQ幻想", "http://117.135.128.217:8080/qqitimesipd/serverlist?type=all&format=stat"
                    }, {
                                    "卓望幻想", "http://221.179.216.49:8872/itimesipd/serverlist?type=all&format=stat"
                    }, {
                                    "卓望武林", "http://221.179.216.49:8872/wulin25ipd/serverlist?type=all&format=stat"
                    }, {
                                    "明珠西游", "http://218.206.80.185:7070/xiyouipd/serverlist?type=all&format=stat"
                    }, {
                                    "三国越南", "http://210.211.99.54:8080/sanguoipd/serverlist?type=all&format=stat"
                    }, {
                                    "明珠轩辕", "http://218.206.80.185:7070/xuanyuanipd/serverlist?type=all&format=stat"
                    }
    };

    public static long[][] game_count = new long[GAMES.length][4];
    public static List<HashMap<String, Integer>> game_server_count;
    public static List<HashMap<String, Integer>> game_server_count_old;

    public static void main(String[] args){
        game_server_count = new ArrayList<HashMap<String, Integer>>();
        game_server_count_old = new ArrayList<HashMap<String, Integer>>();

        for(int i = 0; i < GAMES.length; i++){
            game_server_count.add(new HashMap<String, Integer>());
            game_server_count_old.add(new HashMap<String, Integer>());
        }

        new Thread(new Main()).start();
    }

    public void run(){
        long todayStart = getTodayStart();
        int pipMaxCount = 0;
        int pipMaxCountOld = 0;
        long pipMaxCountTime = 0;
        long pipMaxCountOldTime = 0;

        for(int i = 0; i < game_count.length; i++){
            game_count[i][0] = 0;
            game_count[i][1] = 0;
            game_count[i][2] = 0;
            game_count[i][3] = 0;
        }

        while(true){
            try{
                if(getTodayStart() - todayStart >= 3600 * 24 * 1000){
                    pipMaxCountOld = pipMaxCount;
                    pipMaxCountOldTime = pipMaxCountTime;
                    pipMaxCount = 0;
                    pipMaxCountTime = 0;

                    for(int i = 0; i < game_count.length; i++){
                        game_count[i][1] = game_count[i][0];
                        game_count[i][0] = 0;
                        game_count[i][3] = game_count[i][2];
                        game_count[i][2] = 0;
                    }

                    for(int i = 0; i < game_server_count.size(); i++){
                        game_server_count_old.set(i, game_server_count.get(i));
                        game_server_count.set(i, new HashMap<String, Integer>());
                    }

                    todayStart = getTodayStart();
                }

                HttpURLConnection conn = null;
                int pipCount = 0;

                System.out.println();
                System.out.println();
                System.out.println("统计时间：" + getTimeStr(System.currentTimeMillis()));

                for(int i = 0; i < GAMES.length; i++){
                    try{
                        conn = (HttpURLConnection) (new URL(GAMES[i][1])).openConnection();
                        conn.setConnectTimeout(10000);
                        conn.setReadTimeout(10000);
                        String s = null;

                        if(conn.getResponseCode() == 200){
                            DataInputStream in = new DataInputStream(conn.getInputStream());
                            byte[] data = getBytesFromInput(in);

                            try{
                                s = new String(data, "UTF-8");
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                        String[] servers = splitString(s, '\n');

                        HashMap<String, Integer> tmp = new HashMap<String, Integer>();

                        for(int j = 0; j < servers.length; j++){
                            String name = servers[j].substring(0, 4);
                            Integer allcount = tmp.get(name);
                            Integer count = Integer.parseInt(getLogMessage(servers[j], "[", "]"));

                            if(allcount == null){
                                tmp.put(name, count);
                            }else{
                                tmp.put(name, allcount + count);
                            }
                        }

                        Iterator<String> it = tmp.keySet().iterator();
                        int allcount = 0;

                        HashMap<Integer, Integer> tmp1 = new HashMap<Integer, Integer>();
                        HashMap<String, Integer> server_count = game_server_count.get(i);
                        HashMap<String, Integer> server_count_old = game_server_count_old.get(i);

                        while(it.hasNext()){
                            String name = it.next();
                            int count = tmp.get(name);

                            if(tmp1.get(count) == null){
                                allcount += count;
                                tmp1.put(count, count);
                            }

                            Integer sc = server_count.get(name);

                            if(sc == null){
                                server_count.put(name, count);
                            }else{
                                if(count > sc){
                                    server_count.put(name, count);
                                }
                            }
                        }

                        if(game_count[i][0] < allcount){
                            game_count[i][0] = allcount;
                            game_count[i][2] = System.currentTimeMillis();
                        }

                        System.out.println("    " + GAMES[i][0] + "当前在线人数为：" + allcount + " , 今日最高在线 " + game_count[i][0] + getTimeStr(game_count[i][2]) + " ， " + "昨天最高在线 " + game_count[i][1]
                                        + getTimeStr(game_count[i][3]));

                        it = tmp.keySet().iterator();

                        while(it.hasNext()){
                            String name = it.next();
                            int count = tmp.get(name);
                            Integer oldCount = server_count_old.get(name);

                            if(oldCount == null){
                                oldCount = 0;
                            }

                            System.out.println("      " + name + " : " + count + "    (" + oldCount + ")");
                        }

                        pipCount += allcount;
                        System.out.println("    总在线：" + allcount);

                        if(i < GAMES.length - 1){
                            System.out.println();
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }finally{
                        try{
                            if(conn != null){
                                conn.disconnect();
                            }

                        }catch(Exception e){
                        }
                    }
                }

                if(pipMaxCount < pipCount){
                    pipMaxCount = pipCount;
                    pipMaxCountTime = System.currentTimeMillis();
                }

                System.out.println("明珠游戏当前在线人数：" + pipCount + " , 今日最高在线 " + pipMaxCount + getTimeStr(pipMaxCountTime) + " ， " + "昨天最高在线 " + pipMaxCountOld + getTimeStr(pipMaxCountOldTime));
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try{
                    Thread.sleep(60 * 1000);
                }catch(Exception e){
                }
            }
        }
    }

    public String getTimeStr(long time){
        if(time > 0){
            return " [" + DateFormat.getDateTimeInstance().format(new Date(time)) + "]";
        }else{
            return "";
        }
    }

    public static byte[] getBytesFromInput(DataInputStream in) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int rd = 0;
        int len = 0;
        byte[] buf = new byte[64];

        while((rd = in.read(buf)) != -1){
            len += rd;
            out.write(buf, 0, rd);
        }

        byte[] rt = out.toByteArray();
        out.close();
        return rt;
    }

    public static String getLogMessage(String logStr, String idxStr, String endStr){
        int idx = logStr.indexOf(idxStr);
        String tmp = logStr.substring(idx + idxStr.length());
        idx = tmp.indexOf(endStr);
        tmp = tmp.substring(0, idx);

        return tmp;
    }

    /**
     * 把一个字符串按指定分隔符分段。
     * @param s 原始字符串
     * @param ch 分隔符
     * @return 分出的段的数组
     */
    public static String[] splitString(String s, char ch){
        int startIndex = 0;
        int endIndex = 0;
        Vector vS = new Vector();

        while(true){
            endIndex = s.indexOf(ch, startIndex);

            if(endIndex == -1){
                String tmp = s.substring(startIndex);

                if(tmp.length() > 0){
                    vS.addElement(tmp);
                }

                break;
            }else{
                vS.addElement(s.substring(startIndex, endIndex));
                startIndex = endIndex + 1;
            }
        }

        String[] strs = new String[vS.size()];
        vS.copyInto(strs);

        return strs;
    }

    public static long getTodayStart(){
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }
}
