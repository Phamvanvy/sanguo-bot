package com.pip.sanguo.data.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.graphics.Rectangle;

import com.pip.mapeditor.data.AccurateMapLayer;
import com.pip.mapeditor.data.BlurMapLayer;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.IMapLayer;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.MapNPC;
import com.pip.mapeditor.data.MapNPCLayer;
import com.pip.mapeditor.data.NPCImageInfo;
import com.pip.mapeditor.data.TileInfo;

/**
 * 地图寻路工具类。每个GameMapInfo对象创建一个PathFinder对象来实现场景内任意2点之间寻路。<br>
 * 寻路算法的原理如下：<br>
 * 1. 首先为地图中的每个16x16的格点，如果格点中有超过1半面积不可通过，则格点不可通过。<br>
 * 2. 对于每个格点，用A*算法分析它到周围11x11范围内的每个格点是否有路径，把分析结果保存到缓存中。缓存格式
 * 为byte数组，每个格点对应一个字节：如果到起始点有路径，存储它到起始点的距离；如果到起始点没有路径，存储
 * 其左右最近一个到起始点有路径的格点的坐标（在11x11范围内的坐标，-1表示左上角，-121表示右下角）。<br>
 * 3. 计算地图中任意两点的路径时，如果目标点在起始点缓存的11x11的范围内，则直接通过缓存可找出最近路径；如果
 * 不在11x11范围内，则以11x11范围内举例目标点最近的那个点作为临时目标来计算路径。<br>
 * 4. 如果选中的临时目标到起始点之间没有可用路径，那么取此临时目标左右最近的一个有路径的点作为临时目标。<br>
 * 
 * 注：为提高效率，最后缓存中保存的不是到起始点的距离，而是从起始点走到目标点的第一步的目标格点。
 * @author lighthu
 */
public class PathFinder {
    /** 格点大小对应的位数 */
    public static final int GRID_BITS = 4;
    /** 格点大小 */
    public static final int GRID_SIZE = 16;
    /** 缓存范围半径 */
    public static final int BUFFER_RANGE = 5;
    /** 缓存范围的宽高 */
    public static final int BUFFER_MATRIX_SIZE = 11;
    /** 缓存块的大小 */
    public static final int BUFFER_BLOCK_SIZE = 121;
    
    /* 随机数 */
    protected static Random rand = new Random();

    // 地图对象
    protected GameMapInfo map;
    // 实际地图信息
    protected GameMap gameMap;
    // 地图宽度（格点）
    protected int gridWidth;
    // 地图高度（格点）
    protected int gridHeight;
    // 地图的通过性信息，按16x16的格点计算
    protected byte[][] passable;
    // 地图的通过性信息，按4x4的格点计算，每8个格点拼成1个字节
    protected byte[] passable2;
    // 地图中一个最接近地图中心的可通过点的位置（像素）
    protected int godX, godY;
    // 路径缓存。假定地图大小为640x640，那么有40x40个格点，每个格点的缓存数据是11x11字节，总共的缓存最大
    // 为40x40x11x11=193600，大约190K。如果有1000张地图，PathFinder占用的总空间大约200M。
    // 为了节省空间，如果一个格点周围11x11的范围内的所有格点都是可通过的，那么这个格点对应的缓存将不保存。
    // 所以，pathBuffer中的缓存块对应的格点不是连续的，需要从pathBufferIndex来索引。
    // 还有一种特殊情况，如果起始点本身就不可通过，理论上不应该以其作为起始点。为了容错，这种情况我们也不保
    // 存其路径缓存，当作周围全部可以通过处理，这样至少保证如果怪物“偶然”走进了山里，也能走出来。
    protected byte[] pathBuffer;
    // 每个格点周围的路径缓存在pathBuffer中的索引。为了节省空间，这里用了一个short来存储缓存块的索引（
    // 一个缓存块是11x11字节），而不是保存pathBuffer中的直接索引。如果这里都值是-1，表示这个格点没有缓存。
    protected short[] pathBufferIndex;
    // 地图的可通达性信息，按16x16的格点计算。如果一个格子从任何一个出口可以到达，那么这个格式视为可通达。
    protected List<byte[][]> reachable;
    // 增强通达信息，只有当一个格子周围8个格子有>=3个可以到达的情况下，这个格子才被认为可到达
    protected List<byte[][]> reachable2;
    // 缓存地图可通达信息。记录每个4x4的格子是否能从任意出口到达。
    protected byte[][] reachable3;
    
    // 每个格点的替代格点，按角度差的大小排序
    protected static int[][] REPLACEMENT_SEQUENCE;
    static {
        // 首先计算出每个格点和中间格点之间的角度
        double[] angles = new double[BUFFER_BLOCK_SIZE];
        for (int i = 0; i < BUFFER_BLOCK_SIZE; i++) {
            int xoff = (i % BUFFER_MATRIX_SIZE) - BUFFER_RANGE;
            int yoff = BUFFER_RANGE - (i / BUFFER_MATRIX_SIZE);
            if (xoff == 0 && yoff == 0) {
                angles[i] = 100;
                continue;
            }
            double sin = yoff / Math.sqrt(xoff * xoff + yoff * yoff);
            double arc = Math.asin(sin);
            if (xoff >= 0) {
                if (yoff < 0) {
                    arc += 2 * Math.PI;
                }
            } else {
                arc = Math.PI - arc;
            }
            angles[i] = arc;
        }
        
        // 计算每个格点最接近角度的格点顺序
        REPLACEMENT_SEQUENCE = new int[BUFFER_BLOCK_SIZE][BUFFER_BLOCK_SIZE];
        for (int i = 0; i < BUFFER_BLOCK_SIZE; i++) {
            // 建立本格点和其他所有格点之间的角度差的表
            double[] anglesDup = new double[BUFFER_BLOCK_SIZE];
            for (int j = 0; j < BUFFER_BLOCK_SIZE; j++) {
                REPLACEMENT_SEQUENCE[i][j] = j;
                anglesDup[j] = Math.abs(angles[j] - angles[i]);
                if (anglesDup[j] > Math.PI) {
                    anglesDup[j] = Math.PI * 2 - anglesDup[j];
                }
                
                // 把目标格点到原格点之间的距离作为一个权值调整，避免总是查找到中心点那一条直线上的点。
                int x1 = i % BUFFER_BLOCK_SIZE;
                int y1 = i / BUFFER_BLOCK_SIZE;
                int x2 = j % BUFFER_BLOCK_SIZE;
                int y2 = j / BUFFER_BLOCK_SIZE;
                int dist = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
                anglesDup[j] += Math.sqrt(dist) / 5;
            }
            
            // 按角度从小到大的顺序排序
            for (int ii = 0; ii < BUFFER_BLOCK_SIZE; ii++) {
                for (int jj = ii + 1; jj < BUFFER_BLOCK_SIZE; jj++) {
                    if (anglesDup[ii] > anglesDup[jj]) {
                        double dtemp = anglesDup[ii];
                        anglesDup[ii] = anglesDup[jj];
                        anglesDup[jj] = dtemp;
                        int itemp = REPLACEMENT_SEQUENCE[i][ii];
                        REPLACEMENT_SEQUENCE[i][ii] = REPLACEMENT_SEQUENCE[i][jj];
                        REPLACEMENT_SEQUENCE[i][jj] = itemp;
                    }
                }
            }
        }
    }
    
    /**
     * 创建一个地图的PathFinder，缓存将在此时被创建出来。
     * @param map
     */
    public PathFinder(GameMapInfo map) {
        this.map = map;
        this.gameMap = map.owner.getMapFile().getMaps().get(map.id);
    }
    
    /**
     * 编辑器中测试时用到的构造方法。
     * @param map
     * @param mf
     */
    public PathFinder(GameMapInfo map, GameMap gm) {
        this.map = map;
        this.gameMap = gm;
        buildPathBuffer();
    }
    
    /**
     * 把创建好的路径缓存保存为字节流。
     */
    public byte[] savePathBuffer() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(gridWidth);
            dos.writeInt(gridHeight);
            dos.writeInt(passable.length);
            dos.writeInt(passable[0].length);
            for (byte[] row : passable) {
                dos.write(row);
            }
            dos.writeInt(passable2.length);
            dos.write(passable2);
            dos.writeInt(godX);
            dos.writeInt(godY);
            dos.writeInt(pathBuffer.length);
            dos.write(pathBuffer);
            dos.writeInt(pathBufferIndex.length);
            for (short s : pathBufferIndex) {
                dos.writeShort(s);
            }
            dos.writeInt(reachable.size());
            for (int i = 0; i < reachable.size(); i++) {
                byte[][] matrix = reachable.get(i);
                dos.writeInt(matrix.length);
                dos.writeInt(matrix[0].length);
                for (byte[] row : matrix) {
                    dos.write(row);
                }
            }
            dos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
    
    /**
     * 从预先保存的路径缓存数据中载入。
     */
    public void loadPathBuffer(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);
        gridWidth = dis.readInt();
        gridHeight = dis.readInt();
        passable = new byte[dis.readInt()][dis.readInt()];
        for (byte[] row : passable) {
            dis.readFully(row);
        }
        passable2 = new byte[dis.readInt()];
        dis.readFully(passable2);
        godX = dis.readInt();
        godY = dis.readInt();
        pathBuffer = new byte[dis.readInt()];
        dis.readFully(pathBuffer);
        pathBufferIndex = new short[dis.readInt()];
        for (int i = 0; i < pathBufferIndex.length; i++) {
            pathBufferIndex[i] = dis.readShort();
        }
        reachable = new ArrayList<byte[][]>();
        int count = dis.readInt();
        for (int i = 0; i < count; i++) {
            byte[][] matrix = new byte[dis.readInt()][dis.readInt()];
            for (byte[] row : matrix) {
                dis.readFully(row);
            }
            reachable.add(matrix);
        }
        buildReachable2();
        buildReachable3();
    }
    
    /**
     * 创建路径缓存。
     */
    public void buildPathBuffer() {
        // 计算通过性信息
        passable = getPassableMatrix();
        
        // 为缓存分配空间
        gridWidth = gameMap.width / GRID_SIZE;
        gridHeight = gameMap.height / GRID_SIZE;
        pathBufferIndex = new short[gridWidth * gridHeight];
        pathBuffer = new byte[gridWidth * gridHeight * BUFFER_BLOCK_SIZE];

        // 扫描所有格点，生成每个格点的缓存。
        int index = 0;
        short bufferIndex = 0;
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                // 生成一个格点的缓存，如果返回null，则表示这个格点周围11x11范围内全部可以通过，不需要缓存
                byte[] buf = createGridPathBuffer(i, j);
                if (buf != null) {
                    System.arraycopy(buf, 0, pathBuffer, bufferIndex * BUFFER_BLOCK_SIZE, BUFFER_BLOCK_SIZE);
                    pathBufferIndex[index] = bufferIndex;
                    bufferIndex++;
                } else {
                    pathBufferIndex[index] = -1;
                }
                index++;
            }
        }
        
        // 实际需要的缓存长度应该会比最大长度要小，这里裁掉未用的空间
        if (bufferIndex < gridWidth * gridHeight) {
            byte[] realBuf = new byte[bufferIndex * BUFFER_BLOCK_SIZE];
            System.arraycopy(pathBuffer, 0, realBuf, 0, realBuf.length);
            pathBuffer = realBuf;
        }
        
        // 计算每个格点的可通达性
        reachable = new ArrayList<byte[][]>();
        for (GameMapObject gmo : map.objects) {
            if (!(gmo instanceof GameMapExit)) {
                continue;
            }
            reachable.add(getReachableMatrix((GameMapExit)gmo));
        }
        buildReachable2();
        buildReachable3();
    }
    
    /*
     * 构建增强通达信息（为脱离卡死服务），只有一个格子周围8个格子超过3个可以到达，才算到达。
     */
    protected void buildReachable2() {
        reachable2 = new ArrayList<byte[][]>();
        for (byte[][] r : reachable) {
            int w = r[0].length;
            int h = r.length;
            byte[][] r2 = new byte[h][w]; 
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    // 如果这个格子无法通过，reachable2也是无法通过
                    if (r[i][j] == 0) {
                        r2[i][j] = 0;
                        continue;
                    }
                    
                    // 计算周围8个格子的可通过数
                    int passcount = 0;
                    for (int k = i - 1; k <= i + 1; k++) {
                        if (k < 0 || k >= h) {
                            continue;
                        }
                        for (int l = j - 1; l <= j + 1; l++) {
                            if (l < 0 || l >= w) {
                                continue;
                            }
                            if (r[k][l] == 1) {
                                passcount++;
                            }
                        }
                    }
                    r2[i][j] = (byte)passcount;
                }
            }
            reachable2.add(r2);
        }
    }
    
    /*
     * 构建格子通达信息快速查找表（4x4），用于穿墙判断。如果一个格子周围任意一个格子可通过，都算可通过。
     */
    protected void buildReachable3() {
        int gridw = passable[0].length << 2;
        int gridh = passable.length << 2;
        reachable3 = new byte[gridh][gridw];
        for (GameMapObject gmo : map.objects) {
            if (!(gmo instanceof GameMapExit)) {
                continue;
            }
            byte[][] matrix = getReachableMatrix3((GameMapExit)gmo);
            for (int y = 0; y < gridh; y++) {
                for (int x = 0; x < gridw; x++) {
                    if (matrix[y][x] == 1) {
                        reachable3[y][x] = 1;
                        if (y > 0) {
                            reachable3[y - 1][x] = 1;
                        }
                        if (y < gridh - 1) {
                            reachable3[y + 1][x] = 1;
                        }
                        if (x > 0) {
                            reachable3[y][x - 1] = 1;
                        }
                        if (x < gridw - 1) {
                            reachable3[y][x + 1] = 1;
                        }
                    }
                }
            }
        }
    }
    
    /*
     * 为一个格点创建周围11x11范围内的路径信息，采用A*算法，计算出11x11范围内每个格点到起始点的最短距离。
     * 如果所有120个格点都能够到达，则不需要缓存，直接返回null即可。
     */
    protected byte[] createGridPathBuffer(int row, int col) {
        // 如果起始点不可通过，则不用计算了
        if (passable[row][col] == 0) {
            return null;
        }
        
        // 从中间作为起始点，向四周扩散
        List<int[]> points = new ArrayList<int[]>();        // 待检查点
        byte[][] tempMatrix = new byte[BUFFER_MATRIX_SIZE][BUFFER_MATRIX_SIZE];     // 路径长度缓存，-128表示未经过点
        for (int i = 0; i < BUFFER_MATRIX_SIZE; i++) {
            Arrays.fill(tempMatrix[i], (byte)-128);
        }
        points.add(new int[] { 0, 0 });
        tempMatrix[BUFFER_RANGE][BUFFER_RANGE] = 0;
        while (points.size() > 0) {
            int[] coord = points.remove(0);
            int x = coord[0];
            int y = coord[1];
            byte dist = tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE];
            
            // 检查此点四周四个点是否可以通过，如果可以通过，并且以前没有走过，那么加入检查队列，并且把它的路径长度标记为当前点的路径长度+1
            if (x > -BUFFER_RANGE && canPassGrid(y + row, x + col - 1) && 
                    (tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE - 1] == (byte)-128 || tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE - 1] > dist + 2)) {
                points.add(new int[] { x - 1, y });
                tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE - 1] = (byte)(dist + 2);
            }
            if (x < BUFFER_RANGE && canPassGrid(y + row, x + col + 1) && 
                    (tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE + 1] == (byte)-128 || tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE + 1] > dist + 2)) {
                points.add(new int[] { x + 1, y });
                tempMatrix[y + BUFFER_RANGE][x + BUFFER_RANGE + 1] = (byte)(dist + 2);
            }
            if (y > -BUFFER_RANGE && canPassGrid(y + row - 1, x + col) && 
                    (tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE] == (byte)-128 || tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE] > dist + 2)) {
                points.add(new int[] { x, y - 1 });
                tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE] = (byte)(dist + 2);
            }
            if (y < BUFFER_RANGE && canPassGrid(y + row + 1, x + col) && 
                    (tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE] == (byte)-128 || tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE] > dist + 2)) {
                points.add(new int[] { x, y + 1 });
                tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE] = (byte)(dist + 2);
            }
            
            // 检查4个斜向的格是否可以到达
            if (x > -BUFFER_RANGE && y > -BUFFER_RANGE && canPassGrid(y + row - 1, x + col - 1) &&
                    (canPassGrid(y + row - 1, x + col) || canPassGrid(y + row, x + col - 1)) &&
                    (tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE - 1] == (byte)-128 || tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE - 1] > dist + 3)) {
                points.add(new int[] { x - 1, y - 1 });
                tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE - 1] = (byte)(dist + 3);
            }
            if (x < BUFFER_RANGE && y > -BUFFER_RANGE && canPassGrid(y + row - 1, x + col + 1) && 
                    (canPassGrid(y + row - 1, x + col) || canPassGrid(y + row, x + col + 1)) &&
                    (tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE + 1] == (byte)-128 || tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE + 1] > dist + 3)) {
                points.add(new int[] { x + 1, y - 1 });
                tempMatrix[y + BUFFER_RANGE - 1][x + BUFFER_RANGE + 1] = (byte)(dist + 3);
            }
            if (x > -BUFFER_RANGE && y < BUFFER_RANGE && canPassGrid(y + row + 1, x + col - 1) && 
                    (canPassGrid(y + row + 1, x + col) || canPassGrid(y + row, x + col - 1)) &&
                    (tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE - 1] == (byte)-128 || tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE - 1] > dist + 3)) {
                points.add(new int[] { x - 1, y + 1 });
                tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE - 1] = (byte)(dist + 3);
            }
            if (x < BUFFER_RANGE && y < BUFFER_RANGE && canPassGrid(y + row + 1, x + col + 1) && 
                    (canPassGrid(y + row + 1, x + col) || canPassGrid(y + row, x + col + 1)) &&
                    (tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE + 1] == (byte)-128 || tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE + 1] > dist + 3)) {
                points.add(new int[] { x + 1, y + 1 });
                tempMatrix[y + BUFFER_RANGE + 1][x + BUFFER_RANGE + 1] = (byte)(dist + 3);
            }
        }
        
        // 现在所有能到达的点都已经标记上了，剩下为-128的就是通过11x11范围内的点无法直接到达的点。对于每个无法到达的点，我们要从它出发，向左右
        // 查找第一个可以到达的点，作为替换目标。
        boolean hasUnpassGrid = false;
        for (int i = 0; i < BUFFER_MATRIX_SIZE; i++) {
            for (int j = 0; j < BUFFER_MATRIX_SIZE; j++) {
                if (tempMatrix[i][j] != (byte)-128) {
                    continue;
                }
                int pt = findReplacement(tempMatrix, i, j);
                tempMatrix[i][j] = (byte)(-1 - pt);
                
                // 统计是否有不可到达的有效点
                if (!hasUnpassGrid) {
                    int rx = j + col - BUFFER_RANGE;
                    int ry = i + row - BUFFER_RANGE;
                    if (rx >= 0 && rx < gridWidth && ry >= 0 && ry < gridHeight) {
                        hasUnpassGrid = true;
                    }
                }
            }
        }
        
        // 如果所有点都可以到达或者越界，返回null
        if (!hasUnpassGrid) {
            return null;
        }
        
        // 把缓存展平返回
        byte[] ret = new byte[BUFFER_MATRIX_SIZE * BUFFER_MATRIX_SIZE];
        for (int i = 0; i < BUFFER_MATRIX_SIZE; i++) {
            for (int j = 0; j < BUFFER_MATRIX_SIZE; j++) {
                // 这里转换一下缓存格式以提高算法效率：如果目标不可到达，还是保存最近的可到达点的位置；如果目标可到达，
                // 计算从起始点到此点的最佳路径中第一步的格点坐标。
                if (tempMatrix[i][j] < 0) {
                    ret[i * BUFFER_MATRIX_SIZE + j] = tempMatrix[i][j];
                } else if (tempMatrix[i][j] < 4) {
                    ret[i * BUFFER_MATRIX_SIZE + j] = (byte)(j + i * BUFFER_MATRIX_SIZE);
                } else {
                    List<int[]> path = findBestPath(tempMatrix, i, j);
                    int[] arr = path.get(path.size() - 1);
                    ret[i * BUFFER_MATRIX_SIZE + j] = (byte)(arr[0] + arr[1] * BUFFER_MATRIX_SIZE);
                }
            }
        }
        return ret;
    }

    /*
     * 查找到矩阵中一个目标点的最佳路径。返回列表中，最后一个元素必定是起始点坐标。坐标格式为int[] { x, y }。
     */
    protected List<int[]> findBestPath(byte[][] matrix, int row, int col) {
        List<int[]> ret = new ArrayList<int[]>();
        int dist = matrix[row][col];
        while (dist > 0) {
            ret.add(new int[] { col, row });
            int nextdist = dist;
            int nextrow = row, nextcol = col;
            
            // 上
            if (row > 0) {
                int t = matrix[row - 1][col];
                if (t >= 0 && t < nextdist) {
                    nextrow = row - 1;
                    nextdist = t;
                }
            }
            
            // 下
            if (row < BUFFER_MATRIX_SIZE - 1) {
                int t = matrix[row + 1][col];
                if (t >= 0 && t < nextdist) {
                    nextrow = row + 1;
                    nextdist = t;
                }
            }
            
            // 左
            if (col > 0) {
                int t = matrix[row][col - 1];
                if (t >= 0 && t < nextdist) {
                    nextcol = col - 1;
                    nextdist = t;
                }
            }

            // 右
            if (col < BUFFER_MATRIX_SIZE - 1) {
                int t = matrix[row][col + 1];
                if (t >= 0 && t < nextdist) {
                    nextcol = col + 1;
                    nextdist = t;
                }
            }
            
            // 左上
            if (row > 0 && col > 0) {
                int t = matrix[row - 1][col - 1];
                if (t >= 0 && t < nextdist) {
                    nextrow = row - 1;
                    nextcol = col - 1;
                    nextdist = t;
                }
            }
            
            // 左下
            if (row < BUFFER_MATRIX_SIZE - 1 && col > 0) {
                int t = matrix[row + 1][col - 1];
                if (t >= 0 && t < nextdist) {
                    nextrow = row + 1;
                    nextcol = col - 1;
                    nextdist = t;
                }
            }

            // 右上
            if (row > 0 && col < BUFFER_MATRIX_SIZE - 1) {
                int t = matrix[row - 1][col + 1];
                if (t >= 0 && t < nextdist) {
                    nextrow = row - 1;
                    nextcol = col + 1;
                    nextdist = t;
                }
            }

            // 右下
            if (row < BUFFER_MATRIX_SIZE - 1 && col < BUFFER_MATRIX_SIZE - 1) {
                int t = matrix[row + 1][col + 1];
                if (t >= 0 && t < nextdist) {
                    nextrow = row + 1;
                    nextcol = col + 1;
                    nextdist = t;
                }
            }
            
            if (nextrow == row && nextcol == col) {
                throw new IllegalArgumentException("Bad path buffer.");
            }
            row = nextrow;
            col = nextcol;
            dist = nextdist;
        }
        return ret;
    }
    
    /*
     * 从矩阵中的一个点出发，查找距离最近的可以通过的点。返回的数组中，第一个元素是目标点的索引：左上角为0，右下角为120。
     */
    protected int findReplacement(byte[][] matrix, int row, int col) {
        // 根据目标点所在的分区，取出这个分区对应的查找表
        int pos = col + row * BUFFER_MATRIX_SIZE;
        int[] searchTable = REPLACEMENT_SEQUENCE[pos];

        // 按查找表指定的顺序，依次查找到第一个可以通达的点
        boolean found = false;
        int len = searchTable.length;
        for (int i = 0; i < len; i++) {
            if (found) {
                pos = searchTable[i];
                if (matrix[pos / BUFFER_MATRIX_SIZE][pos % BUFFER_MATRIX_SIZE] >= 0) {
                    return pos;
                }
            } else {
                if (searchTable[i] == pos) {
                    found = true;
                }
            }
        }
        return BUFFER_RANGE + BUFFER_RANGE * BUFFER_MATRIX_SIZE;
    }
    
    /*
     * 检查一个格点是否允许通过。如果格点被标记为不可通过，或者给定的坐标超出地图范围，返回false。
     */
    protected boolean canPassGrid(int row, int col) {
        if (row < 0 || row >= passable.length) {
            return false;
        }
        if (col < 0 || col >= passable[0].length) {
            return false;
        }
        return passable[row][col] != 0;
    }
    
    /*
     * 在一个布尔数据的矩阵中填充一个方块。
     */
    protected void fillMatrix(boolean[][] matrix, int x, int y, int w, int h, boolean value) {
        // 规范位置信息
        if (x >= matrix[0].length || y >= matrix.length) {
            return;
        }
        if (x < 0) {
            w += x;
            x = 0;
        }
        if (y < 0) {
            h += y;
            y = 0;
        }
        if (x + w > matrix[0].length) {
            w = matrix[0].length - x;
        }
        if (y + h > matrix.length) {
            h = matrix.length - y;
        }
        if (w <= 0 || h <= 0) {
            return;
        }
        
        // 逐行填充
        for (int i = y; i < y + h; i++) {
            Arrays.fill(matrix[i], x, x + w, value);
        }
    }
    
    /**
     * 从地图数据中分析通过性数据。
     * @return 每个格点的通过性。如果地图大小为w*h，则返回数组的大小应该是(w/16)*(h/16)。
     */
    protected byte[][] getPassableMatrix() {
        gridWidth = gameMap.width / GRID_SIZE;
        gridHeight = gameMap.height / GRID_SIZE;
        
        ArrayList<TileInfo> tiles = gameMap.parent.getTileImage().tileInfo;
        int tw = gameMap.parent.getTileWidth();
        int tw2 = tw * 4 / GRID_SIZE;
        int th = gameMap.parent.getTileHeight();
        int th2 = th * 4 / GRID_SIZE;
        int btw = gameMap.parent.getBlurTileWidth();
        int btw2 = btw * 4 / GRID_SIZE;
        int bth = gameMap.parent.getBlurTileHeight();
        int bth2 = bth * 4 / GRID_SIZE;
        
        // 创建一个数组来保存通过性信息，格点粒度为每个实际格点16个小格
        boolean[][] gridData = new boolean[gridHeight * 4][gridWidth * 4];
        for (int i = 0; i < gridData.length; i++) {
            Arrays.fill(gridData[i], true);
        }
        boolean afterGround = false;
        for (IMapLayer layer : gameMap.layers) {
            if (layer instanceof AccurateMapLayer) {
                // 扫描贴图数据，不可通过的Tile标记为false。每个精确贴图占8个小格子。
                short[][] mapData = ((AccurateMapLayer)layer).getLayerData();
                for (int i = 0; i < mapData.length; i++) {
                    for (int j = 0; j < mapData[i].length; j++) {
                        int cc = mapData[i][j];
                        if (cc == -1) {
                            continue;
                        }
                        TileInfo cinfo = tiles.get(cc);
                        if (cinfo.unpassable) {
                            fillMatrix(gridData, tw2 * j, th2 * i, tw2, th2, false);
                        }
                    }
                }
            } else if (layer instanceof MapNPCLayer) {
                if (afterGround) {
                    // 天空层不计算碰撞区域
                    continue;
                }
                if (layer == gameMap.groundLayer) {
                    afterGround = true;
                }
                for (MapNPC npc : ((MapNPCLayer)layer).getNpcs()) {
                    long key = gameMap.parent.getNPCKey(npc);
                    NPCImageInfo ninfo = gameMap.parent.getNPCs().get(key);
                    if (ninfo == null) {
                        continue;
                    }
                    for (int i = 0; i < ninfo.cx.length; i++) {
                        int startx = ninfo.cx[i] + npc.x;
                        int starty = ninfo.cy[i] + npc.y;
                        int endx = startx + ninfo.cw[i];
                        int endy = starty + ninfo.ch[i];
                        startx = (int)Math.round(startx * 4.0 / GRID_SIZE);
                        starty = (int)Math.round(starty * 4.0 / GRID_SIZE);
                        endx = (int)Math.round(endx * 4.0 / GRID_SIZE);
                        endy = (int)Math.round(endy * 4.0 / GRID_SIZE);
                        fillMatrix(gridData, startx, starty, endx - startx, endy - starty, false);
                    }
                }
            } else if (layer instanceof BlurMapLayer) {
                // 扫描贴图数据，不可通过的Tile标记为false。每个模糊贴图占16个小格子。
                int[][][] mapData = ((BlurMapLayer)layer).getMapData();
                for (int i = 0; i < mapData.length; i++) {
                    for (int j = 0; j < mapData[i].length; j++) {
                        int[] cc = mapData[i][j];
                        for (int k = cc.length - 1; k >= 0; k--) {
                            if (cc[k] != -1) {
                                int lfid = cc[k] >> 16;
                                int tid = cc[k] & 0xFF;
                                TileInfo cinfo = gameMap.parent.getLandforms().get(lfid).tileInfo.get(tid);
                                if (cinfo.unpassable) {
                                    fillMatrix(gridData, btw2 * j, bth2 * i, btw2, bth2, false);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // 对于每个格点，分析其16个小格，如果超过1半（不含一半）不可通过，则认为这个格点不可通过，否则认为可通过
        int minDistance = Integer.MAX_VALUE;
        int centerX = gridWidth * GRID_SIZE / 2;
        int centerY = gridHeight * GRID_SIZE / 2;
        byte[][] ret = new byte[gridHeight][gridWidth];
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                int passCount = 0;
                for (int ii = 0; ii < 4; ii++) {
                    for (int jj = 0; jj < 4; jj++) {
                        if (gridData[i * 4 + ii][j * 4 + jj]) {
                            passCount++;
                        }
                    }
                }
                ret[i][j] = (byte)(passCount >= 10 ? 1 : 0);
                if (ret[i][j] != 0) {
                    // 计算此格中心距离地图中心的距离，寻找距离中心最近的一个可通过格点
                    int gridCenterX = j * GRID_SIZE + GRID_SIZE / 2;
                    int gridCenterY = i * GRID_SIZE + GRID_SIZE  / 2;
                    int distance = (gridCenterX - centerX) * (gridCenterX - centerX) + (gridCenterY - centerY) * (gridCenterY - centerY);
                    if (distance < minDistance) {
                        minDistance = distance;
                        godX = gridCenterX;
                        godY = gridCenterY;
                    }
                }
            }
        }
        
        // 把通过性数据通过另外一种格式保存在一个byte数组中，每个4x4的格点占1位。
        passable2 = new byte[(gridHeight * 4 * gridWidth * 4 + 7) / 8];
        int tmp = 0;
        int index = 0;
        int bitIndex = 7;
        for (int i = 0; i < gridHeight * 4; i++) {
            for (int j = 0; j < gridWidth * 4; j++) {
                if (gridData[i][j]) {
                    tmp |= 1 << bitIndex;
                }
                bitIndex--;
                if (bitIndex < 0) {
                    passable2[index] = (byte)tmp;
                    index++;
                    bitIndex = 7;
                    tmp = 0;
                }
            }
        }
        if (bitIndex != 7) {
            passable2[index] = (byte)tmp;
        }
        
        return ret;
    }
    
    /**
     * 从通过性数据中分析通达性数据。
     * @return 每个格点的可通达性。如果地图大小为w*h，则返回数组的大小应该是(w/16)*(h/16)。
     */
    protected byte[][] getReachableMatrix(GameMapExit exit) {
        int gridw = passable[0].length;
        int gridh = passable.length;
        byte[][] ret = new byte[gridh][gridw];
        int ex = exit.x / GRID_SIZE;
        int ey = exit.y / GRID_SIZE;
        
        // 从出口点开始，按四方向行走法查找所有可到达的格点并做标记
        List<int[]> openList = new ArrayList<int[]>();
        openList.add(new int[] { ex, ey });
        while (openList.size() > 0) {
            int[] p = openList.remove(0);
            int x = p[0];
            int y = p[1];
            if (ret[y][x] == 0) {
                // 如果这个点还没有走过，把它标记，并把它周围四个格子中可通达的邻居加入待定表
                ret[y][x] = 1;
                if (x > 0 && passable[y][x - 1] == 1) {
                    openList.add(new int[] { x - 1, y });
                }
                if (x < gridw - 1 && passable[y][x + 1] == 1) {
                    openList.add(new int[] { x + 1, y });
                }
                if (y > 0 && passable[y - 1][x] == 1) {
                    openList.add(new int[] { x, y - 1 });
                }
                if (y < gridh - 1 && passable[y + 1][x] == 1) {
                    openList.add(new int[] { x, y + 1 });
                }
            }
        }
        return ret;
    }
    
    /*
     * 从通过性数据中分析通达性数据（按4x4的格子）。
     * @return 每个格点的可通达性。如果地图大小为w*h，则返回数组的大小应该是(w/4)*(h/4)。
     */
    protected byte[][] getReachableMatrix3(GameMapExit exit) {
        int gridw = passable[0].length << 2;
        int gridh = passable.length << 2;
        byte[][] ret = new byte[gridh][gridw];
        int ex = (exit.x << 2) / GRID_SIZE;
        int ey = (exit.y << 2) / GRID_SIZE;
        
        // 从出口点开始，按四方向行走法查找所有可到达的格点并做标记
        List<int[]> openList = new ArrayList<int[]>();
        openList.add(new int[] { ex, ey });
        while (openList.size() > 0) {
            int[] p = openList.remove(0);
            int x = p[0];
            int y = p[1];
            if (ret[y][x] == 0) {
                // 如果这个点还没有走过，把它标记，并把它周围四个格子中可通达的邻居加入待定表
                ret[y][x] = 1;
                if (x > 0 && canPassGrid2(x - 1, y)) {
                    openList.add(new int[] { x - 1, y });
                }
                if (x < gridw - 1 && canPassGrid2(x + 1, y)) {
                    openList.add(new int[] { x + 1, y });
                }
                if (y > 0 && canPassGrid2(x, y - 1)) {
                    openList.add(new int[] { x, y - 1 });
                }
                if (y < gridh - 1 && canPassGrid2(x, y + 1)) {
                    openList.add(new int[] { x, y + 1 });
                }
            }
        }
        return ret;
    }
    
    /**
     * 判断一个点是否可以通过，精度为4像素。
     * @param x 地图坐标（像素）
     * @param y 地图坐标（像素）
     */
    private boolean canPass(int x, int y) {
        int width = passable[0].length << 2;
        int index = (y >> 2) * width + (x >> 2);
        int bitIndex = 7 - (index & 7);
        index >>= 3;
        return (passable2[index] & (1 << bitIndex)) != 0;
    }
    
    /*
     * 判断一个格子是否可以通过，精度为4像素。
     * @param x 格子坐标系横坐标
     * @param y 格子坐标系纵坐标
     */
    private boolean canPassGrid2(int x, int y) {
        int width = passable[0].length << 2;
        int index = y * width + x;
        int bitIndex = 7 - (index & 7);
        index >>= 3;
        return (passable2[index] & (1 << bitIndex)) != 0;
    }
    
    /**
     * 查找路径。
     * @param fromx 起始点x坐标（像素）
     * @param fromy 起始点y坐标（像素）
     * @param tox 目标点x坐标（像素）
     * @param toy 目标点y坐标（像素）
     * @return 向目标点进发路径上的第一个目标点（像素）。
     */
    public int[] findPath(int fromx, int fromy, int tox, int toy) {
        if (tox < 0 || toy < 0 || tox >= gameMap.width || toy >= gameMap.height) {
            return new int[] { fromx, fromy };
        }
        
        // 转换为格点坐标，并检查起始点是否合法。
        int fxg = fromx >> GRID_BITS;
        int fyg = fromy >> GRID_BITS;
        int txg = tox >> GRID_BITS;
        int tyg = toy >> GRID_BITS;
        if (fxg < 0 || fxg >= gridWidth || fyg < 0 || fyg >= gridHeight) {
            return new int[] { tox, toy };
        }
        
        // 取得起始格点的路径缓存数据
        int bindex = pathBufferIndex[fxg + fyg * gridWidth];
        if (bindex == -1) {
            // 如果没有缓存数据，有两种情况：这个格点本身不可通过，或者这个格点周围全部可以通过
            if (passable[fyg][fxg] == 0) {
                // 如果起始点不可通过，那么先把它拉出到旁边一个可通过点，如果四周都不可通过，则向地图中预先选定的一个可通过点行走
                return new int[] { godX, godY };
            } else {
                // 如果全部可以通过，那么直接对这目标冲过去就行了
                return new int[] { tox, toy };
            }
        }
        bindex *= BUFFER_BLOCK_SIZE;
        
        // 如果目标点在起始格点周围11x11的范围内，则取目标点为检查点，否则取目标方向上最远的一个在范围内的点作为目标点
        int xoff = txg - fxg;
        int yoff = tyg - fyg;
        if (xoff > BUFFER_RANGE || xoff < -BUFFER_RANGE || yoff > BUFFER_RANGE || yoff < -BUFFER_RANGE) {
            int edge;       // 判断目标点方向：0 - 3分别是东南西北
            if (xoff > 0) {
                if (yoff > 0) {
                    if (xoff > yoff) {
                        edge = 0;
                    } else {
                        edge = 1;
                    }
                } else {
                    if (xoff > -yoff) {
                        edge = 0;
                    } else {
                        edge = 3;
                    }
                }
            } else {
                if (yoff > 0) {
                    if (-xoff > yoff) {
                        edge = 2;
                    } else {
                        edge = 1;
                    }
                } else {
                    if (-xoff > -yoff) {
                        edge = 2;
                    } else {
                        edge = 3;
                    }
                }
            }
            switch (edge) {
                case 0:
                    yoff = yoff * BUFFER_RANGE / xoff;
                    xoff = BUFFER_RANGE;
                    break;
                case 1:
                    xoff = xoff * BUFFER_RANGE / yoff;
                    yoff = BUFFER_RANGE;
                    break;
                case 2:
                    yoff = yoff * BUFFER_RANGE / (-xoff);
                    xoff = -BUFFER_RANGE;
                    break;
                case 3:
                    xoff = xoff * BUFFER_RANGE / (-yoff);
                    yoff = -BUFFER_RANGE;
                    break;
            }
        }
        
        // 如果目标检查点不可通过，则切换目标点为目标点附近最近的可通过点
        int bindex2 = xoff + BUFFER_RANGE + (yoff + BUFFER_RANGE) * BUFFER_MATRIX_SIZE;
        if (pathBuffer[bindex + bindex2] < 0) {
            bindex2 = -(pathBuffer[bindex + bindex2] + 1);
        }
        
        // 缓存中保存了目标检查点到起始点之间最佳路径的第一步，直接返回就可以了
        byte targetCell = pathBuffer[bindex + bindex2];
        int targetX = (targetCell % BUFFER_MATRIX_SIZE) + fxg - BUFFER_RANGE;
        int targetY = (targetCell / BUFFER_MATRIX_SIZE) + fyg - BUFFER_RANGE;
        return new int[] { targetX * GRID_SIZE + GRID_SIZE / 2, targetY * GRID_SIZE + GRID_SIZE / 2 };
    }
    
    /**
     * 判断一个点周围可通达点的个数。
     * @param x 像素坐标
     * @param y 像素坐标
     * @return 0表示此点不可通达，>0表示周围可通道的格点个数（包括自己）
     */
    private byte getPassLevel(int x, int y) {
        try {
            for (byte[][] rinfo : reachable2) {
                if (rinfo[y / GRID_SIZE][x / GRID_SIZE] > 0) {
                    return rinfo[y / GRID_SIZE][x / GRID_SIZE];
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }
    
    private static int[][] SEARCH_GRIDS1 = {
        { -16, -16 },
        { 0, -16 },
        { 16, -16 },
        { -16, 0 },
        { 16, 0 },
        { -16, 16 },
        { 0, 16 },
        { 16, 16 },
    };
    private static int[][] SEARCH_GRIDS2 = {
        { -32, -32 }, 
        { -16, -32 },
        { 0, -32 },
        { 16, -32 },
        { 32, -32 },
        { -32, -16 }, 
        { 32, -16 },
        { -32, 0 }, 
        { 32, 0 },
        { -32, 16 }, 
        { 32, 16 },
        { -32, 32 }, 
        { -16, 32 },
        { 0, 32 },
        { 16, 32 },
        { 32, 32 },
    };
    
    /**
     * 尝试从当前点脱离卡死。如果周围有一个格子不处于卡死状态，则尝试移动（随机方向）。
     * 如果周围2圈内的格子都不能移动，则返回null。
     * @param x 初始位置
     * @param y 结束位置
     * @return 如果周围2圈内的某个格子不卡死，则返回那个格子的中间位置，否则返回null
     */
    public int[] tryOutPrison(int x, int y) {
        int[] ret = tryOutPrison(x, y, SEARCH_GRIDS1);
//        if (ret == null) {
//            ret = tryOutPrison(x, y, SEARCH_GRIDS2);
//        }
        return ret;
    }
    
    /*
     * 在指定的周围位置搜索可行走的位置。
     */
    private int[] tryOutPrison(int x, int y, int[][] search) {
        int t = search.length;
        int maxPassLevel = 0;
        int maxPassIndex = -1;
        for (int i = 0; i < t; i++) {
            int tx = x + search[i][0];
            int ty = y + search[i][1];
            int lvl = getPassLevel(tx, ty);
            if (lvl > maxPassLevel) {
                maxPassIndex = i;
                maxPassLevel = lvl;
            }
        }
        if (maxPassIndex != -1 && maxPassLevel >= 6) {
            int tx = x + search[maxPassIndex][0];
            int ty = y + search[maxPassIndex][1];
            tx = (tx / GRID_SIZE) * GRID_SIZE + GRID_SIZE / 2;
            ty = (ty / GRID_SIZE) * GRID_SIZE + GRID_SIZE / 2;
            return new int[] { tx, ty };
        } else {
            return null;
        }
    }
    
    /**
     * 判断从指定点出发能否到达指定出口（16x16精度）。
     * @param x 像素坐标
     * @param y 像素坐标
     * @param exitIndex 出口在场景中所有出口列表中的索引
     * @return
     */
    public boolean canReach(int x, int y, int exitIndex) {
        try {
            byte[][] rinfo = reachable.get(exitIndex);
            if (rinfo[y / GRID_SIZE][x / GRID_SIZE] == 1) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    /**
    * 检查场景中某一点是否可到达（从任意出口，4x4精度）。
    * @param x 像素坐标
    * @param y 像素坐标
    * @return
    */
   public boolean canReach(int x, int y) {
       try {
           int gy = (y << 2) / GRID_SIZE;
           int gx = (x << 2) / GRID_SIZE;
           return reachable3[gy][gx] == 1;
       } catch (Exception e) {
       }
       return false;
   }
}
