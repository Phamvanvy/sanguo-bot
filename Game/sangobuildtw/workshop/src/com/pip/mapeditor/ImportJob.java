package com.pip.mapeditor;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.widgets.Shell;

import com.pip.mapeditor.data.*;
import com.pipimage.image.PipImage;
import com.pipimage.utils.ImageUtil;

/**
 * 导入图片到某一地图层的线程。
 * @author lighthu
 *
 */
public class ImportJob implements IRunnableWithProgress {
    private MapEditor editor;
    private Object[][] cells;
    private AccurateMapLayer target;
    private int tolerance;
    private boolean ignoreUnmatched;
    
    public ImportJob(MapEditor editor, Object[][] cells, AccurateMapLayer target, int t, boolean ig) {
        this.editor = editor;
        this.cells = cells;
        this.target = target;
        this.tolerance = t;
        this.ignoreUnmatched = ig;
    }
    
    public void run(IProgressMonitor monitor) {
        short[][] oldData = target.getLayerData();
        short[][] tdata = new short[oldData.length][oldData[0].length];
        if (monitor != null) {
            monitor.beginTask("开始导入...", tdata.length * tdata[0].length + 1);
        }
        
        // 把现有贴图所有的翻转可能性都枚举出来
        ArrayList<int[][]> existFrames = new ArrayList<int[][]>();
        PipImage tileImage = target.parent.parent.getTileImage().image;
        for (int i = 0; i < tileImage.getImgCount(); i++) {
            for (int j = 0; j < 4; j++) {
                existFrames.add(tileImage.getImageDraw(i).getPixels(j));
            }
        }
        if (monitor != null) {
            monitor.worked(1);
        }
        
        // 查找目标图片每个图块，和已有贴图进行比较，找出符合匹配条件的最佳Tile
        int finishedCount = 1;
        int matchCount = 0, addCount = 0, errorCount = 0, ignoreCount = 0;
        for (int i = 0; i < tdata.length; i++) {
            for (int j = 0; j < tdata[0].length; j++) {
                if (monitor != null && monitor.isCanceled()) {
                    return;
                }
                int[][] idata = (int[][])cells[i][j];
                if (isZero(idata)) {
                    // 透明为-1
                    tdata[i][j] = -1;
                    matchCount++;
                } else {
                    // 查找最佳匹配
                    int minFrame = -1;
                    double minError = 101.0;
                    for (int k = existFrames.size() - 1; k >= 0; k--) {
                        double errorRate = ImageUtil.compareData(existFrames.get(k), idata);
                        if (errorRate < minError) {
                            minFrame = k;
                            minError = errorRate;
                            if (minError == 0.0) {
                                break;
                            }
                        }
                    }
                    
                    // 检查最佳匹配率是否符合条件
                    if (minError <= tolerance) {
                        // 如果可以接受，则使用最佳匹配的贴图 
                        int frame = minFrame / 4;
                        int trans = minFrame % 4;
                        tdata[i][j] = (short)target.parent.parent.findOrAddTile(frame, trans);
                        matchCount++;
                    } else if (!ignoreUnmatched) {
                        // 如果不可接受，根据设定的条件添加贴图图片或者保持空白
                        try {
                            tileImage.addFrame(idata);
                            int frame = tileImage.getImgCount() - 1;
                            int trans = 0;
                            tdata[i][j] = (short)target.parent.parent.findOrAddTile(frame, trans);
                            addCount++;
                            matchCount++;
                            for (int tt = 0; tt < 4; tt++) {
                                existFrames.add(tileImage.getImageDraw(frame).getPixels(tt));
                            }
                        } catch (Exception e) {
                            tdata[i][j] = -1;
                            errorCount++;
                        }
                    } else {
                        tdata[i][j] = -1;
                        ignoreCount++;
                    }
                }
                finishedCount++;
                if (monitor != null) {
                    monitor.worked(finishedCount);
                }
            }
        }
        
        // 拷贝导入数据到地图层，并显示导入报告
        for (int i = 0; i < tdata.length; i++) {
            for (int j = 0; j < tdata[i].length; j++) {
                oldData[i][j] = tdata[i][j];
            }
        }
        if (monitor != null) {
            monitor.done();
            editor.getSite().getShell().getDisplay().asyncExec(new ImportReportJob(editor, matchCount, addCount, errorCount, ignoreCount));
        }
    }

    // 显示导入报告
    class ImportReportJob implements Runnable {
        private MapEditor editor;
        private int matchCount, addCount, errorCount, ignoreCount;
        
        public ImportReportJob(MapEditor editor, int mc, int ac, int ec, int ic) {
            this.editor = editor;
            matchCount = mc;
            addCount = ac;
            errorCount = ec;
            ignoreCount = ic;
        }
        
        public void run() {
            editor.onImportFinished();
            String msg = "导入完成！匹配：" + matchCount + "，添加：" + addCount + "，错误：" + 
                errorCount + "忽略：" + ignoreCount + "。";
            MessageDialog.openInformation(editor.getSite().getShell(), "完成", msg);
        }
    }
    
    // 判断一个图片是否全透明
    private boolean isZero(int[][] data) {
        for (int i = data.length - 1; i >= 0; i--) {
            for (int j = data[0].length - 1; j >= 0; j--) {
                if (data[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}