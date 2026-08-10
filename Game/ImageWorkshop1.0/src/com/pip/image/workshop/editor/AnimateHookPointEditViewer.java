package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.pip.image.workshop.Settings;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mapeditor.MapEditor;
import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.ext.EdgeExtension;
import com.pipimage.image.ext.HookPointExtension;
import com.swtdesigner.SWTResourceManager;

/**
 * 编辑一个挂接点在一帧上的位置。
 * @author light.hu
 */
public class AnimateHookPointEditViewer extends AbstractImageViewer implements Runnable {
	protected AnimateEditor ownerEditor;
	
	protected PipAnimateFrame frame;
	protected HookPointExtension.HookPoint hookPoint;
	
	private boolean disposed;
	private Display display;
	private Thread animateThread;
	
	private HookPointExtension.Position position;   // 起始点位置,方向
	private int directionX;     // 方向目标点位置
	private int directionY;
	
	private boolean dragging = false;    // 是否正在拖拽
	private int dragType; 			// 0 - 拖拽起始点，1 - 拖拽目标点
	
	// 刷子设置
	private String[] buttonTexts = { "添加", "删除" ,"切换为先画", "预览粒子效果" };
    // 设置按钮的位置和大小
    private Rectangle[] buttonBounds = new Rectangle[buttonTexts.length];
    
    private ImageDrawCache cache = MapEditor.imageCache;
    
    public void setImageCache(ImageDrawCache cache) {
		this.cache = cache;
	}
    
    public void setOwnerEditor(AnimateEditor owner) {
    	ownerEditor = owner;
    }
	
	public void setInput(PipAnimateFrame frame, HookPointExtension.HookPoint hookPoint) {
		super.setInput(frame);
		this.frame = frame;
		this.hookPoint = hookPoint;
		dragging = false;
		if (hookPoint != null) {
			position = hookPoint.posList.get(frame);
			calcTargetPos();
			if (position != null) {
				if (position.direction >= 1000) {
					buttonTexts[2] = "切换为先画";
				} else {
					buttonTexts[2] = "切换为后画";
				}
			}
		} else {
			position = null;
		}
		redraw();
	}
	
	private void calcTargetPos() {
		if (position != null) {
			// 根据方向计算目标点位置
			int dist = 30;
			int angle = position.direction;
			if (angle >= 1000) {
				angle -= 1000;
			}
			angle = angle % 360;
			double dangle = (angle * Math.PI) / 180;
			directionX = position.x + (int)(dist * Math.cos(dangle));
			directionY = position.y - (int)(dist * Math.sin(dangle));
		}
	}
	
	// 根据起始点和目标点的位置，更新方向值
	private void calcDirection() {
		double dx = directionX - position.x;
		double dy = position.y - directionY;
		double dist = Math.sqrt(dx * dx + dy * dy);
		double angle = Math.asin(Math.abs(dy) / dist);
		if (dx > 0) {
			if (dy > 0) {
//				angle = angle;
			} else {
				angle = Math.PI * 2 - angle;
			}
		} else {
			if (dy > 0) {
				angle = Math.PI - angle;
			} else {
				angle = Math.PI + angle;
			}
		}
		int iangle = (int)((angle / Math.PI) * 180);
		if (iangle < 0) {
			iangle = 0;
		}
		if (iangle >= 360) {
			iangle = 359;
		}
		if (position.direction >= 1000) {
			position.direction = 1000 + iangle;
		} else {
			position.direction = iangle;
		}
	}
	
	private Point calcCoord(Point p) {
		Point size = getSize();
		int x = (int)Math.round((p.x - size.x / 2 - paintOffset.x) / ratio - 0.5);
		int y = (int)Math.round((p.y - size.y / 2 - paintOffset.y) / ratio - 0.5);
		return new Point(x, y);
	}
	
	private Point coordToPos(Point p) {
		Point size = getSize();
		int x = (int)(p.x * ratio + size.x / 2 + paintOffset.x);
		int y = (int)(p.y * ratio + size.y / 2 + paintOffset.y);
		return new Point(x, y);
	}
	
	public AnimateHookPointEditViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND, GLUtils.glEnabled);

		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					// 判断是否点到左下角按钮区
					Point pt = new Point(e.x, e.y);
			        for (int i = 0; i < buttonBounds.length; i++) {
			            if (buttonBounds[i].contains(pt)) {
			            	if (i == 0) {
			            		// 设置点
			            		if (hookPoint != null && position == null) {
			            			position = new HookPointExtension.Position();
			            			position.x = 0;
			            			position.y = 0;
			            			position.direction = 1090;
			            			hookPoint.posList.put(frame, position);
			            			calcTargetPos();
			            			fireContentChanged();
			            		}
			            	} else if (i == 1) {
			            		// 删除点
			            		if (hookPoint != null && position != null) {
			            			position = null;
			            			hookPoint.posList.remove(frame);
			            			fireContentChanged();
			            		}
			            	} else if (i == 2) {
			            		// 切换先画/后画
			            		if (hookPoint != null && position != null) {
			            			if (position.direction < 1000) {
			            				// 当前是先画，切换为后画
			            				position.direction += 1000;
			            				buttonTexts[2] = "切换为先画";
			            			} else {
			            				// 当前是后画，切换为先画
			            				position.direction -= 1000;
			            				buttonTexts[2] = "切换为后画";
			            			}
			            			fireContentChanged();
			            		}
			            	} else if (i == 3) {
			            		// 选择预览粒子效果
			            		onPreviewParticleEffect();
			            	}
			                return;
			            }
			        }
			        
			        // 判断是否点击到起始点和目标点
			        if (position != null) {
			        	Point pt2 = calcCoord(pt);
			        	if (Math.abs(pt2.x - position.x) <= 2 && Math.abs(pt2.y - position.y) <= 2) {
			        		dragging = true;
			        		dragType = 0;
			        	} else if (Math.abs(pt2.x - directionX) <= 2 && Math.abs(pt2.y - directionY) <= 2) {
			        		dragging = true;
			        		dragType = 1;
			        	}
			        }
				}
			}
			public void mouseUp(MouseEvent e) {
				dragging = false;
			}
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (dragging && position != null) {
					Point pt = new Point(e.x, e.y);
					Point pt2 = calcCoord(pt);
					if (dragType == 0) {
						// 拖动起始点
						if (Math.abs(pt2.x - directionX) < 4 && Math.abs(pt2.y - directionY) < 4) {
							// 不能和目标点重合
							return;
						}
						position.x = pt2.x;
						position.y = pt2.y;
						calcDirection();
						fireContentChanged();
					} else if (dragType == 1) {
						// 拖动目标点
						if (Math.abs(pt2.x - position.x) < 4 && Math.abs(pt2.y - position.y) < 4) {
							// 不能和起始点重合
							return;
						}
						directionX = pt2.x;
						directionY = pt2.y;
						calcDirection();
						fireContentChanged();
					}
				}
				redraw();
			}
		});
		
		display = this.getDisplay();
		animateThread = new Thread(this);
		animateThread.start();
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input != null) {
			int drawX = size.x / 2 + paintOffset.x;
			int drawY = size.y / 2 + paintOffset.y;
			frame.draw(gc, drawX, drawY, ratio, cache);
		}
	}

	protected void paintInput(GLGraphics gc) {
		Point size = getSize();
		if (input != null) {
			gc.setZ(0);
			int drawX = size.x / 2 + paintOffset.x;
			int drawY = size.y / 2 + paintOffset.y;
			frame.draw(gc, drawX, drawY, ratio, cache);
		}
	}
	
	protected void drawButtons(GC gc) {
    	Point size = getSize();
        gc.setBackground(getBackground());
        gc.setForeground(AbstractImageViewer.invert(getBackground()));
        int bx = 1;
        int i;
        for (i = 0; i < buttonTexts.length; i++) {
        	Point ts = gc.textExtent(buttonTexts[i]);
        	int by = size.y - ts.y - 8;
        	int bw = ts.x + 7;
        	int bh = ts.y + 6;
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
        	gc.setForeground(AbstractImageViewer.invert(getBackground()));
            gc.drawRectangle(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            bx += bw + 2;
        }		
	}
	
	protected void drawButtons(GLGraphics gc) {
    	Point size = getSize();
        gc.setColor(AbstractImageViewer.invert(getBackground()));
        int bx = 1;
        int i;
        for (i = 0; i < buttonTexts.length; i++) {
        	Point ts = gc.textExtent(buttonTexts[i]);
        	int by = size.y - ts.y - 8;
        	int bw = ts.x + 7;
        	int bh = ts.y + 6;
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
        	gc.setColor(AbstractImageViewer.invert(getBackground()));
            gc.drawRect(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            bx += bw + 2;
        }		
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
		gc.drawLine(0, size.y / 2 + paintOffset.y, size.x, size.y / 2 + paintOffset.y);
		gc.drawLine(size.x / 2 + paintOffset.x, 0, size.x / 2 + paintOffset.x, size.y);
		
		// 绘制起始点和目标点
		if (position != null) {
			int blockSize = 2;
			if (ratio > 1.0f) {
				blockSize *= ratio;
			}
			gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
			Point pt = new Point(position.x, position.y);
			pt = coordToPos(pt);
			gc.drawRectangle(pt.x - blockSize, pt.y - blockSize, blockSize * 2, blockSize * 2);
			gc.drawLine(pt.x - blockSize, pt.y, pt.x + blockSize, pt.y);
			gc.drawLine(pt.x, pt.y - blockSize, pt.x, pt.y + blockSize);
			
			gc.setForeground(SWTResourceManager.getColor(0x00, 0x99, 0xFF));
			Point pt2 = new Point(directionX, directionY);
			pt2 = coordToPos(pt2);
			gc.drawRectangle(pt2.x - blockSize, pt2.y - blockSize, blockSize * 2, blockSize * 2);
			gc.drawLine(pt2.x - blockSize, pt2.y, pt2.x + blockSize, pt2.y);
			gc.drawLine(pt2.x, pt2.y - blockSize, pt2.x, pt2.y + blockSize);
			
			gc.drawLine(pt.x, pt.y, pt2.x, pt2.y);
		}
		
		// 绘制选项按钮
		drawButtons(gc);
	}
	
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		gc.setColor(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
		gc.drawLine(0, size.y / 2 + paintOffset.y, size.x, size.y / 2 + paintOffset.y);
		gc.drawLine(size.x / 2 + paintOffset.x, 0, size.x / 2 + paintOffset.x, size.y);
		
		// 绘制起始点和目标点
		if (position != null) {
			int blockSize = 2;
			if (ratio > 1.0f) {
				blockSize *= ratio;
			}
			gc.setColor(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
			Point pt = new Point(position.x, position.y);
			pt = coordToPos(pt);
			gc.drawRect(pt.x - blockSize, pt.y - blockSize, blockSize * 2, blockSize * 2);
			gc.drawLine(pt.x - blockSize, pt.y, pt.x + blockSize, pt.y);
			gc.drawLine(pt.x, pt.y - blockSize, pt.x, pt.y + blockSize);
			
			gc.setColor(SWTResourceManager.getColor(0x00, 0x99, 0xFF));
			Point pt2 = new Point(directionX, directionY);
			pt2 = coordToPos(pt2);
			gc.drawRect(pt2.x - blockSize, pt2.y - blockSize, blockSize * 2, blockSize * 2);
			gc.drawLine(pt2.x - blockSize, pt2.y, pt2.x + blockSize, pt2.y);
			gc.drawLine(pt2.x, pt2.y - blockSize, pt2.x, pt2.y + blockSize);
			
			gc.drawLine(pt.x, pt.y, pt2.x, pt2.y);
		}
		
		// 绘制选项按钮
		drawButtons(gc);
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    disposed = true;
	    try {
	        this.animateThread.join();
	    } catch (Exception e1) {
	    }
	}

	// 驱动动画
	public void run() {
        while (!disposed) {
        	long preTime = System.currentTimeMillis();
            try {
                display.asyncExec(new Runnable() {
                    public void run() {
                    	if(!isDisposed())
                        redraw();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        	try {
            	long processTime = System.currentTimeMillis() - preTime;
            	if (processTime < Settings.animateFrameDelay) {
            		Thread.sleep(Settings.animateFrameDelay - processTime);
            	}
			} catch (Exception e) {
			}
        }
	}
	
	// 选择一个粒子效果作为绑定的预览效果
	private void onPreviewParticleEffect() {
		if (!glMode) {
			MessageDialog.openError(getShell(), "错误", "只有OpenGL模式才能使用此功能。");
			return;
		}
		
		if (hookPoint == null) {
			MessageDialog.openError(getShell(), "错误", "请先选择一个挂接点。");
			return;
		}
		
		ownerEditor.hookParticleEffect(hookPoint);
	}
}
