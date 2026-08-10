package com.pip.sanguo.data;

import org.jdom.Element;

public class ReadBook extends DataObject{

    @Override
    public boolean changed(DataObject obj) {
       
        return false;
    }

    @Override
    public boolean depends(DataObject obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DataObject duplicate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void load(Element elem) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Element save() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(DataObject obj) {
        // TODO Auto-generated method stub
        
    }

}
