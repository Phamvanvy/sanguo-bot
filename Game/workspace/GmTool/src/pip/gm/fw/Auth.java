package pip.gm.fw;

/**
 * 权限工具类.
 */
public class Auth {
	public static Auth rootAuth;
	/** 按位赋予的权限标记 */
	public long authMask; 
	/** 各个权限的名称 */
	private String authNames[];
	
	public Auth(String[] authNames) {
		this.authNames = authNames;
	}
	public static Auth getRootAuth() {
		if (rootAuth == null) {
			rootAuth = new Auth(null);
			rootAuth.authMask = -1L;
		}
		return rootAuth;
	}
	public static String getAuth(long l) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < AuthConstants.authStrings.length; i++) {
			if ((l & 1) != 0) {
				buf.append(AuthConstants.authStrings[i]).append(" ");
			}
			l >>= 1;
		}
		return buf.toString();
	}
	/** 设定具体权限名称 */
	public void setAuthName(int id, String name) {
		if (id > 0) {
			if (authNames == null || id >= authNames.length) {
				if (id > 63) {
					return;
				}
				String tmp[] = new String[id+1];
				if (authNames != null) {
					System.arraycopy(authNames, 0, tmp, 0, authNames.length);
				}
				authNames = tmp;
			}
			authNames[id] = name;
		}
	}
	public void setAuth(long v) {
		authMask = v;
	}
    public void setAuth(String auth) {
    	authMask = 0;
    	if (auth.startsWith("0x") || auth.startsWith("0X")) {
    		authMask = Long.parseLong(auth.substring(2), 16);
    	} else {
        	int n = 0;
        	while (n < auth.length()) {
        		int k = auth.indexOf('|', n);
        		String s = null;
        		if (k > 0) {
        			s = auth.substring(n, k);
        			n = k+1;
        		} else {
        			s = auth.substring(n);
        			n = auth.length();
        		}
        		boolean done = false;
        		for (int i = 0; i < authNames.length; i++) {
        			if (s.equals(authNames[i])) {
        				authMask |= 1L << i;
        				done = true;
        				break;
        			}
        		}
        		if (!done) {
        			int idx = authNames.length;
        			setAuthName(idx, s);
        			authMask |= 1L << idx;
        		}
        	}
        }
    }
//    public boolean hasAuth(String authStr){
//    	for (int i = 0; i < authNames.length; i++) {
//			if (authStr.equals(authNames[i])) {
//				return (authMask & (1L << i)) != 0;
//			}
//		}
//    	return false;
//    }

    public boolean hasAuth(long authMskes){
    	long k = authMask & authMskes; 
    	return k == authMskes;
    }
}
