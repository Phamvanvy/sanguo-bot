package com.pip.rcp.itimes.admin.factory;


import java.io.File;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jdom.Document;
import org.jdom.Element;

import com.pip.rcp.itimes.admin.data.ServerData;


public class ServerListFactory{
    public static List<ServerData> loadServerList() throws Exception{
        List<ServerData> result = new Vector<ServerData>();

        Document doc = Tools.loadDOM(new File(FileLocator.toFileURL(Platform.getBundle("iTimes1.0RCPAdmin").getEntry("")).getPath(), "servers.xml"));

        List list = doc.getRootElement().getChildren("server");

        for(int i = 0; i < list.size(); i++){
            Element elem = (Element)list.get(i);
            ServerData server = new ServerData();

            server.setIp(elem.getAttributeValue("ip"));
            server.setPort(elem.getAttributeValue("port"));
            server.setDesc(elem.getAttributeValue("desc"));
            server.setUser(elem.getAttributeValue("user"));
            server.setPassword(elem.getAttributeValue("password"));

            result.add(server);
        }

        return result;
    }

    public static void saveServerList(List<ServerData> servers) throws Exception{
        Element root = new Element("servers");
        Document doc = new Document(root);

        List list = root.getMixedContent();

        for(int i = 0; i < servers.size(); i++){
            ServerData server = servers.get(i);

            Element srv = new Element("server");
            srv.addAttribute("ip", server.getIp());
            srv.addAttribute("port", server.getPort());
            srv.addAttribute("desc", server.getDesc());
            srv.addAttribute("user", server.getUser());
            srv.addAttribute("password", server.getPassword());

            list.add(srv);
        }

        Tools.saveDOM(doc, new File(FileLocator.toFileURL(Platform.getBundle("iTimes1.0RCPAdmin").getEntry("")).getPath(), "servers.xml"));
    }
}
