package com.boxfishedu.workorder.common.zookeeper;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetUtil {
	public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";

    private static Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

     public static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                	e.printStackTrace();
                                }
                            }
                        }
                    } catch (Throwable e) {
                    	e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {
        	e.printStackTrace();
        }
        //logger.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }
     
     
     private static boolean isValidAddress(InetAddress address) {
         if (address == null || address.isLoopbackAddress())
             return false;
         String name = address.getHostAddress();
         return (name != null 
                 && ! ANYHOST.equals(name)
                 && ! LOCALHOST.equals(name) 
                 && IP_PATTERN.matcher(name).matches());
     }
     
}
