package com.pip.itimes.server.world.rabbitRace;

public class RabbitLocData {
	private int locFir;
	private int locSec;
	private int locThi;

	public RabbitLocData(int firLoc, int secLoc, int thiLoc) {
		this.locFir = firLoc;
		this.locSec = secLoc;
		this.locThi = thiLoc;
	}

	public RabbitLocData() {

	}

	public int getLocFir() {
		return locFir;
	}

	public void setLocFir(int locFir) {
		this.locFir = locFir;
	}

	public int getLocSec() {
		return locSec;
	}

	public void setLocSec(int locSec) {
		this.locSec = locSec;
	}

	public int getLocThi() {
		return locThi;
	}

	public void setLocThi(int locThi) {
		this.locThi = locThi;
	}

}
