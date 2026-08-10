package peony.game;

@SuppressWarnings("serial")
public class SequenceException extends Exception {
	
	
	public SequenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SequenceException(String msg){
		this(msg,null);
	}
}
