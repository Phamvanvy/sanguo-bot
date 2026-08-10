package com.pip.image.workshop.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mango.jni.GLGraphics;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateFrameRef;
import com.swtdesigner.SWTResourceManager;

/**
 * 
 * @author jhkang
 *
 */
public class EquipAnimateViewer extends AnimateViewer {

	private Rectangle selectingEquipRect;
	private Rectangle selectingHookRect;
	private int hookSelected;
	/**
	 * 装备绑定到人物形象动画中的挂接点ID,必须由使用者指定值
	 */
	private int bindHookId = -1;
	private boolean equipCtsModified = false;
	private Point fontSize;
	private Rectangle saveButtonRect;
	private EquipAnimateSaver equipAniSaver;
	private boolean isDragginAnchor;
	private boolean hookPosChanged;
	
	private static final int 	NULL_SELECTED = 0;
	private static final int 	HOOK_SELECTED = 1;
	private static final int 	EQUIP_SELECTED = 2;

	public EquipAnimateViewer(Composite parent, int style) {
		super(parent, style);
		showRefLines = false;
	}

	@Override
	protected void onMouseDown(MouseEvent e) {
		if (input == null) {
			return;
		}
		if(isPlaying() || e.button != 1){
			return;
		}
		if(equipCtsModified && saveButtonRect != null && saveButtonRect.contains(e.x, e.y)){
			fireSaveEquipAni();
			return;
		}
		if(currentFrame != -1 && input !=null){
			PipAnimateFrameRef frameRef = getFrameData();
			PipAnimateFrame bodyFrame = frameRef.realize();
			Point size = getSize();
			int paintBodyFrameX = (int) (size.x / 2 + frameRef.getDx()*ratio + paintOffset.x);
			int paintBodyFrameY = (int) (size.y / 2 + frameRef.getDy()*ratio + paintOffset.y);
			PipAni4AniFramePiece hook = bodyFrame.getHook(bindHookId);
			if(hook.binded() == false){
				return;
			}
			int aniX = (int) (hook.getRealDx()*ratio + paintBodyFrameX);
			int aniY = (int) (hook.getRealDy()*ratio + paintBodyFrameY);
			Rectangle rect = zoom(hook.getBindAnimate().getBounds());
			rect.x += aniX;
			rect.y += aniY;
			if(hook.getAnchorRect().contains(e.x, e.y) && ((e.stateMask & SWT.SHIFT) == 0) ){
				isDragginAnchor = true;
				dragStartPoint = new Point(e.x, e.y);
				selectingHookRect = hook.getAnchorRect();
			}else if(rect.contains(e.x, e.y)){
				selectingEquipRect = rect;
				isDragging = true;
				dragStartPoint = new Point(e.x, e.y);
			}else{
				hookSelected = NULL_SELECTED;
				setPaintEquipBounds(false);
			}
		}
	}

	private void fireSaveEquipAni() {
		if(equipAniSaver!=null){
			equipCtsModified = !equipAniSaver.saveEquipAnimate(getFrameData().getParent());
		}
	}

	@Override
	protected void onMouseUp(MouseEvent e) {
		if (input == null) {
			return;
		}
		if(e.button != 1){
			return;
		}
		if(selectingHookRect != null && selectingHookRect.contains(e.x, e.y)){
			selectingHookRect = null;
			setPaintEquipBounds(true);
			setPaintHookBounds(true);
			hookSelected = HOOK_SELECTED;
		}else if(selectingEquipRect != null && selectingEquipRect.contains(e.x, e.y)){
			selectingEquipRect = null;
			setPaintEquipBounds(true);
			setPaintHookBounds(false);
			hookSelected = EQUIP_SELECTED;
		}else if(isDragginAnchor == false && isDragging == false){
			setPaintEquipBounds(false);
			setPaintHookBounds(false);
		}
		isDragging = false;
		isDragginAnchor = false;
	}

	private void setPaintHookBounds(boolean b){
		getFrameData().realize().getHook(bindHookId).setPaintAnchorBounds(b);
	}
	
	private void setPaintEquipBounds(boolean b) {
		PipAnimateFrameRef frameRef = getFrameData();
		PipAnimateFrame bodyFrame = frameRef.realize();
		int cnt = bodyFrame.getPieceCount();
		for(int i=0; i<cnt; i++){
			PipAnimateFramePiece piece = bodyFrame.getPiece(i);
			if(piece instanceof PipAni4AniFramePiece){
				((PipAni4AniFramePiece)piece).setPaintBounds(b);
				((PipAni4AniFramePiece)piece).setPaintAnchor(b);
			}
		}		
	}
	protected void onKeyDown(int keyCode) {
		if (keyCode == 'w') {
			zoomin();
			return;
		} else if (keyCode == 's') {
			zoomout();
			return;
		} else if (keyCode == 'g') {
			switchShowGrid();
			return;
		} else if (keyCode == 'l') {
			setShowRefLines(!showRefLines);
			return;
		}
		if (hookSelected == NULL_SELECTED || isDragging || input == null || currentFrame == -1) {
			return;
		}
		PipAni4AniFramePiece ref = ((PipAnimate)input).getFrame(currentFrame).realize().getHook(bindHookId);
		if(ref == null){
			return;
		}
		int dx = 0;
		int dy = 0;
		switch (keyCode) {
		case SWT.ARROW_UP:
			dy = -1;
			break;
		case SWT.ARROW_DOWN:
			dy = 1;
			break;
		case SWT.ARROW_LEFT:
			dx = -1;
			break;
		case SWT.ARROW_RIGHT:
			dx = 1;
			break;
		default:
			return;
		}
		if(hookSelected == HOOK_SELECTED){
			updateHookPos(dx, dy);
		}else if(hookSelected == EQUIP_SELECTED){
			updateEquipPos(dx, dy);
		}
	}
	//prevent paint focucs for frame
	protected void drawSelection(GC gc) {
	}
	
	protected void drawSelection(GLGraphics gc) {
	}

	@Override
	protected void onMouseMove(MouseEvent e) {
		if(! isDragging && !isDragginAnchor){
			return;
		}
		int dx = (int) ((e.x - dragStartPoint.x)/ratio);
		int dy = (int) ((e.y - dragStartPoint.y)/ratio);
		if(dx == 0 && dy == 0){
			return;
		}
		dragStartPoint = new Point(e.x, e.y);
		PipAni4AniFramePiece hook = getFrameData().realize().getHook(bindHookId);
		if(hook == null){
			hookSelected = NULL_SELECTED;
			isDragging = false;
			isDragginAnchor = false;
			return;
		}
		if(isDragging){
			updateEquipPos(dx, dy);
		}else if(isDragginAnchor){
			updateHookPos(dx, dy);
		}
	}

	private void updateHookPos(int dx, int dy) {
		PipAni4AniFramePiece hook = getFrameData().realize().getHook(bindHookId);
		hook.setRealDx(hook.getRealDx()+dx);
		hook.setRealDy(hook.getRealDy()+dy);
		fireHookPosChanged();
		redraw();
	}

	private void updateEquipPos(int dx, int dy) {
		PipAni4AniFramePiece hook = getFrameData().realize().getHook(bindHookId);
		PipAnimate equipAni = hook.getBindAnimate();
		int cnt = equipAni.getFrameCount();
		for(int i=0; i<cnt; i++){
			PipAnimateFrameRef frameRef = equipAni.getFrame(i);
			frameRef.setDx(frameRef.getDx()+dx);
			frameRef.setDy(frameRef.getDy()+dy);
		}
		fireEquipCtsChanged();
		redraw();		
	}

	private void fireHookPosChanged() {
		hookPosChanged = true;
		if(this.equipAniSaver!=null){
			this.equipAniSaver.hookPosChanged();
		}		
	}

	private void fireEquipCtsChanged() {
		equipCtsModified = true;
		if(this.equipAniSaver!=null){
			this.equipAniSaver.equipCtsModifed();
		}
	}

	@Override
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
	}
	
	@Override
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
	}
	
	public interface EquipAnimateSaver{
		public boolean saveEquipAnimate(PipAnimate equipAni);
		public void hookPosChanged();
		public void equipCtsModifed();
	}
	public void setEquipAniSaver(EquipAnimateSaver equipAniSaver) {
		this.equipAniSaver = equipAniSaver;
	}

	public boolean isEquipCtsModified() {
		return equipCtsModified;
	}

	public void setEquipCtsModified(boolean equipCtsModified) {
		this.equipCtsModified = equipCtsModified;
	}

	public boolean isHookPosChanged() {
		return hookPosChanged;
	}

	public void setHookPosChanged(boolean hookPosChanged) {
		this.hookPosChanged = hookPosChanged;
	}

	public int getBindHookId() {
		return bindHookId;
	}

	public void setBindHookId(int bindHookId) {
		this.bindHookId = bindHookId;
	}
}
