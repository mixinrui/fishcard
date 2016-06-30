package com.boxfishedu.workorder.common.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class SortUtil {

	public static long minValue(List<Long> list){
		long min = 0 ;
		if(list != null){
			min = list.get(0) ;
			for(long value : list){
				if(value < min){
					min = value ;
				}
			}
			
		}
		return min ;
	}

	public static long maxValue(List<Long> list){
		long max = 0 ;
		if(list != null){
			max = list.get(0) ;
			for(long value : list){
				if(value > max){
					max = value ;
				}
			}
			
		}
		return max ;
	}

	public static String minString(List<String> list,int prefixLength){
		String minString = "" ;
		if(list != null && list.size() > 0){
			long min = 0 ;
			min = Long.valueOf(list.get(0).substring(prefixLength)) ;
			minString = list.get(0) ;
			for(String value : list){
				long val = Long.valueOf(value.substring(prefixLength)) ;
				if(val < min){
					min = val ;
					minString = value ;
				}
			}
			
		}
		return minString ;
	}

	public static String maxString(List<String> list,int prefixLength){
		long max = 0 ;
		String maxString = "" ;
		if(list != null && list.size() > 0){
			max = Long.valueOf(list.get(0).substring(prefixLength + 1)) ;
			for(String value : list){
				long val = Long.valueOf(value.substring(prefixLength + 1)) ;
				if(val > max){
					max = val ;
					maxString = value ;
				}
			}
			
		}
		return maxString ;
	}
	
}
