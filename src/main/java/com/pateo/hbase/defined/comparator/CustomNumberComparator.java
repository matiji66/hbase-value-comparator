package com.pateo.hbase.defined.comparator;

import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.util.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 自定义比较器：使用方法见 CompareTest
 * 
 * @param : fieldType 传递数据格式的类型，支持的数据类型:double
 * @param : data 通过Bytes转换得到的字节数组 使用注意事项 : 使用的时候要注意数据类型的匹配问题
 */
public class CustomNumberComparator extends ByteArrayComparable {
	/**
	 * 目前只支持 double类型
	 */
	private String fieldType;
	private byte[] data;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            value
	 */
	public CustomNumberComparator(byte[] value, String fieldType) {
		super(value);
		this.fieldType = fieldType;
		this.fieldType = "double";
		this.data = value;
	}

	@Override
	// 重写该方法
	public byte[] toByteArray() {

		MyComparatorProtos.CustomNumberComparator.Builder builder = MyComparatorProtos.CustomNumberComparator
				.newBuilder();
		builder.setValue(ByteString.copyFrom(this.data));
		builder.setFieldType(this.fieldType);
		return builder.build().toByteArray();
	}

	// 定义该方法，用于对象反序列化操作
	public static CustomNumberComparator parseFrom(final byte[] bytes)
			throws DeserializationException {
		MyComparatorProtos.CustomNumberComparator proto = null;
		try {
			proto = MyComparatorProtos.CustomNumberComparator.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			throw new DeserializationException(e);
		}
		return new CustomNumberComparator(proto.getValue().toByteArray(),
				proto.getFieldType());
	}

	// 重写比较方法 里面就可以按照自己的意愿来实现自己的比较器
	@Override
	public int compareTo(byte[] bytes, int offset, int length) {

		if (fieldType.equalsIgnoreCase("int")
				|| fieldType.equalsIgnoreCase("integer")) {
			Integer paramValue = byteConvertObj(Integer.class, this.data);
			Integer currentValue = byteConvertObj(Integer.class,
					Bytes.copy(bytes, offset, length));
			return paramValue.compareTo(currentValue);
		} else if (fieldType.equalsIgnoreCase("long")
				|| fieldType.equalsIgnoreCase("bigint")) {
			Long paramsValue = byteConvertObj(Long.class, this.data);
			Long currentValue = byteConvertObj(Long.class,
					Bytes.copy(bytes, offset, length));
			return paramsValue.compareTo(currentValue);
		} else if (fieldType.equalsIgnoreCase("float")) {
			Float paramsValue = byteConvertObj(Float.class, this.data);
			Float currentValue = byteConvertObj(Float.class,
					Bytes.copy(bytes, offset, length));
			return paramsValue.compareTo(currentValue);
		} else if (fieldType.equalsIgnoreCase("double")) {
			Double paramsValue = byteConvertObj(Double.class, this.data);
			Double currentValue = byteConvertObj(Double.class,
					Bytes.copy(bytes, offset, length));
			return paramsValue.compareTo(currentValue);
		} else if (fieldType.equalsIgnoreCase("short")
				|| fieldType.equalsIgnoreCase("SMALLINT")) {
			Short paramsValue = byteConvertObj(Short.class, this.data);
			Short currentValue = byteConvertObj(Short.class,
					Bytes.copy(bytes, offset, length));
			return paramsValue.compareTo(currentValue);
		}
		return 1;
	}

	private <T> T byteConvertObj(Class<T> clazz, byte[] data) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equalsIgnoreCase("Integer")) {
			Integer paramValue;
			try {
				paramValue = Bytes.toInt(data);
			} catch (IllegalArgumentException e) {
				paramValue = Integer.valueOf(Bytes.toString(data));
			}
			return (T) paramValue;
		} else if (clazzName.equalsIgnoreCase("Long")) {
			Long paramValue;
			try {
				paramValue = Bytes.toLong(data);
			} catch (IllegalArgumentException e) {
				paramValue = Long.valueOf(Bytes.toString(data));
			}
			return (T) paramValue;
		} else if (clazzName.equalsIgnoreCase("Float")) {
			Float paramValue;
			try {
				paramValue = Bytes.toFloat(data);
			} catch (IllegalArgumentException e) {
				paramValue = Float.valueOf(Bytes.toString(data));
			}
			return (T) paramValue;
		} else if (clazzName.equalsIgnoreCase("Double")) {
			Double paramValue;
			try {
				paramValue = Bytes.toDouble(data);
			} catch (IllegalArgumentException e) {
				paramValue = Double.valueOf(Bytes.toString(data));
			}
			return (T) paramValue;
		} else if (clazzName.equalsIgnoreCase("Short")) {
			Short paramValue;
			try {
				paramValue = Bytes.toShort(data);
			} catch (IllegalArgumentException e) {
				paramValue = Short.valueOf(Bytes.toString(data));
			}
			return (T) paramValue;
		}
		return (T) Bytes.toString(data);
	}
}