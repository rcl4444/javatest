package cn.zhijian.passport.bundles;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import jersey.repackaged.com.google.common.collect.Sets;

public class ObjectStore {

	Map<String, Object> store = Maps.newHashMap();

	public <T> void put(T object) {
		Set<Class<?>> classHier = getClassHier(object.getClass());
		classHier.forEach(c -> store.put(c.getName(), object));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		return (T) store.get(clazz.getName());
	}

	public static Set<Class<?>> getClassHier(Class<? extends Object> clazz) {
		Set<Class<?>> clz = Sets.newHashSet(clazz);
		Class<?> sup = clazz.getSuperclass();
		Class<?>[] intfs = clazz.getInterfaces();
		if (intfs != null && intfs.length > 0) {
			clz.addAll(Arrays.asList(intfs).stream().filter(c -> !isBaseType(c)).collect(Collectors.toList()));
		}
		if (sup != null && !isBaseType(sup)) {
			clz.add(sup);
			clz.addAll(getClassHier(sup));
		}
		return clz;
	}

	public static boolean isBaseType(Class<?> clz) {
		String n = clz.getName();
		return n.startsWith("java.lang.") || n.startsWith("java.io");
	}

}
