package pip.util.boot;

import java.io.*;
public class Main {
	public static String requiredLib[] = {
		"sortutil.jar",
		"mina-core-1.1.5.jar", 
		"xercesImpl.jar", 
		"?F1J8.jar", 
		"mysql-connector-java-3.1.10-bin.jar",
		"jdom.jar", 
		"slf4j-jdk14.jar", 
		"dom4j-1.6.1.jar",
	};
	public static void main(String []args) {
		String workDir = null;
		String userHome = null;
		if (args.length == 0) {
			workDir = System.getProperty("user.dir");
		} else if (args.length == 1) {
			workDir = args[0];
		} else {
			System.out.println("Invalid argment");
			System.exit(0);
		}
		File f = new File(System.getProperty("user.home"));
		f = new File(f, pip.gm.fw.BaseConfig.configFileDirName);
		if (!f.exists()) {
			userHome = workDir;
		}
		// 
		String version = System.getProperty("java.version");
		int k = version.indexOf('.');
		if (k < 0) {
			System.out.println("java is not found.");
			System.exit(0);
		}
		if (Integer.parseInt(version.substring(0,k)) < 1) {
			System.out.println("Unsupported java version");
			System.exit(0);
		}
		k++;
		int k2 = version.indexOf('.', k);
		if (k < 0) {
			System.out.println("Unsupported java version");
			System.exit(0);
		}
		if (Integer.parseInt(version.substring(k, k2)) < 6) {
			System.out.println("Unsupported java version");
			System.exit(0);
		}
		
		Runtime r = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder("java -cp \"");
		sb.append(System.getProperty("java.class.path"));
		File dir = new File(workDir);
		dir = new File(dir, "lib");
		if (!dir.exists()) {
			System.out.println("Lib path " + dir.getAbsolutePath() + " is not found.");
			System.exit(0);
		}
		for (String s : requiredLib) {
			boolean canIgnore = s.startsWith("?");
			if (canIgnore) {
				s = s.substring(1);
			}
			f = new File(dir, s);
			if (f.exists()) {
				sb.append(File.pathSeparatorChar);
				sb.append(f.getAbsolutePath());
			} else if (!canIgnore) {
				System.out.println("Lib " + f.getAbsolutePath() + " is not found.");
				System.exit(0);
			}
		}
		sb.append("\" -Xms256m -Xmx384m pip.gm.MainApp");
		if (userHome != null) {
			sb.append(" \"").append(userHome).append("\"");
		}
		try {
			 Process p = r.exec(sb.toString());
			 new OutThread(p.getInputStream()).start();
			 new OutThread(p.getErrorStream()).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static class OutThread extends Thread {
		public InputStream in;
		public OutThread(InputStream in) {
			this.in = in;
		}
		public void run() {
			byte[] buf = new byte[1024];
			try {
				while (true) {
					int k = in.read(buf);
					if (k < 0) {
						break;
					}
					System.out.print(new String(buf, 0, k));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
