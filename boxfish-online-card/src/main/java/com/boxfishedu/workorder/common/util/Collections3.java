package com.boxfishedu.workorder.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

public class Collections3 {

	/**
	 * @param collection
	 *            来源集合
	 * @param keyPropertyName
	 *            要提取为Map中的Key值的属性名
	 * @param valuePropertyName
	 *            要提取为Map中的Value值的属性名
	 * @return map
	 * @Description:提取集合中的对象的两个属性(通过Getter函数), 组合成Map
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:21:32
	 * @Version:1.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map extractToMap(final Collection collection, final String keyPropertyName, final String valuePropertyName) {
		Map map = new HashMap(collection.size());

		try {
			for (Object obj : collection) {
				map.put(PropertyUtils.getProperty(obj, keyPropertyName), PropertyUtils.getProperty(obj, valuePropertyName));
			}
		} catch (Exception e) {
			e.printStackTrace();
//			throw Reflections.convertReflectionExceptionToUnchecked(e);
		}

		return map;
	}

	/**
	 * @param collection
	 *            来源集合
	 * @param propertyName
	 *            要提取的属性名
	 * @return list
	 * @Description:提取集合中的对象的一个属性(通过Getter函数), 组合成List.
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:22:06
	 * @Version:1.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List extractToList(final Collection collection, final String propertyName) {
		if (isEmpty(collection)) {
			return new ArrayList();
		}
		List list = new ArrayList(collection.size());
		try {
			for (Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			e.printStackTrace();
//			throw Reflections.convertReflectionExceptionToUnchecked(e);
		}

		return list;
	}

	/**
	 * @param collection
	 *            来源集合
	 * @param propertyName
	 *            要提取的属性名
	 * @param separator
	 *            分隔符
	 * @return string
	 * @Description:提取集合中的对象的一个属性(通过Getter函数), 组合成由分割符分隔的字符串
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:26:00
	 * @Version:1.0
	 */
	@SuppressWarnings("rawtypes")
	public static String extractToString(final Collection collection, final String propertyName, final String separator) {
		List list = extractToList(collection, propertyName);
		return StringUtils.join(list, separator);
	}

	/**
	 * @param collection
	 * @param separator
	 * @return string
	 * @Description: 转换Collection所有元素(通过toString())为String, 中间以 separator分隔。
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:22:27
	 * @Version:1.0
	 */
	public static String convertToString(@SuppressWarnings("rawtypes") final Collection collection, final String separator) {
		return StringUtils.join(collection, separator);
	}

	/**
	 * @param collection
	 * @param prefix
	 * @param postfix
	 * @return string
	 * @Description: 转换Collection所有元素(通过toString())为String, 每个元素的前面加入prefix，后面加入postfix，如mymessage。
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:22:45
	 * @Version:1.0
	 */
	public static String convertToString(@SuppressWarnings("rawtypes") final Collection collection, final String prefix, final String postfix) {
		StringBuilder builder = new StringBuilder();
		for (Object o : collection) {
			builder.append(prefix).append(o).append(postfix);
		}
		return builder.toString();
	}

	/**
	 * @param collection
	 * @return boolean
	 * @Description:判断是否为空
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:23:11
	 * @Version:1.0
	 */
	public static boolean isEmpty(@SuppressWarnings("rawtypes") Collection collection) {
		return (collection == null) || collection.isEmpty();
	}

	/**
	 * @param map
	 * @return boolean
	 * @Description:判断是否为空
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:23:11
	 * @Version:1.0
	 */
	public static boolean isEmpty(@SuppressWarnings("rawtypes") Map map) {
		return (map == null) || map.isEmpty();
	}

	/**
	 * @param collection
	 * @return boolean
	 * @Description:判断是否为空
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:23:11
	 * @Version:1.0
	 */
	public static boolean isNotEmpty(@SuppressWarnings("rawtypes") Collection collection) {
		return (collection != null) && !(collection.isEmpty());
	}

	/**
	 * @param collection
	 * @return T
	 * @Description:取得Collection的第一个元素，如果collection为空返回null.
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:23:11
	 * @Version:1.0
	 */
	public static <T> T getFirst(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}

		return collection.iterator().next();
	}

	/**
	 * @param collection
	 * @return T
	 * @Description:获取Collection的最后一个元素 ，如果collection为空返回null
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:23:11
	 * @Version:1.0
	 */
	public static <T> T getLast(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}

		// 当类型为List时，直接取得最后一个元素 。
		if (collection instanceof List) {
			List<T> list = (List<T>) collection;
			return list.get(list.size() - 1);
		}

		// 其他类型通过iterator滚动到最后一个元素.
		Iterator<T> iterator = collection.iterator();
		while (true) {
			T current = iterator.next();
			if (!iterator.hasNext()) {
				return current;
			}
		}
	}

	/**
	 * @param a
	 * @param b
	 * @return list
	 * @Description:两个集合的并集
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:24:08
	 * @Version:1.0
	 */
	public static <T> List<T> union(final Collection<T> a, final Collection<T> b) {
		List<T> result = new ArrayList<T>(a);
		result.addAll(b);
		return result;
	}

	/**
	 * @param a
	 * @param b
	 * @return list
	 * @Description:两个集合的非交集
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:24:08
	 * @Version:1.0
	 */
	public static <T> List<T> subtract(final Collection<T> a, final Collection<T> b) {
		List<T> list = new ArrayList<T>(a);
		for (T element : b) {
			list.remove(element);
		}

		return list;
	}

	/**
	 * @param a
	 * @param b
	 * @return list
	 * @Description:两个集合非交集
	 * @Author:liuchen#xingyue.com
	 * @Since:2015年1月20日 上午10:24:08
	 * @Version:1.0
	 */
	public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
		List<T> list = new ArrayList<T>();
		for (T element : a) {
			if (b.contains(element)) {
				list.add(element);
			}
		}
		return list;
	}
}
