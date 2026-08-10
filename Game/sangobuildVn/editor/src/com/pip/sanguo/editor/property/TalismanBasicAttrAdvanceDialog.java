package com.pip.sanguo.editor.property;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.TalismanBasicAttrAdvance;

public class TalismanBasicAttrAdvanceDialog extends Dialog {

    private Text textExp;
    private Text textDuration;
    private Text textAntiCrit;
    private Text textSpeed;
    private Text textMpResume;
    private Text textHpResume;
    private Text textMagicArmor;
    private Text textArmor;
    private Text textMagicAtk;
    private Text textAtk;
    private Text textMagicDodge;
    private Text textDodge;
    private Text textHit;
    private Text textCrit;
    private Text textMp;
    private Text textHp;
    private Text textWis;
    private Text textSta;
    private Text textAgi;
    private Text textStr;
    private Text textLevel;
    private TalismanBasicAttrAdvance attrAdvance;
    /**
     * Create the dialog
     * @param parentShell
     */
    public TalismanBasicAttrAdvanceDialog(Shell parentShell, TalismanBasicAttrAdvance attr) {
        super(parentShell);
        attrAdvance = attr;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("等级：");

        textLevel = new Text(container, SWT.BORDER);
        textLevel.setText("1");
        final GridData gd_textLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLevel.setLayoutData(gd_textLevel);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("力量：");

        textStr = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textStr.setText("0");
        } else {
            textStr.setText(String.valueOf(attrAdvance.str));
        }
        final GridData gd_textStr = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textStr.setLayoutData(gd_textStr);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("敏捷：");

        textAgi = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textAgi.setText("0");
        } else {
            textAgi.setText(String.valueOf(attrAdvance.agi));
        }
        final GridData gd_textAgi = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAgi.setLayoutData(gd_textAgi);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("体力：");

        textSta = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textSta.setText("0");
        } else {
            textSta.setText(String.valueOf(attrAdvance.sta));
        }
        final GridData gd_textSta = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSta.setLayoutData(gd_textSta);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setLayoutData(new GridData());
        label_5.setText("智力：");

        textWis = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textWis.setText("0");
        } else {
            textWis.setText(String.valueOf(attrAdvance.wis));
        }
        final GridData gd_textWis = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textWis.setLayoutData(gd_textWis);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("生命：");

        textHp = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textHp.setText("0");
        } else {
            textHp.setText(String.valueOf(attrAdvance.hp));
        }
        final GridData gd_textHp = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHp.setLayoutData(gd_textHp);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("精力：");

        textMp = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textMp.setText("0");
        } else {
            textMp.setText(String.valueOf(attrAdvance.mp));
        }
        final GridData gd_textMp = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMp.setLayoutData(gd_textMp);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("暴击：");

        textCrit = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textCrit.setText("0");
        } else {
            textCrit.setText(String.valueOf(attrAdvance.crit));
        }
        final GridData gd_textCrit = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textCrit.setLayoutData(gd_textCrit);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setText("命中：");

        textHit = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textHit.setText("0");
        } else {
            textHit.setText(String.valueOf(attrAdvance.hit));
        }
        final GridData gd_textHit = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHit.setLayoutData(gd_textHit);

        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setText("物闪：");

        textDodge = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textDodge.setText("0");
        } else {
            textDodge.setText(String.valueOf(attrAdvance.dodge));
        }
        final GridData gd_textDodge = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textDodge.setLayoutData(gd_textDodge);

        final Label label_10 = new Label(container, SWT.NONE);
        label_10.setText("法闪：");

        textMagicDodge = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textMagicDodge.setText("0");
        } else {
            textMagicDodge.setText(String.valueOf(attrAdvance.magicDodge));
        }
        final GridData gd_textMagicDodge = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicDodge.setLayoutData(gd_textMagicDodge);

        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setText("物攻：");

        textAtk = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textAtk.setText("0");
        } else {
            textAtk.setText(String.valueOf(attrAdvance.atk));
        }
        final GridData gd_textAtk = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAtk.setLayoutData(gd_textAtk);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setText("法攻：");

        textMagicAtk = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textMagicAtk.setText("0");
        } else {
            textMagicAtk.setText(String.valueOf(attrAdvance.magicAtk));
        }
        final GridData gd_textMagicAtk = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicAtk.setLayoutData(gd_textMagicAtk);

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("护甲：");

        textArmor = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textArmor.setText("0");
        } else {
            textArmor.setText(String.valueOf(attrAdvance.armor));
        }
        final GridData gd_textArmor = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArmor.setLayoutData(gd_textArmor);

        final Label label_14 = new Label(container, SWT.NONE);
        label_14.setText("法防：");

        textMagicArmor = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textMagicArmor.setText("0");
        } else {
            textMagicArmor.setText(String.valueOf(attrAdvance.magicArmor));
        }
        final GridData gd_textMagicArmor = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicArmor.setLayoutData(gd_textMagicArmor);

        final Label label_15 = new Label(container, SWT.NONE);
        label_15.setText("回血：");

        textHpResume = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textHpResume.setText("0");
        } else {
            textHpResume.setText(String.valueOf(attrAdvance.hpResume));
        }
        final GridData gd_textHpResume = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHpResume.setLayoutData(gd_textHpResume);

        final Label label_16 = new Label(container, SWT.NONE);
        label_16.setText("回气：");

        textMpResume = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textMpResume.setText("0");
        } else {
            textMpResume.setText(String.valueOf(attrAdvance.mpResume));
        }
        final GridData gd_textMpResume = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMpResume.setLayoutData(gd_textMpResume);

        final Label label_17 = new Label(container, SWT.NONE);
        label_17.setText("速度：");

        textSpeed = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textSpeed.setText("0");
        } else {
            textSpeed.setText(String.valueOf(attrAdvance.speed));
        }
        final GridData gd_textSpeed = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSpeed.setLayoutData(gd_textSpeed);

        final Label label_18 = new Label(container, SWT.NONE);
        label_18.setText("免暴：");

        textAntiCrit = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textAntiCrit.setText("0");
        } else {
            textAntiCrit.setText(String.valueOf(attrAdvance.antiCrit));
        }
        final GridData gd_textAntiCrit = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAntiCrit.setLayoutData(gd_textAntiCrit);

        final Label label_19 = new Label(container, SWT.NONE);
        label_19.setText("耐久：");

        textDuration = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textDuration.setText("0");
        } else {
            textDuration.setText(String.valueOf(attrAdvance.duration));
        }
        final GridData gd_textDuration = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textDuration.setLayoutData(gd_textDuration);

        final Label label_20 = new Label(container, SWT.NONE);
        label_20.setText("经验：");

        textExp = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textExp.setText("0");
        } else {
            textExp.setText(String.valueOf(attrAdvance.exp));
        }
        final GridData gd_textExp = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textExp.setLayoutData(gd_textExp);
        
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("新等级");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            try {
                attrAdvance.level = Integer.parseInt(textLevel.getText());
            } catch (Exception e) {
                attrAdvance.level = 1;
            }
            try {
                attrAdvance.str = Integer.parseInt(textStr.getText());
            } catch (Exception e) {
                attrAdvance.str = 0;
            }
            try {
                attrAdvance.agi = Integer.parseInt(textAgi.getText());
            } catch (Exception e) {
                attrAdvance.agi = 0;
            }
            try {
                attrAdvance.sta = Integer.parseInt(textSta.getText());
            } catch (Exception e) {
                attrAdvance.sta = 0;
            }
            try {
                attrAdvance.wis = Integer.parseInt(textWis.getText());
            } catch (Exception e) {
                attrAdvance.wis = 0;
            }
            try {
                attrAdvance.hp = Integer.parseInt(textHp.getText());
            } catch (Exception e) {
                attrAdvance.hp = 0;
            }
            try {
                attrAdvance.mp = Integer.parseInt(textMp.getText());
            } catch (Exception e) {
                attrAdvance.mp = 0;
            }
            try {
                attrAdvance.crit = Integer.parseInt(textCrit.getText());
            } catch (Exception e) {
                attrAdvance.crit = 0;
            }
            try {
                attrAdvance.hit = Integer.parseInt(textHit.getText());
            } catch (Exception e) {
                attrAdvance.hit = 0;
            }
            try {
                attrAdvance.dodge = Integer.parseInt(textDodge.getText());
            } catch (Exception e) {
                attrAdvance.dodge = 0;
            }
            try {
                attrAdvance.magicDodge = Integer.parseInt(textMagicDodge.getText());
            } catch (Exception e) {
                attrAdvance.magicDodge = 0;
            }
            try {
                attrAdvance.atk = Integer.parseInt(textAtk.getText());
            } catch (Exception e) {
                attrAdvance.atk = 0;
            }
            try {
                attrAdvance.magicAtk = Integer.parseInt(textMagicAtk.getText());
            } catch (Exception e) {
                attrAdvance.magicAtk = 0;
            }
            try {
                attrAdvance.armor = Integer.parseInt(textArmor.getText());
            } catch (Exception e) {
                attrAdvance.armor = 0;
            }
            try {
                attrAdvance.magicArmor = Integer.parseInt(textMagicArmor.getText());
            } catch (Exception e) {
                attrAdvance.magicArmor = 0;
            }
            try {
                attrAdvance.hpResume = Integer.parseInt(textHpResume.getText());
            } catch (Exception e) {
                attrAdvance.hpResume = 0;
            }
            try {
                attrAdvance.mpResume = Integer.parseInt(textMpResume.getText());
            } catch (Exception e) {
                attrAdvance.mpResume = 0;
            }
            try {
                attrAdvance.speed = Integer.parseInt(textSpeed.getText());
            } catch (Exception e) {
                attrAdvance.speed = 0;
            }
            try {
                attrAdvance.antiCrit = Integer.parseInt(textAntiCrit.getText());
            } catch (Exception e) {
                attrAdvance.antiCrit = 0;
            }
            try {
                attrAdvance.duration = Integer.parseInt(textDuration.getText());
            } catch (Exception e) {
                attrAdvance.duration = 0;
            }
            try {
                attrAdvance.exp = Integer.parseInt(textExp.getText());
            } catch (Exception e) {
                attrAdvance.exp = 0;
            }
        }
        super.buttonPressed(buttonId);
    }

}
