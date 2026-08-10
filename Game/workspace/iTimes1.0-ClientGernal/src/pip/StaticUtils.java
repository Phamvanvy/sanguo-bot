package pip;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import pip.io.UWAPConnection;
import pip.io.UWAPSegment;

public class StaticUtils {
	//#if TouchScreen == true
	
	// 用于添加按钮的临时空间，每个元素是一个int[5]
	private static Vector tempButtons;
	
	// 屏幕上的按钮ID和按钮位置
	private static int[] buttonID;
	private static short[] buttonLeft;
	private static short[] buttonTop;
	private static short[] buttonWidth;
	private static short[] buttonHeight;
	
	/// 当前是否正在拖动
	private static boolean dragging = false;
	public static boolean isDragging() {
		return dragging;
	}
	// 当前拖动位置上的按钮索引，-1表示没有
	private static int focusButton = -1;
	public static void setFocusButton(int focusButton) {
		StaticUtils.focusButton = focusButton;
	}
	// 刚刚按下的按钮ID，-1表示没有
	private static int pressedButton = -1;
	
	//是否双击事件
	public static boolean pressedDoubleButton = false;
	
	public static boolean touchBuff;
	public static void beginButtonSetting() {
		tempButtons = new Vector();
	}
	public static void addButton(int id, int x, int y, int w, int h) {
		tempButtons.addElement(new int[] { id,x, y, w, h });
	}
	public static void removeAllButton() {
		tempButtons.removeAllElements();
	}
	//老子做半天，最后竟然没有api，气死我了
	/*public static void removeButton(int id) {
		for(int i = 0; i < tempButtons.size(); i++){
			int temp[]=(int[])tempButtons.elementAt(i);
			if(temp[0] == id)
			{
				tempButtons.remove(i);
			}
		}
	}*/
	public synchronized static int getButtonAt(int x, int y) {
		int count = buttonID.length;
		int id=-1;
		for (int i = 0; i < count; i++) {
			if (x >= buttonLeft[i] && x < buttonLeft[i] + buttonWidth[i]
					&& y >= buttonTop[i] && y < buttonTop[i] + buttonHeight[i]) {
				id=i;
				if(buttonID[i] >= 3000){
					break;
				}
			}
		}
		return id;
	}
	/**
	 * 结束屏幕按钮设置。这个方法应该在每个cycle结束时调用。
	 */
	public static void endButtonSetting() {
		int count = tempButtons.size();
		int[] id = new int[count];
		short[] x = new short[count];
		short[] y = new short[count];
		short[] w = new short[count];
		short[] h = new short[count];
		for (int i = 0; i < count; i++) {
			int[] arr = (int[]) tempButtons.elementAt(i);
			id[i] = arr[0];
			x[i] = (short) arr[1];
			y[i] = (short) arr[2];
			w[i] = (short) arr[3];
			h[i] = (short) arr[4];
		}
		registerButtons(id, x, y, w, h);
		tempButtons = null;
	}

	/**
	 * 设置屏幕按钮。
	 */
	/**
	 * 设置屏幕按钮。
	 */
	public synchronized static void registerButtons(int[] id, short[] x,
			short[] y, short[] w, short[] h) {
		buttonID = id;
		buttonLeft = x;
		buttonTop = y;
		buttonWidth = w;
		buttonHeight = h;
	}
	/**
	 * 绘制当前选中按钮的选择框，如果没有按下按钮，则什么都不画。
	 */
	public synchronized static void drawFocusButton(Graphics g) {
		if (focusButton >= 0 && focusButton < buttonID.length) {
			g.setColor(0xCC0000);
			g.drawRoundRect(buttonLeft[focusButton], buttonTop[focusButton],
					buttonWidth[focusButton] - 1,
					buttonHeight[focusButton] - 1, 5, 5);
		}
	}
	/**
	 * 触笔按下事件处理。
	 */
	public static void pointerPressed(int x, int y) {
		int btn = getButtonAt(x, y);
		if (btn != -1) {
			dragging = true;
			focusButton = btn;
		} else {
			dragging = false;
			focusButton = -1;
		}
	}
	/**
	 * 触笔放开事件处理。如果放开的时候位置在一个按钮上，则按下这个按钮。
	 */
	public static void pointerReleased(int x, int y) {
		focusButton = -1;

		if (dragging) {
			dragging = false;
			int btn = getButtonAt(x, y);
			if (btn != -1) {
				btn = buttonID[btn];

				// 按钮被按下，如果按钮是在32以内，则按钮ID和按键ID对应，否则是特殊ID特殊处理
				if (btn < 32) {
					World.keyFlag |= 1L << (btn << 1);
				} else {
					pressedButton = btn;
					
				}
				//如果是双击时间，需要处理聚焦
				if(pressedDoubleButton){
					focusButton = getButtonAt(x, y);
				}
			}
		}
	}
	/**
	 * 取得刚刚按下的按钮ID，-1表示没有按下。
	 * 
	 * @return 如果有按钮按下，第一次调用本方法将返回按钮ID，后续的调用返回-1。所以注意每个 cycle这个方法应该被调用且只被调用一次。
	 */
	public synchronized static int getPressedButton() {
		int ret = pressedButton;
		pressedButton = -1;
		return ret;
	}
	/**
	 * 触笔拖动事件处理。
	 */
	public static void pointerDragged(int x, int y) {
		if (dragging) {
			focusButton = getButtonAt(x, y);
		}
	}
	/**
	 * 得到当前拖动过的按钮的ID。这个方法用于菜单实现拖动焦点。
	 * 
	 * @return 如果没有拖动到某个按钮上，返回-1
	 */
	public synchronized static int getDragOverButton() {
		if (focusButton == -1 || buttonID.length ==0) {
			return -1;
		}
		return buttonID[focusButton];
	}
	//#endif
}
