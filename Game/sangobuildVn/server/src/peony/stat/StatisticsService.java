package peony.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import peony.service.Service;

public class StatisticsService implements Service {

	public volatile boolean running = true;
	
	protected List<CycleStatistics> cycles = new Vector<CycleStatistics>(100);
	
	protected List<WorldStatistics> worlds = new Vector<WorldStatistics>(100);
	
	protected List<NIMapManagerStatistics> nis = new Vector<NIMapManagerStatistics>(100);
	
	protected List<NMMapManagerStatistics> nms = new Vector<NMMapManagerStatistics>(100);
	
	public void addCycleStatistics(CycleStatistics cycleStat){
		cycles.add(cycleStat);
		if(cycles.size()>=100){
			if(running){
//				printCycleStatistics();
			}
			cycles.clear();
		}
	}
	
	protected void printCycleStatistics() {
		long maxWorldTime = 0L;
		long avgWorldTime = 0L;
		long maxEventTime = 0L;
		long avgEventTime = 0L;
		long maxPkTime = 0L;
		long avgPkTime = 0L;
		long maxRollTime = 0L;
		long avgRollTime = 0L;
		long maxCycleTime = 0L;
		long avgCycleTime = 0L;
		for (CycleStatistics stat : cycles) {
			if (stat.cycleWorldTime > maxWorldTime) {
				maxWorldTime = stat.cycleWorldTime;
			}
			avgWorldTime += stat.cycleWorldTime;
			if (stat.cycleEventTime > maxEventTime) {
				maxEventTime = stat.cycleEventTime;
			}
			avgEventTime += stat.cycleEventTime;
			if (stat.cyclePkTime > maxPkTime) {
				maxPkTime = stat.cyclePkTime;
			}
			avgPkTime += stat.cyclePkTime;
			if (stat.cycleRollTime > maxRollTime) {
				maxRollTime = stat.cycleRollTime;
			}
			avgRollTime += stat.cycleRollTime;
			if (stat.cycleTime > maxCycleTime) {
				maxCycleTime = stat.cycleTime;
			}
			avgCycleTime += stat.cycleTime;
		}
		avgWorldTime /= 100;
		avgEventTime /= 100;
		avgPkTime /= 100;
		avgRollTime /= 100;
		avgCycleTime /= 100;
		StringBuilder sb = new StringBuilder();
		sb.append("CycleStastics[maxWorldTime=").append(maxWorldTime).append(
				",avgWorldTime=").append(avgWorldTime).append(",maxEventTime=")
				.append(maxEventTime).append(",avgEventTime=").append(
						avgEventTime).append(",maxPkTime=").append(maxPkTime)
				.append(",avgPkTime=").append(avgPkTime)
				.append(",maxRollTime=").append(maxRollTime).append(
						",avgRollTime=").append(avgRollTime).append(
						",maxCycleTime=").append(maxCycleTime).append(
						",avgCycleTime=").append(avgCycleTime).append("]");
		System.out.println(sb.toString());
	}
	
	public void addWorldStatistics(WorldStatistics stat){

		worlds.add(stat);
		if(worlds.size()>=100){
			if(running){
//				printWorldStatistics();
			}
			worlds.clear();
		}
	}
	
	protected void printWorldStatistics() {
		long maxSessionTime = 0L;
		long avgSessionTime = 0L;
		long maxWorldTime = 0L;
		long avgWorldTime = 0L;
		for (WorldStatistics stat : worlds) {
			if (stat.sessionCycleTime > maxSessionTime) {
				maxSessionTime = stat.sessionCycleTime;
			}
			avgSessionTime += stat.sessionCycleTime;
			if (stat.worldManagerCycleTime > maxWorldTime) {
				maxWorldTime = stat.worldManagerCycleTime;
			}
			avgWorldTime += stat.worldManagerCycleTime;
		}
		avgSessionTime /= 100;
		avgWorldTime /= 100;
		StringBuilder sb = new StringBuilder();
		sb.append("WorldStatistics[").append("maxSessionTime=").append(
				maxSessionTime).append(",avgSessionTime=").append(
				avgSessionTime).append(",maxWorldTime=").append(maxWorldTime)
				.append(",avgWorldTime=").append(avgWorldTime).append("]");
		System.out.println(sb.toString());
	}
	
	public void addNIMapManagerStatistics(NIMapManagerStatistics stat){
		nis.add(stat);
		if(nis.size()>=100){
			if(running){
//				printNIStatistics();
			}
			nis.clear();
		}
	}
	
	protected void printNIStatistics() {
		long maxCycleTime = 0L;
		long avgCycleTime = 0L;
		for (NIMapManagerStatistics stat : nis) {
			if (stat.mapCycleTime > maxCycleTime) {
				maxCycleTime = stat.mapCycleTime;
			}
			avgCycleTime += stat.mapCycleTime;
		}
		avgCycleTime /= 100;
		StringBuilder sb = new StringBuilder();
		sb.append("NIStatistics[").append("maxCycleTime=").append(maxCycleTime)
				.append(",avgCycleTime=").append(avgCycleTime).append("]");
		System.out.println(sb.toString());
	}
	
	public void addNMMapManagerStatistics(NMMapManagerStatistics stat){
		nms.add(stat);
		if(nms.size()>=100){
			if(running){
//				printNMStatistics();
			}
			nms.clear();
		}
	}
	
	protected void printNMStatistics(){
		long maxCycleTime = 0L;
		long avgCycleTime = 0L;
		for (NMMapManagerStatistics stat : nms) {
			if (stat.mapCycleTime > maxCycleTime) {
				maxCycleTime = stat.mapCycleTime;
			}
			avgCycleTime += stat.mapCycleTime;
		}
		avgCycleTime /= 100;
		StringBuilder sb = new StringBuilder();
		sb.append("NMStatistics[").append("maxCycleTime=").append(maxCycleTime)
				.append(",avgCycleTime=").append(avgCycleTime).append("]");
		System.out.println(sb.toString());
	}
	
	public void shutdown() {

	}

	public void startup() throws Exception {

	}

	
}
