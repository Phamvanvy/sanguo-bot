package pip.gm.fw;

public interface ArrayOfParameters {
	public String getParameterSetName(int n);
	public int getNumOfParameterSet();
	public int getNumOfParameters(int n);
	public String getParameterTitle(int n, int i);
	public String getParameterTips(int n, int i);
}
