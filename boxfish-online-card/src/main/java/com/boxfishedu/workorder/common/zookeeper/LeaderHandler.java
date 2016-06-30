package com.boxfishedu.workorder.common.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


public class LeaderHandler implements Watcher{
	static ZooKeeper zk = null;
	static Integer mutex ;
	private ZookeeperBean bean = null ;
	private boolean isLeader = false ;
	private LeaderNotify runable ;

	public void leaderRun(ZookeeperBean bean,LeaderNotify runable) throws Exception{
		this.bean = bean ;
		this.runable = runable ;
		makeSureParentNodeExists(bean) ;
		mutex = new Integer(-1) ;
		zk.create(bean.getPath(), bean.getValue().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL) ;
		electLeader() ;
		if(isLeader){
			runable.run() ;
		}
		
		
	}

	private void electLeader() throws Exception{
		List<String> children = zk.getChildren(bean.getParentPath(), true) ;
		if(children != null && children.size() > 0){
//			String minString = SortUtil.minString(children,bean.getPathLength()) ;
			String minString = StringUtils.EMPTY;
			String leaderHost = new String(zk.getData(bean.getParentPath() + "/" + minString, false, null)) ;
			if(leaderHost.equals(bean.getValue())){
				System.out.println("I'm the leader this time");
				isLeader = true ;
			}else{
				System.out.println("I'm not the leader this time");
				isLeader = false ;
				runable.unLeader() ;
			}
		}
	}

	public void process(WatchedEvent event) {
		try{
			if(!isLeader){
				electLeader() ;
			}
		}catch(Exception e){
		}
		
	}

	public void makeSureParentNodeExists(ZookeeperBean bean) throws IOException, KeeperException, InterruptedException{
		if(zk == null){
			zk = new ZooKeeper("127.0.0.1", bean.getSessionTimeout(),this) ;
		}
		Stat s = zk.exists(bean.getParentPath(), false);
		if(s == null){
			zk.create(bean.getParentPath(), new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
		}
	}
}
