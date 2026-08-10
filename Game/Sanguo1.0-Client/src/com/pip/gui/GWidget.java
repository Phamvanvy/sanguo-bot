package com.pip.gui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.image.ImageSet;
import com.pip.sanguo.GameIcon;
import com.pip.sanguo.GameMain;
import com.pip.ui.VM;
import com.pip.ui.VMGame;
import com.pip.util.SortHashtable;

public class GWidget{	
	//脚本中对应的对象结构
	public final static int GW_VM_SYS_TYPE                    = 0;  //编译器产生的系统typeid
	public final static int GW_VM_TYPE                        = 1;
	public final static int GW_VM_VERAION                     = 2;
	public final static int GW_VM_X                           = 3;
	public final static int GW_VM_Y                           = 4;
	public final static int GW_VM_W                           = 5;
	public final static int GW_VM_H                           = 6;
	public final static int GW_VM_XX                          = 7;  //绝对x坐标
	public final static int GW_VM_YY                          = 8;  //绝对y坐标
	public final static int GW_VM_BORDERLEFT                  = 9;
	public final static int GW_VM_BORDERTOP                   = 10;
	public final static int GW_VM_BORDERRIGHT                 = 11;
	public final static int GW_VM_BORDERBOTTOM                = 12;
	
	public final static int GW_VM_MAX_WIDTH                   = 13;  //最大宽度
	public final static int GW_VM_MIN_WIDTH                   = 14;  //最小宽度
	public final static int GW_VM_MAX_HEIGHT                  = 15;  //最大高度
	public final static int GW_VM_MIN_HEIGHT                  = 16;  //最小高度

	public final static int GW_VM_Z_ORDER                     = 17;  //	
	
	public final static int GW_VM_FUNC_CYCLE                  = 18;
	public final static int GW_VM_FUNC_CYCLEUI                = 19;
	public final static int GW_VM_FUNC_PAINT                  = 20;
	public final static int GW_VM_FUNC_PACKET                 = 21;
	public final static int GW_VM_FUNC_DESTROY                = 22;
	public final static int GW_VM_FUNC_SEND_EVENT             = 23;
	public final static int GW_VM_FUNC_GET_PERFECT_WIDTH      = 24;  //最佳宽
	public final static int GW_VM_FUNC_GET_PERFECT_HEIGHT     = 25;  //最佳高
	public final static int GW_VM_FUNC_RESIZE                 = 26;
	
	public final static int GW_VM_CAN_MOUSE_CLICKED           = 27;  //是否能被鼠标点击
	public final static int GW_VM_CAN_MOUSE_DRAGGED           = 28;  //是否能被鼠标拖动
	
	public final static int GW_VM_JAVA_GWIDGET                = 29;
	public final static int GW_VM_ID                          = 30;
	public final static int GW_VM_OFFSET_X                    = 31;
	public final static int GW_VM_OFFSET_Y                    = 32;
	public final static int GW_VM_SELF                        = 33; //窗口本身的引用	
	public final static int GW_VM_FUNC_PAINT_BEFORE           = 34;
	public final static int GW_VM_FUNC_PAINT_AFTER            = 35;
	
	public int[] vmData; //存放脚本中的对象
	
	//事件定义
	public final static int GW_EVENT_GET_FOCUS = 0;
	public final static int GW_EVENT_LOST_FOCUS = 1;
	public final static int GW_EVENT_MOUSE_CLICKED = 2;
	
	public final static int PAINT_TYPE_IN_VM    = 0; //脚本中绘制
	public final static int PAINT_TYPE_IN_JAVA  = 1; //容器的绘制都在java中
	public final static int PAINT_TYPE_AFTER    = 2; //容器绘制完后绘制
	public final static int PAINT_TYPE_GIPAINT  = 3; //java中绘制
	
	public GContainer parent;
		
	public String name;
	
	public boolean isScale = true; //是否能自动缩放
	
	boolean noNeedLayout;
		
	boolean isFocus;
	boolean enableFocus = true;
	
	private boolean isOutView; //layout时跃出边界了
	int realHeight;  //真实的高度
	int outHeight;   //出边界的高度
	
	int[] grid3Data;
	//鼠标按下时的坐标
	int pressX;
	int pressY;
	
	public boolean isShow = true;
	
	private static final Tool keyMaker = new Tool();
	public VMGame vmGame;
	public int borderLayoutType;
	public Font font = Utilities.font;

	//为了提速去掉new int
    public int[] rect = new int[4];
    //#if NewUI2
    private boolean needClip = false;
    //#if NewUI2
    public int[] paintParams = new int[2];
    public int[] cycleParams = new int[1];
    public int[] paintClip = new int[4];
    //#endif
    
    public static final int PAINT_TYPE_BACKGROUND = 0;	
    public static final int PAINT_TYPE_BUTTON = 1;	
    public static final int PAINT_TYPE_STATUS_BAR = 2;	
    public static final int PAINT_TYPE_RECT = 3;	
    public static final int PAINT_TYPE_VM_FUNC = 4;	
    public static final int PAINT_TYPE_SPRITE = 5;	
    public static final int PAINT_TYPE_IMAGE = 6;
    public static final int PAINT_TYPE_LINE = 7;
    public static final int PAINT_TYPE_CLIP = 8;
    
    
    SortHashtable paints = null; 
    SortHashtable paintBefores = null; 
    SortHashtable paintAfters = null; 
    
    public static final int  PAINT_LAYER_BEFORE	=		0 ;	
    public static final int  PAINT_LAYER_PAINT	=		1 ;	
    public static final int  PAINT_LAYER_AFTER	=		2 ;
    
    //数据常量
    public static final int PAINT_DATA_TYPE       					=  0		;		//type;
    public static final int PAINT_DATA_IMAGE       					=  1		;		//ImageSet image;
    public static final int PAINT_DATA_FILLCOLOR     				=  2		;		//int fillColor;
    public static final int PAINT_DATA_IS_INNER       				=  3		;		//boolean isInner = true;
    public static final int PAINT_DATA_ANCHOR       				=  4		;		//int anchor;
    public static final int PAINT_DATA_TOPLEFT_INDEX       			=  5		;		//int topLeftIndex;
    public static final int PAINT_DATA_TOP_INDEX       				=  6		;		//int topIndex;
    public static final int PAINT_DATA_TOPRIGHT_INDEX       		=  7		;		//int topRightIndex;
    public static final int PAINT_DATA_LEFT_INDEX       			=  8		;		//int leftIndex;
    public static final int PAINT_DATA_BOTTOMLEFT_INDEX       		=  9		;		//int bottomLeftIndex;
    public static final int PAINT_DATA_BOTTOM_INDEX       			=  10		;		//int bottomIndex;
    public static final int PAINT_DATA_RIGHT_INDEX       			=  11		;		//int rightIndex;
    public static final int PAINT_DATA_BOTTOMRIGHT_INDEX       		=  12		;		//int bottomRightIndex;
    public static final int PAINT_DATA_TOPLEFT_TRANS       			=  13		;		//int topLeftTrans;
    public static final int PAINT_DATA_TOP_TRANS       				=  14		;		//int topTrans;
    public static final int PAINT_DATA_TOPRIGHT_TRANS       		=  15		;		//int topRightTrans;
    public static final int PAINT_DATA_LEFT_TRANS       			=  16		;		//int leftTrans;
    public static final int PAINT_DATA_BOTTOMLEFT_TRANS       		=  17		;		//int bottomLeftTrans;
    public static final int PAINT_DATA_BOTTOM_TRANS       			=  18		;		//int bottomTrans;
    public static final int PAINT_DATA_RIGHT_TRANS       			=  19		;		//int rightTrans;
    public static final int PAINT_DATA_BOTTOMRIGHT_TRANS       		=  20		;		//int bottomRightTrans;
    public static final int PAINT_DATA_FUNC       					=  21		;		//int func;
    public static final int PAINT_DATA_IS_HEAD       				=  22		;		//boolean isHead;
    public static final int PAINT_DATA_ICON       					=  23		;		//Object icon;
    public static final int PAINT_DATA_X       			=  24		;		//int imgIconOffX;
    public static final int PAINT_DATA_Y       			=  25		;		//int imgIconOffY;
    public static final int PAINT_DATA_STATUS_LEN       			=  26		;		//int statusLen;
    public static final int PAINT_DATA_LAYER		       			=  27		;		//int layer;
    public static final int PAINT_DATA_KEY			       			=  28		;		//int key;
    public static final int PAINT_DATA_W			       			=  29		;
    public static final int PAINT_DATA_H			       			=  30		;
    public static final int PAINT_DATA_HIDE			       			=  31		;
    public static final int PAINT_DATA_ORIENTATION			       	=  32		;
    public static final int PAINT_DATA_USE_IMAGE_FILL      			=  33		;
    
    /**
     * 通用底板
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintBackgroud(GWidget gw,Graphics g,int[] paint){
    	int x,y,w,h;
    	ImageSet image = (ImageSet)gw.vmGame.gtvm.followPointer(paint[PAINT_DATA_IMAGE]);
    	int topLeftW = image.getFrameWidth(paint[PAINT_DATA_TOPLEFT_INDEX]);
    	int topLeftH = image.getFrameHeight(paint[PAINT_DATA_TOPLEFT_INDEX]);
    	int topRightW = image.getFrameWidth(paint[PAINT_DATA_TOPRIGHT_INDEX]);
    	int topRightH = image.getFrameHeight(paint[PAINT_DATA_TOPRIGHT_INDEX]);
    	int bottomLeftW = image.getFrameWidth(paint[PAINT_DATA_BOTTOMLEFT_INDEX]);
    	int bottomLeftH = image.getFrameHeight(paint[PAINT_DATA_BOTTOMLEFT_INDEX]);
    	int bottomRightW = image.getFrameWidth(paint[PAINT_DATA_BOTTOMRIGHT_INDEX]);
    	int bottomRightH = image.getFrameHeight(paint[PAINT_DATA_BOTTOMRIGHT_INDEX]);
    	if(paint[PAINT_DATA_W] == 0){
			w = gw.vmData[GW_VM_W];
		} else {
			w = paint[PAINT_DATA_W];
		}
    	if(paint[PAINT_DATA_H] == 0){
			h = gw.vmData[GW_VM_H];
		} else {
			h = paint[PAINT_DATA_H];
		}
    	x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
		y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
    	//inside the gw or outside
    	if(paint[PAINT_DATA_IS_INNER] == VM.FALSE){
    		w += topLeftW + topRightW;
    		h += topLeftH + bottomLeftH;
    		x -= topLeftW;
    		y -= topLeftH;
    	}
    	//4 corners
    	image.drawFrame(g, paint[PAINT_DATA_TOPLEFT_INDEX], x, y, paint[PAINT_DATA_TOPLEFT_TRANS]);
    	image.drawFrame(g, paint[PAINT_DATA_TOPRIGHT_INDEX], x + w, y, paint[PAINT_DATA_TOPRIGHT_TRANS],Graphics.TOP|Graphics.RIGHT);
    	image.drawFrame(g, paint[PAINT_DATA_BOTTOMLEFT_INDEX], x, y + h, paint[PAINT_DATA_BOTTOMLEFT_TRANS],Graphics.BOTTOM|Graphics.LEFT);
    	image.drawFrame(g, paint[PAINT_DATA_BOTTOMRIGHT_INDEX], x + w, y + h, paint[PAINT_DATA_BOTTOMRIGHT_TRANS],Graphics.BOTTOM|Graphics.RIGHT);
    	//4 borders
    	Tool.drawSpellRow(g, x + topLeftW, y, w - topLeftW - topRightW, image, paint[PAINT_DATA_TOP_INDEX], paint[PAINT_DATA_TOP_TRANS]);
    	Tool.drawSpellRow(g, x + bottomLeftW, y + h - bottomLeftH, w - bottomLeftW - bottomRightW, image, paint[PAINT_DATA_BOTTOM_INDEX], paint[PAINT_DATA_BOTTOM_TRANS]);
    	Tool.drawSpellCol(g, x, y + topLeftH, h - topLeftH - bottomLeftH, image, paint[PAINT_DATA_LEFT_INDEX], paint[PAINT_DATA_LEFT_TRANS]);
    	Tool.drawSpellCol(g, x + w - topRightW, y + topRightH, h - topRightH - bottomRightH, image, paint[PAINT_DATA_RIGHT_INDEX], paint[PAINT_DATA_RIGHT_TRANS]);
    	//fill rect
    	if(paint[PAINT_DATA_FILLCOLOR] != -1){
    		if(paint[PAINT_DATA_USE_IMAGE_FILL] == VM.TRUE){
    			//#if NewUI2
    			//# image.drawFrame(g, paint[PAINT_DATA_FILLCOLOR], x + topLeftW, y + topLeftH, Tool.TRANS_NONE, Graphics.TOP|Graphics.LEFT,w - topLeftW - topRightW, h - topLeftH - bottomLeftH);
    			//#else
    			Tool.drawSpellArea(g, x + topLeftW, y + topLeftH, w - topLeftW - topRightW, h - topLeftH - bottomLeftH, image, paint[PAINT_DATA_FILLCOLOR], Tool.TRANS_NONE);
    			//#endif
    		} else {
    			if((paint[PAINT_DATA_FILLCOLOR] & 0xFF000000) != 0){
    	    		Tool.fillAlphaRect(g, paint[PAINT_DATA_FILLCOLOR], x + topLeftW, y + topLeftH, w - topLeftW - topRightW, h - topLeftH - bottomLeftH);
    	    	} else {
    	    		g.setColor(paint[PAINT_DATA_FILLCOLOR]);
    	        	g.fillRect(x + topLeftW, y + topLeftH, w - topLeftW - topRightW, h - topLeftH - bottomLeftH);
    	    	}
    		}
    	}
    	
    }
    
    /**
     * 按钮
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintButton(GWidget gw,Graphics g,int[] paint){
    	int x,y,w;
    	if(paint[PAINT_DATA_W] > 0){
    		w = paint[PAINT_DATA_W];
    	} else {
    		w = gw.vmData[GW_VM_W];
    	}
    	
		x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
		y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
		ImageSet image = (ImageSet)gw.vmGame.gtvm.followPointer(paint[PAINT_DATA_IMAGE]);
		int leftW = image.getFrameWidth(paint[PAINT_DATA_LEFT_INDEX]);
		int rightW = image.getFrameWidth(paint[PAINT_DATA_RIGHT_INDEX]);
    	image.drawFrame(g, paint[PAINT_DATA_LEFT_INDEX], x, y ,paint[PAINT_DATA_LEFT_TRANS]);
    	Tool.drawSpellRow(g, x + leftW, y, w - leftW - rightW, image, paint[PAINT_DATA_TOP_INDEX], paint[PAINT_DATA_TOP_TRANS]);
    	image.drawFrame(g, paint[PAINT_DATA_RIGHT_INDEX], x + w - rightW, y, paint[PAINT_DATA_RIGHT_TRANS]);
    }
    
    /**
     * 状态条
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintStatusBar(GWidget gw,Graphics g,int[] paint){
    	int x,y,w,h;
    	x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
    	y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
		ImageSet image = (ImageSet)gw.vmGame.gtvm.followPointer(paint[PAINT_DATA_IMAGE]);
		if(paint[PAINT_DATA_ORIENTATION] == 0){
    		Tool.drawSpellRow(g, x, y, paint[PAINT_DATA_STATUS_LEN], image, paint[PAINT_DATA_TOP_INDEX], paint[PAINT_DATA_TOP_TRANS]);
    	} else {
    		Tool.drawSpellCol(g, x, y, paint[PAINT_DATA_STATUS_LEN], image, paint[PAINT_DATA_TOP_INDEX], paint[PAINT_DATA_TOP_TRANS]);
    	}
    }
    
    /**
     * fill rect
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintRect(GWidget gw,Graphics g,int[] paint){
    	int x,y,w,h;
    	x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
    	y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
    	if(paint[PAINT_DATA_W] == 0){
			w = gw.vmData[GW_VM_W];
		} else {
			w = paint[PAINT_DATA_W];
		}
    	if(paint[PAINT_DATA_H] == 0){
			h = gw.vmData[GW_VM_H];
		} else {
			h = paint[PAINT_DATA_H];
		}
		if((paint[PAINT_DATA_FILLCOLOR] & 0xFF000000) != 0){
    		Tool.fillAlphaRect(g, paint[PAINT_DATA_FILLCOLOR], x, y, w, h);
    	} else {
    		g.setColor(paint[PAINT_DATA_FILLCOLOR]);
        	g.fillRect(x, y, w, h);
    	}
    }
    
    /**
     * 画线
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintLine(GWidget gw,Graphics g,int[] paint){
    	int x,y,w,h;
    	x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
    	y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
    	if(paint[PAINT_DATA_W] == 0){
			w = gw.vmData[GW_VM_W];
		} else {
			w = paint[PAINT_DATA_W];
		}
    	if(paint[PAINT_DATA_H] == 0){
			h = gw.vmData[GW_VM_H];
		} else {
			h = paint[PAINT_DATA_H];
		}
		g.setColor(paint[PAINT_DATA_FILLCOLOR]);
    	g.drawLine(x, y, x + w, y);
    }
    
    /**
     * 执行一个脚本绘制函数（尽量不用）
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintVMFunc(GWidget gw,Graphics g,int[] paint){
    	int func = paint[PAINT_DATA_FUNC];
    	VM vm = gw.vmGame.gtvm;
    	int[] params = new int[2];
    	params[0] = gw.vmData[GW_VM_SELF];
    	params[1] = vm.getRealizeAdrr(vm.makeTempObject(g));
    	vm.execute(func, params);
    	
//    	vm.free(params[0]);
    	vm.free(params[1]);
    }
    
    /**
     * 画目标头像或
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintSprite(GWidget gw,Graphics g,int[] paint){
    	int x;
    	int y;
    	x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
    	y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
    	GameIcon icon = (GameIcon)gw.vmGame.gtvm.followPointer(paint[PAINT_DATA_ICON]);
    	if(icon == null){
    		return;
    	}
    	if(paint[PAINT_DATA_IS_HEAD] == VM.TRUE){
    		icon.drawImageIcon(g, x, y);
    	} else {
    		icon.draw(g, 0, 0);
    	}
    }
    
    
    /**
     * 画贴图
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintImage(GWidget gw,Graphics g,int[] paint){
    	int x = gw.vmData[GW_VM_XX] + paint[PAINT_DATA_X];
    	int y = gw.vmData[GW_VM_YY] + paint[PAINT_DATA_Y];
    	int w = paint[PAINT_DATA_W];
    	int h = paint[PAINT_DATA_H];
    	ImageSet image = (ImageSet)gw.vmGame.gtvm.followPointer(paint[PAINT_DATA_IMAGE]);
    	if(paint[PAINT_DATA_TOPLEFT_INDEX] < 0){
    		return;
    	}
    	if(w > 0 || h > 0){
    		if(w <= 0){
    			w = image.getFrameWidth(paint[PAINT_DATA_TOPLEFT_INDEX]);
    		}
    		if(h <= 0){
    			h = image.getFrameHeight(paint[PAINT_DATA_TOPLEFT_INDEX]);
    		}
//#if opengl == true    		
    		//#    		image.drawFrame(g, paint[PAINT_DATA_TOPLEFT_INDEX], x, y, paint[PAINT_DATA_TOPLEFT_TRANS],paint[PAINT_DATA_ANCHOR],w,h);
//#endif
    	} else {
    		image.drawFrame(g, paint[PAINT_DATA_TOPLEFT_INDEX], x, y, paint[PAINT_DATA_TOPLEFT_TRANS],paint[PAINT_DATA_ANCHOR]);
    	}
    }
    
    /**
     * 添加一个clip
     * @param gw
     * @param g
     * @param paint
     */
    public static void paintClip(GWidget gw,Graphics g,int[] paint){
    	g.setClip(paint[PAINT_DATA_X], paint[PAINT_DATA_Y], paint[PAINT_DATA_W], paint[PAINT_DATA_H]);
    }
    
    public void addPaint(int[] paint){
    	int key = keyMaker.nextKey();
    	paint[GWidget.PAINT_DATA_KEY] = key;
    	switch(paint[GWidget.PAINT_DATA_LAYER]){
    	case GWidget.PAINT_LAYER_BEFORE:
    		if(paintBefores == null){
    			paintBefores = new SortHashtable();
    		}
    		paintBefores.put(new Integer(key), paint);
    		break;
    	case GWidget.PAINT_LAYER_PAINT:
    		if(paints == null){
    			paints = new SortHashtable();
    		}
    		paints.put(new Integer(key), paint);
    		break;
    	case GWidget.PAINT_LAYER_AFTER:
    		if(paintAfters == null){
    			paintAfters = new SortHashtable();
    		}
    		paintAfters.put(new Integer(key), paint);
    		break;
    	default:
    		break;
    	}
    	
    }
    
    /**
     * 用key获得Paint
     * @param key
     * @param layer
     * @return
     */
    public int[] getPaint(int key,int layer){
    	int[] ret = null;
    	switch(layer){
    	case GWidget.PAINT_LAYER_BEFORE:
    		if(paintBefores != null){
    			ret = (int[])paintBefores.get(new Integer(key));
    		}
    		break;
    	case GWidget.PAINT_LAYER_PAINT:
    		if(paints != null){
    			ret = (int[])paints.get(new Integer(key));
    		}
    		break;
    	case GWidget.PAINT_LAYER_AFTER:
    		if(paintAfters != null){
    			ret = (int[])paintAfters.get(new Integer(key));
    		}
    		break;
    	default:
    		break;
    	}
    	return ret;
    }
    
    /**
     * 用index获得Paint
     * @param index
     * @param layer
     * @return
     */
    public int[] getPaint2(int index,int layer){
    	int[] ret = null;
    	switch(layer){
    	case GWidget.PAINT_LAYER_BEFORE:
    		if(paintBefores != null){
    			if(index < paintBefores.size()){
    				ret = (int[])paintBefores.getValue(index);
    			}
    		}
    		break;
    	case GWidget.PAINT_LAYER_PAINT:
    		if(paints != null){
    			if(index < paints.size()){
    				ret = (int[])paints.getValue(index);
    			}
    		}
    		break;
    	case GWidget.PAINT_LAYER_AFTER:
    		if(paintAfters != null){
    			if(index < paintAfters.size()){
    				ret = (int[])paintAfters.getValue(index);
    			}
    		}
    		break;
    	default:
    		break;
    	}
    	return ret;
    }
    
    /**
     * 移出paint
     * @param key
     * @param layer
     */
    public void removePaint(int key,int layer){
    	switch(layer){
    	case GWidget.PAINT_LAYER_BEFORE:
    		if(paintBefores != null){
    			paintBefores.remove(new Integer(key));
    		}
    		break;
    	case GWidget.PAINT_LAYER_PAINT:
    		if(paints != null){
    			paints.remove(new Integer(key));
    		}
    		break;
    	case GWidget.PAINT_LAYER_AFTER:
    		if(paintAfters != null){
    			paintAfters.remove(new Integer(key));
    		}
    		break;
    	default:
    		break;
    	}
    }
    
    public void replacePaint(int[] paint,int key,int layer){
    	paint[PAINT_DATA_KEY] = key;
    	switch(layer){
    	case GWidget.PAINT_LAYER_BEFORE:
    		if(paintBefores != null){
    			paintBefores.put(new Integer(key),paint);
    		}
    		break;
    	case GWidget.PAINT_LAYER_PAINT:
    		if(paints != null){
    			paints.put(new Integer(key),paint);
    		}
    		break;
    	case GWidget.PAINT_LAYER_AFTER:
    		if(paintAfters != null){
    			paintAfters.put(new Integer(key),paint);
    		}
    		break;
    	default:
    		break;
    	}
    }
    
    /**
     * paintafter,paint,paintbefore
     * @param g
     */
    public void drawPaints(Graphics g,int type){
    	SortHashtable list = null;
    	if(type == PAINT_LAYER_BEFORE){
    		list = paintBefores;
    	} else if(type == PAINT_LAYER_AFTER){
    		list = paintAfters;
    	} else {
    		list = paints;
    	}
    	if(list == null){
    		return;
    	}
    	
    	int size = list.size();
    	for (int i = 0; i < size; i++) {
			int[] paint = (int[])list.getValue(i);
			if(paint[PAINT_DATA_HIDE] == VM.TRUE){
				continue;
			}
			switch(paint[PAINT_DATA_TYPE]){
			case PAINT_TYPE_STATUS_BAR:
				paintStatusBar(this, g, paint);
				break;
			case PAINT_TYPE_BACKGROUND:
				paintBackgroud(this, g, paint);
				break;
			case PAINT_TYPE_BUTTON:
				paintButton(this, g, paint);
				break;
			case PAINT_TYPE_IMAGE:
				paintImage(this, g, paint);
				break;
			case PAINT_TYPE_RECT:
				paintRect(this, g, paint);
				break;
			case PAINT_TYPE_SPRITE:
				paintSprite(this, g, paint);
				break;
			case PAINT_TYPE_VM_FUNC:
				paintVMFunc(this, g, paint);
				break;
			case PAINT_TYPE_LINE:
				paintLine(this, g, paint);
				break;
			case PAINT_TYPE_CLIP:
				paintClip(this, g, paint);
				break;
			default:
				break;
			}
		}
    }
    
  //#endif
	int[] getVMDataCopy() {
		int[] vmDataCopy = new int[vmData.length];
		System.arraycopy(this.vmData, 0, vmDataCopy, 0, vmDataCopy.length);
		return vmDataCopy;
	}
	
	void setCloneData(GWidget gWidget) {		
		gWidget.enableFocus = this.enableFocus;
		gWidget.parent = this.parent;
		gWidget.name = this.name;
		gWidget.noNeedLayout = this.noNeedLayout;
		gWidget.isOutView = this.isOutView;
		gWidget.isFocus = this.isFocus;
		gWidget.isScale = this.isScale;
		gWidget.pressX = this.pressX;
		gWidget.pressY = this.pressY;
		gWidget.isShow = this.isShow;
		gWidget.borderLayoutType = this.borderLayoutType;
		if(this.grid3Data != null) {
			gWidget.grid3Data = new int[this.grid3Data.length];
			System.arraycopy(this.grid3Data, 0, gWidget.grid3Data, 0, gWidget.grid3Data.length);
		}
		gWidget.realHeight = this.realHeight;
		gWidget.outHeight = this.outHeight;		
	}
	
	public GWidget getClone(VMGame _vmGame) {		
		GWidget gWidget = new GWidget(_vmGame, 0, getVMDataCopy(), "");		
		setCloneData(gWidget);
		return gWidget;
	}
	
	public static Object[] getCloneArray(VMGame _vmGame, GWidget gWidget, int count) {
		Object[] cloneArray = new Object[count];
		for(int i=0; i<count; i++) {
			cloneArray[i] = gWidget.getClone(_vmGame).vmData;
		}
		
		return cloneArray;
	}

	/**
	 * @param self
	 * @param vmData
	 * @param name
	 */
	public GWidget(VMGame _vmGame, int self, int[] vmData, String name) {
		this.vmData = vmData;
		this.name = name;
		this.vmGame = _vmGame;
		
		if(vmData[GW_VM_MAX_WIDTH] == 0) {
			vmData[GW_VM_MAX_WIDTH] = Integer.MAX_VALUE;	
		}
		if(vmData[GW_VM_MAX_HEIGHT] == 0) {
			vmData[GW_VM_MAX_HEIGHT] = Integer.MAX_VALUE;
		}		
		
		vmData[GW_VM_JAVA_GWIDGET] = keyMaker.nextKey();
		vmGame.putGWidget(this);
		
		vmData[GW_VM_TYPE] = vmData[GW_VM_SYS_TYPE]; 

		synchronized(vmGame.gtvm) {
			vmData[GW_VM_SELF] = vmGame.gtvm.getRealizeAdrr(vmGame.gtvm.makeTempObject(vmData));			
		}
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public Font getFont() {
		return this.font;
	}
	
	public boolean getShow() {
		return isShow;
	}
	
	public void setShow(boolean isShow) {
		this.isShow = isShow;
		
		reCreateStack();
	}
	
	public void SetNeedLayout(boolean needLayout) {
		noNeedLayout = !needLayout;
//		if(!needLayout) {
//			isScale = false;	
//		}
		
	}
	
	public void setPressXY(int x, int y){
		this.pressX = x;
		this.pressY = y;
	}
	
	public int getPressX() {
		return this.pressX;
	}
	
	public int getPressY() {
		return this.pressY;
	}
 	
	public void move(int offsetX, int offsetY) {
		vmData[GW_VM_X] += offsetX;
		vmData[GW_VM_Y] += offsetY;
		
		//获得绝对坐标
		vmData[GW_VM_XX] += offsetX;
		vmData[GW_VM_YY] += offsetY;	
	}
	
	/**
	 * 获得自己的所属window
	 * @param gWidget
	 * @return
	 */
	public GWindow getParentWindow() {	
		if(this.parent instanceof GWindow) {
			return (GWindow)this.parent;
		} else {
			if(this.parent != null) {
				return this.parent.getParentWindow();
			} else {
				return null;
			}
			
		}
	}
	/**
	 * 设置grid3layout的数据
	 * @param gridX x轴grid 
	 * @param gridY y轴grid        
	 * @param gridHCount 横向占用格数
	 * @param gridVCount 纵向占用格数
	 * @param borderTop 
	 * @param borderBottom
	 * @param borderLeft
	 * @param borderRight
	 */
	public void setGrid3Data(int gridX, int gridY, int gridHCount, int gridVCount, int borderTop, int borderBottom, int borderLeft, int borderRight) {
		grid3Data = new int[8];
		grid3Data[0] = gridX;
		grid3Data[1] = gridY;
		grid3Data[2] = gridHCount;
		grid3Data[3] = gridVCount;
		grid3Data[4] = borderTop;
		grid3Data[5] = borderBottom;
		grid3Data[6] = borderLeft;
		grid3Data[7] = borderRight;		
	}
		
	public boolean isFocus() {
		return isFocus;
	}
	
	public void setScale(boolean isScale) {
		this.isScale = isScale;
	}
		
	public void setEnableFocus(boolean enableFocus) {
		this.enableFocus = enableFocus;
	}
	
	/**
	 * 
	 * @param gtvm
	 * @param _x
	 * @param _y
	 * @param _w
	 * @param _h
	 */
	public void setBounds(int _x, int _y, int _w, int _h) {
		this.setPos(_x, _y);
//#if NewUI2
//#else
		//#if ScreenCanReset == false
		if(_x+_w>GameMain.viewWidth){
			_w = GameMain.viewWidth - _x;
		}
		//#else
		//# if(this instanceof GWindow && ((GWindow)this).fullScreen || (this.getParentWindow() != null && this.getParentWindow().fullScreen)) {
		//# if(_x+_w>GameMain.viewWidth){
			//# _w = GameMain.viewWidth - _x;
		//# }
		//# } else if(_w > GWindow.uiMaxWidth){
		//# _w = GWindow.uiMaxWidth;
		//# }
		//#endif

//#endif
		vmData[GW_VM_W] = _w;
		vmData[GW_VM_H] = _h;
				
		//处理resize事件
		if(this.isScale && vmData[GW_VM_FUNC_RESIZE] != 0) {
			synchronized (vmGame.gtvm) {
				vmGame.gtvm.callback(vmData[GW_VM_FUNC_RESIZE], new int[] { 
						vmData[GW_VM_SELF], _x, _y, _w, _h
					});
			}
		}			
	}
		
	/**
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y) {
		vmData[GW_VM_X] = x;
		vmData[GW_VM_Y] = y;
		
		//获得绝对坐标
		vmData[GW_VM_XX] = getAbsX();
		vmData[GW_VM_YY] = getAbsY();
	}
	
	/**
	 * @param w
	 * @param h
	 */
	public void setSize(int w, int h) {
		vmData[GW_VM_W] = w;
		vmData[GW_VM_H] = h;
	}
	
	/**
	 * @param w
	 * @param h
	 */
	public void setMinSize(int w, int h) {
		vmData[GW_VM_MIN_WIDTH] = w;
		vmData[GW_VM_MIN_HEIGHT] = h;
	}
	
	/**
	 * @param borderTop
	 * @param borderLeft
	 * @param borderRight
	 * @param borderBottom
	 */
	public void setBorder(int borderTop, int borderBottom, int borderLeft, int borderRight) {
		vmData[GW_VM_BORDERTOP]     =  borderTop    ;
		vmData[GW_VM_BORDERLEFT]    =  borderLeft   ;
		vmData[GW_VM_BORDERRIGHT]   =  borderRight  ;
		vmData[GW_VM_BORDERBOTTOM]  =  borderBottom ;
	}

	/**
	 * @return  绝对x坐标
	 */
	public int getAbsX() {
		int _x = vmData[GW_VM_X];
		
		GWidget _parent = this.parent;
		while(_parent != null) {			
			_x += _parent.vmData[GW_VM_X];
			_parent = _parent.parent;
			
		}
		
		return _x;
	}

	/**
	 * @return  绝对y坐标
	 */
	public int getAbsY() {
		int _y = vmData[GW_VM_Y];
		GWidget _parent = this.parent;
		while(_parent != null) {			
			_y += _parent.vmData[GW_VM_Y];
			_parent = _parent.parent;
			
		}
		
		return _y;
	}

	public void setOutView(boolean isOutView) {
		this.isOutView = isOutView;		
		reCreateStack();
	}

	public void reCreateStack() {
		if(this instanceof GWindow) {
			((GWindow)this).reCreateStack = true;
		} else {
			GWindow gParentWindow = this.getParentWindow();
			if(gParentWindow != null) {
				gParentWindow.reCreateStack = true;
			}
		}
		
	}
	
	public boolean isOutView() {
		return isOutView;
	}
	
	public int getX(){
		return vmData[GW_VM_X];
	}
	
	public int getY(){
		return vmData[GW_VM_Y];
	}
	
	public int getW(){
		return vmData[GW_VM_W];
	}
	
	public int getMinW(){
		return vmData[GW_VM_MIN_WIDTH];
	}
	
	public int getMaxW(){
		return vmData[GW_VM_MAX_WIDTH];
	}
	
	public void freeVMObj() {
		synchronized(vmGame.gtvm) {
			if(vmGame.gtvm.followPointer(this.vmData[GW_VM_SELF]) != null) {
				vmGame.gtvm.free(this.vmData[GW_VM_SELF]);	
			}			
		}
	}
	
	//遍历判断父组件是否需要滚动
	public boolean parentNeedScroll() {
		GContainer _parent = parent;
		while(_parent != null) {
			if(_parent.needScrollBar) {
				return true;
			} else {
				_parent = _parent.parent;
			}
		}
		
		return false;
		
	}
	
	//获取组件在容器的可视范围内的相交矩形
	public void getIntersect(int[] ret) {
		if(parent != null) {
			this.parent.getIntersect(parent.rect);
			Tool.rectGetIntersection(
					vmData[GW_VM_XX] + vmData[GW_VM_OFFSET_X], vmData[GW_VM_YY] + vmData[GW_VM_OFFSET_Y], vmData[GW_VM_W], vmData[GW_VM_H],
					parent.rect[0], parent.rect[1], parent.rect[2], parent.rect[3], ret);
		} else {
			ret[0] = this.vmData[GW_VM_XX] + vmData[GW_VM_OFFSET_X];
			ret[1] = this.vmData[GW_VM_YY] + vmData[GW_VM_OFFSET_Y];
			ret[2] = this.vmData[GW_VM_W];
			ret[3] = this.vmData[GW_VM_H];
		}
	}
	
	//#if NewUI2
	public void setNeedClip(boolean b){
		this.needClip = b;
	}
	
	public boolean getNeedClip(){
		return this.needClip;
	}
	//#endif
}
