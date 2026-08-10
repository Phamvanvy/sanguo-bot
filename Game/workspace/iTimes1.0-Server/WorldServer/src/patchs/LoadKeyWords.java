package patchs;

import java.io.File;

import com.pip.itimes.server.util.KeywordsUtil;

public class LoadKeyWords implements Runnable {

	public void run() {
        try {
			KeywordsUtil.loadKeywords(new File(System.getProperty("user.dir") +
			"/keywords.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
