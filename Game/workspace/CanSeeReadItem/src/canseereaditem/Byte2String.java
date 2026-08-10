package canseereaditem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Byte2String {
	
    private static final byte[] highDigits;

    private static final byte[] lowDigits;
	static{
        final byte[] digits = {
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

        int i;
        byte[] high = new byte[256];
        byte[] low = new byte[256];
        
        for(i = 0; i < 256; i++){
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 0x0F];
        }

        highDigits = high;
        lowDigits = low;
    }

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		for(int i = 0;i < highDigits.length; i++ ){
//			System.out.print((char)highDigits[i]);
//		}
//		System.out.println();
//		System.out.println("============================");
//		System.out.println();
//		for(int i = 0;i < highDigits.length; i++ ){
//			System.out.print((char)highDigits[i]);
//		}
		String tmp = "02 08 00 00 00 64";
//		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
//		DataInputStream dos = new DataInputStream(biss);
		readitem.getMailItem(readitem.getdata(tmp));
	}

}
