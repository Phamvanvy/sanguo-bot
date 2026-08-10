package peony.vm;

import peony.game.GameQuest;
import peony.game.Player;

public class Quest001 extends AbstractASMQuest {
	

	public static final String MESSAGE = "ABCDEFG";
	public static final int INT = 100000;
	
	public Quest001(GameQuest quest){
		super(quest);
	}
	
	public int finishCondition(ASMGameVM vm){
		return 1;
	}
	
	public int preCondition(ASMGameVM vm){
		return 1;
	}
	public String getDesc(ASMGameVM vm){
		return null;
	}
	
	public String getUnFinishDesc(ASMGameVM vm){
		return null;
	}
	
	public void execute(ASMGameVM vm) {
		StringBuilder sb = new StringBuilder();
		sb.append("sdljsdkfs");
		Player p = vm.player;
		String s = p.name;
//		Player p = vm.player;
//		int i = id;
//		vm.taskFinished(100);
//		if(a()!=0){
//			b();
//		}else{
//			c();
//		}
//		if(player.level>10)
//			f();
//		setValue(1,1);
//		vm.taskFinished(100);
//		setValue();
//		if(a()==INT){
//			f();
//		}
//		if(a()==1&&INT!=1){
//			x(MESSAGE);
//		}
	}
	
	public void f(){
		int i = 1;
		while(i==1){
			;
		}
	}
	
	public int a(){
		if(b()==1)
			return 1;
		return 0;
	}
	
	public int b(){
		return 1;
	}
	
	public int c(){
		return 1;
	}
	
	public int d(){
		return 1;
	}
	
	public int x(String d){
		System.out.println(d);
		return 1;
	}
	
	public int y(){
		return INT==0?0:1;
	}
	
	public String x(ASMGameVM vm){
		StringBuilder sb = new StringBuilder();
		sb.append("ddss");
		sb.append(vm.player.name);
		return sb.toString();
	}
	
	public String getPostDesc(ASMGameVM vm) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPreDesc(ASMGameVM vm) {
		// TODO Auto-generated method stub
		return null;
	}

}
