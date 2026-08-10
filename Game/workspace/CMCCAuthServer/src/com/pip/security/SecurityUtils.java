package com.pip.security;

import com.pip.server.util.Utils;

public class SecurityUtils {
    public static boolean verifyMD5(String s,String md5String){
        MD5 md = new MD5();
        byte[] bytes = s.getBytes();
        md.update(bytes,0,bytes.length);
        String digest = Utils.getHexString(md.digest());
        if(digest.equalsIgnoreCase(md5String)){
            return true;
        }else{
            return false;
        }
    }

    public static void main(String[] args) throws Exception{
        System.out.println("");
        System.out.println(verifyMD5("pipadmin","2eb34039bfe7157cc55df2e72b604c77"));
    }
}
