package com.github.cxfplus.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public final class StringUtils extends org.apache.commons.lang.StringUtils{
	/**
	 * 文本转换到整数int
	 * 
	 * @param o
	 * @param defaultValue
	 * @return
	 */
	public static int toInt(String o, Integer defaultValue) {
		if (isBlank(o))
			return defaultValue;// 空白则返回默认值，即便默认值为null也返回null
		try {
			return Integer.valueOf(o);
		} catch (NumberFormatException e) {
			if (defaultValue == null)// 默认值为null，且数值非法的情况下抛出异常
				throw e;
			return defaultValue;
		}
	}
	/**
	 * 用简易语法校验两个String是否匹配。 注意，这个方法和String.matches的逻辑完全不同 注：
	 * 简易语法就是用*表示匹配任意字符，用?表示匹配0~1个任意字符，用+表示匹配1个或以上任意字符。 其他字符一律按照字面理解。
	 * 这是为了和Windows用户的习惯相吻合
	 */
	public static boolean matches(String s, String key, boolean IgnoreCase) {
		return matches(s, key, IgnoreCase, true, true, false);
	}

	/**
	 * 用简易语法校验两个String是否匹配。 注意，这个方法和String.matches的逻辑完全不同 注：
	 * 简易语法就是用*表示匹配任意字符，用?表示匹配0~1个任意字符，用+表示匹配1个或以上任意字符。 其他字符一律按照字面理解。
	 * 这是为了和Windows用户的习惯相吻合
	 * 
	 * @param IgnoreCase
	 *            忽略大小写
	 * @param matchStart
	 *            要求头部匹配（即源字符串在头部没有多余的字符）
	 * @param matchEnd
	 *            要求尾部匹配（即源字符串在尾部没有多余的字符）
	 * @param wildcardSpace
	 *            关键字中的空格可以匹配任意数量的（\n\t空格等）
	 * @return
	 */
	public static boolean matches(String s, String key, boolean IgnoreCase, boolean matchStart, boolean matchEnd, boolean wildcardSpace) {
		if (s == null && key == null)
			throw new NullPointerException();
		if (s == null)
			return false;
		if (key == null)
			return true;
		if (IgnoreCase) {
			s = s.toUpperCase();
		}
		Pattern p = RegexpUtils.simplePattern(key, IgnoreCase, matchStart, matchEnd, wildcardSpace);
		return p.matcher(s).matches();
	}
	
	/**
	 * 文本转换为boolean，如果不能转换则返回默认值
	 * 
	 * @param s
	 * @param defaultValue
	 * @return
	 */
	public static final boolean toBoolean(String s, Boolean defaultValue) {
		if ("true".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s) || "1".equals(s) || "ON".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "T".equalsIgnoreCase(s)) {
			return true;
		}
		if ("false".equalsIgnoreCase(s) || "N".equalsIgnoreCase(s) || "0".equals(s) || "OFF".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "F".equalsIgnoreCase(s)) {
			return false;
		}
		if (defaultValue == null) {// 特别的用法，不希望有缺省值，如果字符串不能转换成布尔，则抛出异常。
			throw new IllegalArgumentException(s + "can't be cast to boolean.");
		}
		return defaultValue;
	}
	/**
	 * 合并多个String,在参数为3个和以内时请直接使用String.concat。
	 * 5个和超过5个String相加后，concat方法性能急剧下降，此时此方法最快
	 * 
	 * @param args
	 * @return
	 */
	public final static String concat(String... args) {
		if (args.length == 1)
			return args[0];
		int n = 0;
		for (String s : args) {
			if (s == null)
				continue;
			n += s.length();
		}
		StringBuilder sb = new StringBuilder(n);
		for (String s : args) {
			if (s == null)
				continue;
			sb.append(s);
		}
		return sb.toString();
	}
	
	/**
	 * 计算CRC摘要,8位十六进制数
	 */
	public static String getCRC(InputStream in) {
		CRC32 crc32 = new CRC32();
		byte[] b = new byte[4096];
		int len = 0;
		try {
			while ((len = in.read(b)) != -1) {
				crc32.update(b, 0, len);
			}
			return Long.toHexString(crc32.getValue());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * 计算CRC摘要,8位十六进制数
	 */
	public static String getCRC(String s) {
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
		return getCRC(in);
	}
	
	/**
	 * 取字符串右侧的部分
	 * 
	 * @param source
	 *            源字符串
	 * @param rev
	 *            查找
	 * @param keepSourceIfNotFound
	 *            为true时找不到字串时返回全部，否则返回空串
	 * @return
	 */
	public static Substring stringRight(String source, String rev, boolean keepSourceIfNotFound) {
		if (source == null)
			return null;
		int n = source.indexOf(rev);
		if (n == -1) {
			if (keepSourceIfNotFound) {
				return new Substring(source);
			} else {
				return new Substring(source, source.length(), source.length());
			}
		}
		return new Substring(source, n + rev.length(), source.length());
	}

	/**
	 * 取字符串左侧的部分
	 * 
	 * @param source
	 *            源字符串
	 * @param rev
	 *            查找
	 * @param keepSourceIfNotFound为true时找不到字串时返回全部
	 *            ，否则返回空串
	 * @return
	 */
	public static Substring stringLeft(String source, String rev, boolean keepSourceIfNotFound) {
		if (source == null)
			return null;
		int n = source.indexOf(rev);
		if (n == -1) {
			if (keepSourceIfNotFound) {
				return new Substring(source);
			} else {
				return new Substring(source, 0, 0);
			}
		}
		return new Substring(source, 0, n);
	}

}
