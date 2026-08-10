package com.pip.sanguo;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimateCache;
import com.pip.engine.AnimatePlayer;
import com.pip.engine.IAnimateCallback;
import com.pip.engine.IAnimateOwner;
import com.pip.engine.IVMGameProcessor;
import com.pip.engine.Sprite;
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;
import com.pip.io.UASegment;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

/**
 * @author ybai
 *
 */
public abstract class GameSprite implements IAnimateCallback, IAnimateOwner, IVMGameProcessor{
    private byte type;
    private int id;
    private int instanceId;
    private String name;
    private String[] extraName;

    private Hashtable gameData = new Hashtable();

    public Sprite sprite;
    protected VM vm;
    protected boolean onHorse;
    protected boolean canAttack; //是否可攻击
    protected boolean canSelect = true; //是否可选
    protected boolean hold;
    protected int weaponType;
    public boolean die;
    protected boolean attacking; //是否在攻击

    public int hpMax;//health point
    public int mpMax;//magic point
    public int hp;//health point
    public int mp;//magic point
    public int state;
    public byte faction;//阵营
    public int level;//级别
    public boolean isFollowing; //是否为跟随状态
    private boolean isTeamState;  //是否为组队状态
    public boolean infoRecved = false; //收到过sprite的完整信息了
    public boolean infoForceRequested = false; //是否已经在请求完整信息了
    public Hashtable status = new Hashtable(); //特殊状态
    public int horsepara; //坐骑的参数，高16位留用，低16位为imageId
    public int horsePoint; //坐骑的评分，目前留用，以后用来制作高级坐骑闪光等特效
    
    private Vector forceWayPointList = new Vector();
    public boolean forceWayPointMode;
    private int[] forceWayPoint;
    private int forceWayPointSpeed;
    
    public boolean chaseMode;
    protected boolean chaseAlways;
    protected int chaseDistanceAllow;
    protected int chaseSpeed;
    protected int chaseTargetInstanceId;
    protected Vector chaseList = new Vector();
    protected short[] chasePoint;
    protected int[] chaseCallbackPara;

    public int moveAnimateIndex;
    public int stopAnimateIndex;
    public int chaseMoveAnimateIndex;
    public int chaseStopAnimateIndex;
    public int pendingStopAnimateIndex;  //由移动到停下来有段时间处于pending状态
    public int chasePendingStopAnimateIndex;  //由移动到停下来有段时间处于pending状态
    
    private int pendingTick;
    
    public boolean miniMapShow = false;
    public boolean forceMiniMapShow = false;
    public int[] miniMapColor = null;
    
    public Object[] miniMapImage = null;
    
    /**
     * 走出视野后归位的位置
     */
    public int[] leavingPosition = null;

    public GameSprite followOwner;//该GameSprite跟随的宿主对象  

    //#if ModelID == AndroidAuto
    //# public static short DEFAULT_SPEED = 45;
    //#elif DoubleScreen == true
    //# public static final short DEFAULT_SPEED = 90; //1秒60个像素
    //#else
    public static final short DEFAULT_SPEED = 45; //1秒60个像素
    //#endif
    
    public static final byte DEFAULT_DIR = Tool.DIR_DOWN;

    public static final int SPECIAL_STATUS_FEAR = 6;
    public static final int SPECIAL_STATUS_PALSY = 7;
    public static final int SPECIAL_STATUS_STOP = 8;
    
    //拥有的跟随者
    protected Vector followers = new Vector();

    //来自脚本播放的动画
    private Hashtable animates = new Hashtable();
    
    public boolean isOutView; //是否在视野外

    private boolean startForceFollow;
    
    protected GameSprite(int type, int id, int instanceId){
        this.type = (byte) type;
        this.id = id;
        this.instanceId = instanceId;
        this.name = "未知";

        sprite = new Sprite(this);
        sprite.setSpeed(DEFAULT_SPEED);
        onHorse = false;

        sprite.setDir(Tool.DIR_DOWN);
        sprite.setAnimateDir(Tool.DIR_DOWN);
        sprite.setWork(true);
        sprite.setCollision(false);
    }

    /**
     * 返回精灵类型
     * @return
     */
    public byte getType(){
        return type;
    }

    /**
     * 返回精灵id
     * @return
     */
    public int getId(){
        return id;
    }

    /**
     * 返回精灵名字
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * 设置精灵名字
     * @param name
     */
    public void setName(String name){
        this.name = name;
        this.infoRecved = true;
        
        int extraIdx = name.indexOf('|');
        
        if(extraIdx >= 0){
            this.name = name.substring(0, extraIdx);
            extraName = Tool.splitString(name.substring(extraIdx + 1), '|');
        }else{
            extraName = null;
        }
    }
    /**
     * 组队状态
     * @return
     */
	public boolean isTeamState(){
	  return isTeamState;	
	}
    /**
     * 添加一个跟随者
     * @param follower
     */
    public boolean addFollower(GameSprite follower){
        //限制跟随者数量, 跟随者不能是自己的宿主对象
        if(follower == null || followers.size() >= Tool.FOLLOW_DIS_MATRIX.length >> 1 || follower == followOwner || followers.contains(follower)){
            return false;
        }

        if(follower.followOwner != null){
            //解除跟随关系
            follower.followOwner.removeFollower(follower);
        }

        followers.addElement(follower);
        follower.followOwner = this;

        setFollowersPosition();

        return true;
    }

    /**
     * 删除一个跟随者
     * @param follower
     */
    public void removeFollower(GameSprite follower){
        follower.followOwner = null;
        followers.removeElement(follower);
    }

    /**
     *  删除所有跟随者
     */
    public void removeAllFollowers(){
        int count = followers.size();

        for(int i = 0; i < count; i++){
            ((GameSprite) followers.elementAt(i)).followOwner = null;
        }

        followers.removeAllElements();
    }

    void setFollowersPosition(){
        int count = followers.size();

        if(count > 0){
            int disx = Tool.FOLLOW_DIS_X;
            int disy = Tool.FOLLOW_DIS_Y;

            if((sprite.getDir() == Tool.DIR_DOWN || sprite.getDir() == Tool.DIR_UP)){
                //居中
                if(count == 2){
                    disx = disx >> 1;
                }
            }else{
                disx = Tool.FOLLOW_DIS_Y;
                disy = Tool.FOLLOW_DIS_X;

                //居中
                if(count == 2){
                    disy = disy >> 1;
                }
            }

            //第一个追随者的位置
            int firstX = sprite.getX() + disx * Tool.ASSIS_RIGHT_MATRIX[(sprite.getDir() << 1) + Tool.X_AXIS];
            int firstY = sprite.getY() + disy * Tool.ASSIS_RIGHT_MATRIX[(sprite.getDir() << 1) + Tool.Y_AXIS];
            int followX = 0;
            int followY = 0;
            for(int i = 0; i < count; i++){
                GameSprite gs = (GameSprite) followers.elementAt(i);
                if(gs.sprite.getMapId() == this.sprite.getMapId()) {
                	if(gs.sprite.getX() == gs.followOwner.sprite.getX() && gs.sprite.getY() == gs.followOwner.sprite.getY()) {
                		startForceFollow = false;
                		break;
                    }
                	
                    followX = firstX + Tool.FOLLOW_DIS_MATRIX[(i << 1) + ((sprite.getDir() == Tool.DIR_DOWN || sprite.getDir() == Tool.DIR_UP)? Tool.X_AXIS: Tool.Y_AXIS)]
                                                               * Tool.ASSIS_LEFT_MATRIX[(sprite.getDir() << 1) + Tool.X_AXIS];
                                               followY = firstY + Tool.FOLLOW_DIS_MATRIX[(i << 1) + ((sprite.getDir() == Tool.DIR_DOWN || sprite.getDir() == Tool.DIR_UP)? Tool.Y_AXIS: Tool.X_AXIS)]
                                                               * Tool.ASSIS_LEFT_MATRIX[(sprite.getDir() << 1) + Tool.Y_AXIS];

                                               
                                               if((gs.sprite.getX() != followX || gs.sprite.getY() != followY)){
                                            	   if(GameWorld.gameView.canMove(gs.sprite.getX(), gs.sprite.getY()) && startForceFollow == false) {
                                            		   gs.sprite.addWayPoint(followX, followY, gs.moveAnimateIndex, gs.stopAnimateIndex, gs.sprite.getSpeed(), this);   
                                            	   }else {
                                            		   gs.sprite.addWayPoint(gs.followOwner.sprite.getX(), gs.followOwner.sprite.getY(), gs.moveAnimateIndex, gs.stopAnimateIndex, gs.sprite.getSpeed(), this);
                                            		   startForceFollow = true;
                                            	   }
                                            	   
                                               }
                }

            }
        }

    }

    public void addWayPoint(int dest_x, int dest_y, int moveAnimateIndex, int stopAnimateIndex, boolean needGuess, int angle, int time, int speed, int targetPos){
        if(GameWorld.player.sprite.getMapId() == sprite.getMapId()) {
        	sprite.addWayPoint(dest_x, dest_y, moveAnimateIndex, stopAnimateIndex, needGuess, angle, time, speed, this, true, targetPos);
        } else {
        	sprite.setPosition(-1000, dest_y);
        }
    }

    public void setHeadStringConfig(int type, int space, int drawMode, int offsetx, int offsety, int order){
        int[] config = new int[6];

        config[0] = type;
        config[1] = space;
        config[2] = drawMode;
        config[3] = offsetx;
        config[4] = offsety;
        config[5] = order;

        sprite.setHeadStringConfig(config);
    }

    public void destroy(){
        Vector animateNames = sprite.getAllAnimateNames();

        for(int i = 0; i < animateNames.size(); i++){
            String animateName = (String) animateNames.elementAt(i);
            AnimateCache.releaseAnimate(this, animateName, false);
        }
        
        sendCommand(VMGame.GAME_COMMAND_SPRITE_DESTROY, null);
    }
    
    public synchronized void sendCommand(int command, Object commandData){
        synchronized(vm){
            int[] params = new int[3];

            params[0] = vm.makeTempObject(this);
            params[1] = command;
            params[2] = vm.makeTempObject(commandData);
            vm.callback(VMGame.CALLBACK_GAME_COMMAND, params);
        }
    }

    public void vm_sprite_set_mini_map_color(int[] miniMapColor){
        this.miniMapColor = new int[miniMapColor.length];
        System.arraycopy(miniMapColor, 0, this.miniMapColor, 0, miniMapColor.length);
    }
    
    public void vm_sprite_set_mini_map_image(String resName,int frame){
    	if(frame == -1){
    		this.miniMapImage = null;
    	} else {
        	this.miniMapImage = new Object[2];
        	this.miniMapImage[0] = resName;
        	this.miniMapImage[1] = new Integer(frame);
    	}
    }
    
    public void vm_sprite_set_mini_map_show(boolean show){
    	if(this.forceMiniMapShow==false){
    		this.miniMapShow = show;
    	}
    }
    
    public void forceMiniMapShow(int b){
   		this.forceMiniMapShow = b==1;
    }

    public int getForceMiniMapShow(){
   		return this.forceMiniMapShow?1:0;
    }
    
    public int vm_sprite_get_mini_map_show(){
        return this.miniMapShow?1:0;
    }
    
    public void vm_request_animates(String[] animateNames){
        for(int i = 0; i < animateNames.length; i++){
            AnimateCache.requestAnimate(this, animateNames[i]);
        }
    }
    
    public void vm_set_sprite_show(boolean show){
        sprite.setShow(show);
    }
    
    public void vm_add_animate(String animateName){
        String tmpName = VMGame.VM_DATA_PREFIX_ANIMATE + animateName;
        AnimatePlayer animate = (AnimatePlayer) gameData.get(tmpName);
        sprite.addAnimate(animate);
        gameData.remove(tmpName);
    }
    
    public void vm_remove_animate(String animateName){
        sprite.removeAnimate(animateName);
        AnimateCache.releaseAnimate(this, animateName, false);
    }
    
    public void vm_set_animate_show(String animateName, boolean show){
        if(show){
            sprite.showAnimate(animateName);
        }else{
            sprite.hideAnimate(animateName);
        }
    }
    
    public void vm_add_animate_replace_images(String animateName, String[] imageNames){
        int count = imageNames.length;
        
        for(int i = 0; i < count; i++){
            sprite.addReplaceAnimateImage(animateName, imageNames[i]);
        }
    }
    
    public boolean vm_test_animate_ok(String[] names){
        boolean allOk = true;
        
        for(int i = 0; i < names.length; i++){
            if(!sprite.hasAnimate(names[i])){
                allOk = false;
                
                break;
            }
        }
        
        return allOk;
    }
    
    public boolean vm_sprite_has_animate(String name){
        return sprite.hasAnimate(name);
    }
    
    public int[] vm_sprite_get_animate_box(){
        return sprite.getAnimateBox();
    }

    public String vm_sprite_get_name(){
        return getName();
    }
    
    public void vm_sprite_set_name(String _name){
        setName(_name);
    }
    
    public int vm_sprite_get_faction(){
        return faction;
    }
    
    public void vm_sprite_set_faction(int _faction){
        this.faction = (byte)_faction;
    }
    
    public int vm_sprite_get_level(){
        return level;
    }
    
    public void vm_sprite_set_level(int _level){
        this.level = _level;
    }
    
    public int vm_sprite_get_hp(){
        return hp;
    }
    
    public int vm_sprite_get_hp_max(){
        return hpMax;
    }
    
    public int vm_sprite_get_mp(){
        return mp;
    }
    
    public int vm_sprite_get_mp_max(){
        return mpMax;
    }
    
    public int[] vm_sprite_get_pos(){
        return new int[]{
                        sprite.getX(), sprite.getY()
        };
    }
    
    public void vm_sprite_set_pos(int x, int y){
        sprite.setPosition(x, y);
    }
    
    public int vm_sprite_get_map_id(){
        return sprite.getMapId();
    }
    
    public int vm_sprite_get_map_instance_id(){
        return sprite.getMapInstanceId();
    }
    
    public void vm_game_set_animate_index(String _animateName, int _index, int _playType, int _callBackIndex){
        sprite.setAnimateIndex(_animateName, _index, _playType, _callBackIndex, this, false);
    }
    
    public int vm_game_sprite_play_animate(AnimatePlayer _animatePlayer, int _anchor, int _order){
        _animatePlayer.setAnchor(_anchor);
        _animatePlayer.setOrder(_order);
        _animatePlayer.setShown(true);
        animates.put(new Integer(_animatePlayer.getKey()), _animatePlayer);
        
        return _animatePlayer.getKey();
    }
    
    public void vm_sprite_set_can_attack(boolean _canAttact){
        this.canAttack = _canAttact;
    }
    
    public boolean vm_sprite_get_can_attack(){
        return canAttack;
    }
    
    public void vm_sprite_set_can_select(boolean _canSelect){
        this.canSelect = _canSelect;
    }
    
    public boolean vm_sprite_get_can_select(){
        return canSelect;
    }
    
    public int[] vm_sprite_get_animate_para(){
        int dir = sprite.getDir();
        int animateDir = sprite.getAnimateDir();
        int animateSubDir = sprite.getAnimateSubDir();
        int moving = sprite.getMove()? VM.TRUE: VM.FALSE;
        int onHorse = this.onHorse? VM.TRUE: VM.FALSE;
        int holding = this.hold? VM.TRUE: VM.FALSE;
        int attacking = this.attacking? VM.TRUE: VM.FALSE;
        int die = this.die? VM.TRUE: VM.FALSE;
        int isHuman = this.isHumanAnimate()? VM.TRUE: VM.FALSE;
        int chasing = chaseMode? VM.TRUE: VM.FALSE;
        
        return new int[]{
                        dir, animateDir, animateSubDir, moving, onHorse, holding, attacking, die, isHuman, weaponType, chasing, horsepara, horsePoint
        };
    }
    
    public void vm_game_sprite_stop_animate(int _animatePlayerKey){
        animates.remove(new Integer(_animatePlayerKey));
    }
    
    public void vm_game_sprite_add_fly_string(int _type, String _str, int _number, int _paletteColor, int _distance, int _time, int _order, int _delayTick){
        sprite.addFlyingString((byte)_type, _str, _number, _paletteColor, _distance, _time, _order, _delayTick);
    }
    
    public void vm_game_sprite_add_across_fly_string(int _type, String _str, int _number, int _paletteColor, int _dir, int _hCycleCount, int _hSpeed, int _stopCycleCount, int _vCycleCount, int _vSpeed, int _order, int _delayTick){
        sprite.addFlyingString((byte)_type, _str, _number, _paletteColor, _dir, _hCycleCount, _hSpeed, _stopCycleCount, _vCycleCount, _vSpeed, _order, _delayTick);
    }
    
    public void vm_game_sprite_add_vibar(int _time, int _distance){
        sprite.addVibar(sprite.getDir(), _time, _distance);
    }
    public void vm_game_sprite_set_die(boolean _die){
        this.die = _die;
        sprite.setWork(!die);
    }
    
    public void vm_game_sprite_set_waypoint_animate(int _moveAnimate, int _stopAnimate, int _chaseMoveAnimate, int _chaseStopAnimate, int _pendingStopAnimate, int _chasePendingStopAnimate){
        moveAnimateIndex = _moveAnimate;
        stopAnimateIndex = _stopAnimate;
        chaseMoveAnimateIndex = _chaseMoveAnimate;
        chaseStopAnimateIndex = _chaseStopAnimate;
        pendingStopAnimateIndex = _pendingStopAnimate;
        chasePendingStopAnimateIndex = _chasePendingStopAnimate;
        sprite.wayPointInfo.moveAnimateIndex = moveAnimateIndex;
        sprite.wayPointInfo.stopAnimateIndex = stopAnimateIndex;
    }
    
    public void vm_sprite_set_head_string_config(int type, int space, int drawMode, int offsetX, int offsetY, int order){
        setHeadStringConfig(type, space, drawMode, offsetX, offsetY, order);
    }
    
    public void vm_sprite_add_head_string(String _str, int _color, Object _image, int[] _imageIndex){
        if(_image == null){
            sprite.addHeadString(_str, _color, null, null);
        }else{
            sprite.addHeadString(_str, _color, (ImageSet)Tool.getGlobalObject((String)_image), _imageIndex);
        }
    }
    
    public void vm_sprite_clear_head_string(){
        sprite.clearHeadString();
    }
    
    public void vm_sprite_set_head_string_show(boolean _show){
        sprite.setHeadStringShow(_show);
    }
    
    public void vm_sprite_set_collision(boolean _collision){
        sprite.setCollision(_collision);
    }
    
    public int vm_sprite_get_type(){
        return getType();
    }
    
    public int vm_sprite_get_id(){
        return getId();
    }
    
    public int vm_sprite_get_instanceid(){
        return getInstanceId();
    }
    
    public int vm_sprite_get_dir(){
        return sprite.getDir();
    }
    
    public AnimatePlayer vm_sprite_get_animate_player(String _animateName){
        return sprite.getAnimatePlayer(_animateName);
    }
    
    public void vm_sprite_send_command(int _command, Object _data){
        sendCommand(_command, _data);
    }
    
    public boolean vm_sprite_start_chase_position(int _distanceAllow, int _targetX, int _targetY, int _speed, int[] _callbackPara, boolean _always){
        boolean find = false;
        clearChase();
        
        if(GameWorld.gameView != null){
            if(_speed < 0){
                _speed = sprite.getSpeed();
            }
            find = startChase(sprite.getX(), sprite.getY(), _targetX, _targetY, _distanceAllow, _speed, -1, _callbackPara, _always);
        }
        
        return find;
    }
    
    public boolean vm_sprite_start_chase_sprite(int _distanceAllow, int _speed, GameSprite _targetSprite, int[] _callbackPara, boolean _always){
        boolean find = false;
        clearChase();
        
        if(GameWorld.gameView != null){
            if(_speed < 0){
                _speed = sprite.getSpeed();
            }
            find = startChase(sprite.getX(), sprite.getY(), _targetSprite.sprite.getX(), _targetSprite.sprite.getY(), _distanceAllow, _speed, _targetSprite.getInstanceId(), _callbackPara, _always);
        }
        
        return find;
    }
    
    public void vm_sprite_clear_chase(){
        clearChase();
    }
    
    public void vm_sprite_set_dir(int _dir){
        sprite.setDir(_dir);
        sprite.setAnimateDir(_dir);
    }
    
    public void vm_sprite_adjust_animate_dir(int _targetInstanceId, boolean _setAnimate){
        GameSprite targetSprite = GameWorld.getSprite(_targetInstanceId);
                
        if(targetSprite != null){
            int[] newDir = targetSprite.sprite.wayPointInfo.dirArray;
            Tool.calulateDirWithWayPointMatrix(sprite.getDir(), sprite.getAnimateSubDir(), sprite.getX(), sprite.getY(), targetSprite.sprite.getX(), targetSprite.sprite.getY(), newDir);
            if(!sprite.getMove()) {
                sprite.setDir(newDir[0]);
            }
            sprite.setAnimateDir(newDir[0]);
            sprite.setAnimateSubDir(newDir[1]);
            
            if(_setAnimate){
                sendCommand(VMGame.GAME_COMMAND_SPRITE_SET_ANIMATE, null);
            }
        }
    }
    
    public String[] vm_sprite_get_extra_name(){
        return extraName;
    }
    
    public int vm_sprite_get_speed(){
        return sprite.getSpeed();
    }
    
    public void vm_sprite_set_speed(int _speed){
        sprite.setSpeed(_speed);
    }
    
    public int vm_sprite_get_animate_sub_dir(){
        return sprite.getAnimateSubDir();
    }
    
    public void vm_sprite_set_animate_sub_dir(int _subDir){
        sprite.setAnimateSubDir(_subDir);
    }
    
    public void vm_sprite_set_attacking(boolean _attacking){
        this.attacking = _attacking;
    }
    
    public int vm_sprite_get_weapon_type(){
        return weaponType;
    }
    
    public void vm_sprite_set_weapon_type(int _weaponType){
        this.weaponType = _weaponType;
    }
    
    public int vm_sprite_get_animate_dir(){
        return sprite.getAnimateDir();
    }
    
    public void vm_sprite_set_animate_dir(int _animateDir){
        if(_animateDir < 0){
            sprite.setAnimateDir(sprite.getDir());
        }else{
            sprite.setAnimateDir(_animateDir);
        }
    }
    
    public boolean vm_sprite_get_move(){
        return sprite.getMove();
    }
    
    public void vm_sprite_set_following(boolean isFollowing){
        this.isFollowing = isFollowing; 
    }
    
    public boolean vm_sprite_get_following(){
        return isFollowing;
    }
    
    public GameSprite vm_sprite_get_follow_owner(){
        return followOwner;
    }
    
    public void vm_sprite_set_leaving_pos(){
        if(leavingPosition == null){
            leavingPosition = new int[2];
            leavingPosition[0] = sprite.getX();
            leavingPosition[1] = sprite.getY();
        }
    }
    
    public boolean vm_sprite_test_status(int _status){
        return status.get(new Integer(_status)) != null;
    }
    
    public void vm_sprite_add_status(int _status){
        Integer tmp = new Integer(_status);
        status.put(tmp, tmp);
    }
    
    public void vm_sprite_remove_status(int _status){
        status.remove(new Integer(_status));
    }
    
    public void vm_sprite_clear_status(){
        status.clear();
    }
    
    public void vm_sprite_set_force_way_point(boolean _hasReturnPoint, int _forceSpeed, int _returnX, int _returnY, int[] _wayPointList){
        clearForceWayPoint();
                
        for(int i = 0; i < _wayPointList.length >> 1; i++){
            forceWayPointList.addElement(new int[]{
                            _wayPointList[i << 1], _wayPointList[(i << 1) + 1]
            });
        }
        
        if(_hasReturnPoint) {
            forceWayPointList.addElement(new int[]{
                    _returnX, _returnY
            });
        }                
        
        forceWayPointSpeed = _forceSpeed;
        forceWayPointMode = true;
    }
    
    public void vm_sprite_clear_force_way_point(){
        if(forceWayPointList.size() > 0){
            int[] returnPoint = (int[])forceWayPointList.lastElement();
            sprite.setPosition(returnPoint[0], returnPoint[1]);
        }
        
        clearForceWayPoint();
    }
    
    public int vm_sprite_get_speed_addon(){
        return sprite.getSpeedAddon();
    }
    
    public void vm_sprite_set_speed_addon(int _speedAddon){
        sprite.setSpeedAddon(_speedAddon);
    }
    
    public void vm_set_animate_not_icon(String animateName){
        sprite.setAnimateNotIcon(animateName);
    }
    
    public void vm_game_sprite_set_animate_layer(String _animateName, int _layer){
        sprite.setAnimateLayer(_animateName, _layer);
    }
    
    public void vm_game_sprite_regroup_animate(){
        sprite.regroupAnimate();
    }
    
    public boolean vm_sprite_is_out_view(){
        return isOutView;
    }
    
    public void vm_set_game_sprite_horse(boolean riding){
        processAction(sprite.getDir(), false, riding, hold);
    }

    public void vm_set_game_sprite_hold(boolean _hold){
        this.hold = _hold;
    }
    
    public boolean vm_sprite_is_team_state(){
        return isTeamState;
    }
    
    public void vm_sprite_set_team_state(boolean _isTeamState){
        this.isTeamState = _isTeamState;
    }
    
    public void vm_sprite_set_animate_draw_replace_data(String _animateName, int _sourceImageId, int[] _sourceFrameId, int _destImageId, int[] _destFrameId){
        int count = _sourceFrameId.length;
        
        for(int i = 0; i < count; i++){
            sprite.setAnimateImageDrawOffset(_animateName, _sourceImageId, _sourceFrameId[i], _destImageId, _destFrameId[i]);
        }
    }
    
    public Object processCommand(int command, Object data){
        Object result = null;

        switch(command){
        }

        return result;
    }

    public void processAction(int newDir, boolean newMoving, boolean newOnHorse, boolean newHolding){
        boolean animateIndexChanged = false;
        
        if(sprite.getDir() != newDir){
            sprite.setDir(newDir);
            sprite.setAnimateDir(newDir);
            animateIndexChanged = true;
        }

        if(sprite.getMove() != newMoving || hold != newHolding){
            hold = newHolding;
            sprite.setMove(newMoving);
            animateIndexChanged = true;
        }

        if(onHorse != newOnHorse){
            onHorse = newOnHorse;
            animateIndexChanged = true;
        }

        if(animateIndexChanged){
            sendCommand(VMGame.GAME_COMMAND_SPRITE_SET_ANIMATE, null);
            sendCommand(VMGame.GAME_COMMAND_PANEL_REFRESH_TARGET, null);
        }
    }

    public Object readGameData(String dataName){
        return gameData.get(dataName);
    }

    public void saveGameData(String dataName, Object data){
        gameData.put(dataName, data);
    }

    public void removeGameData(String dataName){
        gameData.remove(dataName);
    }

    public void animateReady(String animateName, PipAnimateSet animate){
        AnimatePlayer animateData = new AnimatePlayer(animateName);
        animateData.init(animate);

        saveGameData(VMGame.VM_DATA_PREFIX_ANIMATE + animateName, animateData);
        sendCommand(VMGame.GAME_COMMAND_SPRITE_ANIMATE_OK, animateName);
    }

    public boolean canRemoved(){
        if(sprite.isPlayingAnimate() == false && animates.size() == 0){
            return true;
        }

        if(sprite.isPlayingAnimate() == false){
            Enumeration enu = animates.elements();

            while(enu.hasMoreElements()){
                AnimatePlayer ap = (AnimatePlayer) enu.nextElement();

                if(ap.playing()){
                    return false;
                }
            }
        }else{
            return false;
        }

        return true;
    }

    public void cycle(){
        Enumeration enu = animates.elements();

        while(enu.hasMoreElements()){
            AnimatePlayer ap = (AnimatePlayer) enu.nextElement();
            ap.cycle();
        }

        if(forceWayPointMode){
            processForceWayPoint();
        }else{
            if(chaseMode){
                processChase();
            }
        }

        if(sprite.getMove() || sprite.wayPointInfo.needHandle || this.attacking) {
        	pendingTick = 0;
        }
        
        if(pendingTick > GameMain.animatePendingTick) {
        	if(chaseMode){
        		if(chaseStopAnimateIndex != chasePendingStopAnimateIndex) {
        			this.sendCommand(VMGame.GAME_COMMAND_SPRITE_PENDING_STOP, new Integer(chasePendingStopAnimateIndex));
        		}
        	} else if(stopAnimateIndex != pendingStopAnimateIndex){
        		this.sendCommand(VMGame.GAME_COMMAND_SPRITE_PENDING_STOP, new Integer(pendingStopAnimateIndex));
        	}
        	pendingTick = 0;
        } else {
        	pendingTick ++;
        }

    }

    public int getInstanceId(){
        return instanceId;
    }

    public void drawAnimate(Graphics g, int viewX, int viewY, boolean isBack){
        int[] animateBox = sprite.getAnimateBox();
        Enumeration enu = animates.elements();
        int ax, ay;

        while(enu.hasMoreElements()){
            AnimatePlayer ap = (AnimatePlayer) enu.nextElement();

            if(isBack){
                if(ap.getOrder() != Tool.DRAW_ORDER_BACK){
                    continue;
                }

                ax = getAnimateAnchorX(ap.getAnchor(), animateBox);
                ay = getAnimateAnchorY(ap.getAnchor(), animateBox);

                ap.draw(g, ax - viewX, ay - viewY);
            }else{
                ax = getAnimateAnchorX(ap.getAnchor(), animateBox);
                ay = getAnimateAnchorY(ap.getAnchor(), animateBox);

                if(ap.getOrder() == Tool.DRAW_ORDER_TOP){
                    GameWorld.gameView.addPendingAnimate(ap, ax - viewX, ay - viewY, false);
                }else if(ap.getOrder() == Tool.DRAW_ORDER_TOP_TOP){
                    GameWorld.gameView.addPendingAnimate(ap, ax - viewX, ay - viewY, true);
                }else if(ap.getOrder() == Tool.DRAW_ORDER_BACK){
                    continue;
                }else{
                    ap.draw(g, ax - viewX, ay - viewY);
                }
            }
        }
    }

    private int getAnimateAnchorX(int anchor, int[] animateBox){
        int ax = animateBox[0];

        if((anchor & Tool.SPRITE_ANCHOR_BOX_RIGHT) != 0){
            ax += animateBox[2];
        }else if((anchor & Tool.SPRITE_ANCHOR_BOX_HCENTER) != 0){
            ax += animateBox[2] >> 1;
        }else if((anchor & Tool.SPRITE_ANCHOR_X_REF) != 0){
            ax = sprite.getX();
        }

        return ax;
    }

    private int getAnimateAnchorY(int anchor, int[] animateBox){
        int ay = animateBox[1];

        if((anchor & Tool.SPRITE_ANCHOR_BOX_BOTTOM) != 0){
            ay += animateBox[3];
        }else if((anchor & Tool.SPRITE_ANCHOR_BOX_VCENTER) != 0){
            ay += animateBox[3] >> 1;
        }else if((anchor & Tool.SPRITE_ANCHOR_Y_REF) != 0){
            ay = sprite.getY();
        }else if((anchor & Tool.SPRITE_ANCHOR_HEAD) != 0){
            ay -= sprite.getHeadStringHeight();
        }

        return ay;
    }

    private void processChase(){
        if(chaseList.size() > 0){
            short[] targetPoint = (short[])chaseList.lastElement();
            targetPoint = new short[]{
                            targetPoint[0], targetPoint[1]
            };
            
            if(chaseTargetInstanceId >= 0){
                GameSprite gameSprite = GameWorld.getSprite(chaseTargetInstanceId);
                
                if(gameSprite != null){
                    targetPoint[0] = (short)gameSprite.sprite.getX();
                    targetPoint[1] = (short)gameSprite.sprite.getY();
                }
            }
            
            if(chaseDistanceAllow > 0 && Tool.distance(sprite.getX(), sprite.getY(), targetPoint[0], targetPoint[1]) <= chaseDistanceAllow){
                sprite.wayPointInfo.finishWayPoint(true);
                chaseList.removeAllElements();
            }else{
                short[] path = (short[]) chaseList.firstElement();

                if(sprite.getX() != path[0] || sprite.getY() != path[1]){
                    if(chasePoint == null || chasePoint[0] != path[0] || chasePoint[1] != path[1]){
                        sprite.addWayPoint(path[0], path[1], chaseMoveAnimateIndex, chaseStopAnimateIndex, chaseSpeed, this);
                        chasePoint = path;
                    }
                }else{
                    chaseList.removeElementAt(0);
                    processChase();
                    
                    return;
                }
            }
        }

        if(chaseList.size() == 0){
            if(!chaseAlways){
                int[] callBackPara = chaseCallbackPara;
                clearChase();
                
                if(callBackPara != null){
                    sendCommand(VMGame.GAME_COMMAND_SPRITE_CHASE_END, callBackPara);
                }
            }else{
                if(chaseTargetInstanceId >= 0){
                    short[] targetPoint = new short[]{
                                    (short)sprite.getX(), (short)sprite.getY()
                    };
                    
                    if(chaseList.size() > 0){
                        targetPoint = (short[])chaseList.lastElement();
                    }
    
                    GameSprite gameSprite = GameWorld.getSprite(chaseTargetInstanceId);
                    
                    if(gameSprite != null && Tool.distance(targetPoint[0], targetPoint[1], gameSprite.sprite.getX(), gameSprite.sprite.getY()) > chaseDistanceAllow){
                        startChase(sprite.getX(), sprite.getY(), gameSprite.sprite.getX(), gameSprite.sprite.getY(), chaseDistanceAllow, chaseSpeed, chaseTargetInstanceId, chaseCallbackPara, chaseAlways);
                    }else if(gameSprite == null){
                        clearChase();
                    }
                }else{
                    clearChase();
                }
            }
        }
    }

    public boolean startChase(int startX, int startY, int endX, int endY, int distanceAllow, int speed, int targetInstanceId, int[] callbackPara, boolean always){
        if(status.size() > 0){
            return false;
        }
        
        if(this == GameWorld.player) {
        	int[] callBackPara = this.chaseCallbackPara;                    
            if(callBackPara != null){
                this.sendCommand(VMGame.GAME_COMMAND_SPRITE_CHASE_STOP, callBackPara);
            }
        }        
                    
        clearChase();
        
        int sx = startX / GameWorld.gameView.pathTileWidth;
        int sy = startY / GameWorld.gameView.pathTileHeight;
        int tx = endX / GameWorld.gameView.pathTileWidth;
        int ty = endY / GameWorld.gameView.pathTileHeight;

        short[][] path = null;
        
        //#if SupportChase == all
        path = GameWorld.gameView.searchPath_AStar(sx, sy, tx, ty,type);
        //#elif SupportChase == player
        //# if(this instanceof GameRole){
        //#     path = GameWorld.gameView.searchPath_AStar(sx, sy, tx, ty,type);
        //# }else{
        //#     path = new short[0][];
        //# }
        //#else
        //# path = new short[0][];
        //#endif
        
        if(always){
            chaseMode = true;
            chaseAlways = true;
        }
        
        if(path != null){
            chaseMode = true;
            chaseDistanceAllow = distanceAllow;
            chaseSpeed = speed;
            chaseTargetInstanceId = targetInstanceId;
            chasePoint = null;
            chaseCallbackPara = callbackPara;
    
            chaseList.addElement(new short[]{
                            (short) startX, (short) startY
            });
            
            for(int i = 1; i < path.length - 1; i++){
                chaseList.addElement(new short[]{
                                (short) (path[i][0] * GameWorld.gameView.pathTileWidth + GameWorld.gameView.pathTileWidth / 2), (short) (path[i][1] * GameWorld.gameView.pathTileHeight + GameWorld.gameView.pathTileHeight / 2)
                });
            }
    
            chaseList.addElement(new short[]{
                            (short) endX, (short) endY
            });
            
            GameMain.path = path;

            return true;
        }
        
        return false;
    }

    public void clearChase(){
        chaseMode = false;
        chaseAlways = false;
        chaseDistanceAllow = 0;
        chaseTargetInstanceId = -1;
        chaseSpeed = sprite.getSpeed();
        chaseList.removeAllElements();
        chasePoint = null;
        chaseCallbackPara = null;
        
        sprite.wayPointInfo.finishWayPoint(!die);
        attacking = false;

        GameMain.path = null;
    }
    
    public void processForceWayPoint(){
        if(forceWayPointList.size() > 0){
            int[] path = (int[]) forceWayPointList.firstElement();

            if(sprite.getX() != path[0] || sprite.getY() != path[1]){
                if(forceWayPoint == null || forceWayPoint[0] != path[0] || forceWayPoint[1] != path[1]){
                    sprite.addWayPoint(path[0], path[1], chaseMoveAnimateIndex, chaseStopAnimateIndex, forceWayPointSpeed, this);
                    forceWayPoint = path;
                }
            }else{
                forceWayPointList.removeElementAt(0);
                processChase();
                
                return;
            }
        }

        if(forceWayPointList.size() == 0){
            clearForceWayPoint();
        }
    }

    public void clearForceWayPoint(){
        forceWayPointList.removeAllElements();
        forceWayPoint = null;
        forceWayPointMode = false;
        forceWayPointSpeed = sprite.getSpeed();
    }

    public void callback(int callbackFunction, int animateKey){
        sendCommand(VMGame.GAME_COMMAND_SPRITE_ANIMATE_PLAY_END, new int[]{
                        callbackFunction, animateKey
        });
    }

    public abstract void draw(Graphics g, int viewX, int viewY);

    public abstract boolean isHumanAnimate();
}
