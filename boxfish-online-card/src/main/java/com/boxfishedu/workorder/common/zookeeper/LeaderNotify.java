package com.boxfishedu.workorder.common.zookeeper;

public interface LeaderNotify {
	public void run(Object... obj) throws Exception;

	public void onLeader(Object... obj) throws Exception ;
	
	public void unLeader(Object... obj) throws Exception ;
}
