package pip.gm.fw;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import pip.io.uwap.PDataFactory;

public abstract class GmFunction extends Command {
	public abstract void registerPackage(PDataFactory factory);
	public abstract PDProcessor getPackageProcessor();

	public boolean exec(String cmd, AbstractClient aworld, String []s) throws Exception {
    	return false;
    }
    public String getCommand(Auth auth) {
        return null;
    }
    public String getName(Auth auth) {
        return null;
    }
    public String getDescription(Auth auth) {
        return null;
    }
    
    // 下面是VIP查询功能的代码（Light：不知道放在那里好了。。。）
    
    public static HashMap<Integer, int[]> vipLevelCache = new HashMap<Integer, int[]>();
    
    public static String getStar(int count) {
		if (count == 0) {
			return "-";
		}
		char[] arr = new char[count];
		Arrays.fill(arr, '★');
		return new String(arr);
	}
    
    public static int[] queryVIPLevel(int accountID) {
		if (vipLevelCache.containsKey(accountID)) {
			return vipLevelCache.get(accountID);
		}
		String url = "http://211.151.99.70:7070/uts/query/queryvip.jsp?id=" + accountID + "&password=pip@game2008";
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			int code = conn.getResponseCode();
			if (code != 200) {
				return null;
			}
			is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int level1 = Integer.parseInt(br.readLine());
			int level2 = Integer.parseInt(br.readLine());
			int[] ret = new int[] { level1, level2 };
			vipLevelCache.put(accountID, ret);
			return ret;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e) {
			}
		}
	}
}
