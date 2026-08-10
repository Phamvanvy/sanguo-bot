package com.pip.mapeditor.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import org.eclipse.swt.graphics.Point;

/**
 * NPC碰撞区域信息。
 * @author lighthu
 */
public class NPCImageInfo {
	/**
	 * 引用的animate的hashCode值
	 */
	public int animateRef;
    public int[] cx = new int[0];
    public int[] cy = new int[0];
    public int[] cw = new int[0];
    public int[] ch = new int[0];
    
    public void enlarge() {
        for (int i = 0; i < cx.length; i++) {
            cx[i] *= 2;
        }
        for (int i = 0; i < cy.length; i++) {
            cy[i] *= 2;
        }
        for (int i = 0; i < cw.length; i++) {
            cw[i] *= 2;
        }
        for (int i = 0; i < ch.length; i++) {
            ch[i] *= 2;
        }
    }
    
    public void smaller() {
        for (int i = 0; i < cx.length; i++) {
            cx[i] /= 2;
        }
        for (int i = 0; i < cy.length; i++) {
            cy[i] /= 2;
        }
        for (int i = 0; i < cw.length; i++) {
            cw[i] /= 2;
        }
        for (int i = 0; i < ch.length; i++) {
            ch[i] /= 2;
        }
    }
}
