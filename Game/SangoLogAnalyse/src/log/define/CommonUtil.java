package log.define;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class CommonUtil {

	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yy-MM-dd HH:mm");
	public static String getDateString(Date date) {
		if (date == null) {
			return "";
		}
		return format.format(date);
	}

	private static final DecimalFormat percentFormat = new DecimalFormat(
			"####.#");

	public static String formatValue(int value) {
		return String.valueOf(Math.abs(value));
	}

	public static String formatValue(float value) {
		return formatFloat(Math.abs(value));
	}

	public static String formatValue(String value) {
		return value;
	}

	public static String formatPercent(float p) {
		return percentFormat.format(Math.abs(p)) + "%";
	}

	public static String formatFloat(double p) {
		return percentFormat.format(Math.abs(p));
	}

	public static String formatSecond(int sec) {
		if (sec < 60) {
			return sec + "秒";
		} else if (sec < 3600) {
			return (sec / 60) + "分" + (sec % 60) + "秒";
		} else {
			return (sec / 3600) + "小时" + ((sec % 3600) / 60) + "分";
		}
	}

	public static String formatMillSecond(int ms) {
		int sec = ms / 1000;
		;
		float sec2 = ms / 1000.0f;
		if (sec < 60) {
			return formatFloat(sec2) + "秒";
		} else if (sec < 3600) {
			return (sec / 60) + "分" + (sec % 60) + "秒";
		} else {
			return (sec / 3600) + "小时" + ((sec % 3600) / 60) + "分";
		}
	}

	public static final int currentMillis() {
		return (int) (System.currentTimeMillis() & 0xFFFFFFFF);
	}

	public static final int getMapId(int globalId) {
		return globalId >> 12;
	}

	public static boolean hit(Random rnd, int chance, int base) {
		int r = rnd.nextInt(base);
		if (r <= chance)
			return true;
		return false;
	}

	public static int getCount(Random rnd, int min, int max) {
		if (min == max)
			return min;
		return rnd.nextInt(max - min + 1) + min;
	}

	public static int getInt(byte[] bytes, int offset) {
		return (((bytes[offset] << 24) & 0xFF000000)
				| ((bytes[offset + 1] << 16) & 0xFF0000)
				| ((bytes[offset + 2] << 8) & 0xFF00) | ((bytes[offset + 3] & 0xFF)));
	}

	public static void main(String[] args) {
		// int angle = Unit.calcAngle(220, 155, 274, 159);
		// int angle1 = Unit.calcAngle(274, 159, 193, 212);
		// int angle2 = Unit.calcAngle(192, 212, 220, 155);
		// // int angle = Unit.calcAngle(274, 159, 220, 155);
		// // int angle1 = Unit.calcAngle(212, 193, 274, 159);
		// // int angle2 = Unit.calcAngle(220, 155, 192, 212);
		// System.out.println(String.format("%d  %d  %d", angle,angle1,angle2));
		// Random rnd = new Random();
		// for(int i=0;i<50;i++){
		// System.out.println(rnd.nextInt(1000000));
		// }
		// System.out.println(true^true);
		// System.out.println(false^false);
		// System.out.println(true^false);
		// try {
		// f(0);
		// }finally{
		//			
		// }
		// int j= 0;
		Pattern pattern = Pattern.compile("/\\d\\d");
		String s = "sjfksasdfkdfdsa;/11/1sdfdsfj1i28384/23";
		Matcher matcher = pattern.matcher(s);
		boolean m = matcher.find();
		while (m) {
			String g = matcher.group();
			System.out.println(g);
			m = matcher.find();
		}
	}

	/**
	 * Light删除：这个方法较危险，应使用XML文档自带的编码解释。 public static Document
	 * getDocument(Reader r) throws Exception{ SAXReader reader = new
	 * SAXReader(); return reader.read(r); }
	 */

	public static Document getDocument(InputStream r) throws Exception {
		SAXReader reader = new SAXReader();
		return reader.read(r);
	}

	public static void saveDocument(Document doc, Writer w) {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("GBK");
		XMLWriter writer = new XMLWriter(w, format);
		try {
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}

	}

	public static String ip2str(int ip) {
		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
				+ ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}

	public static void f(int i) {
		if (i == 0)
			throw new IllegalArgumentException();
	}

	/**
	 * 得到一个日期距离现在的剩余时间。
	 * 
	 * @param date
	 * @return
	 */
	public static String getRemainTimeString(Date date) {
		if (date == null) {
			return "";
		}
		long remain = (date.getTime() - System.currentTimeMillis()) / 60000L;
		if (remain <= 0) {
			return "0分钟";
		} else if (remain < 60) {
			return remain + "分钟";
		} else if (remain < 60 * 24) {
			return (remain / 60) + "小时";
		} else {
			return (remain / 1440) + "天";
		}
	}
}
