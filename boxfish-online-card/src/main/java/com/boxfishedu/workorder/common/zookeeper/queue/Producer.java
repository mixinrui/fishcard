package com.boxfishedu.workorder.common.zookeeper.queue;

import java.io.IOException;

import com.boxfishedu.workorder.common.zookeeper.AppConstants;
import com.boxfishedu.workorder.common.zookeeper.ZookeeperBean;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class Producer implements Watcher{
	
	static ZooKeeper zk ;
	static ZookeeperBean bean ;

	public boolean produce(ZookeeperBean bean) throws IOException, KeeperException, InterruptedException{
		Producer.bean = bean ;
		if(zk == null){
			zk = new ZooKeeper(AppConstants.ZOOKEEPER_HOSTS, 40000, this) ;
		}
		Stat s = zk.exists(bean.getParentPath(), false) ;
		if(s == null){
			zk.create(bean.getParentPath(), new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
		}
		String path = zk.create(bean.getPath(), bean.getValue().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL) ;
		if(path != null && !"".equals(path)){
			return true ;
		}
		
		return false ;
	}
	
	
	public void process(WatchedEvent event) {}
}
