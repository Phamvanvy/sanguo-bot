package com.pip.server.billing.f3g;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 服务器配置信息（畅游）。
 * @author lighthu
 */
@Entity
@Table(name = "tbl_server")
public class ServerInfo {
    /** ID */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private int id;
    /** 区号 */
    @Column(name="regionid",nullable=false)
    private int regionID;
    /** 主机名 */
    @Column(name="host",nullable=false)
    private String host;
    /** 世界服务器主机名 */
    @Column(name="worldhost",nullable=false)
    private String worldHost;
	/** 服务器端口号 */
    @Column(name="port",nullable=false)
    private int port;
    /** 服务器HTTP端口号 */
    @Column(name="httpport",nullable=false)
    private int httpPort;
    /** 充值页面入口 */
    @Column(name="payurl",nullable=false)
    private String payURL;
    /** 客户端资源地址 */
    @Column(name="resourceurl",nullable=false)
    private String resourceURL;
    /** 标题 */
    @Column(name="title",nullable=false)
    private String title;
    /** 页面标题 */
    @Column(name="pagetitle",nullable=false)
    private String pageTitle;

    public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRegionID() {
		return regionID;
	}
	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getHttpPort() {
		return httpPort;
	}
	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
	public String getPayURL() {
		return payURL;
	}
	public void setPayURL(String payURL) {
		this.payURL = payURL;
	}
	public String getResourceURL() {
		return resourceURL;
	}
	public void setResourceURL(String resourceURL) {
		this.resourceURL = resourceURL;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    public String getWorldHost() {
		return worldHost;
	}
	public void setWorldHost(String worldHost) {
		this.worldHost = worldHost;
	}
}
