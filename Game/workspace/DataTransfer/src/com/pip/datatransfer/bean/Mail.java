package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_mail")
public class Mail implements Serializable {
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="sourceid",nullable=false)
	private int sourceid;

	@Column(name="sourcename",nullable=false)
    private String sourcename;

	@Column(name="destid",nullable=false)
    private int destid;

	@Column(name="destname",nullable=false)
    private String destname;
	
	@Column(name="title",nullable=false)
    private String title;
	
	@Column(name="content",nullable=true)
    private String content;
	
	@Column(name="attachment",nullable=true)
    private byte[] attachment;
	
	@Column(name="price",nullable=false)
    private int price;

	@Column(name="posttime",nullable=false)
    private Date posttime;
	
	@Column(name="readed",nullable=false)
    private boolean readed;

	@Column(name="validtime",nullable=false)
    private Date validtime;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getSourceid(){
        return sourceid;
    }

    public void setSourceid(int sourceid){
        this.sourceid = sourceid;
    }

    public String getSourcename(){
        return sourcename;
    }

    public void setSourcename(String sourcename){
        this.sourcename = sourcename;
    }

    public int getDestid(){
        return destid;
    }

    public void setDestid(int destid){
        this.destid = destid;
    }

    public String getDestname(){
        return destname;
    }

    public void setDestname(String destname){
        this.destname = destname;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public byte[] getAttachment(){
        return attachment;
    }

    public void setAttachment(byte[] attachment){
        this.attachment = attachment;
    }

    public int getPrice(){
        return price;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public Date getPosttime(){
        return posttime;
    }

    public void setPosttime(Date posttime){
        this.posttime = posttime;
    }

    public boolean isReaded(){
        return readed;
    }

    public void setReaded(boolean readed){
        this.readed = readed;
    }

    public Date getValidtime(){
        return validtime;
    }

    public void setValidtime(Date validtime){
        this.validtime = validtime;
    }
}
