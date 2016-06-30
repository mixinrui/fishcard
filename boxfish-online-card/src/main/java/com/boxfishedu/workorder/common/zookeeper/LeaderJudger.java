package com.boxfishedu.workorder.common.zookeeper;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


public class LeaderJudger {

	private static ZooKeeper zk ;
	
	private static String leaderPath = "" ;
	
	private static Logger log = Logger.getLogger("zk") ;

	public static boolean isLeader(ZookeeperBean bean) throws Exception{
		makeSureParentNodeExists(bean) ;
		String localAddress = "" ;
		try{
			InetAddress inet = NetUtil.getLocalAddress0() ;
			localAddress = inet.getHostAddress() ;
		}catch(Exception e){
		}
		if(leaderPath == null || "".equals(leaderPath)){
			leaderPath = zk.create(bean.getPath(), localAddress.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL) ;
		}
		List<String> children = zk.getChildren(bean.getParentPath(), false) ;
		if(children == null || children.size() == 0){
			leaderPath = zk.create(bean.getPath(), localAddress.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL) ;
			children = zk.getChildren(bean.getParentPath(), false) ;
		}
		String minChild = SortUtil.minString(children, bean.getPathLength()) ;
		if((bean.getParentPath() + "/" + minChild).equals(leaderPath)){
			return true ;
		}
		return false ;
	}

	public static void makeSureParentNodeExists(ZookeeperBean bean) throws IOException, KeeperException, InterruptedException{
		if(zk == null){
			zk = new ZooKeeper(AppConstants.ZOOKEEPER_HOSTS, 40000,new NullWatcher()) ;
		}
		Stat s = zk.exists(bean.getParentPath(), false);
		if(s == null){
			zk.create(bean.getParentPath(), new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
		}
	}

	public static int getZKChildNodeCount(String strParentNodePath) throws KeeperException, InterruptedException, IOException{
		if(zk == null){
			zk = new ZooKeeper(AppConstants.ZOOKEEPER_HOSTS, 40000,new NullWatcher()) ;
		}
		List<String> nodes = zk.getChildren(strParentNodePath, false, null) ;
		
		return null == nodes ? 0 : nodes.size();
	}
}
