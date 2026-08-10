package com.pipimage.image;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试把一组图片加到一个区域里。模拟opengl客户端拼接材质的算法。
 * @author lighthu
 */
public class MergeAreaTest {
	protected int width;
	protected int height;
	protected ArrayList<int[]> remainAreas = new ArrayList<int[]>();
	
	public MergeAreaTest(int w, int h) {
		width = w;
		height = h;
		remainAreas.add(new int[] { 0, 0, w, h });
	}
	
	/**
	 * 把一个新的图片加入到材质中去。
	 * @param bmp 图片对象
	 * @return 返回新图片在材质中的位置（x, y, w, h）。如果空间不够，返回null。
	 */
	public int[] addImage(int neww, int newh) {
		// 从现存的空闲区域表中查找一个浪费最少的组合进行插入。
		int minWaste = Integer.MAX_VALUE;
		int insertPoint = -1;
		for (int i = 0; i < remainAreas.size(); i++) {
			int[] info = useArea(i, neww, newh, false);
			if (info != null) {
				if (info[2] == 0) {
					// 没有浪费
					insertPoint = i;
					break;
				} else if (info[2] < minWaste) {
					minWaste = info[2];
					insertPoint = i;
				}
			}
		}
		if (insertPoint == -1) {
			return null;
		}
		int[] info = useArea(insertPoint, neww, newh, true);
		return new int[] { info[0], info[1], neww, newh };
	}
	
	/**
	 * 把一组新的图片加入到材质中去。
	 * @param bmps 图片对象
	 * @param pos 输出参数，存储给定的图片在材质中的存放位置(x,y,w,h)
	 * @return 如果空间足够，返回true，否则返回false。
	 */
	public boolean addImage(int[][] sizes, int[][] outPos) {
		// 按图片宽度从大到小顺序排列，先加大的
		int[] ids = new int[sizes.length];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = i;
		}
		for (int i = 0; i < ids.length - 1; i++) {
			for (int j = i + 1; j < ids.length; j++) {
				if (sizes[ids[i]][0] < sizes[ids[j]][0]) {
					int t = ids[i];
					ids[i] = ids[j];
					ids[j] = t;
				}
			}
		}
		
		for (int i = 0; i < ids.length; i++) {
			int[] pos = addImage(sizes[ids[i]][0], sizes[ids[i]][1]);
			if (pos == null) {
				return false;
			}
			if (outPos != null) {
				outPos[ids[i]] = pos;
			}
		}
		return true;
	}
	
	/**
	 * 把两个垂直方向上相邻的矩形进行合并，合并成1-3个水平方向上相邻的矩形。
	 * @param rect1 矩形1
	 * @param rect2 矩形2（必须在矩形1的右边，rect2.x >= rect1.x）
	 * @param output 保存合并后的矩形（1-3个）
	 * @return 合并后的矩形数量。
	 */
	private int mergeVertAreas(int[] rect1, int[] rect2, int[][] output) {
		int r1 = rect1[0] + rect1[2];
		int r2 = rect2[0] + rect2[2];
		if (rect1[1] < rect2[1]) {
			// rect2在右下方
			if (rect1[0] < rect2[0]) {
				// X没对齐，rect1左边留一块
				if (r1 < r2) {
					// rect2右边突出一块，总共分成3块：rect1左边一块，rect1和rect2中间合并一块，rect2右边一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[0] - rect1[0], rect1[3] };
					output[1] = new int[] { rect2[0], rect1[1], r1 - rect2[0], rect1[3] + rect2[3] };
					output[2] = new int[] { r1, rect2[1], r2 - r1, rect2[3] };
					return 3;
				} else if (r1 == r2) {
					// rect2和rect1右边对齐，总共分成2块：rect1左边一块，rect1和rect2中间合并一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[0] - rect1[0], rect1[3] };
					output[1] = new int[] { rect2[0], rect1[1], r1 - rect2[0], rect1[3] + rect2[3] };
					return 2;
				} else {
					// rect1右边突出一块，总共分成3块：rect1左边一块，rect1和rect2中间合并一块，rect1右边一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[0] - rect1[0], rect1[3] };
					output[1] = new int[] { rect2[0], rect1[1], r2 - rect2[0], rect1[3] + rect2[3] };
					output[2] = new int[] { r2, rect1[1], r1 - r2, rect1[3] };
					return 3;
				}
			} else {
				// X左边对齐
				if (r1 < r2) {
					// rect2右边突出一块，总共分成2块：rect1和rect2中间合并一块，rect2右边一块
					output[0] = new int[] { rect1[0], rect1[1], rect1[2], rect1[3] + rect2[3] };
					output[1] = new int[] { r1, rect2[1], r2 - r1, rect2[3] };
					return 2;
				} else if (r1 == r2) {
					// rect2和rect1左右对齐，合并成一块
					output[0] = new int[] { rect1[0], rect1[1], rect1[2], rect1[3] + rect2[3] };
					return 1;
				} else {
					// rect1右边突出一块，总共分成2块：rect1和rect2中间合并一块，rect1右边一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[2], rect1[3] + rect2[3] };
					output[1] = new int[] { r2, rect1[1], r1 - r2, rect1[3] };
					return 2;
				}
			}
		} else {
			// rect2在右上方
			if (rect1[0] < rect2[0]) {
				// X没对齐，rect1左边留一块
				if (r1 < r2) {
					// rect2右边突出一块，总共分成3块：rect1左边一块，rect1和rect2中间合并一块，rect2右边一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[0] - rect1[0], rect1[3] };
					output[1] = new int[] { rect2[0], rect2[1], r1 - rect2[0], rect1[3] + rect2[3] };
					output[2] = new int[] { r1, rect2[1], r2 - r1, rect2[3] };
					return 3;
				} else if (r1 == r2) {
					// rect2和rect1右边对齐，总共分成2块：rect1左边一块，rect1和rect2中间合并一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[0] - rect1[0], rect1[3] };
					output[1] = new int[] { rect2[0], rect2[1], r1 - rect2[0], rect1[3] + rect2[3] };
					return 2;
				} else {
					// rect1右边突出一块，总共分成3块：rect1左边一块，rect1和rect2中间合并一块，rect1右边一块
					output[0] = new int[] { rect1[0], rect1[1], rect2[0] - rect1[0], rect1[3] };
					output[1] = new int[] { rect2[0], rect2[1], r2 - rect2[0], rect1[3] + rect2[3] };
					output[2] = new int[] { r2, rect1[1], r1 - r2, rect1[3] };
					return 3;
				}
			} else {
				// X左边对齐
				if (r1 < r2) {
					// rect2右边突出一块，总共分成2块：rect1和rect2中间合并一块，rect2右边一块
					output[0] = new int[] { rect1[0], rect2[1], rect1[2], rect1[3] + rect2[3] };
					output[1] = new int[] { r1, rect2[1], r2 - r1, rect2[3] };
					return 2;
				} else if (r1 == r2) {
					// rect2和rect1左右对齐，合并成一块
					output[0] = new int[] { rect1[0], rect2[1], rect1[2], rect1[3] + rect2[3] };
					return 1;
				} else {
					// rect1右边突出一块，总共分成2块：rect1和rect2中间合并一块，rect1右边一块
					output[0] = new int[] { rect1[0], rect2[1], rect2[2], rect1[3] + rect2[3] };
					output[1] = new int[] { r2, rect1[1], r1 - r2, rect1[3] };
					return 2;
				}
			}
		}
	}
	
	/**
	 * 尝试把指定位置上的矩形和它前面或后面的矩形在水平方向上合并。只有相邻并且高度相等的矩形可以合并。
	 * @param index 要合并的矩形的索引
	 */
	private void tryMergeAreasHorz(int index) {
		int[] rect1 = remainAreas.get(index);
		
		// 合并后面的
		int r = rect1[0] + rect1[2];
		for (int i = index + 1; i < remainAreas.size(); i++) {
			int[] rect2 = remainAreas.get(i);
			if (rect2[0] == r) {
				if (rect2[1] == rect1[1] && rect2[3] == rect1[3]) {
					rect1[2] += rect2[2];
					remainAreas.remove(i);
					i--;
					break;
				}
			} else if (rect2[0] > r) {
				break;
			}
		}
		
		// 合并前面的
		for (int i = index - 1; i >= 0; i--) {
			int[] rect2 = remainAreas.get(i);
			if (rect2[0] + rect2[2] == rect1[0] && rect2[1] == rect1[1] && rect2[3] == rect1[3]) {
				rect2[2] += rect1[2];
				remainAreas.remove(index);
				index = i;
				break;
			}
		}
	}
	
	/**
	 * 尝试在垂直方向上合并X范围在指定范围内的矩形。只要垂直方向相邻的矩形都被合并，保证可用区域是连续的竖条。
	 * 如果在垂直方向上合并成功，本方法还顺便尝试在水平方向上合并相邻矩形。
	 * @param minx 改动的区域的最小起始X位置
	 * @param maxx 改动的区域的最大结束X位置
	 */
	private void tryMergeAreasVert(int minx, int maxx) {
		boolean changed = false;
		for (int i = 0; i < remainAreas.size() - 1; i++) {
			int[] rect1 = remainAreas.get(i);
			int r = rect1[0] + rect1[2];
			if (r <= minx) {
				continue;
			}
			if (rect1[0] >= maxx) {
				break;
			}
			for (int j = i + 1; j < remainAreas.size(); j++) {
				int[] rect2 = remainAreas.get(j);
				if (rect2[0] >= r) {
					break;
				}
				if (rect2[1] + rect2[3] == rect1[1] || rect1[1] + rect1[3] == rect2[1]) {
					// 后面的矩形和前面的矩形上下相邻，进行合并
					minx = Math.min(rect1[0], minx);
					maxx = Math.max(rect2[0] + rect2[2], maxx);

					int[][] mergeBuffer = new int[3][];
					int count = mergeVertAreas(rect1, rect2, mergeBuffer);
					remainAreas.remove(j);
					System.arraycopy(mergeBuffer[0], 0, rect1, 0, 4);
					for (int k = 1; k < count; k++) {
						addArea(mergeBuffer[k], j);
					}
					changed = true;
					i--;
					break;
				}
			}
		}
		if (changed) {
			// 如果有变化，再全部搜索一遍是否需要水平合并
			for (int i = 0; i < remainAreas.size() - 1; i++) {
				int[] rect1 = remainAreas.get(i);
				int r = rect1[0] + rect1[2];
				for (int j = i + 1; j < remainAreas.size(); j++) {
					int[] rect2 = remainAreas.get(j);
					if (rect2[0] > r) {
						break;
					} else if (rect2[0] == r && rect2[1] == rect1[1] && rect2[3] == rect1[3]) {
						// 正好在右方，Y和H一致，进行合并
						rect1[2] += rect2[2];
						r += rect2[2];
						remainAreas.remove(j);
						j--;
					}
				}
			}
		}
	}
	
	/**
	 * 比较两个矩形的位置顺序。在空闲矩形表中，矩形按照X从小到大，Y从小到大的顺序排列。
	 * @param rect1
	 * @param rect2
	 * @return 如果rect1排在rect2前面，返回-1；如果想等范围0；如果rect1排在rect2后面，返回1。
	 */
	private int compareRect(int[] rect1, int[] rect2) {
		if (rect1[0] < rect2[0]) {
			return -1;
		} else if (rect1[0] == rect2[0]) {
			if (rect1[1] < rect2[1]) {
				return -1;
			} else if (rect1[1] == rect2[1]) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}
	
	/**
	 * 在指定位置加入一个空闲矩形。如果要加入的矩形应该排在给定位置现有的矩形后面，那么实际插入的位置会在给定的位置后面。
	 * @param rect 新空闲矩形
	 * @param insertPos 参考位置，新矩形不会在这个位置之前插入（优化速度）
	 * @return 实际插入的位置。
	 */
	private int addArea(int[] rect, int insertPos) {
		while (insertPos < remainAreas.size() && compareRect(rect, remainAreas.get(insertPos)) > 0) {
			insertPos++;
		}
		remainAreas.add(insertPos, rect);
		return insertPos;
	}
	
	/**
	 * 尝试把指定位置的矩形和后面相邻的矩形进行合并，以储存一个比较大的图块。
	 * @param rect 指定的矩形（这个矩形的宽度必然比要加入的图块小，但高度足够）
	 * @param startPos 指定的矩形在remainAreas中的位置
	 * @param needWidth 图块宽度
	 * @param needHeight 图块高度
	 * @return 如果找到一个合并方案可以存放新的图块，返回{ Y位置，startPos，第二个矩形索引，第三个矩形索引... }；如果没有可用的合并方案，返回null。
	 */
	private int[] findMergePlan(int[] rect, int startPos, int needWidth, int needHeight) {
		List<Integer> indList = new ArrayList<Integer>();
		indList.add(startPos);
		int startY = rect[1];
		int endY = rect[1] + rect[3];
		needWidth -= rect[2];
		int endX = rect[0] + rect[2];
		startPos++;
		while (startPos < remainAreas.size()) {
			int[] rect2 = remainAreas.get(startPos);
			if (rect2[0] == endX) {
				int newSY = Math.max(startY, rect2[1]);
				int newEY = Math.min(endY, rect2[1] + rect2[3]);
				if (newEY - newSY >= needHeight) {
					startY = newSY;
					endY = newEY;
					indList.add(startPos);
					needWidth -= rect2[2];
					if (needWidth <= 0) {
						// 宽度够了，返回
						int[] ret = new int[indList.size() + 1];
						ret[0] = startY;
						for (int i = 0; i < indList.size(); i++) {
							ret[i + 1] = indList.get(i);
						}
						return ret;
					} else {
						endX += rect2[2];
					}
				}
			} else if (rect2[0] > endX) {
				break;
			}
			startPos++;
		}
		return null;
	}
	
	/**
	 * 尝试把指定大小的图片加入到指定索引的空闲区域。
	 * @param index 空闲区域索引
	 * @param w 宽度
	 * @param h 高度
	 * @param apply 是否实际执行加入操作（修改空闲区域表）
	 * @return 返回插入位置x，y，以及插入导致的浪费区域大小（被截断无法再使用或者宽度小于40的区域）
	 */
	private int[] useArea(int index, int w, int h, boolean apply) {
		int[] rect1 = remainAreas.get(index);
		if (rect1[3] < h) {
			// 高度不够，放弃尝试
			return null;
		}
		int[] ret = new int[3];
		if (rect1[2] >= w) {
			// 宽度够，直接拆分本区域
			ret[0] = rect1[0];
			ret[1] = rect1[1];
			if (rect1[2] - w < 40) {
				// 有一点浪费
				ret[2] = (rect1[2] - w) * h;
			} else {
				ret[2] = 0;
			}
			if (apply) {
				// 修改剩余空闲区域表
				if (rect1[2] == w) {
					rect1[1] += h;
					rect1[3] -= h;
				} else {
					int[] newRect = new int[] { rect1[0] + w, rect1[1], rect1[2] - w, rect1[3] };
					rect1[1] += h;
					rect1[3] -= h;
					rect1[2] = w;
					
					// 插入新的矩形（保证排序）
					addArea(newRect, index + 1);
				}
				if (rect1[3] == 0) {
					remainAreas.remove(index);
				} else {
					tryMergeAreasHorz(index);
				}
			}
			return ret;
		}
		
		// 宽度不够，尝试和邻近的区域合并
		int[] inds = findMergePlan(rect1, index, w, h);
		if (inds == null) {
			// 合并了还是不够，放弃
			return null;
		}
		
		// 宽度凑够了，拆分所有区域计算浪费
		ret[0] = rect1[0];
		ret[1] = inds[0];
		ret[2] = 0;
		for (int i = 1; i < inds.length; i++) {
			int[] r = remainAreas.get(inds[i]);
			int takew = Math.min(ret[0] + w - r[0], r[2]);
			if (inds[0] - r[1] < 40 || (r[2] >= 40 && takew < 40)) {
				// 上方有浪费
				ret[2] += takew * (inds[0] - r[1]);
			}
			if (r[1] + r[3] - inds[0] - h < 40 || (r[2] >= 40 && takew < 40)) {
				// 下方有浪费
				ret[2] += takew * (r[1] + r[3] - inds[0] - h);
			}
			if (i == inds.length - 1) {
				// 右边最后一块，右边可能有浪费
				if (r[2] - takew < 40) {
					ret[2] += r[3] * (r[2] - takew);
				}
			}
		}
		if (apply) {
			int[] needMerge = new int[inds.length * 2];
			int needMergeCount = 0;
			for (int i = inds.length - 1; i >= 1; i--) {
				int[] r = remainAreas.get(inds[i]);
				int takew = Math.min(ret[0] + w - r[0], r[2]);
				if (i == inds.length - 1) {
					// 最后一块右边留有空余
					if (r[2] - takew > 0) {
						int[] newRect = new int[] { r[0] + takew, r[1], r[2] - takew, r[3] };
						addArea(newRect, inds[i] + 1);
					}
				}
				if (inds[0] > r[1] && r[1] + r[3] > inds[0] + h) {
					// 上下方都有浪费
					int[] newRect = new int[] { r[0], inds[0] + h, takew, r[1] + r[3] - inds[0] - h };
					r[2] = takew;
					r[3] = inds[0] - r[1];
					remainAreas.add(inds[i] + 1, newRect);
					for (int j = 0; j < needMergeCount; j++) {
						needMerge[j]++;
					}
					needMerge[needMergeCount++] = inds[i] + 1;
					needMerge[needMergeCount++] = inds[i];
				} else if (inds[0] > r[1]) {
					// 只上方有浪费
					r[2] = takew;
					r[3] = inds[0] - r[1];
					needMerge[needMergeCount++] = inds[i];
				} else if (r[1] + r[3] > inds[0] + h) {
					// 只下方有浪费
					r[2] = takew;
					r[3] = r[1] + r[3] - inds[0] - h;
					r[1] = inds[0] + h;
					needMerge[needMergeCount++] = inds[i];
				} else {
					// 占满，删除
					remainAreas.remove(inds[i]);
					for (int j = 0; j < needMergeCount; j++) {
						needMerge[j]--;
					}
				}
			}
			
			// 合并所有需要合并的矩形
			for (int i = 0; i < needMergeCount; i++) {
				tryMergeAreasHorz(needMerge[i]);
			}
		}
		
		return ret;
	}
	
	/**
	 * 释放一个矩形空间。
	 * @param area
	 */
	public void releaseArea(int[] area) {
		addArea(area, 0);
		tryMergeAreasVert(area[0], area[0] + area[2]);
	}
	
	/**
	 * 释放多个矩形空间。
	 * @param area
	 */
	public void releaseAreas(int[][] area) {
		int minx = width;
		int maxx = 0;
		for (int i = 0; i < area.length; i++) {
			addArea(area[i], 0);
			if (area[i][0] < minx) {
				minx = area[i][0];
			}
			if (area[i][0] + area[i][2] > maxx) {
				maxx = area[i][0] + area[i][2];
			}
		}
		tryMergeAreasVert(minx, maxx);
	}
	
	public void addHeight(int value) {
		height += value;
		releaseArea(new int[] { 0, height - value, width, value });
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
