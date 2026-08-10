package com.pip.itimes.server.world.trace;

import java.io.File;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

public class BasicPassData {
	public int id;
	
	/** 在基础数据中0表示可通过,Integer.MaxValue表示不可通过.
	 *  在高速数据中: 第i bit置位 表示和第i个节点联通。 最高位 0x80000000 表示是否可通过*/
	public int [][]data;
	
	
	public int w; 
	/** 地图瓦片行数 */
	public int h;
	/** 瓦片宽度，16 */
	public int tw = 16;
	/** 瓦片高度，精细地图8，模糊地图16*/
	public int th = 16;
	public int getTh() {
		return th;
	}
	ArrayList<InterestedPoint> targetPoints = new ArrayList<InterestedPoint>();	
	
	public ArrayList<InterestedPoint> getTargetPoints() {
		return targetPoints;
	}
	private void load(Document doc) {
		Element root = doc.getRootElement();
		id = Integer.parseInt(root.attributeValue("stageId"));
		int w = Integer.parseInt(root.attributeValue("width"));
		int h = Integer.parseInt(root.attributeValue("height"));
		th = Integer.parseInt(root.attributeValue("tileHeight"));
		this.w = w;
		this.h = h;
		data = new int[h][w];
		int i = 0;
		Element map = root.element("data");
		Iterator<Element> rows = map.elementIterator("row");
		while (rows.hasNext()) {
			Element row = rows.next();
			char []s = row.getText().trim().toCharArray();
			for (int j = 0; j < s.length && j < w; j++) {
				if (s[j] != '.' && s[j] != '+') {
					data[i][j] = Integer.MAX_VALUE;
				}
			}
			i++;
			if (i >= h) {
				break;
			}
		}
		rows = root.elementIterator("npc");
		while (rows.hasNext()) {
			Element npc = rows.next();
			InterestedPoint p = new InterestedPoint();
			p.id = Integer.parseInt(npc.attributeValue("id"));
			p.x = Short.parseShort(npc.attributeValue("x"));
			p.y = Short.parseShort(npc.attributeValue("y"));
			p.name = npc.attributeValue("name") + "(" + p.x + "," + p.y + ")";
			targetPoints.add(p);
			// NPC可穿透
//			data[p.y][p.x] = Integer.MAX_VALUE;
		}
		rows = root.elementIterator("door");
		while (rows.hasNext()) {
			Element npc = rows.next();
			Entrices p = new Entrices();
			p.id = Integer.parseInt(npc.attributeValue("id"));
			p.x = Short.parseShort(npc.attributeValue("x"));
			p.y = Short.parseShort(npc.attributeValue("y"));
			p.targetMapId = Short.parseShort(npc.attributeValue("targetMap"));
			p.name = "去往" + npc.attributeValue("name") + "(" + p.x + "," + p.y + ")";
			p.tx = Integer.parseInt(npc.attributeValue("targetx"));
			p.ty = Integer.parseInt(npc.attributeValue("targety"));
			targetPoints.add(p);
			// 地图传送点不可作为中转点
			for (int dx = p.x - 1; dx <= p.x; dx++) {
				for (int dy = p.y + 1; dy >= p.y-1; dy--) {
					if (dx >= 0 && dy < h - 1 && dy > 0) {
						data[dy][dx] = Integer.MIN_VALUE;
					}
				}
			}
		}
		rows = root.elementIterator("teleport");
		while (rows.hasNext()) {
			Element npc = rows.next();
			Teleport p = new Teleport();
			p.id = 0;
			p.x = Short.parseShort(npc.attributeValue("x"));
			p.y = Short.parseShort(npc.attributeValue("y"));
			p.targetMapId = Short.parseShort(npc.attributeValue("targetMap"));
			p.name = "传送";
			p.tx = Integer.parseInt(npc.attributeValue("targetx"));
			p.ty = Integer.parseInt(npc.attributeValue("targety"));
			targetPoints.add(p);
		}
		rows = root.elementIterator("monster");
		while (rows.hasNext()) {
			Element npc = rows.next();
			InterestedPoint p = new InterestedPoint();
			p.id = Integer.parseInt(npc.attributeValue("id"));
			p.x = Short.parseShort(npc.attributeValue("x"));
			p.y = Short.parseShort(npc.attributeValue("y"));
			p.name = "怪:" + npc.attributeValue("name") + "(" + p.x + "," + p.y + ")";
			targetPoints.add(p);
		}
	}

	public void load(File file) {
		SAXReader reader = new SAXReader();
        try {
			Document document = reader.read(file);
			load(document);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<SimplePoint> getRoad(int x0, int y0, int x1, int y1) {
		long t = System.currentTimeMillis();
//		System.out.println("=" + x0 + "," + y0 + " - " + x1 + "," + y1);
		SimplePoint sp = getReachPoint(x0, y0, x1, y1);
		if (sp != null) {
			int ptx = (sp.x - x1)/tw;
			int pty = (sp.y - y1)/th;
			ptx *= ptx;
			pty *= pty;
			pty += ptx;
			if (pty < 3) {
				ArrayList<SimplePoint> ret = new ArrayList<SimplePoint>();
				sp.y += 4;
				ret.add(sp);
//				System.out.println("DeltaTime = " + (System.currentTimeMillis() - t));
				return ret;
			}
		}
		x0 = (x0 + 4) / tw;
		y0 /= th;
		x1 /= tw;
		y1 /= th;
		
		ArrayList<SimplePoint> ret = null;
		int w = this.w;
		int h = this.h;
		int map[][] = null;
		if (x0 >= 0 && x0 < w && y0 >= 0 && y0 < h && x1 >= 0 && x1 < w && y1 >= 0 && y1 < h) {
			map = new int[h][w];
			for (int i = h; i > 0; ) {
				i--;
				System.arraycopy(data[i], 0, map[i], 0, w);
			}
			// 强制玩家站的位置可通过 TODO 开放玩家周围
			map[y0][x0] = 0;

			map[y1][x1] = 1;
			boolean changed = true;
			int k = 0;
			while (changed) {
				k++;
				changed = false;
				// 找到起点
				if (map[y0][x0] != 0) {
					break;
				}
				for (int ii = h; ii > 0; ) {
					ii--;
					for (int jj = w; jj > 0; ) {
						jj--;
						if (map[ii][jj] == k) {
							if (ii > 0 && map[ii - 1][jj] <= 0) {
								map[ii - 1][jj] = k+1;
								changed = true;
							}
							if (ii < h - 1 && map[ii + 1][jj] <= 0) {
								map[ii + 1][jj] = k+1;
								changed = true;
							}
							if (jj > 0 && map[ii][jj - 1] <= 0) {
								map[ii][jj - 1] = k+1;
								changed = true;
							}
							if (jj < w - 1 && map[ii][jj + 1] <= 0) {
								map[ii][jj + 1] = k+1;
								changed = true;
							}
						}
					}
				}
			}
			if (map[y0][x0] != 0) { // found
				ret = new ArrayList<SimplePoint>();
				findPath(ret, map, x0, y0);
			}
		}
//		debug(ret, map);
//		System.out.println("DeltaTime = " + (System.currentTimeMillis() - t));
		return ret;
	}
	public void findPath(ArrayList<SimplePoint> path, int map[][], int x, int y) {
		int dir = 0;
		int k = map[y][x];
		int step = findLongPath(map, x, y, -1, 0);
		int xx = x + step - k;
		int yy = y;
		
		int n  = findLongPath(map, x, y, 1, 0);
		if (n < step) {
			step = n;
			xx = x - step + k;
		}
		n  = findLongPath(map, x, y, 0, -1);
		if (n < step) {
			step = n;
			xx = x;
			yy = y + step - k;
		}
		n  = findLongPath(map, x, y, 0, 1);
		if (n < step) {
			step = n;
			xx = x;
			yy = y - step + k;
		}
		// 避免停留在不可停留的点之上
		int altX = xx;
		int altY = yy;
		while (data[altY][altX] == Integer.MIN_VALUE && map[altY][altX] != 1) {
			boolean changed = false;
			if (altX < x) {
				altX ++;
				changed = true;
			} else if (altX > x) {
				altX --;
				changed = true;
			}
			if (altY < y) {
				altY ++;
				changed = true;
			} else if (altY > y) {
				altY --;
				changed = true;
			}
			if (!changed) { // 连续双位不能停留,忽略此点
				break; 
			}
		}
		if (altX != x || altY != y) { // 不重复发点
			path.add(new SimplePoint(altX * tw, altY * th + 4));
		}
		if (step > 1) {
			findPath(path, map, xx, yy);
		}
	}
	public int findLongPath(int map[][], int x, int y, int dx, int dy) {
		int w = this.w;
		int h = this.h;
		int k = map[y][x] - 1; // the target cell weight
		int origK = k;
		while (k > 0) {
			x += dx;
			y += dy;
			if (x < 0 || x >= w || y < 0 || y >= h || map[y][x] != k) {
				break;
			}
			k--;
		}
		return k + 1;
	}
//	String fixSize(String s, int len) {
//		s = "                   " + s;
//		return s.substring(s.length() - len);
//	}
//	public void debug(ArrayList<SimplePoint> ret, int [][]map) {
//		for (int i = 0; i < h; i++) {
//			System.out.print("\n" + fixSize("" + i, 2) + "   ");
//			for (int j = 0; j < w; j++) {
//				int k = map[i][j];
//				if (k == Integer.MAX_VALUE) {
//					k = 99;
//				}
//				System.out.print(" " + fixSize("0" + k, 2));
//			}
//		}
//		System.out.println();
//		if (ret != null) {
//			for (SimplePoint p: ret) {
//				System.out.println("=  " + p.x + "," + p.y + "    " + map[p.y][p.x]);
//			}
//		}
//	}
	/**
	 * 寻找直线可达最远点
	 */
	public SimplePoint getReachPoint(int x0, int y0, int x1, int y1) {
		int dx = x1 - x0;
		int dy = y1 - y0;
		int adx = Math.abs(dx);
		int ady = Math.abs(dy);
		if (adx >= ady) {
			int n = adx / tw;
			int dd = dx >= 0 ? tw : -tw;
			int xx = x0;
			int yy = y0 - (y0 % th);
			int nx = xx;
			int ny = yy;
			for (int i = 0; i < n; i++) {
				boolean canPass = canPass(nx, ny);
				ny += dy * tw / adx;
				if (ny != yy) {
					canPass |= canPass(nx, ny);
				}
				if (!canPass) {
					return new SimplePoint(xx, yy);
				}
				yy = ny;
				xx = nx;
				nx += dd;
			}
			return new SimplePoint(x1, y1);
		} else {
			int n = ady / th;
			int dd = dy >= 0 ? th : -th;
			int xx = x0 - (x0 % tw);
			int yy = y0;
			int nx = xx;
			int ny = yy;
			for (int i = 0; i < n; i++) {
				boolean canPass = canPass(nx, ny);
				nx += dx * th / ady;
				if (nx != xx) {
					canPass |= canPass(nx, ny);
				}
				if (!canPass) {
					return new SimplePoint(xx, yy);
				}
				yy = ny;
				xx = nx;
				ny += dd;
			}
			return new SimplePoint(x1, y1);
		}
	}
	public boolean canPass(int x, int y) {
		x /= tw;
		y /= th;
		return x >= 0 && x < w && y >= 0 && y < h && data[y][x] != Integer.MAX_VALUE;
	}

}
