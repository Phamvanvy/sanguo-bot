package peony.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import peony.game.Time;

public class SimpleTimer {
	
	protected List<Job> jobs = new ArrayList<Job>();
	
	public SimpleTimer(){
		
	}
	
	public void addJob(int time,Runnable runnable,int interval){
		if(time>=Time.currTime){
			jobs.add(new Job(time,runnable,interval));
		}else{
			int x = (Time.currTime - time) / interval;
			if((Time.currTime-time)%interval!=0){
				x++;
			}
			jobs.add(new Job(time+interval*x,runnable,interval));
		}
	}
	
	public void addJob(Date date,Runnable runnable,int interval){
		int t = Time.elapseTime(date.getTime());
		addJob(t,runnable,interval);
	}
	
	public void update(){
		if(jobs.size()>0){
			List<Job> ts = new ArrayList<Job>(jobs);
			for(Job job:ts){
				if(Time.currTime<=job.time){
					job.runnable.run();
					if(job.interval>0){
						jobs.add(new Job(job.time+job.interval,job.runnable,job.interval));
					}
				}
			}
		}
	}
}
class Job{
	int time;
	Runnable runnable;
	int interval;
	public Job(int time,Runnable runnable,int interval){
		this.time = time;
		this.runnable = runnable;
		this.interval = interval;
	}
}
