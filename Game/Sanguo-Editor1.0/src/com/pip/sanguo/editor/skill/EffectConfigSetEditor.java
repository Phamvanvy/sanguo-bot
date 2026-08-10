package com.pip.sanguo.editor.skill;

import java.io.UnsupportedEncodingException;
import java.util.*;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.skill.*;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.property.ItemCellEditor;
import com.pip.sanguo.editor.property.LocationCellEditor;
import com.pip.sanguo.editor.property.SkillSetCellEditor;
import com.pip.sanguo.editor.util.FloatCellEditorValidator;
import com.pip.sanguo.editor.util.IntegerCellEditorValidator;

/**
 * 效果集合编辑器。
 * @author lighthu
 */
public class EffectConfigSetEditor extends Composite {
    /*
     * 参数表编辑控制。
     */
    class ParamListCellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            int index = Integer.parseInt(property.substring(1));
            return index > 0;
        }
        
        /**
         * 取得某个格子的编辑目标对象。
         */
        public Object getValue(Object element, String property) {
            int index = Integer.parseInt(property.substring(1));
            int level = ((Integer)element).intValue();
            if (index == 0) {
                return null;
            }
            try {
                // 目前除location编辑器外，所有编辑器都接收String类型的数据
                EffectParamRef paramRef = editObject.getParamAt(index - 1);
                if (paramRef.getParamClass() == int[].class) {
                    return paramRef.getParamValue(level);
                } else {
                    return String.valueOf(paramRef.getParamValue(level));
                }
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 修改参数。
         */
        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            if (ti.getData() instanceof Integer) {
                int level = ((Integer)ti.getData()).intValue();
                int index = Integer.parseInt(property.substring(1));
                if (index == 0) {
                    return;
                }
                try {
                    EffectParamRef paramRef = editObject.getParamAt(index - 1);
                    boolean modified = false;
                    Class cls = paramRef.getParamClass();
                    // 目前所有编辑器都返回String类型的数据，这里需要根据类型转换
                    // 编辑整数或浮点数时，如果输入值以字符a开头，则特殊处理。如果在第一行输入，
                    // 则设置整列为新值，如果在第二行后输入，则后面的行等比递增。
                    if (cls == Integer.class) {
                        String s = (String)value;
                        if (s.startsWith("a")) {
                            modified = paramRef.autoSetParamValues(level, new Integer(s.substring(1)));
                        } else {
                            modified = paramRef.setParamValue(level, new Integer((String)value));
                        }
                    } else if (cls == Float.class) {
                        String s = (String)value;
                        if (s.startsWith("a")) {
                            modified = paramRef.autoSetParamValues(level, new Float(s.substring(1)));
                        } else {
                            modified = paramRef.setParamValue(level, new Float((String)value));
                        }
                    } else if (cls == String.class) {
                        modified = paramRef.autoSetParamValues(level, value);
                    } else if (cls == BuffConfig.class) {
                        modified = paramRef.autoSetParamValues(level, new Integer((String)value));
                    } else if (cls == SkillConfig[].class) {
                        modified = paramRef.autoSetParamValues(level, value);
                    } else if (cls == ParamIndicator.class) {
                        modified = paramRef.autoSetParamValues(level, value);
                    } else if (cls == int[].class) {
                        modified = paramRef.autoSetParamValues(level, value);
                    } else {
                        modified = paramRef.setParamValue(level, value);
                    }
                    if (modified) {
                        fireModified();
                        paramListViewer.refresh();
//                        paramListViewer.update(ti.getData(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 参数表文本。
     */
    class ParamListLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            int level = ((Integer)element).intValue();
            if (columnIndex == 0) {
                return level + "级";
            } else {
                try {
                    EffectParamRef paramRef = editObject.getParamAt(columnIndex - 1);
                    Class cls = paramRef.getParamClass();
                    if (cls == BuffConfig.class) {
                        int id = ((Integer)paramRef.getParamValue(level)).intValue();
                        BuffConfig bc = (BuffConfig)EditorApplication.getProj().findObject(BuffConfig.class, id);
                        if (bc != null) {
                            return bc.toString();
                        } else {
                            return "无效BUFF";
                        }
                    } else if (cls == SkillConfig[].class) {
                        String idList = (String)paramRef.getParamValue(level);
                        return SkillConfig.toString(EditorApplication.getProj(), idList);
                    } else if (cls == ParamIndicator.class) {
                        return ((ParamIndicator)paramRef.getParamValue(level)).toString(EditorApplication.getProj());
                    } else if (cls == int[].class) {
                        int[] loc = (int[])paramRef.getParamValue(level);
                        return GameMapInfo.locationToString(EditorApplication.getProj(), loc, false);
                    } else {
                        return String.valueOf(paramRef.getParamValue(level));
                    }
                } catch (Exception e) {
                    return "";
                }
            }
        }
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                try {
                    return EditorPlugin.getDefault().getImageRegistry().get("empty");
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }
    
    /*
     * 参数表内容，每行的对象是值为 i + 1的Integer。
     */
    class ParamListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = new Object[editObject.getLevelCount()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new Integer(i + 1);
            }
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    /*
     * 类型表内容。每行是值为i的Integer。
     */
    class TypeListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = new Object[allowedEffects.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new Integer(i);
            }
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    /*
     * 类型表文本
     */
    class TypeListLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            int index = ((Integer)element).intValue();
            int type = allowedEffects[index];
            return EffectConfig.TYPE_NAMES[type][0];
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    private CheckboxTableViewer typeListViewer;
    private TableViewer paramListViewer;
    private Table paramList;
    private Table typeList;
    private EffectConfigSet editObject = new EffectConfigSet();
    private boolean updating = false;
    private ModifyListener listener = null;
    private int[] allowedEffects = null;
    
    private LinkedHashMap<Integer, String> weight=new LinkedHashMap<Integer, String>();

    /**
     * Create the composite
     * @param parent
     * @param style
     */
    public EffectConfigSetEditor(Composite parent, int style) {
        super(parent, style);
        
        allowedEffects = new int[EffectConfig.TYPE_NAMES.length];
        for (int i = 0; i < allowedEffects.length; i++) {
            allowedEffects[i] = i;
        }
        
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        setLayout(gridLayout);

        typeListViewer = CheckboxTableViewer.newCheckList(this, SWT.FULL_SELECTION | SWT.BORDER);
        typeListViewer.setContentProvider(new TypeListContentProvider());
        typeListViewer.setLabelProvider(new TypeListLabelProvider());
        typeList = typeListViewer.getTable();
        typeList.setLinesVisible(true);
        typeList.setHeaderVisible(true);
        final GridData gd_typeList = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_typeList.widthHint = 234;
        typeList.setLayoutData(gd_typeList);
        typeListViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                int index = ((Integer)event.getElement()).intValue();
                int type = allowedEffects[index];
                boolean selected = event.getChecked();
                if (selected) {
                    addEffect(type);
                    addWeight(type);
                } else {
                    removeEffect(type);
                    removeWeight(type);
                }
                fireModified();
            }
        });
        typeListViewer.setInput(this);

        final TableColumn effectTypeColumn = new TableColumn(typeList, SWT.NONE);
        effectTypeColumn.setWidth(187);
        effectTypeColumn.setText("效果");

        paramListViewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.BORDER);
        paramListViewer.setLabelProvider(new ParamListLabelProvider());
        paramListViewer.setContentProvider(new ParamListContentProvider());
        paramList = paramListViewer.getTable();
        paramList.setLinesVisible(true);
        paramList.setHeaderVisible(true);
        final GridData gd_paramList = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_paramList.widthHint = 648;
        paramList.setLayoutData(gd_paramList);

        final TableColumn levelColumn = new TableColumn(paramList, SWT.NONE);
        levelColumn.setWidth(57);
        levelColumn.setText("级别");

        editObject.setLevelCount(10);
        refresh();
    }
    
    public void setAllowedEffects(int[] arr) {
        allowedEffects = arr;
        typeListViewer.refresh();
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
    
    public void setEditObject(EffectConfigSet newSet) {
         editObject = newSet;
         refresh();
    }
    
    public EffectConfigSet getEditObject() {
        return editObject;
    }
    
    public void refresh() {
        updateTypeList();
        updateParamList();
    }
    
    private void updateTypeList() {
        List<Integer> checkedType = new ArrayList<Integer>();
        for (EffectConfig eff : editObject.getAllEffects()) {
            for (int i = 0; i < allowedEffects.length; i++) {
                if (allowedEffects[i] == eff.getType()) {
                    checkedType.add(i);
                }
            }
        }
        updating = true;
        typeListViewer.setCheckedElements(checkedType.toArray());
        updating = false;
    }
    
    private void updateParamList() {
        // 重新设置参数列
        List<EffectParamRef> allParams = editObject.getAllParams();
        String[] propertyNames = new String[allParams.size() + 1];
        CellEditor[] columnEditors = new CellEditor[allParams.size() + 1];
        propertyNames[0] = "c0";
        columnEditors[0] = new TextCellEditor(paramList);
        for (int i = 0; i < allParams.size(); i++) {
            String pname = allParams.get(i).getParamName();
            if (i + 1 < paramList.getColumnCount()) {
                paramList.getColumn(i + 1).setText(pname);
            } else {
                TableColumn newColumn = new TableColumn(paramList, SWT.NONE);
                try {
                    newColumn.setWidth(pname.getBytes("GBK").length * 7 + 20);
                } catch (UnsupportedEncodingException e) {
                }
                newColumn.setText((i + 1) + ". " + pname);
            }
            propertyNames[i + 1] = "c" + (i + 1);
            Class cls = allParams.get(i).getParamClass();
            if (cls == Integer.class) {
                columnEditors[i + 1] = new TextCellEditor(paramList);
                columnEditors[i + 1].setValidator(new IntegerCellEditorValidator());
            } else if (cls == Float.class) {
                columnEditors[i + 1] = new TextCellEditor(paramList);
                columnEditors[i + 1].setValidator(new FloatCellEditorValidator());
            } else if (cls == String.class) {
                columnEditors[i + 1] = new TextCellEditor(paramList);
            } else if (cls == BuffConfig.class) {
                columnEditors[i + 1] = new BuffConfigCellEditor(paramList, EditorApplication.getProj());
            } else if (cls == SkillConfig[].class) {
                columnEditors[i + 1] = new SkillSetCellEditor(paramList);
            } else if (cls == ParamIndicator.class) {
                columnEditors[i + 1] = new ParamIndicatorCellEditor(paramList);
            } else if (cls == int[].class) {
                columnEditors[i + 1] = new LocationCellEditor(paramList);
            } else {
                columnEditors[i + 1] = new TextCellEditor(paramList);
            }
        }
        while (paramList.getColumnCount() > allParams.size() + 1) {
            paramList.getColumn(paramList.getColumnCount() - 1).dispose();
        }
        paramListViewer.setColumnProperties(propertyNames);
        paramListViewer.setCellModifier(new ParamListCellModifier());
        paramListViewer.setCellEditors(columnEditors);
        
        paramListViewer.setInput(editObject);
        paramListViewer.refresh();
    }
    
    private void addEffect(int type) {
        if (editObject.exists(type)) {
            return;
        }
        try {
            EffectConfig eff = EffectConfig.create(type, editObject.getLevelCount());
            editObject.addEffect(eff);
            updateParamList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public LinkedHashMap<Integer, String> getWeight(){
        return weight;
    }
    public void updateWeight(int weightId,String weight1,String weight2){
        String weightStr=weightId+","+weight1+","+weight2+";";
        weight.remove(weightId);
        weight.put(weightId, weightStr);
    }
    
    private void addWeight(int type){
        if(weight.containsKey(new Integer(type))){
            return;
        }
        weight.put(new Integer(type), type+",0,0;");
    }
    public void setWeight(String weightText){
        String[] weights=weightText.split(";");
        for(int i=0;i<weights.length;i++){
            this.weight.put(Integer.parseInt(weights[i].split(",")[0]), weights[i]+";");
        }
    }
    
    private void removeWeight(int type) {
        weight.remove(new Integer(type));
    }
    
    private void removeEffect(int type) {
        editObject.removeEffect(type);
        updateParamList();
    }
}
