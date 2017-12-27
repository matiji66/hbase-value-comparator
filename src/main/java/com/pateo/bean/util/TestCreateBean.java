package com.pateo.bean.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pateo.bean.NewCustInfo;

/**
 * 该方法只处理string类型
 * @author sh04595
 *
 */
public class TestCreateBean<T> {

	public static List<String> paramBodyKey = new ArrayList<String>();
	static {
		paramBodyKey.add("dataId");
		paramBodyKey.add("custId");
		paramBodyKey.add("provCode");
		paramBodyKey.add("regionId");
		paramBodyKey.add("billId");
		paramBodyKey.add("custName");
		paramBodyKey.add("custCertType");
		paramBodyKey.add("custCertNo");
		paramBodyKey.add("custCertAddr");
		paramBodyKey.add("certValiddate");
		paramBodyKey.add("certExpdate");
		paramBodyKey.add("gender");
		paramBodyKey.add("nation");
		paramBodyKey.add("birthday");
		paramBodyKey.add("linkaddr");
		paramBodyKey.add("linkman");
		paramBodyKey.add("linkphone");
		paramBodyKey.add("issuingAuthority");
		paramBodyKey.add("fistGetDate");
		paramBodyKey.add("validduration");
		paramBodyKey.add("archiveNo");
		paramBodyKey.add("state");
		paramBodyKey.add("createDate");
		paramBodyKey.add("authType");
		paramBodyKey.add("notes");
		paramBodyKey.add("ext1");
		paramBodyKey.add("ext2");
		paramBodyKey.add("ext3");
		paramBodyKey.add("ext4");
		paramBodyKey.add("ext5");
		paramBodyKey.add("ext6");
		paramBodyKey.add("ext7");
		paramBodyKey.add("ext8");
		paramBodyKey.add("ext9");
		paramBodyKey.add("ext10");
	}

	public static Map<String, String> padBean(String str) {
		Map<String, String> map = new HashMap<String, String>();
		String[] strs = str.split("\\|");
		for (int i = 0; i < strs.length; i++) {
			String key = paramBodyKey.get(i);
			String value = strs[i];
			map.put(key, value);
		}
		return map;
	}

	public static NewCustInfo getbean(Map<String, String> map) {
		NewCustInfo newcustinfo = new NewCustInfo();
		Class clazz = NewCustInfo.class;
		for (String s : map.keySet()) {
			String key = "set" + s.substring(0, 1).toUpperCase()
					+ s.substring(1, s.length());
			try {
				Method method = clazz.getMethod(key, String.class);
				method.invoke(newcustinfo, map.get(s));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return newcustinfo;
	}

	public static <T> T getbean2(Map<String, String> map, Class<T> clazz) throws InstantiationException, IllegalAccessException {
		T newcustinfo = clazz.newInstance();
		//NewCustInfo newcustinfo = new NewCustInfo();
		//Class<NewCustInfo> clazz = NewCustInfo.class;
		for (String s : map.keySet()) {
			String key = "set" + s.substring(0, 1).toUpperCase()
					+ s.substring(1, s.length());
			try {
				Method method = clazz.getMethod(key, String.class);
				method.invoke(newcustinfo, map.get(s));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return  newcustinfo;
	}
	public static void main(String[] args) {
		String str = "||XX|||XX|XX|XX|XX||XX|XX|XXX||||XX|||||XX||XX||XX|XX|XX|XX|XX|XX||XX|XX|";
		Map<String, String> map = padBean(str);
		
		System.out.println(getbean(map));
		
		
		try {
			System.out.println(getbean2(map,NewCustInfo.class));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
 
	}
}