
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import billing.*;
import ut.*;
public class Loging extends Canvas implements LogingPage
{    
    /**
    * 應用程式物件。
    */
    public  static MIDlet application;
    /**
    * 顯示物件。
    */
    public static Display display;
    public boolean leavewhile; 
    boolean isOK;
    public Loging()
    {
    }
    public boolean doLogin( MIDlet application )
    {
        
        boolean result = false;
        this.application = application;
        this.display = Display.getDisplay( application ); 
        Displayable last = this.display.getCurrent();        
        this.display.setCurrent( this );
        
        
        while(true)
        {
            if(leavewhile) 
            {
            	synctsi();
            	break;            
            }
            repaint();
        }
        leavewhile = false;
        this.display.setCurrent( last );
        if(isOK)result=true;
        return result;
    }
    protected void paint(Graphics g)
    {      
        g.setColor(0xFFFFFF);
        g.fillRect(0,0,300,300);
        g.setColor(0x000000);
        g.drawString( "按下1 登入 /2 返回", 10 ,10, Graphics.TOP | Graphics.LEFT); 
    }
    public static void synctsi()
    {
        String temp = Utilities.executeHttpRequest( 
        			"http://idc2.somuch.com.tw/igb/api/index.php?do=tsi&ac=sync&ver=4.0"
        			+"&qmeacc=ping"+"&qmepwd=pingkey"
        			+(IGBC.use_test_tsi ? "&rd=1":"") 
                                                     , 0, 1 );
        System.out.println("temp="+temp);
        String[] tempdata = Utilities.stringTokenize( temp, ',' );
        UserData.setTsiData(tempdata);
                                            
    }
    
    protected void keyPressed( int keyCode )
    {
       int action = super.getGameAction(keyCode); 
       switch(keyCode)
       {
           case KEY_NUM1:
        	   
                isOK = true;
                leavewhile = true;
                System.out.println("1");
           break; 
           case KEY_NUM2:
                isOK = false;
                leavewhile = true;
                System.out.println("2");
           break; 
           case FIRE:
           //leavewhile = true;
           break;   
       } 
    }
}
