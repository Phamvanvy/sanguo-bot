package pip;


import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import pip.io.UWAPSegment;


public class MonsterSprite{

    public static final byte STATUS_WAYPOINT = 0; //巡逻状态
    public static final byte STATUS_FOLLOW = 1; //跟踪主角状态
    public static final byte STATUS_GOBACK = 2; //返回原地

    public static final byte WPSTATUS_START = 0; //开始按照路点前进
    public static final byte WPSTATUS_END = 1; //已到达最后一个路点
    public static final byte WPSTATUS_WALK = 2;//按照路点行进中

    public static final byte TYPE_MONSTER = 0; //怪物
    public static final byte TYPE_NETPLAYER = 1; //网络玩家
    public static final byte TYPE_NPC = 2;

    public static final byte STEP = 1;
    public static final byte FOLLOWSTEP = 3;

    public static final int CLR_NPCNAME = 0xcccccc;

    public byte step = STEP;

    public byte type;

    public static int dimID;

    //=======task hint
    public static final byte[][] TASK_HINT_FRAME = {
                    //! (NPC_HINT_HAS_TASK)
                    {
                                    0, 0, 0, 0, 1, 1, 1, 1
                    },
                    //?(NPC_HINT_DOING_TASK)
                    {
                                    6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9
                    },
                    //?(NPC_HINT_FINISH_TASK)
                    {
                                    2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5
                    }

    };

    public static final byte NPC_HINT_NONE = 0;
    public static final byte NPC_HINT_HAS_TASK = 1;
    public static final byte NPC_HINT_DOING_TASK = 2;
    public static final byte NPC_HINT_FINISH_TASK = 3;

    public byte taskHintFrame;

    public void setTaskHint(byte hint){
        if(arrayIndex < hint){
            arrayIndex = hint;
            taskHintFrame = 0;
        }
    }

    public void clearHint(){
        arrayIndex = NPC_HINT_NONE;
        taskHintFrame = 0;
    }

    //==================

    /**
     * TYPE_MONSTER:    arrayIndex<br>
     * TYPE_NPC:        (hintStatus)任务提示状态，没任务，有信任务，有任务未完成，已完成任务
     */
    public int arrayIndex;

    /**
     * TYPE_MONSTER:    怪物组ID<br>
     * TYPE_NETPLAYER:  角色ID<br>
     * TYPE_NPC:        (id)NPC ID<br>
     */
    public int id;
    /**
     * TYPE_MONSTER:    怪物刷新时间<br>
     * TYPE_NETPLAYER:  用户级别<br>
     * TYPE_NPC:        (refreshTime)刷新时间<br>
     */
    public short refreshTime;

    /**
     * TYPE_MONSTER:    x<br>
     * TYPE_NETPLAYER:  x<br>
     * TYPE_NPC:        x
     */
    public int rx;
    /**
     * TYPE_MONSTER:    y<br>
     * TYPE_NETPLAYER:  y<br>
     * TYPE_NPC:        y<br>
     */
    public int ry;
    /**
     * TYPE_MONSTER:    警戒范围<br>
     * TYPE_NETPLAYER:  场景号<br>
     * TYPE_NPC:        imageID<br>
     */
    public short alertRange;
    /**
     * TYPE_MONSTER:    标志<br>
     * TYPE_NETPLAYER:  组队状态    0 – 未组队，1 – 队长，2 – 队员。<br>
     * TYPE_NPC:        flag
     */
    public byte flag;

    /**
     * TYPE_MONSTER:    阵营<br>
     * TYPE_NETPLAYER:  转生次数<br>
     * TYPE_NPC:        (type)NPC类型 
     */
    public byte politics;
    /**
     * TYPE_MONSTER:    图标ID<br>
     * TYPE_NETPLAYER:  用户性别<br>
     * TYPE_NPC:        (boolean inYOrder)
     */
    public short iconID;

    /**
     * type = TYPE_NETPLAYER时有效
     */
    public PetSprite petCurrent = null;

    /**
     * type = TYPE_NETPLAYER:   玩家名称<br>
     * type = TYPE_NPC:         (name)NPC名称
     */
    public String playerName = "";
    public String tongName = "";
    public String titleName = "";
    public String creditName = "";
    public String questionName = null;

    public boolean inBattle = false;
    public int color;
    public byte protectMode = 0;

    public boolean needWholeUpdate;
    public byte teamRole;
    public boolean followMode;

    public static final byte TEAM_ROLE_NONE = 0;
    public static final byte TEAM_ROLE_MEMBER = 1;
    public static final byte TEAM_ROLE_LEADER = 2;

    public byte x;
    public byte y;
    public short viewRange;
    public byte[] moveRange;

    public int backx;
    public int backy;

    /**
     * type = TYPE_MONSTER: 表示怪物类型（0表示本地刷新，1表示服务器刷新）<br>
     * type = TYPE_NPC:     (sendMessage)接触后是否需要向服务器发送消息<br>
     */
    public boolean serverRefresh;

    /**
     * type = TYPE_MONSTER: 示战斗失败后，怪物是否消失(0表示不消失，1表示消失)
     * type = TYPE_NPC:     (changeMessage)表改变状态后是否需要广播消息（0不广播，1广播）
     */
    public boolean hide;

    /**
     * 表示怪物是否可见（0不可见，1可见）
     */
    public boolean visible;
    public byte frame = 0;
    public byte frameIndex = 0;

    public ImageSet imageSet;

    public byte status = STATUS_WAYPOINT;
    public byte direction;

    public Vector wpList;
    public int wpPointer;
    public byte wpDir;
    public byte wpType;

    public short[][] armys;

    public byte[][] frameSequence;
    public byte dir;

    public MonsterSprite(byte type){
        this.type = type;
    }

    public MonsterSprite(MonsterSprite np){
        try{
            loadNetPlayer(np, true);
        }catch(IOException e){
            // TODO Auto-generated catch block
            //#debug
            e.printStackTrace();
        }
    }

    public void paint(Graphics g){
        if(!visible){
            return;
        }

        int frame = this.frame;
        if(type == TYPE_NETPLAYER){
            frame = frameSequence[dir][frameIndex];

            int fw = 0;
            int ty = ry - World.viewY - imageSet.getHeight(0);

            if(protectMode != 0){
                if(World.nameFlag != 1){
                    fw = GameState.font.stringWidth(playerName) + World.protectImageWidth;
                }else{
                    fw = World.protectImageWidth;
                }
            }else{
                fw = GameState.font.stringWidth(playerName);
            }

            int tx = rx - World.viewX - (fw - imageSet.getWidth(0)) / 2;

            if(World.nameFlag != 1 || id == dimID){
                if(protectMode != 0){
                    g.drawImage(World.protectImage, tx, ty, Graphics.BOTTOM | Graphics.LEFT);
                    tx += World.protectImageWidth;
                }

                g.setFont(GameState.font);
                //#if Draw3DString == TRUE                
                if(id == dimID){
                    World.draw3DString(g, inBattle? playerName + "(*)": playerName, tx, ty, Graphics.LEFT | Graphics.BOTTOM, 0x000000, color == 0x000000? Sprite.CLR_NAME: color);
                }else{
                    World.draw3DString(g, inBattle? playerName + "(*)": playerName, tx, ty, Graphics.LEFT | Graphics.BOTTOM, color == 0x000000? Sprite.CLR_NAME: color);
                }
                //#else
                //# if(id == dimID){
                //# 	g.setColor(0x000000);
                //#	}else {
                //# 	g.setColor(color==0x000000?Sprite.CLR_NAME:color);
                //# }
                //# g.drawString(inBattle?playerName+"(*)":playerName, tx, ty, Graphics.LEFT | Graphics.BOTTOM);
                //#endif

                String drawName = "";

                if(World.titleFlag == 0){
                    if(!tongName.equals("")){
                        drawName = "<" + tongName + ">";
                    }
                }else if(World.titleFlag == 1){
                    drawName = titleName;
                }else if(World.titleFlag == 2){
                    drawName = creditName;
                }else if(World.titleFlag == 3){
                    drawName = creditName;
                }

                if(!drawName.equals("")){
                    ty -= GameState.LINE_HEIGHT - 2;
                    //#if Directory == SE-K300
                    //# ty += 2;
                    //#endif

                    tx = rx - World.viewX - (GameState.font.stringWidth(drawName) - imageSet.getWidth(0)) / 2;
                    //#if Draw3DString == TRUE
                    if(id == dimID){
                        World.draw3DString(g, drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM, 0x000000, color == 0x000000? Sprite.CLR_NAME: color);
                    }else{
                        World.draw3DString(g, drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM, color == 0x000000? Sprite.CLR_NAME: color);
                    }
                    //#else
                    //# if(id == dimID){
                    //#     g.setColor(0x000000);
                    //# }else {
                    //#     g.setColor(color==0x000000?Sprite.CLR_NAME:color);
                    //# }
                    //# g.drawString(drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM);
                    //#endif
                }

                if(id == dimID){
                    if(World.titleFlag == 0 || World.titleFlag == 4){
                        if(titleName.equals("") || creditName.equals("")){
                            drawName = titleName + creditName;
                        }else{
                            drawName = titleName + "-" + creditName;
                        }
                    }else if(World.titleFlag == 1){
                        drawName = creditName;
                    }else if(World.titleFlag == 2){
                        drawName = titleName;
                    }else if(World.titleFlag == 3){
                        drawName = titleName;
                    }

                    ty -= GameState.LINE_HEIGHT - 2;
                    //#if Directory == SE-K300
                    //# ty += 2;
                    //#endif

                    tx = rx - World.viewX - (GameState.font.stringWidth(drawName) - imageSet.getWidth(0)) / 2;
                    //#if Draw3DString == TRUE
                    World.draw3DString(g, drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM, 0x000000, color == 0x000000? Sprite.CLR_NAME: color);
                    //#else
                    //# g.setColor(0x000000);
                    //# g.drawString(drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM);
                    //#endif
                }
            }else{
                if(protectMode != 0){
                    g.drawImage(World.protectImage, tx, ty, Graphics.BOTTOM | Graphics.LEFT);
                }
            }

            if(flag == 1 && id != dimID){
                ty -= GameState.font.getHeight() + 6;
                ty += ((World.tick / 3) % 2) * 2;
                GameState.drawButtons(g, (byte)3, tx + (fw - 6) / 2, ty);
            }

        }else if(type == TYPE_NPC){
            imageSet.drawFrame(g, frame, rx * World.tileWidth - World.viewX, (ry + 1) * World.tileHeight - World.viewY, Graphics.BOTTOM | Graphics.LEFT);

            int px = World.player.getX() + World.player.getWidth() / 2;
            int py = World.player.getY();

            int d = (px - (rx * World.tileWidth + imageSet.getWidth(0) / 2)) * (px - (rx * World.tileWidth + imageSet.getWidth(0) / 2)) + (py - ((ry + 1) * World.tileHeight))
                            * (py - (ry + 1) * World.tileHeight);

            boolean showName = false;
            if(d < GameState.NPC_SHOWNAME_SPACE * GameState.NPC_SHOWNAME_SPACE){
                showName = true;
                g.setFont(GameState.font);
                int nx = rx * World.tileWidth - World.viewX;
                int ny = (ry + 1) * World.tileHeight - World.viewY - imageSet.getHeight(0);
                int fw = GameState.font.stringWidth(playerName);
                nx -= (fw - imageSet.getWidth(0)) / 2;
                World.draw3DString(g, playerName, nx, ny, Graphics.BOTTOM | Graphics.LEFT, CLR_NPCNAME);
            }

            //draw taskhint
            if(arrayIndex != NPC_HINT_NONE){
                int hf = TASK_HINT_FRAME[arrayIndex - 1][taskHintFrame];
                int hx = rx * World.tileWidth + imageSet.getWidth(0) / 2 - World.viewX;
                int hy = (ry + 1) * World.tileHeight - World.viewY - imageSet.getHeight(0) - (showName? GameState.CHAR_HEIGHT: 0) - 2;

                World.taskHint.drawFrame(g, hf, hx, hy, Graphics.HCENTER | Graphics.BOTTOM);

            }
        }
        if(type == TYPE_NETPLAYER && (World.nameFlag != 2 || id == dimID) || type == TYPE_MONSTER){
            try{
                imageSet.drawFrame(g, frame, rx - World.viewX, ry - World.viewY, Graphics.BOTTOM | Graphics.LEFT);
            }catch(Exception e){
            }
        }
    }

    public int[] getPetBackXY(){
        int ret[] = {
                        rx + getWidth() / 2, ry
        };
        switch(dir){
            case Sprite.UP:
            case Sprite.DOWN:
                ret[0] += getWidth();
                if(ret[0] < 5){
                    ret[0] = rx + getWidth() / 2 + getWidth();
                }
                ret[1] += getHeight() / 2;
                if(ret[1] - World.viewY > World.viewHeight - 5){
                    ret[1] = ry - getHeight() / 2;
                }
                break;
            case Sprite.LEFT:
            case Sprite.RIGHT:
                ret[0] -= getWidth();
                if(ret[0] < 5){
                    ret[0] = rx + getWidth() / 2 + getWidth();
                }
                ret[1] += getHeight() / 2;
                if(ret[1] - World.viewY > World.viewHeight - 5){
                    ret[1] = ry - getHeight() / 2;
                }
                break;
        }
        return ret;
    }

    public void cycle(long delta, World world){
        if(!visible || (type == TYPE_NETPLAYER && alertRange != World.currMapId)){
            return;
        }

        if(type == TYPE_MONSTER){
            if(World.tick % 3 == 0){
                frame++;
            }

            //#if (DrawMethod == SetClip) || (DrawMethod == DrawRegion)
            int maxFrame = imageSet.getFrameLength();
            //#elif DrawMethod == DrawPixels
            //# int maxFrame = imageSet.pos.length;
            //#else
            //# int maxFrame = imageSet.frames.length;
            //#endif

            if(frame >= maxFrame){
                frame = 0;
            }
        }else if(type == TYPE_NPC){
            if(arrayIndex != NPC_HINT_NONE){
                taskHintFrame++;
                if(taskHintFrame >= TASK_HINT_FRAME[arrayIndex - 1].length){
                    taskHintFrame = 0;
                }
            }
        }else{
            if(x == -1 || y == -1){
                x = (byte)((rx + getWidth() / 2) / World.tileWidth);
                y = (byte)(ry / World.tileHeight);
            }

            frameIndex++;
            if(frameIndex >= frameSequence[dir].length){
                frameIndex = 0;
            }

            if(petCurrent != null){
                int xy[] = getPetBackXY();

                int dx = petCurrent.x - xy[0];
                dx *= dx;

                int dy = petCurrent.y - xy[1];
                dy *= dy;

                if(dx + dy > World.viewWidth * World.viewWidth){
                    petCurrent.x = (short)xy[0];
                    petCurrent.y = (short)xy[1];
                }else{
                    petCurrent.wpMoveTo(xy[0], xy[1]);
                    petCurrent.cycle(delta, world);
                }
            }

        }

        int[] origin = World.player.getOrigin();
        int dx = origin[0] - rx;
        int dy = origin[1] - ry;

        int dest = dx * dx + dy * dy;

        if(type == TYPE_MONSTER && World.player.runAwayTime == -1 && inMoveRange(origin[0], origin[1]) && viewRange > 0 && dest < viewRange * viewRange && status == STATUS_WAYPOINT && !World.GOD_MODE
                        && (World.teamStatus != World.TEAM_STATUS_FOLLOW || World.teamLeader)){
            //进入追击范围
            status = STATUS_FOLLOW;
            backx = rx + getWidth() / 2;
            backy = ry;
            step = FOLLOWSTEP;
        }

        switch(status){
            case STATUS_WAYPOINT:
                if(wpList != null){
                    if(frameSequence != Sprite.FRAMESEQUENCE_WALK)
                        setFrameSequence(Sprite.FRAMESEQUENCE_WALK);
                    goWithWayPoint();
                }

                break;
            case STATUS_FOLLOW:
                if(inMoveRange(origin[0], origin[1]) && !World.GOD_MODE){
                    moveTo(origin[0], origin[1]);

                    if(World.nowBattle >= 0){
                        status = STATUS_WAYPOINT;
                        rx = backx - getWidth() / 2;
                        ry = backy;
                    }else if(dest <= alertRange * alertRange){
                        try{
                            World.instance.startBattle(arrayIndex);
                            status = STATUS_WAYPOINT;
                            rx = backx - getWidth() / 2;
                            ry = backy;
                        }catch(IOException e){
                            //#debug
                            e.printStackTrace();
                        }
                    }

                }else{
                    status = STATUS_GOBACK;
                }

                break;
            case STATUS_GOBACK:
                moveTo(backx, backy);
                step = STEP;

                if(rx + getWidth() / 2 == backx && ry == backy){
                    status = STATUS_WAYPOINT;
                }

                break;
        }

    }

    public boolean inMoveRange(int dx, int dy){
        int tx = dx / World.tileWidth;
        int ty = dy / World.tileHeight;

        if(moveRange[0] == 0 && moveRange[1] == 0 && moveRange[2] == 0 && moveRange[3] == 0){
            return true;
        }

        if(tx >= moveRange[0] && tx < moveRange[0] + moveRange[2] && ty >= moveRange[1] && ty < moveRange[1] + moveRange[3]){
            return true;
        }

        return false;
    }

    public int[] getBackXY(){
        int ret[] = {
                        rx + getWidth() / 2, ry
        };
        switch(dir){
            case Sprite.UP:
                ret[1] += /*World.tileHeight*/getHeight() / 2;
                break;
            case Sprite.DOWN:
                ret[1] -= /*World.tileHeight*/getHeight() / 2;
                break;
            case Sprite.LEFT:
                ret[0] += getWidth() + 2;
                break;
            case Sprite.RIGHT:
                ret[0] -= getWidth() + 2;
                break;
        }
        return ret;
    }

    public short getWidth(){
        return (short)imageSet.getWidth(frame);
    }

    public short getHeight(){
        return (short)imageSet.getHeight(frame);
    }

    public int[] getCollisionBox(){
        int x1, y1, w1, h1;
        int x = this.rx;
        int y = this.ry;

        if(type == TYPE_NPC){
            x = rx * World.tileWidth;
            y = ry * World.tileHeight;
        }

        if(imageSet.collision != null && imageSet.collision[frame] != null){
            x1 = x * World.tileWidth + imageSet.collision[frame][0];
            y1 = y - imageSet.getHeight(frame) + imageSet.collision[frame][1];
            w1 = imageSet.collision[frame][2];
            h1 = imageSet.collision[frame][3];
        }else{
            x1 = x;
            y1 = y;
            w1 = getWidth();
            h1 = World.tileHeight;
        }

        return new int[]{
                        x1, y1, w1, h1
        };
    }

    public void move(int dx, int dy){
        switch(direction){
            case Sprite.LEFT:
                dx = -dx;
                dy = 0;

                break;
            case Sprite.DOWN:
                dx = 0;

                break;
            case Sprite.RIGHT:
                dy = 0;

                break;
            case Sprite.UP:
                dx = 0;
                dy = -dy;

                break;
        }

        go(dx, dy);

    }

    public void go(int dx, int dy){
        this.rx += dx;
        this.ry += dy;
        x = (byte)(rx / World.tileWidth);
        y = (byte)(ry / World.tileHeight);
    }

    private byte goWithWayPoint(){
        byte status = WPSTATUS_WALK;

        int npcX = rx + imageSet.getWidth(0) / 2;
        int npcY = ry;
        int dest = ((Integer)(wpList.elementAt(wpPointer))).intValue();

        int destx = (dest >> 16) + imageSet.getWidth(0) / 2;
        int desty = dest & 0x0000ffff;

        int diffx = destx - npcX;
        int diffy = desty - npcY;

        if(diffx == 0 && diffy == 0){
            //到达路点
            wpPointer += wpDir;

            if(wpPointer == wpList.size()){
                //到达最后一个路点
                status = WPSTATUS_END;

                if(type == TYPE_MONSTER){
                    if(wpType == 0){
                        //绕圈
                        wpPointer = 1;
                    }else{
                        //往返
                        wpPointer -= 2;
                        wpDir = -1;
                    }
                }else{
                    wpList = null;
                    setFrameSequence(Sprite.FRAMESEQUENCE_STAND);
                    frame = 0;
                }
            }else if(wpPointer == -1){
                //到达第一个路点
                status = WPSTATUS_START;

                if(wpType == 1){
                    //往返
                    wpPointer = 1;
                    wpDir = 1;
                }
            }
        }else{
            moveTo(destx, desty);
        }

        return status;
    }

    public void addWayPoint(byte x, byte y){
        short rx = (short)(x * World.tileWidth);
        short ry = (short)(y * World.tileHeight);
        addWayPoint(rx, ry);
    }

    public void addWayPoint(short x, short y){
        if(wpList == null){
            wpList = new Vector();
            wpList.addElement(new Integer(this.rx << 16 | this.ry));
            wpPointer = 1;
            wpDir = 1;
        }
        wpList.addElement(new Integer((x << 16 | y)));
    }

    public boolean moveTo(int destx, int desty){
        int dx, dy;
        int npcX = rx + imageSet.getWidth(0) / 2;
        int npcY = ry;

        if(type == TYPE_NETPLAYER){
            dx = Sprite.STEP;
        }else{
            dx = step;
        }
        dy = dx;

        int diffx = Math.abs(destx - npcX);
        int diffy = Math.abs(desty - npcY);

        if(diffx >= diffy && diffx != 0){
            int v = dx * 10000 / Math.abs(diffx);
            dy = diffy * v / 10000;
        }

        if(diffx < diffy && diffy != 0){
            int v = dy * 10000 / Math.abs(diffy);
            dx = diffx * v / 10000;
        }

        if(Math.abs(diffx) < dx){
            dx = Math.abs(diffx);
        }

        if(Math.abs(diffy) < dy){
            dy = Math.abs(diffy);
        }

        if(destx - npcX < 0){
            dx = -dx;
        }

        if(desty - npcY < 0){
            dy = -dy;
        }

        go(dx, dy);

        if(type == TYPE_NETPLAYER){
            int difx = destx - npcX;
            int dify = desty - npcY;
            byte dir = this.dir;
            if(Math.abs(difx) > Math.abs(dify)){
                if(difx > 0)
                    dir = Sprite.RIGHT;
                else if(difx < 0)
                    dir = Sprite.LEFT;
            }else{
                if(dify > 0)
                    dir = Sprite.DOWN;
                else if(dify < 0)
                    dir = Sprite.UP;
            }
            if(dir != this.dir){
                this.dir = dir;
                frame = 0;
            }
        }

        return dx == 0 && dy == 0;
    }

    public void load(Object dis) throws IOException{
        if(type == TYPE_MONSTER)
            loadMonster((DataInputStream)dis);
        else
            loadNetPlayer((UWAPSegment)dis);
    }

    public void loadNetPlayer(MonsterSprite np, boolean updatePosition) throws IOException{
        type = np.type;
        id = np.id;
        playerName = np.playerName;
        iconID = np.iconID;
        color = np.color;
        inBattle = np.inBattle;
        protectMode = np.protectMode;

        setFrameSequence(Sprite.FRAMESEQUENCE_STAND);

        imageSet = World.getPlayerImage(World.getFaceIndex(iconID, false));

        refreshTime = np.refreshTime;

        if(updatePosition){
            alertRange = np.alertRange;
            rx = np.rx;
            ry = np.ry;

            if(World.tileWidth != 0){
                x = (byte)((rx + getWidth() / 2) / World.tileWidth);
                y = (byte)(ry / World.tileHeight);
            }else{
                x = -1;
                y = -1;
            }

            dir = Sprite.DOWN;
            wpList = null;
        }

        flag = np.flag;
        tongName = np.tongName;
        titleName = np.titleName;
        creditName = np.creditName;
        politics = np.politics;

        visible = true;

        if(np.petCurrent != null){
            petCurrent = np.petCurrent;
        }
    }

    public void loadNetPlayer(UWAPSegment dis) throws IOException{
        needWholeUpdate = false;

        id = dis.readInt();

        int mapFlag = dis.readInt();
        alertRange = (short)(mapFlag & 0xFFFF);
        mapFlag >>= 16;
        mapFlag &= 0xFFFF;

        int xyFlag = dis.readInt();
        rx = (xyFlag >> 16) & 0xFFFF;
        ry = xyFlag & 0xFFFF;

        if(mapFlag != 0 && alertRange >= 0){
            needWholeUpdate = true;

            playerName = dis.readString();
            iconID = dis.readByte();

            setFrameSequence(Sprite.FRAMESEQUENCE_STAND);

            imageSet = World.getPlayerImage(World.getFaceIndex(iconID, false));

            refreshTime = dis.readShort();

            if(World.tileWidth != 0){
                x = (byte)((rx + getWidth() / 2) / World.tileWidth);
                y = (byte)(ry / World.tileHeight);
            }else{
                x = -1;
                y = -1;
            }

            flag = dis.readByte();
            tongName = dis.readString();
            politics = dis.readByte();

            dir = Sprite.DOWN;
            visible = true;

            wpList = null;

            byte petType = dis.readByte();

            String petName = "";
            boolean petBaby;
            short petLevel;

            if(petType > 0){
                petName = dis.readString();
                petBaby = dis.readBoolean();
                petLevel = dis.readShort();

                PetSprite ps = new PetSprite(this, petType, 0, 0, petName);

                if(petCurrent != null){
                    ps.x = petCurrent.x;
                    ps.y = petCurrent.y;
                }else{
                    ps.x = (short)rx;
                    ps.y = (short)ry;
                }

                petCurrent = ps;
            }else{
                petCurrent = null;
            }

            try{
                titleName = dis.readString();
                creditName = dis.readString();

                color = dis.readInt();
                inBattle = dis.readBoolean();
                protectMode = dis.readByte();

                //#mdebug
                if(petType > 0){
                    System.out.println("net move : mapid(" + alertRange + ") " + id + " , " + playerName + "，称号：" + titleName + "，荣誉：" + creditName + " ，" + rx + " , " + ry + " , " + petType + " , "
                                    + petName);
                }else{
                    System.out.println("net move : mapid(" + alertRange + ") " + id + " , " + playerName + "，称号：" + titleName + "，荣誉：" + creditName + " , " + rx + " , " + ry + " , no pet");
                }
                //#enddebug
            }catch(Exception e){
            }
        }else{
            MonsterSprite old = World.findInNetPlayers(id);

            if(old == null){
                needWholeUpdate = true;

                playerName = "未知";
                iconID = 0;
                imageSet = World.playerImageSet[0];
                refreshTime = 1;

                setFrameSequence(Sprite.FRAMESEQUENCE_STAND);

                if(World.tileWidth != 0){
                    x = (byte)((rx + getWidth() / 2) / World.tileWidth);
                    y = (byte)(ry / World.tileHeight);
                }else{
                    x = -1;
                    y = -1;
                }

                flag = 0;
                tongName = "";
                politics = 0;

                dir = Sprite.DOWN;
                visible = true;

                wpList = null;
                petCurrent = null;

                //#debug
                System.out.println("net move error : mapid(" + alertRange + ") " + id + " , " + playerName + " , " + rx + " , " + ry);
            }else{
                //#debug
                System.out.println("net move quick : mapid(" + alertRange + ") " + id + " , " + old.playerName + " , " + rx + " , " + ry);
            }
        }
    }

    public void loadMonster(DataInputStream dis) throws IOException{
        id = dis.readInt();

        iconID = dis.readShort();
        flag = dis.readByte();

        serverRefresh = (flag & 1) != 0;
        hide = (flag & 2) != 0;

        if(serverRefresh){
            visible = true;
        }else{
            visible = (flag & 0x80) != 0;
        }

        if(World.monsterRefreshPool != null){
            Integer rt = (Integer)World.monsterRefreshPool.get(new Integer(id));
            if(rt != null){
                if(rt.intValue() > 0){
                    visible = false;
                }else{
                    World.monsterRefreshPool.remove(new Integer(id));
                }
            }
        }

        refreshTime = dis.readShort();
        if(refreshTime == 0){
            //#debug
            System.out.println("monster " + id + " refreshTime = 0 , reset");
            refreshTime = 240;
        }

        //#debug
        //refreshTime = 10;

        x = dis.readByte();
        y = dis.readByte();
        rx = x * World.tileWidth;
        ry = (y + 1) * World.tileHeight;

        politics = dis.readByte();

        byte tmp = dis.readByte();
        parseRange(tmp);

        moveRange = new byte[4];
        dis.read(moveRange);

        int len = dis.readByte();

        if(len != 0){
            wpList = new Vector();
        }

        for(int i = 0; i < len; i++){
            int dest = dis.readShort();

            byte destx = (byte)(dest >> 8);
            byte desty = (byte)(dest & 0x00ff);
            addWayPoint(destx, desty);
        }

        if(len == 1){
            wpList = null;
        }

        if(wpList != null){
            int startWP = ((Integer)wpList.elementAt(0)).intValue();
            int endWP = ((Integer)wpList.elementAt(wpList.size() - 1)).intValue();

            if(startWP == endWP){
                wpType = 0;
            }else{
                wpType = 1;
            }
            wpPointer = 1;
            wpDir = 1;
        }

        armys = new short[3][2];

        int size = (int)dis.readByte();
        for(int i = 0; i < size; i++){
            armys[i][0] = dis.readByte();
            armys[i][1] = dis.readShort();
        }
    }

    public void parseFlag(){
        serverRefresh = (flag & 1) != 0;
        visible = (flag & 0x80) != 0;
        hide = (flag & 4) != 0;
        frame = 0;
    }

    public void parseRange(byte value){
        viewRange = (byte)(value >> 3);
        alertRange = (byte)(value & 0x7);
        viewRange = (short)(viewRange * World.tileWidth / 2);
        alertRange = (short)(alertRange * World.tileWidth / 2);
    }

    public short[] parseID(){
        return new short[]{
                        (short)(id & 0x1fff), (short)((id & 0xffff) >> 13), (short)(id >> 16)
        };
    }

    public void setImageSet(ImageSet image){
        this.imageSet = image;
        if(type == TYPE_NETPLAYER)
            setFrameSequence(Sprite.FRAMESEQUENCE_STAND);
    }

    public void setFrameSequence(byte[][] inSequence){
        frameSequence = inSequence;
        frameIndex = 0;
    }
}