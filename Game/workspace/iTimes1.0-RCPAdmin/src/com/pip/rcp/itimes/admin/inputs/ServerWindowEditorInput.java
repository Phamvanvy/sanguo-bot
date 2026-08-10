package com.pip.rcp.itimes.admin.inputs;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.rcp.itimes.admin.data.ServerData;


public class ServerWindowEditorInput implements IEditorInput{
    private ServerData server;

    public ServerWindowEditorInput(ServerData server){
        this.server = server;
    }

    public boolean exists(){
        return false;
    }

    public ImageDescriptor getImageDescriptor(){
        return null;
    }

    public String getName(){
        return server.getDesc() + ":" + server.getUser();
    }

    public IPersistableElement getPersistable(){
        return null;
    }

    public String getToolTipText(){
        return server.getIp() + ":" + server.getPort() + "[" + server.getDesc() + ":" + server.getUser() + "]";
    }

    public Object getAdapter(Class adapter){
        return null;
    }

    public boolean equals(Object obj){
        if(obj instanceof ServerWindowEditorInput){
            ServerWindowEditorInput other = (ServerWindowEditorInput)obj;

            if(server == null || other == null || other.server == null){
                return false;
            }else{
                return server.equals(other.server);
            }
        }

        return false;
    }

    public ServerData getServer(){
        return server;
    }

    public void setServer(ServerData server){
        this.server = server;
    }
}
