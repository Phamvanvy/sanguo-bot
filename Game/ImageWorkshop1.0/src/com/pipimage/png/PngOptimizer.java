package com.pipimage.png;

import java.io.*;
import java.util.*;

/**
 * This class optimizes PNG files.
 */
public class PngOptimizer {
    private InputStream input = null;
    private OutputStream output = null;
    private boolean optimizeTransparency = true;

    /**
     * Create a PngOptmizer to read PNG file from a stream and save optimized
     * PNG into another stream.
     */
    public PngOptimizer(InputStream is, OutputStream os, boolean optimizeTrans) {
         input = is;
         output = os;
         optimizeTransparency = optimizeTrans;
    }

    /**
     * Perform the optmizing process.
     */
    public void process() throws IOException {
        PngFile pf = new PngFile();
        pf.readPng(new DataInputStream(input));
        if (optimizeTransparency && pf.transparency != null) {
            optimizeTransparency(pf);
        }
        pf.writePng(new DataOutputStream(output), true);
    }

    // Optmize transparency data of a PNG file. In PNG specification, all 0xFF
    // at the end of tRNS trunk can be truncated.
    public static void optimizeTransparency(PngFile pf) {
        int[] map = swapTransparency(pf.transparency);
        swapPalette(pf.palette, map);
        for (int i = 0; i < pf.scanlines.size(); i++) {
            byte[] line = (byte[])pf.scanlines.get(i);
            swapData(line, 0, pf.width, map);
        }
        int last = pf.transparency.length - 1;
        while (last >= 0 && pf.transparency[last] == (byte)0xFF) {
            last--;
        }
        if (last >= 0) {
            byte[] newData = new byte[last + 1];
            System.arraycopy(pf.transparency, 0, newData, 0, last + 1);
            pf.transparency = newData;
        } else {
            pf.transparency = null;
        }
    }

    // Swap the palette data according to the optimization of transparency data.
    private static void swapPalette(int[] palette, int[] map) {
        int[] paletteNew = new int[palette.length];
        for (int i = 0; i < palette.length; i++) {
            if (i >= map.length) {
                paletteNew[i] = palette[i];
            } else {
                paletteNew[i] = palette[map[i]];
            }
        }
        for (int i = 0; i < palette.length; i++) {
            palette[i] = paletteNew[i];
        }
    }

    // Swap the scanline data according to the optimization of transparency data.
    private static void swapData(byte[] data, int start, int len, int[] map) {
        for (int i = start; i < start + len; i++) {
            int index = data[i] & 0xFF;
            if (index >= map.length) {
                continue;
            }
            if (map[index] == index) {
                continue;
            }
            data[i] = (byte)map[index];
        }
    }

    // Swap the transparency data to optmize it(move all none 0xFF data to front).
    private static int[] swapTransparency(byte[] transData) {
        int[] retMap = new int[transData.length];
        for (int i = 0; i < retMap.length; i++) {
            retMap[i] = i;
        }
        for (int i = 0, j = transData.length - 1; i < j; i++, j--) {
            if (transData[i] == (byte)0) {
                j++;
                continue;
            }
            if (transData[j] == (byte)0xFF) {
                i--;
                continue;
            }
            byte bTemp = transData[i];
            transData[i] = transData[j];
            transData[j] = bTemp;
            retMap[i] = j;
            retMap[j] = i;
        }
        return retMap;
    }

    public static void main(String[] args) {
        try {
            // Parse command line.
            boolean optimizeTrans = true;
            boolean makeBackup = true;
            ArrayList inputFiles = new ArrayList();
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if ("-dontoptimizetrns".equals(args[i])) {
                        optimizeTrans = false;
                    } else if ("-dontbackup".equals(args[i])) {
                        makeBackup = false;
                    }
                } else {
                    inputFiles.add(args[i]);
                }
            }
            if (inputFiles.size() == 0) {
                System.out.println("Usage: po [-dontoptimizetrns] [-dontbackup] pngfile1 pngfile2 ...");
            }

            // Optimize PNG files.
            for (int i = 0; i < inputFiles.size(); i++) {
                // Optimize PNG file.
                String pngFile = (String)inputFiles.get(i);
                FileInputStream fis = new FileInputStream(pngFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PngOptimizer po = new PngOptimizer(fis, bos, optimizeTrans);
                po.process();
                fis.close();

                // Make backup file.
                if (makeBackup) {
                    backup(pngFile);
                }

                // Save optmized image.
                FileOutputStream fos = new FileOutputStream(pngFile);
                fos.write(bos.toByteArray());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // backup a file.
    private static void backup(String file) {
        try {
            File f = new File(file);
            String n = f.getName();
            int pos = n.lastIndexOf('.');
            String backupFile = null;
            if (pos < 0) {
                backupFile = file + "_backup.png";
            } else {
                String name = n.substring(0, pos);
                String ext = n.substring(pos);
                f = new File(f.getParent(), name + "_backup" + ext);
                backupFile = f.getPath();
            }

            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(backupFile);
            byte[] buf = new byte[256];
            int len;
            while ((len = fis.read(buf)) >= 0) {
                fos.write(buf, 0, len);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            System.out.println("Can't backup file: " + file);
            e.printStackTrace();
        }
    }
}
