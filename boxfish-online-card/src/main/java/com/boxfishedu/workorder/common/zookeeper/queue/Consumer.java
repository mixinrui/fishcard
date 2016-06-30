package com.boxfishedu.workorder.common.zookeeper.queue;

import java.io.IOException;
import java.util.List;

import com.boxfishedu.workorder.common.zookeeper.AppConstants;
import com.boxfishedu.workorder.common.zookeeper.SortUtil;
import com.boxfishedu.workorder.common.zookeeper.ZookeeperBean;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;


public class Consumer implements Watcher{

	private static ZooKeeper zk ; 
    static Integer mutex;
    Logger log = Logger.getLogger("zk") ;

	public byte[] consum(ZookeeperBean bean) throws IOException, InterruptedException, KeeperException{
		mutex = new Integer(-1) ;
		byte[] result = new byte[0] ;
		while(true){
			if(zk == null){
				zk = new ZooKeeper(AppConstants.ZOOKEEPER_HOSTS,40000,this) ;
			}
			synchronized (mutex) {
				List<String> elements = zk.getChildren(bean.getParentPath(), true) ;
				if(elements.size() == 0){
					try {
						mutex.wait(5000L) ;
					} catch (InterruptedException e) {
					}
				}else{
					try{
						String key = SortUtil.minString(elements, bean.getPathLength()) ;
						result = zk.getData(bean.getParentPath() + "/" + key, false, null) ;
						zk.delete(bean.getParentPath() + "/" + key, 0) ;
						return result ;
					}catch(KeeperException e){
						continue ;
					}
				}
			}
		}
	}

	public void process(WatchedEvent event) {
		synchronized (mutex) {
			mutex.notify() ;
		}
		
	}
}
