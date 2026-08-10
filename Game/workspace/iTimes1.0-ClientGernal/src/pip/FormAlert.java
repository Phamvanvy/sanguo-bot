package pip;


import javax.microedition.lcdui.*;


/**
 * @author Jeffery
 * @version 1.0
 */
public class FormAlert
//#if JBlend == true
                extends Canvas implements CommandListener
//#endif
{
    //#if JBlend == true
    private Displayable _pre;
    private String _msg[];

    public FormAlert(Displayable pre, String title, String msg){
        _pre = pre;
        _msg = World.splitString(msg, World.viewWidth - 10, GameState.font);
        addCommand(new Command("·µ»Ø", Command.BACK, 0));
        setCommandListener(this);
    }

    public void paint(Graphics g){
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0x000000);
        g.setFont(GameState.font);
        int h = GameState.font.getHeight() * _msg.length;
        int y = (getHeight() - h) / 2;
        for(int i = 0; i < _msg.length; i++){
            g.drawString(_msg[i], getWidth() / 2, y, Graphics.TOP | Graphics.HCENTER);
            y += GameState.font.getHeight();
        }
    }

    public void commandAction(Command command, Displayable displayable){
        //World.display.setCurrent(_pre);
        World.RecordPreousDisplay(_pre);
    }
    //#endif
}