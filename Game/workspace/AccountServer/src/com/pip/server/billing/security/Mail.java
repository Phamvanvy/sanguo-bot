package com.pip.server.billing.security;

public class Mail {
	
	protected String from;
	protected String to;
	protected String subject;
	protected String message;
	protected String html;
	
	public Mail(String from,String to,String subject,String message){
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.message = message;
	}
	
	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}
	
	public String getHTML() {
	    return html;
	}
	
	public void setHTML(String html) {
	    this.html = html;
	}
}
