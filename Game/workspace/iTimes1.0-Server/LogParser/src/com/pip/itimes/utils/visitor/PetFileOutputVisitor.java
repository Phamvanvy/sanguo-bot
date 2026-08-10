package com.pip.itimes.utils.visitor;

import java.io.*;

import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PetFileOutputVisitor
    extends AbstractVisitor {

    private BufferedWriter writer = null;

    public PetFileOutputVisitor(File out) throws Exception{
        writer = new BufferedWriter(new FileWriter(out));
    }



    public void visit(BattleResultAction action) {
        IAward[] award = action.getAwards();
        String s = "玩家["+action.getSource()+"]从怪物["+action.getMonsterId()+"]获得了[";
        for(int i=0;i<award.length;i++){
            s += award[i].toString();
        }
        s += "]";
        try {
            writer.write(s);
            writer.newLine();
        }
        catch (IOException ex) {
        }
    }

    public void close(){
        if(writer!=null){
            try {
                writer.close();
            }
            catch (IOException ex) {
            }
        }
    }
}
