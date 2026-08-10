package com.pip.uieditor.model.util;

import java.io.OutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.persist.PersistMapping;

public class ScreenModelWriter implements Constants{
	
	public void write(OutputStream stream, Screen screen, PersistMapping mapping)
			throws Exception {
		XMLOutputter out = new XMLOutputter("    ", true, "UTF-8");
		out.output(createDocument(screen, mapping), stream);
		stream.flush();
	}

	protected Document createDocument(Screen screen, PersistMapping mapping)
			throws Exception {
		Element root = screen.toXml(mapping);
		Document doc = new Document(root);
		return doc;
	}
}
