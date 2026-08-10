package com.pip.mapeditor.tool;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mango.jni.GLGraphics;
import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 在场景中模拟一个人物行走。
 * @author lighthu
 */
public class EmulateWalkTool implements IMapEditTool, IUpdatableTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 角色动画
    private PipAnimateSet roleAnimates;
    // 当前方向0123分别表示下右左上
    private int direction = 0;
    // 是否正在移动
    private boolean moving = false;
    // 当前角色位置（地图位置）
    private int roleX, roleY;
    // 移动速度（每cycle像素）
    private int speed = 9;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public EmulateWalkTool(MapViewer viewer) throws Exception {
        this.viewer = viewer;
        roleX = viewer.getMap().width / 2;
        roleY = viewer.getMap().height / 2;
        
        // 载入角色动画
        File f = new File(System.getProperty("user.home"), "imageworkshop/role.cts");
        roleAnimates = new PipAnimateSet();
    	roleAnimates.load(f);
    }
    
    /**
     * 定时更新。
     */
    public void update() {
    	if (moving) {
    		for (int i = 0; i < speed; i++) {
		    	switch (direction) {
		    	case 0:
		    		if (checkCollision(roleX, roleY + 1)) {
		    			roleY++;
		    		}
		    		break;
		    	case 1:
		    		if (checkCollision(roleX + 1, roleY)) {
		    			roleX++;
		    		}
		    		break;
		    	case 2:
		    		if (checkCollision(roleX - 1, roleY)) {
		    			roleX--;
		    		}
		    		break;
		    	case 3:
		    		if (checkCollision(roleX, roleY - 1)) {
		    			roleY--;
		    		}
		    		break;
		    	}
    		}
    	}
    	viewer.setTempShowNPC(getActionAnimate(), roleX, roleY);
    }
    
    /*
     * 检查碰撞区域，如果可通过返回true。
     */
    private boolean checkCollision(int x, int y) {
    	GameMap map = viewer.getMap();
    	Rectangle rect = new Rectangle(x - 4, y - 2, 8, 4);
    	if (rect.x < 0 || rect.y < 0 || rect.x + rect.width >= map.width || rect.y + rect.height >= map.height) {
    		return false;
    	}
    	if (map.parent.isLibMode) {
    		// 库模式的地图，碰撞区域已经是格点数据了
    		int startX = rect.x / map.parent.getCellSize();
    		int startY = rect.y / map.parent.getCellSize();
    		int endX = (rect.x + rect.width - 1) / map.parent.getCellSize();
    		int endY = (rect.y + rect.height - 1) / map.parent.getCellSize();
    		for (int i = startY; i <= endY; i++) {
    			for (int j = startX; j <= endX; j++) {
    				if ((map.tileInfo[i][j] & 0x02) == 0) {
    					return false;
    				}
    			}
    		}
    		return true;
    	} else {
    		// 非库模式
    		boolean afterGround = false;
    		for (IMapLayer l : map.layers) {
                if (l instanceof AccurateMapLayer) {
                	// 精确贴图
                	AccurateMapLayer layer = (AccurateMapLayer)l;
        			int startX = rect.x / map.parent.getTileWidth();
            		int startY = rect.y / map.parent.getTileHeight();
            		int endX = (rect.x + rect.width - 1) / map.parent.getTileWidth();
            		int endY = (rect.y + rect.height - 1) / map.parent.getTileHeight();
            		for (int i = startY; i <= endY; i++) {
            			for (int j = startX; j <= endX; j++) {
            				short tid = layer.getLayerData()[i][j];
            				if (tid == -1) {
            					return false;
            				}
            				if (map.parent.getTileImage().tileInfo.get(tid).unpassable) {
            					return false;
            				}
            			}
            		}
                } else if (l instanceof MapNPCLayer) {
                    if (afterGround) {
                        // 天空层不计算碰撞区域
                        continue;
                    }
                    if (l == map.groundLayer) {
                        afterGround = true;
                    }
                    for (MapNPC npc : ((MapNPCLayer)l).getNpcs()) {
                    	long key = map.parent.getNPCKey(npc);
                        NPCImageInfo ninfo = map.parent.getNPCs().get(key);
                        if (ninfo == null) {
                            continue;
                        }
                        for (int i = 0; i < ninfo.cx.length; i++) {
                            Rectangle r = new Rectangle(ninfo.cx[i], ninfo.cy[i], ninfo.cw[i], ninfo.ch[i]);
                            r.x += npc.x;
                            r.y += npc.y;
                            if (r.intersects(rect)) {
                            	return false;
                            }
                        }
                    }
                } else if (l instanceof BlurMapLayer) {
                	BlurMapLayer layer = (BlurMapLayer)l;
        			int startX = rect.x / map.parent.getBlurTileWidth();
            		int startY = rect.y / map.parent.getBlurTileHeight();
            		int endX = (rect.x + rect.width - 1) / map.parent.getBlurTileWidth();
            		int endY = (rect.y + rect.height - 1) / map.parent.getBlurTileHeight();
            		for (int i = startY; i <= endY; i++) {
            			for (int j = startX; j <= endX; j++) {
            				int[] cc = layer.getMapData()[i][j];
            				for (int k = cc.length - 1; k >= 0; k--) {
                                if (cc[k] != -1) {
                                    int lfid = cc[k] >> 16;
                                    int tid = cc[k] & 0xFF;
                                    TileInfo cinfo = map.parent.getLandforms().get(lfid).tileInfo.get(tid);
                                    if (cinfo.unpassable) {
                                        return false;
                                    }
                                    break;
                                }
                            }
            			}
            		}
                }
            }
    	}
    	return true;
    }
    
    private PipAnimate getActionAnimate() {
    	if (moving) {
    		return roleAnimates.getAnimate(direction + 4);
    	} else {
    		return roleAnimates.getAnimate(direction);
    	}
    }

    /**
     * 鼠标按下事件。开始拖动绘制。
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
    }

    /**
     * 鼠标抬起事件。拖动绘制结束，把拖动过的点都设置为选中的Tile。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
    	if ((mask & SWT.SHIFT) != 0) {
    		// SHIFT点击，提速
    		speed++;
        	if (speed >= 20) {
        		speed = 3;
        	}
    	} else {
    		// 普通点击，直接移动人物
    		roleX = x;
    		roleY = y;
    		viewer.redraw();
    	}
    }
    
    /**
     * 鼠标移动事件。拖动刷子。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
    }

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
    }

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GLGraphics gc) {
    }
    
    /**
     * 键按下事件。
     */
    public void onKeyDown(int keyCode) {
    	if (keyCode == SWT.ARROW_DOWN) {
    		direction = 0;
        	moving = true;
    	} else if (keyCode == SWT.ARROW_RIGHT) {
    		direction = 1;
        	moving = true;
    	} else if (keyCode == SWT.ARROW_LEFT) {
    		direction = 2;
        	moving = true;
    	} else if (keyCode == SWT.ARROW_UP) {
    		direction = 3;
        	moving = true;
    	}
    }

    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode) {
    	if (keyCode == SWT.ARROW_DOWN) {
    		if (direction == 0) {
    			moving = false;
    		}
    	} else if (keyCode == SWT.ARROW_RIGHT) {
    		if (direction == 1) {
    			moving = false;
    		}
    	} else if (keyCode == SWT.ARROW_LEFT) {
    		if (direction == 2) {
    			moving = false;
    		}
    	} else if (keyCode == SWT.ARROW_UP) {
    		if (direction == 3) {
    			moving = false;
    		}
    	}
    }

    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        return null;
    }
}
