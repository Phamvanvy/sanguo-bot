package patchs;

import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.NormalEquipment;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.IItemTemplate;
import java.lang.reflect.Field;

public class ModifyEquipment implements Runnable{
    public void run(){
        WorldPlayer player = Server.instance.playerService.getWorldPlayer(38656);
        IItemTemplate template = Items.getTemplate(1001180);
        if(player!=null){
            Grid[] grids = player.getEquipments();
            for(int i=0;i<grids.length;i++){
                if(grids[i]!=null){
                    NormalEquipment equ = (NormalEquipment)grids[i].item;
                    if(equ.getItemId()==1001180){
                        try {
                            Field field = NormalEquipment.class.getDeclaredField("template");
                            field.setAccessible(true);
                            field.set(equ, template);
                            System.out.println("modifyok");
                            break;
                        } catch (IllegalAccessException ex) {
                        } catch (IllegalArgumentException ex) {
                        } catch (SecurityException ex) {
                        } catch (NoSuchFieldException ex) {
                        }
                    }
                }
            }
        }
    }
}
