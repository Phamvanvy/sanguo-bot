package peony.util;

public class Reg {
	
	private TestService testService;
	
	public Reg(){
		this.testService = new TestService();
	}
	
	public TestService getTestService(){
		return testService;
	}
}
