import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class AddIMoney implements Runnable{
    private static final String SERVER = "http://127.0.0.1:7500/backdoor?admin=jlin&pass=jlin";

    private static final int[][] users = {
        {
                        1, 1
        }
    };

    public static void main(String[] args){
        new Thread(new AddIMoney()).start();
    }

    public void run(){
        for(int i = 0; i < users.length; i++){
            HttpURLConnection conn = null;

            try{
                String url = SERVER + "&id=" + users[i][0] + "&cmd=bbalance&value=" + users[i][1];

                conn = (HttpURLConnection)(new URL(url).openConnection());
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                String s = null;

                if(conn.getResponseCode() == 200){
                    DataInputStream in = new DataInputStream(conn.getInputStream());
                    byte[] data = getBytesFromInput(in);
                    s = new String(data, "UTF-8");

                    if(s.indexOf("ok") < 0){
                        System.out.println("异常终止于：" + users[i][0] + " , " + i);

                        break;
                    }

                    System.out.println("发放玩家id：" + users[i][0] + " , i币：" + users[i][1] + " , 服务器回应：" + s);
                }else{
                    System.out.println("异常终止于：" + users[i][0] + " , " + i);

                    break;
                }
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("异常终止于：" + users[i][0] + " , " + i);

                break;
            }finally{
                try{
                    if(conn != null){
                        conn.disconnect();
                    }
                }catch(Exception e){
                }
            }
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
}
