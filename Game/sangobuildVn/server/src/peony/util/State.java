package peony.util;

public interface State {
	public int getId();
	public void update();
	public void enter();
	public void exit();
}
