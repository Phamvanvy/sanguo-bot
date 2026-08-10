package com.pip.servermgr.report.xuanyuan;

import java.io.*;
import java.util.*;

public class Xuanyuan_VM {
	public Set<Integer> pending = new HashSet<Integer>();
	public Set<Integer> removing = new HashSet<Integer>();
	public Set<Integer> finished = new HashSet<Integer>();
	public Set<Integer> current = new HashSet<Integer>();
	public Set<Integer> failed = new HashSet<Integer>();
	
	public static Xuanyuan_VM parse(byte[] bytes) throws IOException {
		Xuanyuan_VM ret = new Xuanyuan_VM();
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		byte version = dis.readByte(); // version
		fromDBBytes(dis, ret);
		return ret;
	}

	protected static void fromDBBytes(DataInputStream dis, Xuanyuan_VM vm) throws IOException {
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
			int qid = dis.readInt();
			dis.readLong();
			vm.finished.add(qid);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.current.add(id);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
	}
}
