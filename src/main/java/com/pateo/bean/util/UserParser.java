package com.pateo.bean.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.pateo.bean.User;

public class UserParser {

	private static final Field[] fields = User.class.getDeclaredFields();
	
	private static final byte[] family = Bytes.toBytes("f1");
	private static final byte[] col_name = Bytes.toBytes("name");
	private static final byte[] col_age = Bytes.toBytes("age");

	/**
	 * 解析User
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	public static User parser(Result rs) throws Exception {

		List<Cell> listCells = rs.listCells();
		Class<User> clazz = User.class;

		User user = new User();
		for (Cell cell : listCells) {
			String key = Bytes.toString(CellUtil.cloneRow(cell));
			
			String cloneFamily = Bytes.toString(CellUtil.cloneFamily(cell));
			String cloneQualifier = Bytes.toString(CellUtil
					.cloneQualifier(cell));
			String cloneValue = Bytes.toString(CellUtil.cloneValue(cell));
			for (Field field : fields) {
				Object ret = null;
				String fieldName = field.getName();
				if (fieldName.equalsIgnoreCase(cloneQualifier)) {
					String mathodName = "set" + toUpperCase4Index(fieldName);
					ret = getFieldValue(cloneValue, field);
					try {
						Method method = clazz.getMethod(mathodName,field.getType());
						method.invoke(user, ret);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
			user.setUserid(key);
		}
		return user;
	}

	/**
	 * 根据需要传入的value 解析成制定的类型的Object，比如 Double，Integer
	 * 
	 * @param cloneValue
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static Object getFieldValue(String cloneValue, Field field)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Object ret = null;
		Class<?> fieldType = field.getType();
		if (fieldType == String.class)
			ret = cloneValue;
		else {
			Method methodType = fieldType.getMethod("parse"
					+ fixparse(fieldType.getSimpleName()), String.class);
			ret = methodType.invoke(null, cloneValue);
		}
		return ret;
	}

	private static Object parseField(Field field, Object value) {
		Object ret = null;

		// field为目标，value为投进来的值
		String ftype = field.getType().getName(); // field为反射出来的字段类型
		String fstype = field.getType().getSimpleName();
		if (field.getType() == String.class)
			value.toString();
		else if (ftype.indexOf("java.lang.") == 0) {
			// java.lang下面类型通用转换函数
			Class<?> class1 = field.getType();

			Method method = null;
			try {
				method = class1.getMethod("parse" + fixparse(fstype),
						String.class);
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
			if (method != null) {
				try {
					ret = method.invoke(null, value.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (ret != null)
					return ret;
			}
		}
		return ret;
	}


//	public void setProperty(Object bean, String propertyName,
//			Object propertyValue) throws Exception {
//		Class cls = bean.getClass();
//		Method[] methods = cls.getMethods();// 得到所有方法
//		for (Method m : methods) {
//			if (m.getName().equalsIgnoreCase("set" + propertyName)) {
//				// 找到方法就注入
//				m.invoke(bean, propertyValue);
//				break;
//			}
//		}
//	}

	private static String fixparse(String fstype) {
		switch (fstype) {
		case "Integer":
			return "Int";
		default:
			return fstype;
		}
	}

	/**
	 * 首字母大写
	 * 
	 * @param string
	 * @return
	 */
	public static String toUpperCase4Index(String string) {
		char[] methodName = string.toCharArray();
		methodName[0] = toUpperCase(methodName[0]);
		return String.valueOf(methodName);
	}

	/**
	 * 字符转成大写
	 * 
	 * @param chars
	 * @return
	 */
	public static char toUpperCase(char chars) {
		if (97 <= chars && chars <= 122) {
			chars ^= 32;
		}
		return chars;
	}
}
