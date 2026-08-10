package pip.gm.cmd;

import org.apache.mina.common.ByteBuffer;
import org.dom4j.Element;

import pip.gm.fw.AbstractClient;
import pip.gm.fw.Auth;
import pip.gm.fw.AuthConstants;
import pip.gm.fw.GmChatTrace;
import pip.gm.fw.GmFunction;
import pip.gm.fw.IMessage;
import pip.gm.fw.PDProcessor;
import pip.io.uwap.PDataFactory;
import pip.io.uwap.UWapData;
import pip.util.StringUtil;
import org.dom4j.Element;
import pip.gm.fw.ArrayOfParameters;

import java.awt.Component;
import java.awt.TextField;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.*;
public class GeneralFunction extends pip.gm.fw.Command implements ArrayOfParameters {
	private static final Charset utf8 = Charset.forName("utf-8");
	public String gmCommand;
	public String funcTitle;
	public String helpMsg;
	public Element element;
	public long authId;
	public int protocolId;
	
	
	public GeneralFunction(Element element) {
		this.element = element;
		gmCommand = element.attributeValue("command");
		funcTitle = element.attributeValue("title");
		helpMsg = element.elementText("tutorial");
		protocolId = Integer.parseInt(element.attributeValue("protocol")); 
		String s = element.attributeValue("auth");
		if (s != null) {
			try {
				if (s.startsWith("0x")) {
					authId = Long.parseLong(s.substring(2), 16);
				} else {
					authId = Long.parseLong(s);
				}
			} catch (NumberFormatException e) {
			}
		}
	}
	public long getAuth() {
    	return authId;
    }
	public static class GeneralProcessor implements  PDProcessor {
	    public boolean process(pip.gm.fw.AbstractClient master, UWapData data) {
	        if (data != null && data instanceof Data) {
	        	String msg = ((Data)data).toString();
	        	if (msg != null && msg.length() > 0) {
	        		master.onMessage(IMessage.MSG_TYPE_LOG, msg, null);
	        	}
		        return true;
	        }
	        return false;
	    }
	};
	public boolean exec(String cmd, AbstractClient world, String[] s) throws Exception {
		if (cmd != null) {
			if (isCommand(world.auth, cmd)) {
				Data d = new Data(protocolId, element);
				d.params = new String[s.length - 1];
				System.arraycopy(s, 1, d.params, 0, s.length - 1);
				
				//added by leo for 在命令参数里添加回车字符，如需要让参数中的字符串可以多行显示，需要用\\\n来表示一个回车
				for(int i = 0; i < d.params.length; i++){
    				StringBuffer sb = new StringBuffer();
                    for(int j = 0; j < d.params[i].length(); j++){
                        if(d.params[i].charAt(j) == '\\' && j <= d.params[i].length() - 1 && d.params[i].charAt(j + 1) == 'n'){
                            sb.append('\n');
                            j++;
                        }else{
                            sb.append(d.params[i].charAt(j));
                        }
                    }
                    d.params[i] = sb.toString();
				}
				
				world.sndRequest(d);
				String log[] = d.getLog();
				if (log != null) {
					GmChatTrace.traceGm(world.getConfig().account, world.getUniqServerId(), Integer.parseInt(log[0]), Integer.parseInt(log[1]), log[2], world.getConfig().title + ":" + log[3]);
				}
				return true;
			}
		}
		return false;
	}
	/** 基本的简单协议*/
	public static class Data extends UWapData {
		/** 协议信息 */
		StringBuilder buf;
		String []params;
		public Element element;
		public Data() {
		}
		public Data(int id, Element element) {
			realProtocolId = id;
			this.element = element;
		}
		public String[] getLog() throws Exception {
			Element log = element.element("log");
			if (log == null) {
				return null;
			}
			String fmt = log.getText();
			if (fmt == null || fmt.length() == 0) {
				return null;
			}
			String ret[] = new String[4];
			ret[2] = parseArray(fillParam(fmt));
			ret[0] = parseArray(fillParam(log.attributeValue("type")));
			ret[1] = parseArray(fillParam(log.attributeValue("target")));
			ret[3] = parseArray(fillParam(log.attributeValue("ref")));
			return ret;
		}
		public String fillParam(String fmt) throws Exception {
			int j = fmt.length();
			while (true) {
				int k = fmt.lastIndexOf("${", j);
				if (k < 0) {
					break;
				}
				int k2 = fmt.indexOf("}", k);
				if (k2 < 0) {
					throw new Exception("Format error: unmatched ${}");
				}
				String paraId = fmt.substring(k + 2, k2);
				String paramValue = null; 
				int comaIndex = paraId.indexOf("-");
				if (comaIndex >= 0) {
					int v = Integer.parseInt(paraId.substring(0,comaIndex));
					int subIndex = Integer.parseInt(paraId.substring(comaIndex+1));
					String vs[] = params[v].split(",");
					if (vs.length > subIndex) {
						paramValue = vs[subIndex];
					}
				} else {
					int v = Integer.parseInt(paraId);
					paramValue = params[v];
				}
				fmt = fmt.substring(0, k) + paramValue+ fmt.substring(k2+1);
			}
			return fmt;
		}
		
		public ByteBuffer genUWAP(int uWapVersion) throws Exception {
			ByteBuffer ret = ByteBuffer.allocate(32);
			ret.setAutoExpand(true);
			if (uWapVersion == -1) {
				ret.putShort((short)getAppDataType());
			} else {
				// skip
				ret.putInt(0);
				ret.putShort((short)0);
				if (uWapVersion != 1) {
					ret.put((byte) 0);
				}
			}
			
			for(Iterator<Element> i = element.elementIterator("Param"); i.hasNext();) {
				Element elem = (Element)i.next();
				String ss = elem.attributeValue("value");
				if (ss.startsWith("$")) { // The value is extracted from parameters.
					int comaIndex = ss.indexOf("-"); 
					if (comaIndex >= 0) { // The value is part of the parameters which separated by comma 
						int n = Integer.parseInt(ss.substring(1, comaIndex));
						int subIdx = Integer.parseInt(ss.substring(1 + comaIndex));
						if (n >= params.length) {
							ss = "";
						} else {
							String ps[] = params[n].split(",");
							if (ps.length > subIdx) {
								ss = ps[subIdx];
							} else {
								ss = "";
							}
						}
					} else {
						int n = Integer.parseInt(ss.substring(1));
						if (n >= params.length) {
	//						throw new Exception("参数不足");
							ss = "";
						} else {
							ss = params[n];
						}
					}
					// Other input control: bits, enum int, enum etc.
				}
				String type = elem.attributeValue("type").toLowerCase();
				if (type.equals("byte")) {
					if (uWapVersion != -1) ret.put(DTYPE_BYTE);
					ret.put((byte)getInt(ss));
				} else if (type.equals("short")) {
					if (uWapVersion != -1) ret.put(DTYPE_SHORT);
					ret.putShort((short)getInt(ss));
				} else if (type.equals("boolean")) {
					if (uWapVersion != -1) ret.put(DTYPE_BOOLEAN);
					ret.put((byte) ("1tTyY".indexOf(ss) >= 0 ? 1 : 0));
				} else if (type.endsWith("byte[]")) {
					byte[] dd = getBytes(ss);
					if (uWapVersion != -1) {
						ret.put((byte) (DTYPE_BYTE | DTYPE_ARRAY));
						ret.putInt(dd.length);
					} else {
						ret.putShort((short)dd.length);
					}
					ret.put(dd);
				} else if (type.equals("string")) {
					
					if (uWapVersion != -1) ret.put(DTYPE_UTF8);
					byte bs[] = ss.getBytes(utf8);
					ret.putShort((short)bs.length);
					ret.put(bs);
				} else if (type.equals("int")) {
					if (uWapVersion != -1)	ret.put(DTYPE_INT);
					ret.putInt(getInt(ss));
				} else if (type.equals("long")) {
					if (uWapVersion != -1) ret.put(DTYPE_LONG);
						ret.putLong(getLong(ss));
				} else if (type.equals("int[]")) {
					if (uWapVersion != -1) ret.put((byte) (DTYPE_INT | DTYPE_ARRAY));
					int[] dd = getInts(ss);
					ret.putShort((short)dd.length);
					for (int k : dd) {
						ret.putInt(k);
					}
				} else if (type.equals("short[]")) {
					if (uWapVersion != -1) ret.put((byte) (DTYPE_SHORT | DTYPE_ARRAY));
					int[] dd = getInts(ss);
					ret.putShort((short)dd.length);
					for (int k : dd) {
						ret.putShort((short)k);
					}
				} else if (type.equals("string[]")) {
					if (uWapVersion != -1) ret.put((byte) (DTYPE_UTF8 | DTYPE_ARRAY));
					String[] dd = ss.split(",");
					ret.putShort((short)dd.length);
					for (String k : dd) {
						byte[] bs = k.getBytes(utf8);
						ret.putShort((short)bs.length);
						ret.put(bs);
					}
				} else {
//					throw new RuntimeException("Unsupported type:" + type);
				}
			}
			if (uWapVersion != -1) {
				int len = ret.position();
				ret.position(0);
				if (uWapVersion == 1) {
					ret.put((byte) getAppDataType()); // 0 type
				} else {
					ret.putShort((short)getAppDataType()); // 0 type
				}
				ret.putInt(len);
				ret.put((byte) dataNum);
				ret.position(len);
			}
			ret.flip();
			return ret;
		}
		public String toString() {
			if (buf != null) {
				return buf.toString();
			}
			return super.toString();
		}
		public String getHex(int v, int len) {
			String s = "0000000000000000000" + Integer.toHexString(v);
			return s.substring(s.length() - len);
		}
		public void setElement(Element e) {
			element = e;
		}
		public String initElement(Element ele, ByteBuffer data, int uWapVersion) throws Exception {
			StringBuilder ret = new StringBuilder();
			ArrayList<String> dat = new ArrayList<String>();
			for(Iterator<Element> i = ele.elementIterator("Param"); i.hasNext();) {
				Element elem = (Element)i.next();
				String type = elem.attributeValue("type").toLowerCase();
				try {
					if (type.equals("byte")) {
						if (uWapVersion != -1) assertType(data, DTYPE_BYTE, false);
						dat.add(String.valueOf(data.get()));
					} else if (type.equals("short")) {
						if (uWapVersion != -1) assertType(data, DTYPE_SHORT, false);
						dat.add(String.valueOf(data.getShort()));
					} else if (type.equals("boolean")) {
						if (uWapVersion != -1) assertType(data, DTYPE_BOOLEAN, false);
						dat.add(data.get() == 0? "0" : "1");
					} else if (type.endsWith("byte[]")) {
						int n = 0;
						if (uWapVersion != -1) {
							assertType(data, DTYPE_BYTE | DTYPE_ARRAY, false);
							n = data.getInt();
						} else {
							n = data.getShort();
						}
						byte []d = new byte[n];
						data.get(d);
						StringBuilder bbuf = new StringBuilder("0x");
						for (int j = 0; j < n; j++) {
							if (j != 0) {
								bbuf.append(",");
							}
							bbuf.append(getHex(d[j], 2));
						}
						dat.add(bbuf.toString());
					} else if (type.equals("string")) {
						if (uWapVersion != -1) assertType(data, DTYPE_UTF8, false);
						int n = data.getShort();
						if (uWapVersion == -1 && (n & 0x8000) != 0) {
							n &= 0x7fff;
							byte bs[] = new byte[n];
							data.get(bs);
							char[] charr = new char[n / 2];
				            for (int j = 0; j < charr.length; j++) {
				            	charr[j] = (char)(((bs[j*2] & 0xFF) << 8) | (bs[j*2 + 1] & 0xFF));
				            }
				            dat.add(new String(charr));
						} else {
							byte d[] = new byte[n];
							data.get(d);
							dat.add(new String(d, "UTF8"));
						}
					} else if (type.equals("int")) {
						if (uWapVersion != -1) assertType(data, DTYPE_INT, false);
						dat.add(String.valueOf(data.getInt()));
					} else if (type.equals("long")) {
						if (uWapVersion != -1) assertType(data, DTYPE_LONG, false);
						dat.add(String.valueOf(data.getLong()));
					} else if (type.equals("int[]")) {
						if (uWapVersion != -1) assertType(data, DTYPE_INT | DTYPE_ARRAY, false);
						int n = data.getShort();
						StringBuilder bbuf = new StringBuilder("0x");
						for (int j = 0; j < n; j++) {
							if (j != 0) {
								bbuf.append(",");
							}
							bbuf.append(getHex(data.getInt(), 8));
						}
						dat.add(bbuf.toString());
					} else if (type.equals("short[]")) {
						if (uWapVersion != -1) assertType(data, DTYPE_SHORT | DTYPE_ARRAY, false);
						int n = data.getShort();
						StringBuilder bbuf = new StringBuilder("0x");
						for (int j = 0; j < n; j++) {
							if (j != 0) {
								bbuf.append(",");
							}
							bbuf.append(getHex(data.getShort(), 4));
						}
						dat.add(bbuf.toString());
					} else if (type.equals("sub")) {
						Iterator<Element> subIt = elem.elementIterator("sub");
						if (uWapVersion != -1) assertType(data, DTYPE_SHORT, false);
						int n = data.getShort();
						StringBuffer tbuf = new StringBuffer();
						if (subIt.hasNext()) {
							Element subEle = (Element)subIt.next();
							String deli = subEle.elementText("deli");
							if (deli ==null) {
								deli = "";
							}
							for (int j = 0; j < n; j++) {
								tbuf.append(deli).append(j+1).append(".").append(initElement(subEle, data, uWapVersion));
							}
							dat.add(tbuf.toString());
						} else {
							throw new Exception("Format Error");
						}
					} else if (type.equals("string[]")) {
						if (uWapVersion != -1) assertType(data, DTYPE_UTF8 | DTYPE_ARRAY, false);
						int n = data.getShort();
						StringBuilder bbuf = new StringBuilder();
						for (int j = 0; j < n; j++) {
							if (j != 0) {
								bbuf.append(",");
							}
							int m = data.getShort();
							byte b[] = new byte[m];
							data.get(b);
							bbuf.append(new String(b, "UTF8"));
						}
						dat.add(bbuf.toString());
					} else {
						throw new RuntimeException("Unsupported type: " + type);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Protocol error：" + ele.attributeValue("name") + "(" + ele.attributeValue("protocolId") + ")->" + elem.attributeValue("title") + " type:" + type);
					throw e;
				}
			}
			String fmt = ele.elementText("format");
			if (fmt == null) {
				int j = 0;
				for(Iterator<Element> i = ele.elementIterator("Param"); i.hasNext();) {
					Element elem = (Element)i.next();
					String type = elem.attributeValue("type").toLowerCase();
					ret.append(elem.attributeValue("title")).append("[").append(dat.get(j++)).append("]");
				}
			} else {
				int j = fmt.length();
				while (true) {
					int k = fmt.lastIndexOf("${", j);
					if (k < 0) {
						break;
					}
					int k2 = fmt.indexOf("}", k);
					if (k2 < 0) {
						throw new Exception("Format error, unmatched ${}");
					}
					int v = Integer.parseInt(fmt.substring(k + 2, k2));
					fmt = fmt.substring(0, k) + dat.get(v) + fmt.substring(k2+1);
				}
				ret.append(parseArray(fmt));
			}
			return ret.toString();
		}
		public String parseArray(String fmt) throws Exception {
			int j = fmt.length();
			while (true) {
				int k = fmt.lastIndexOf("$[", j);
				if (k < 0) {
					break;
				}
				int k2 = fmt.indexOf("]", k);
				if (k2 < 0) {
					throw new Exception("Format error: unmatched $[]");
				}
				String var = fmt.substring(k + 2, k2);
				int k3 = var.indexOf(":");
				if (k3 < 0) {
					throw new Exception("Format error: unmatched $[id:index]");
				}
				String ekey = var.substring(0, k3);
				String kk3 = var.substring(k3+1);
				var = getDef(ekey, kk3);
				fmt = fmt.substring(0, k) + var+ fmt.substring(k2+1);
			}
			return fmt;
		}
		public String getDef(String nodeName, String key) {
			Iterator it = element.getParent().elementIterator("consts");
			while (it.hasNext()) {
				Element e = (Element)it.next();
				if (nodeName.equals(e.attributeValue("name"))) {
					it = e.elementIterator("map");
					while (it.hasNext()) {
						e = (Element)it.next();
						if (key.equals(e.attributeValue("key"))) {
							return e.getText();
						}
					}
					break;
				}
			}
			return "《" + key + "》";
		}

		public void init(ByteBuffer data) throws Exception {
			Element el = element; // 
			if (el != null) {
				int uWapVersion = 1;
				String s = el.attributeValue("version");
				if (s != null) {
					uWapVersion = getInt(s);
				}
				if (uWapVersion != -1) {
					int len = data.getInt();
					dataNum = data.get() & 0xff;
				}
				buf = new StringBuilder();
				buf.append(initElement(el, data, uWapVersion));
			}
		}

	    public int getAppDataType() {
	        return realProtocolId;
	    }

	    public String[] getProperties() {
	        return null;
	    }
	}
	public static int getInt(String s) {
		try {
			if (s.startsWith("0x") || s.startsWith("0X")) {
				return Integer.parseInt(s.substring(2), 16);
			}
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return -1;
	}
	public static long getLong(String s) {
		try {
			if (s.startsWith("0x") || s.startsWith("0X")) {
				return Long.parseLong(s.substring(2), 16);
			}
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return -1;
	}
	public static byte[] getBytes(String s) {
		boolean hex = s.startsWith("0x") || s.startsWith("0X");
		if (hex) {
			s = s.substring(2);
		}
		String []ss = s.split(",");
		byte ret[] = new byte[ss.length];
		for (int i = ss.length; i-- > 0; ) {
			int k;
			if (hex) {
				k = Integer.parseInt(ss[i], 16);
			} else {
				k = Integer.parseInt(ss[i]);
			}
			ret[i] = (byte)k;
		}
		return ret;
	}
	public static long[] getLongs(String s) {
		boolean hex = s.startsWith("0x") || s.startsWith("0X");
		if (hex) {
			s = s.substring(2);
		}
		String []ss = s.split(",");
		long ret[] = new long[ss.length];
		for (int i = ss.length; i-- > 0; ) {
			long k;
			if (hex) {
				k = Long.parseLong(ss[i], 16);
			} else {
				k = Long.parseLong(ss[i]);
			}
			ret[i] = k;
		}
		return ret;
	}
	public static int[] getInts(String s) {
		boolean hex = s.startsWith("0x") || s.startsWith("0X");
		if (hex) {
			s = s.substring(2);
		}
		String []ss = s.split(",");
		int ret[] = new int[ss.length];
		for (int i = ss.length; i-- > 0; ) {
			int k;
			if (hex) {
				k = Integer.parseInt(ss[i], 16);
			} else {
				k = Integer.parseInt(ss[i]);
			}
			ret[i] = k;
		}
		return ret;
	}
	public String getCommand(Auth auth) {
		if (authId == 0 || auth.hasAuth(authId)) {
			return gmCommand;
		} else {
			return null;
		}
	}

	public String getName(Auth auth) {
		if (authId == 0 || auth.hasAuth(authId)) {
			return funcTitle;
		}
		return null;
	}

	public String getDescription(Auth auth) {
		return helpMsg;
	}

	String [][] parameterSet; // Parameter information {title,tip}* 	 
	public String getParameterSetName(int n) {
		return funcTitle;
	}
	public int getNumOfParameterSet() {
		return 1;
	}
	public int getNumOfParameters(int n) {
		if (parameterSet == null) {
			constructParas();
		}
		return parameterSet.length;
	}
	private void constructParas() {
		HashMap<Integer,HashMap<Integer,Element>> paras = new HashMap<Integer,HashMap<Integer,Element>>();
		int paranNum = 0;
		for(Iterator<Element> i = element.elementIterator("Param"); i.hasNext();) {
			Element elem = (Element)i.next();
			String ss = elem.attributeValue("value");
			if (ss.startsWith("$")) {				
				int comaIndex = ss.indexOf("-");
				int n = 0;
				int subIdx = 0;
				if (comaIndex >= 0) {
					n = Integer.parseInt(ss.substring(1, comaIndex));
					subIdx = Integer.parseInt(ss.substring(1 + comaIndex));
				} else {
					n = Integer.parseInt(ss.substring(1));
					subIdx = 0;
				}
				if (paranNum  <= n) {
					paranNum = n + 1;
				}
				HashMap<Integer,Element> pp = paras.get(n);
				if (pp == null) {
					pp = new HashMap<Integer,Element>();
					paras.put(n, pp);
				}
				pp.put(subIdx, elem);
			}
		}
		parameterSet = new String[paranNum][];
		for (int i = 0; i < paranNum; i++) {
			HashMap<Integer,Element> pp = paras.get(i);
			if (pp.size() == 1) {
				Element element = pp.get(0);
				parameterSet[i] = new String[]{element.attributeValue("name"), element.attributeValue("tip")};
			} else {
				StringBuffer buf = new StringBuffer();
				StringBuffer tip = new StringBuffer();
				for (int j = 0; ;j++) {
					Element element = pp.get(j);
					if (element == null) {
						break;
					}
					if (j > 0) {
						buf.append(",");
					}
					buf.append(element.attributeValue("name"));
					String tipt = element.attributeValue("tip");
					if (tipt != null) {
						if (tip.length() > 0) {
							tip.append(";");
						}
						tip.append(tipt);
					}
				}
				parameterSet[i] = new String[]{buf.toString(), tip.toString()};
			}
		}
	}
	public String getParameterTitle(int m, int n) {
		if (parameterSet == null) {
			constructParas();
		}
		if (n < parameterSet.length && parameterSet[n] != null && parameterSet[n].length > 0) {
			return parameterSet[n][0];
		}
		return null;
		
	}
	public String getParameterTips(int m, int n) {
		if (parameterSet == null) {
			constructParas();
		}
		if (n < parameterSet.length && parameterSet[n] != null && parameterSet[n].length > 1) {
			return parameterSet[n][1];
		}
		return null;
	}
}
