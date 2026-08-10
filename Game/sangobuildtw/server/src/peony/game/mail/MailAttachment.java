package peony.game.mail;

public interface MailAttachment {
	public byte[] toClientBytes();
	public MailAttachment clone();
}
