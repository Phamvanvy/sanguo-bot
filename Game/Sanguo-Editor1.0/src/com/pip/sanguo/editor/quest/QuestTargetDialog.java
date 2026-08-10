package com.pip.sanguo.editor.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.DirectoryType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.ChooseLocationDialog;
import com.pip.sanguo.editor.property.ChooseNPCDialog;

public class QuestTargetDialog extends Dialog {
    private Text textDescription;
    private Text textHint;
    private Text textPath;
    private QuestTarget target;
    private QuestDesigner questDesigner;
    private QuestInfo questInfo;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public QuestTargetDialog(Shell parentShell, QuestTarget target, QuestInfo qinfo) {
        super(parentShell);
        this.target = target;
        this.questInfo = qinfo;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textDescription.setLayoutData(gd_textDescription);
        
        textDescription.setText(target.description);

        final Button editTextButton = new Button(container, SWT.NONE);
        editTextButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                RichTextDialog dlg = new RichTextDialog(getShell(), questInfo);
                dlg.setText(textDescription.getText());
                if (dlg.open() == Dialog.OK) {
                    textDescription.setText(dlg.getText());
                }
            }
        });
        editTextButton.setText("...");

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("提示：");

        textHint = new Text(container, SWT.BORDER);
        final GridData gd_textHint = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHint.setLayoutData(gd_textHint);
        
        textHint.setText(target.hint);

        final Button editHintButton = new Button(container, SWT.NONE);
        editHintButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                RichTextDialog dlg = new RichTextDialog(getShell(), questInfo);
                dlg.setText(textHint.getText());
                if (dlg.open() == Dialog.OK) {
                    textHint.setText(dlg.getText());
                }
            }
        });
        editHintButton.setText("...");
        
        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("寻路：");

        textPath = new Text(container, SWT.BORDER);
        final GridData gd_textPath = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPath.setLayoutData(gd_textPath);
        
        textPath.setText(target.path);

        final Button editPathButton = new Button(container, SWT.NONE);
        editPathButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                ChooseNPCDialog dlg = new ChooseNPCDialog(getShell());
                if(target.path.length()>0){
                    int startPos = target.path.indexOf("<n>")+3;
                    int endPos = target.path.indexOf(',');
                    String ids = target.path.substring(startPos, endPos);
                    int id = Integer.parseInt(ids);
                    dlg.setSelectedNPC(id);
                }
                if (dlg.open() == ChooseNPCDialog.OK) {
                    int npcid = dlg.getSelectedNPC();
                    GameMapNPC npc = (GameMapNPC)GameMapNPC.findByID(EditorApplication.getProj(), npcid);
                    if (npc.owner.name.contains(":")) {
                        MessageDialog.openError(getShell(), "错误", "场景名称中不能包含':'符号。");
                        return;
                    }
                    String showName = npc.name;
                    if (showName.contains("|")) {
                        showName = npc.name.substring(0, showName.indexOf('|'));
                    }
                    String mapName = npc.owner.name;
                    if (mapName.contains("|")) {
                        mapName = mapName.substring(0, mapName.indexOf('|'));
                    }
                    String str = "<n>" + npc.getGlobalID() + "," + showName + "(" + mapName + ":" +
                        (npc.x / 8) + "," + (npc.y / 8) + ")</n>";
                    String currText = textPath.getText();
                    if(currText.equals("")){
                        currText = str;
                    } else {
                        currText = currText + ";" + str;
                    }
                    textPath.setText(currText);
                }
            }
        });
        editPathButton.setText("NPC");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Button editPathButton2 = new Button(container, SWT.NONE);
        editPathButton2.setLayoutData(new GridData());
        editPathButton2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
             // 选择一个位置
                ChooseLocationDialog dlg = new ChooseLocationDialog(getShell());
                if (dlg.open() == Dialog.OK && dlg.getLocation()[0] != -1) {
                    int[] location = dlg.getLocation();
                    GameMapInfo mi = GameMapInfo.findByID(EditorApplication.getProj(), location[0]);
                    if (mi.name.contains(":")) {
                        MessageDialog.openError(getShell(), "错误", "场景名称中不能包含':'符号。");
                        return;
                    }
                    String showName = mi.name;
                    int pos1 = showName.indexOf('(');
                    int pos2 = showName.indexOf('|');
                    int splitPos = -1;
                    if (pos1 == -1) {
                        splitPos = pos2;
                    } else {
                        if (pos2 == -1) {
                            splitPos = pos1;
                        } else {
                            splitPos = Math.min(pos1, pos2);
                        }
                    }
                    if (splitPos != -1) {
                        showName = mi.name.substring(0, splitPos);
                    }
                    String str = "<l>" + mi.getGlobalID() + "," + showName + ":" + (location[1] / 8) + "," + 
                        (location[2] / 8) + "</l>";
                    String currText = textPath.getText();
                    if(currText.equals("")){
                        currText = str;
                    } else {
                        currText = currText + ";" + str;
                    }
                    textPath.setText(currText);
                }
            }
        });
        editPathButton2.setText("地点");

        questDesigner = new QuestDesigner(container, SWT.NONE, questInfo);
        questDesigner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        questDesigner.setup(1, target.condition);
        
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(990, 610);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("任务目标");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	target.condition = questDesigner.saveCondition();
        	target.description = textDescription.getText();
        	target.hint = textHint.getText();
        	target.path = textPath.getText();
        }
        super.buttonPressed(buttonId);
    }
    /*
    public Object[] getChildren(DataObjectCategory parentElement) {
            List<Card> retList = new ArrayList<Card>();
            for (DataObject dobj : ((DataObjectCategory)parentElement).objects) {
                retList.add((Card)dobj);
            }
            return retList.toArray();
    }
    
    public static List<String> currPathList = new ArrayList<String>();
    public static void getPathList(String desc){
        currPathList.clear();
        String copy = desc;
        while(true){
            int startpos = copy.indexOf("<n>");
            int endpos = copy.indexOf("</n>");
            if(endpos < 0 || startpos < 0){
                break;
            }
            if(endpos < startpos){
//                System.out.println(desc);
                copy = copy.substring(endpos + 4);
            } else {
                if(endpos + 4 <= copy.length()){
                    String npc = copy.substring(startpos, endpos + 4);
                    currPathList.add(npc);
                    copy = copy.substring(endpos + 4);
                } else {
                    break;
                }
            }
            
        }
        
        while(true){
            int startpos = copy.indexOf("<l>");
            int endpos = copy.indexOf("</l>");
            if(endpos < 0 || startpos < 0){
                break;
            }
            if(endpos < startpos){
//                System.out.println(desc);
                copy = copy.substring(endpos + 4);
            } else {
                if(endpos + 4 <= copy.length()){
                    String npc = copy.substring(startpos, endpos + 4);
                    currPathList.add(npc);
                    System.out.println(desc);
                    copy = copy.substring(endpos + 4);
                } else {
                    break;
                }
            }
            
        }
    }
    
    public static boolean isHanzi(char ch){
       return java.lang.Character.toString(ch).matches("[\\u4E00-\\u9FA5]+");  
    }
    
    public static boolean hasHanzi(String str){
        boolean ret = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(isHanzi(ch)){
                ret = true;
                break;
            }
        }
        return ret;
    }
    
    public static String getGreatestCommonSubStr(String str1,String str2){
        String ret = "";
//        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= str1.length(); i++) {
            int startPos = 0;
            while(startPos + i <= str1.length()){
                String substr = str1.substring(startPos, startPos + i);
                startPos++;
                if(str2.indexOf(substr) != -1){
                    if(substr.length() > 1 && hasHanzi(substr)){
                        if(substr.length() > ret.length()){
                            ret = substr;
                        }
                    }
                }
            }
            
        }
        
        return ret;
    }
    
    public static String matchPath(List<String> pathes,String targetDesc){
        String ret = "";
        int greatestIndex = -1;
        String maxStr = "";
        for (int i=0;i<pathes.size();i++) {
            String comstr = getGreatestCommonSubStr(pathes.get(i), targetDesc);
            if(comstr.length() > 1){
                if(comstr.length() > maxStr.length()){
                    maxStr = comstr;
                    greatestIndex = i;
                }
            }
        }
        if(greatestIndex != -1){
            ret = pathes.get(greatestIndex);
        }
        return ret;
    }
    
    public static void main(String[] args) {
        ProjectData pd = new ProjectData();
        try {
            pd.load(new File("D:\\workspace\\Sanguo1.0-Data\\data"));
            List<DataObjectCategory> cateList = pd.getCategoryListByType(Quest.class);
            for (DataObjectCategory cate : cateList) {
                for (DataObject dobj : ((DataObjectCategory)cate).objects) {
                    Quest quest = (Quest)dobj;
                    getPathList(quest.unfinishDescription);
                    List<QuestTarget> targets = quest.targets;
                    for (QuestTarget questTarget : targets) {
                        String path = matchPath(currPathList, questTarget.description);
                        questTarget.path = path;
                    }
                }
            }
            
            pd.saveDataList(Quest.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//        String a = "鹫鸟肉<i>${GetItemCount(3)}</i>/<i>3</i>";  
//        String b = "<n>327692,鹫鸟(峨眉后山(蜀):30,41)</n>";  
//        String ret = getGreatestCommonSubStr(a, b);
//        System.out.println(ret);
        
        
        
//        String isHanzi = "鹫鸟肉<i>";
//        for (int i = 0; i < isHanzi.length(); i++) {
//            char ch = isHanzi.charAt(i);
//            System.out.println(isHanzi(ch));
//        }
        
//        String desc = "和<n>5906437,王大夫(许田镇(魏):41,23)</n>交谈，他会给你一件护身装备。";
//        getPathList(desc);
        
    }
*/
}
