package com.pateo.bean.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 
 * 解析工具,对代码进行抽取,避免bean类型的依赖,提高代码的通用性 如何优雅的反射出一个对象
 * 
 * @author sh04595 20171220
 * 
 * @param <T>
 *            Bean Type
 */
public class HbaseBeanUtils<T> {

	// private static final Field[] fields = User.class.getDeclaredFields();
	// private static final String keyField = "userId";

	// private static final byte[] family = Bytes.toBytes("f1");
	// private static final byte[] col_name = Bytes.toBytes("name");
	// private static final byte[] col_age = Bytes.toBytes("age");

//	public static void main(String[] args) {
//		
//		Object object = null;
//		String aa = object+"";
//		byte[] bytes = Bytes.toBytes(aa);
//		String bb = Bytes.toString(bytes);
//		System.out.println(bytes);
//		System.out.println(bb);
//	}
	
	/**
	 * 此处 bean不能为null
	 * @param bean
	 * @param clazz
	 * @param key
	 * @param cfInfo
	 * @return
	 */
	public static <T> Put bean2Put(T bean,  String key,String cfInfo) { /// Class<T> clazz,
		final byte[] CF_INFO = Bytes.toBytes(cfInfo);
		final Field[] fields = bean.getClass().getDeclaredFields();
		Put put = new Put(Bytes.toBytes(key));
		for (Field field : fields) {
			String fieldName = field.getName();
			String fieldValue = null;
			try {
				field.setAccessible(true);
				Object object = field.get(bean);
				if (object == null)  continue;
				fieldValue = object.toString();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (fieldValue != null && fieldValue.length() > 0) {
				put.addColumn(CF_INFO, Bytes.toBytes(fieldName), Bytes.toBytes(fieldValue));
			}
		}
		return put;
	}

	/**
	 * 优雅解析User，根据bean属性类型自动判断处理赋值问题
	 * 
	 * @param rs
	 *            scan result
	 * @param clazz
	 *            bean class
	 * @param keyField
	 *            table中key对应的属性
	 * @return bean
	 * @throws Exception
	 */
	public static <T> T parser2Bean(Result rs, Class<T> clazz, String keyField)
			throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		List<Cell> listCells = rs.listCells();

		T user = clazz.newInstance();
		
		// 遍历key对应的所有Cell
		for (Cell cell : listCells) {
			String key = Bytes.toString(CellUtil.cloneRow(cell));

			// String cloneFamily = Bytes.toString(CellUtil.cloneFamily(cell));
			// // 列簇名
			String cloneQualifier = Bytes.toString(CellUtil
					.cloneQualifier(cell)); // 列名
			String cloneValue = Bytes.toString(CellUtil.cloneValue(cell)); // 列值

			for (Field field : fields) { // 遍历所有的属性
				// Object value = null;
				String fieldName = field.getName();

				// 处理key对应的属性
				if (fieldName.equalsIgnoreCase(keyField)) {
					// 此處可以賦值也可以不賦值，引用改變
					user = invokField(clazz, keyField, user, key, field);
				} else if (fieldName.equalsIgnoreCase(cloneQualifier)) {
					invokField(clazz, fieldName, user, cloneValue, field);
					break;
				}
			}
		}
		return user;
	}

	/**
	 * 根據具體的屬性進行處理
	 * 
	 * @param clazz
	 * @param keyField
	 * @param user
	 * @param key
	 * @param field
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static <T> T invokField(Class<T> clazz, String keyField, T user,
			String key, Field field) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Object value;
		String mathodName = "set" + toUpperCase4Index(keyField);
		value = getFieldValue(key, field);
		Method keymethod = clazz.getMethod(mathodName, field.getType());
		keymethod.invoke(user, value); // 执行反射，对属性进行set vale
		return user;
	}

	/**
	 * 根据需要传入的value 解析成制定的类型的Object，比如 Double，Integer ref
	 * https://www.zhihu.com/question/67687909
	 * 
	 * @param sValue
	 *            source string value
	 * @param field
	 *            object field
	 * @return field relevant value
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static Object getFieldValue(String sValue, Field field)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Object value = null;
		Class<?> fieldType = field.getType();
		if (fieldType == String.class)
			value = sValue;
		else {
			Method methodType = fieldType.getMethod("parse"
					+ fixparse(fieldType.getSimpleName()), String.class);
			value = methodType.invoke(null, sValue);
		}
		return value;
	}

	/**
	 * 当属性为 Integer的时候特殊处理
	 * 
	 * @param fstype
	 * @return
	 */
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

// @Deprecated # 避免 显示的使用set方法
// public static <T> T parser(Result rs,Class<T> clazz) throws Exception {
// Field[] fields = clazz.getDeclaredFields();
// List<Cell> listCells = rs.listCells();
// //Class<User> clazz = User.class;
// T user = clazz.newInstance();
// // 遍历key对应的所有Cell
// for (Cell cell : listCells) {
// String key = Bytes.toString(CellUtil.cloneRow(cell));
// //user.setUserid(key);
//
// //String cloneFamily = Bytes.toString(CellUtil.cloneFamily(cell)); // 列簇名
// String cloneQualifier = Bytes.toString(CellUtil.cloneQualifier(cell)); // 列名
// String cloneValue = Bytes.toString(CellUtil.cloneValue(cell)); // 列值
//
// for (Field field : fields) { // 遍历所有的属性
// Object value = null;
// String fieldName = field.getName();
//
// // 当列名和属性名一致的时候，通过getFieldValue获取属性类型的value
// if (fieldName.equalsIgnoreCase(cloneQualifier)) {
// //拼接得到对应set方法名
// String mathodName = "set" + toUpperCase4Index(fieldName);
// value = getFieldValue(cloneValue, field);// 从string得到属性类型对应的value
// try {
// Method method = clazz.getMethod(mathodName,field.getType());
// method.invoke(user, value); // 执行反射，对属性进行set vale
//
// } catch (Exception e) {
// e.printStackTrace();
// }
// break;
// }
// }
// }
// return user;
// }

// 下面的方法不够优雅
// private static Object parseField(Field field, Object value) {
// Object ret = null;
//
// // field为目标，value为投进来的值
// String ftype = field.getType().getName(); // field为反射出来的字段类型
// String fstype = field.getType().getSimpleName();
// if (field.getType() == String.class)
// value.toString();
// else if (ftype.indexOf("java.lang.") == 0) {
// // java.lang下面类型通用转换函数
// Class<?> class1 = field.getType();
//
// Method method = null;
// try {
// method = class1.getMethod("parse" + fixparse(fstype),
// String.class);
// } catch (NoSuchMethodException e1) {
// e1.printStackTrace();
// } catch (SecurityException e1) {
// e1.printStackTrace();
// }
// if (method != null) {
// try {
// ret = method.invoke(null, value.toString());
// } catch (Exception e) {
// e.printStackTrace();
// }
// if (ret != null)
// return ret;
// }
// }
// return ret;
// }

// public void setProperty(Object bean, String propertyName,
// Object propertyValue) throws Exception {
// Class cls = bean.getClass();
// Method[] methods = cls.getMethods();// 得到所有方法
// for (Method m : methods) {
// if (m.getName().equalsIgnoreCase("set" + propertyName)) {
// // 找到方法就注入
// m.invoke(bean, propertyValue);
// break;
// }
// }
// }