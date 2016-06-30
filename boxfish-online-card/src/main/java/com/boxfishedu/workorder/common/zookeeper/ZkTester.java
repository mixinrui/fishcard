package com.boxfishedu.workorder.common.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;


public class ZkTester {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		ZooKeeper zk = new ZooKeeper(AppConstants.ZOOKEEPER_HOSTS, 4000, new NullWatcher()) ;
		List<String> nodes = zk.getChildren("/comm/queue", false, null) ;
		System.out.println(nodes.size());
//		for(String node:nodes){
//			System.out.println(node);
//		}
	}
}
