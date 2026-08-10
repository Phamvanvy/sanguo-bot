package com.pip.sanguo.editor.skill;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.pip.sanguo.data.skill.BuffConfig;

/**
 * 选择武器类型的控件。用一个int数组来传递。如果全部武器都选中，返回int[0]。
 * @author lighthu
 */
public class WeaponChooser extends Group {
    private ModifyListener listener = null;
    private Button buttonSword;
    private Button buttonKnife;
    private Button buttonAxe;
    private Button buttonSpear;
    private Button buttonPolearm;
    private Button buttonFan;
    private Button buttonBow;
    
    private EventHandler eh = new EventHandler();
    private class EventHandler extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            fireModified();
        }
    }
    
    /**
     * Create the composite
     * @param parent
     * @param style
     */
    public WeaponChooser(Composite parent, int style) {
        super(parent, style);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 7;
        setLayout(gridLayout);
        setText("适用武器");

        buttonSword = new Button(this, SWT.CHECK);
        buttonSword.addSelectionListener(eh);
        buttonSword.setText("剑");

        buttonKnife = new Button(this, SWT.CHECK);
        buttonKnife.addSelectionListener(eh);
        buttonKnife.setText("刀");

        buttonAxe = new Button(this, SWT.CHECK);
        buttonAxe.addSelectionListener(eh);
        buttonAxe.setText("斧");

        buttonSpear = new Button(this, SWT.CHECK);
        buttonSpear.addSelectionListener(eh);
        buttonSpear.setText("枪");

        buttonPolearm = new Button(this, SWT.CHECK);
        buttonPolearm.addSelectionListener(eh);
        buttonPolearm.setText("长柄");

        buttonFan = new Button(this, SWT.CHECK);
        buttonFan.addSelectionListener(eh);
        buttonFan.setText("扇");

        buttonBow = new Button(this, SWT.CHECK);
        buttonBow.addSelectionListener(eh);
        buttonBow.setText("弓");
    }
    
    @Override
    protected void checkSubclass () {
    }
    
    public void addModifyListener(ModifyListener l) {
        this.listener = l;
    }
    
    private void fireModified() {
        if (listener != null) {
            Event e = new Event();
            e.widget = this;
            ModifyEvent event = new ModifyEvent(e);
            listener.modifyText(event);
        }
    }
    
    public void setWeapons(int[] weapon) {
        if (weapon.length == 0) {
            buttonSword.setSelection(true);
            buttonKnife.setSelection(true);
            buttonAxe.setSelection(true);
            buttonSpear.setSelection(true);
            buttonPolearm.setSelection(true);
            buttonFan.setSelection(true);
            buttonBow.setSelection(true);
        } else {
            buttonSword.setSelection(false);
            buttonKnife.setSelection(false);
            buttonAxe.setSelection(false);
            buttonSpear.setSelection(false);
            buttonPolearm.setSelection(false);
            buttonFan.setSelection(false);
            buttonBow.setSelection(false);
            for (int t : weapon) {
                switch (t) {
                case BuffConfig.MINORTYPE_SWORD:
                    buttonSword.setSelection(true);
                    break;
                case BuffConfig.MINORTYPE_KNIFE:
                    buttonKnife.setSelection(true);
                    break;
                case BuffConfig.MINORTYPE_AXE:
                    buttonAxe.setSelection(true);
                    break;
                case BuffConfig.MINORTYPE_SPEAR:
                    buttonSpear.setSelection(true);
                    break;
                case BuffConfig.MINORTYPE_POLEARM:
                    buttonPolearm.setSelection(true);
                    break;
                case BuffConfig.MINORTYPE_FAN:
                    buttonFan.setSelection(true);
                    break;
                case BuffConfig.MINORTYPE_BOW:
                    buttonBow.setSelection(true);
                    break;
                }
            }
        }
    }
    
    public int[] getWeapons() {
        List<Integer> wps = new ArrayList<Integer>();
        if (buttonSword.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_SWORD);
        }
        if (buttonKnife.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_KNIFE);
        }
        if (buttonAxe.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_AXE);
        }
        if (buttonSpear.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_SPEAR);
        }
        if (buttonPolearm.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_POLEARM);
        }
        if (buttonFan.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_FAN);
        }
        if (buttonBow.getSelection()) {
            wps.add(BuffConfig.MINORTYPE_BOW);
        }
        if (wps.size() == 7) {
            return new int[0];
        } else {
            int[] ret = new int[wps.size()];
            for (int i = 0; i < wps.size(); i++) {
                ret[i] = wps.get(i);
            }
            return ret;
        }
    }
}
