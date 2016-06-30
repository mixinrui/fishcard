package com.boxfishedu.workorder.common.zookeeper;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

public class ZookeeperBean {

	private String path ;
	private String value ;
	private int sessionTimeout ;
	private int count ;
	private List<ACL> acl ;
	private CreateMode createModel ;
	private boolean leader ;
	public ZookeeperBean() {
		super();
	}
	public ZookeeperBean(String path, String value,int count,int sessionTimeout) {
		super();
		this.count = count;
		this.path = path;
		this.value = value;
		this.sessionTimeout = sessionTimeout;
	}
	public ZookeeperBean(String path, String value,int count,int sessionTimeout,boolean leader) {
		super();
		this.count = count;
		this.path = path;
		this.value = value;
		this.sessionTimeout = sessionTimeout;
		this.leader = leader ;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<ACL> getAcl() {
		return acl;
	}
	public void setAcl(List<ACL> acl) {
		this.acl = acl;
	}
	public CreateMode getCreateModel() {
		return createModel;
	}
	public void setCreateModel(CreateMode createModel) {
		this.createModel = createModel;
	}
	public String getParentPath() {
		return path.substring(0,path.lastIndexOf("/"));
	}
	public int getPathLength() {
		return path.substring(path.lastIndexOf("/") + 1).length();
	}
	public boolean isLeader() {
		return leader;
	}
	public void setLeader(boolean leader) {
		this.leader = leader;
	}
	
}
