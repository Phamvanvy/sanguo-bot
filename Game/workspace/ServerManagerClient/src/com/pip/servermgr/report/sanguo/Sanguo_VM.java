package com.pip.servermgr.report.sanguo;

import java.io.*;
import java.util.*;

public class Sanguo_VM {
	public Set<Integer> pending = new HashSet<Integer>();
	public Set<Integer> removing = new HashSet<Integer>();
	public List<Integer> finished = new ArrayList<Integer>();
	public List<Long> finishedTime = new ArrayList<Long>();
	public Set<Integer> current = new HashSet<Integer>();
	public Set<Integer> failed = new HashSet<Integer>();
	
	public static Sanguo_VM parse(byte[] bytes) throws IOException {
		Sanguo_VM ret = new Sanguo_VM();
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		byte version = dis.readByte(); // version
		if (version == 1) {
			fromDBBytes1(dis, ret);
		} else if (version == 2) {
			fromDBBytes2(dis, ret);
		} else if (version == 3) {
			fromDBBytes3(dis, ret);
		} else if (version == 4) {
			fromDBBytes4(dis, ret);
		} else if (version == 5){
			fromDBBytes5(dis, ret);
		} else if (version == 6){
			fromDBBytes6(dis, ret);
		}
		return ret;
	}

	protected static void fromDBBytes1(DataInputStream dis, Sanguo_VM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.finished.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.current.add(dis.readInt());
		}
	}

	protected static void fromDBBytes2(DataInputStream dis, Sanguo_VM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.finished.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.current.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
	}

	protected static void fromDBBytes3(DataInputStream dis, Sanguo_VM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.finished.add(dis.readInt());
			vm.finishedTime.add(dis.readLong());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.current.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
	}
	
	
	protected static void fromDBBytes4(DataInputStream dis, Sanguo_VM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.finished.add(dis.readInt());
			vm.finishedTime.add(dis.readLong());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.current.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
	}

	protected static void fromDBBytes5(DataInputStream dis, Sanguo_VM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.finished.add(dis.readInt());
			vm.finishedTime.add(dis.readLong());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.current.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
	}
	
	protected static void fromDBBytes6(DataInputStream dis, Sanguo_VM vm)
	throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.finished.add(dis.readInt());
			vm.finishedTime.add(dis.readLong());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.current.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
	}
}
