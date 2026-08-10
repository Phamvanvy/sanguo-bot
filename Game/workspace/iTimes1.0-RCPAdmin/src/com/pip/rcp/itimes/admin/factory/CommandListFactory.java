package com.pip.rcp.itimes.admin.factory;


import java.io.File;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jdom.Document;
import org.jdom.Element;

import com.pip.rcp.itimes.admin.data.CommandData;


public class CommandListFactory{
    public static List<CommandData> loadCommandList() throws Exception{
        List<CommandData> result = new Vector<CommandData>();

        Document doc = Tools.loadDOM(new File(FileLocator.toFileURL(Platform.getBundle("iTimes1.0RCPAdmin").getEntry("")).getPath(), "commands.xml"));

        List list = doc.getRootElement().getChildren("command");

        for(int i = 0; i < list.size(); i++){
            Element elem = (Element)list.get(i);
            CommandData command = new CommandData();

            command.setName(elem.getAttributeValue("name"));
            command.setCommand(elem.getAttributeValue("command"));
            command.setNeedConfirm("Y".equals(elem.getAttributeValue("confirm")));

            List parmlist = elem.getChildren("parm");

            for(int j = 0; j < parmlist.size(); j++){
                Element parmElem = (Element)parmlist.get(j);

                command.addParm(parmElem.getAttributeValue("parmname"));
            }

            result.add(command);
        }

        return result;
    }

    public static void saveCommandList(List<CommandData> commands) throws Exception{
        Element root = new Element("commands");
        Document doc = new Document(root);

        List list = root.getMixedContent();

        for(int i = 0; i < commands.size(); i++){
            CommandData command = commands.get(i);

            Element cmd = new Element("command");

            cmd.addAttribute("name", command.getName());
            cmd.addAttribute("command", command.getCommand());
            cmd.addAttribute("confirm", command.isNeedConfirm()? "Y": "N");

            String[] parms = command.getParms();

            if(parms.length > 0){
                List parmList = cmd.getMixedContent();

                for(int j = 0; j < parms.length; j++){
                    Element parmElem = new Element("parm");

                    parmElem.addAttribute("parmname", parms[j]);

                    parmList.add(parmElem);
                }
            }

            list.add(cmd);
        }

        Tools.saveDOM(doc, new File(FileLocator.toFileURL(Platform.getBundle("iTimes1.0RCPAdmin").getEntry("")).getPath(), "commands.xml"));
    }
}
