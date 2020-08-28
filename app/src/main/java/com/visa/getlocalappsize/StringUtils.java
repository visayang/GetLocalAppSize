package com.visa.getlocalappsize;

import java.text.DecimalFormat;

public class StringUtils {

	/**
	 * 判断字符串是否为空 为空即true
	 *
	 * @param str 字符串
	 * @return
	 */
	public static boolean isNullString(String str) {
		return str == null || str.length() == 0 || "null".equals(str);
	}


	/**
	 * 将字符串格式化为带两位小数的字符串
	 *
	 * @param str 字符串
	 * @return
	 */
	public static String format2Decimals(String str) {
		DecimalFormat df = new DecimalFormat("#.00");
		if (df.format(stringToDouble(str)).startsWith(".")) {
			return "0" + df.format(stringToDouble(str));
		} else {
			return df.format(stringToDouble(str));
		}
	}

	/**
	 * 字符串转换成double ,转换失败将会 return 0;
	 *
	 * @param str 字符串
	 * @return
	 */
	public static double stringToDouble(String str) {
		if (isNullString(str)) {
			return 0;
		} else {
			try {
				return Double.parseDouble(str);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}

}
