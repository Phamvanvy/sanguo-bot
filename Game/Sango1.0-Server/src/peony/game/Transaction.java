package peony.game;


public interface Transaction {
	
	public static final int ACTIVE = 0;
	public static final int COMMITED = 1;
	public static final int ROLLEDBACK = 2;
	
	public boolean isActive();
	public boolean wasCommitted();
	public boolean wasRolledback();
	public void commit() throws TransactionException;
	public void rollback() throws TransactionException;
	public String getCause();
}
