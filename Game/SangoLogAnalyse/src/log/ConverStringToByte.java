package log;

public class ConverStringToByte {
	
	/**
	 * Convert hex string to byte[]
	 * @param hexString  the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.trim();
		int j = 0;
		//如果两个16进制之间有空格，消掉空格
		while ((j = hexString.indexOf(" ")) != -1) {
			hexString = hexString.substring(0, j).concat(hexString.substring(j + 1));
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * @param c char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public static String bytesToHexString(byte[] ret){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<ret.length;i++){
			if(i>0)
				sb.append(" ");
			String hex = Integer.toHexString(ret[i] & 0xff);
			if(hex.length() == 1){
				hex = "0"+hex;
			}
			sb.append(hex);
		}
		return  sb.toString();
	}
	
	public static void main(String[] args){
		String msg = "01 00 00 02 6c ff ff ff ff 01";
		byte[] by = hexStringToBytes(msg);
		for(int i=0;i<by.length;i++){
			System.out.print(by[i]);
			if(i<by.length-1)
				System.out.print(",");
		}
		System.out.println();
		byte[] b = {1,0,0,2,108,-1,-1,-1,-1,1};
		String hexString = bytesToHexString(b);
		System.out.print(hexString);
	}
}
