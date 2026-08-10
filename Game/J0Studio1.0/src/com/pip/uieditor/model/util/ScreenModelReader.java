package com.pip.uieditor.model.util;

import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.persist.PersistMapping;

/**
 * 从XML读取ui配置文件
 * @author Jeffrey
 *
 */
public class ScreenModelReader implements Constants{
	
	
	public Screen read(InputStream stream, PersistMapping mapping) throws Exception{
        SAXBuilder sb = new SAXBuilder();
        sb.setValidation(false);
        Document doc = sb.build(stream);
        return readScreen(doc.getRootElement(), mapping);
	}
	
	protected Screen readScreen(Element element, PersistMapping mapping) throws Exception{
		Screen screen = new Screen();
		screen.load(null, element, mapping);
		return screen;
	}
}
