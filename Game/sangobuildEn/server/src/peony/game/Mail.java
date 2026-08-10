package peony.game;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import peony.game.mail.MailAttachment;

@Entity
@Table(name = "mail")
public class Mail {
	public static final int UNREADED = 0;
	public static final int READED = 1;
	public static final int UNREADED_FAVORITE = 1<<1;
	public static final int READED_FAVORITE = (1<<1)+1;
	
	private static final byte[] EMPTY_ATTACHMENT = new byte[0];
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	protected int id;
	@Column(name="sourceid",nullable=false)
	protected int sourceId;
	@Column(name="sourcename",nullable=false)
	protected String sourceName;
	@Column(name="destid",nullable=false)
	protected int destId;
	@Column(name="title",nullable=false)
	protected String title;
	@Column(name="content",nullable=false)
	protected String content;
	@Column(name="attachment")
	@Type(type="peony.game.MailAttachmentUserType")
	protected MailAttachment attachment;
	@Column(name="price",nullable=false)
	protected int price;
	@Column(name="posttime",nullable=false)
	protected Date postTime;
	@Column(name="status",nullable=false)
	protected int status;
	@Column(name="validtime",nullable=false)
	protected Date validTime;
	@Column(name="expirationtime",nullable=false)
	protected Date expirationTime;
	
	@Transient
	public String cause;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public int getDestId() {
		return destId;
	}
	public void setDestId(int destId) {
		this.destId = destId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public MailAttachment getAttachment() {
		return attachment;
	}
	public void setAttachment(MailAttachment attachment) {
		this.attachment = attachment;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public Date getPostTime() {
		return postTime;
	}
	public void setPostTime(Date postTime) {
		this.postTime = postTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getValidTime() {
		return validTime;
	}
	public void setValidTime(Date validTime) {
		this.validTime = validTime;
	}
	public Date getExpirationTime(){
		return expirationTime;
	}
	public void setExpirationTime(Date expirationTime){
		this.expirationTime=expirationTime;
	}
	
	
	public byte[] getAttachmentClientBytes(){
		if(attachment!=null)
			return attachment.toClientBytes();
		else
			return EMPTY_ATTACHMENT;
	}
}
