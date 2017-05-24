package com.sioo.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MyUtils {

	/**
	 * @param splitContent
	 *            按多少字1条拆分内容
	 * @param content
	 *            待拆分的内容
	 * @return 拆分后的数组
	 * */
	public static List<String> cutContent(int splitContent, String content) {
		List<String> list = new ArrayList<String>();
		int count = 0;
		if (splitContent != 0) {
			count = (content.length() % splitContent) == 0 ? (content.length() / splitContent) : (content.length() / splitContent + 1);
			for (int i = 0; i < count; i++) {
				if (i < count - 1) {
					list.add(content.substring(i * splitContent, (i + 1) * splitContent));
				} else {
					list.add(content.substring(i * splitContent, content.length()));
				}
			}
		}
		return list;
	}

	/**
	 * @param num
	 *            多少号码拆分为1条
	 * @param arr
	 *            待拆分的号码数组
	 * @return 拆分后的号码集合
	 */
	public static List<String> cutMobile(int num, String[] arr) {
		List<String> strList = new ArrayList<String>();
		String string = "";
		for (int i = 0; i < arr.length; i++) {
			string += arr[i] + ",";
			if ((i + 1) % num == 0) {
				strList.add(string.substring(0, string.length() - 1));
				string = "";
			} else if (i == (arr.length - 1)) {
				strList.add(string.substring(0, string.length() - 1));
			}
		}
		return strList;
	}

	public static String getHashKey(int uid, String store) throws UnsupportedEncodingException {
		return Md5Util.getMD5(uid + java.net.URLEncoder.encode(store, "utf-8")).substring(0, 3);
	}

	public static String getHashKeyByString(String expid, String store) throws UnsupportedEncodingException {
		return Md5Util.getMD5(expid + java.net.URLEncoder.encode(store, "utf-8")).substring(0, 3);
	}

	public static final String YD_STRING = "1(3[4-9]|47|5[0124789]|8[23478]|9[9]|7[8])\\d{8}|170[5]\\d{7}";
	public static final String LT_STRING = "1(3[0-2]|4[45]|5[56]|8[56]|7[6])\\d{8}|170[789]\\d{7}";
	public static final String DX_STRING = "1(8[019]|[35]3|7[7])\\d{8}|170[012]\\d{7}";
	public static final String M_STRING = "1(3[0-9]|4[457]|7[678]|5[012356789]|8[0123456789])\\d{8}|170[0125789]\\d{7}";

	/**
	 * 查询号码类型
	 * */
	public static int checkMobileType(String mb) {
		if (mb.matches(YD_STRING)) {
			return 1;
		} else if (mb.matches(LT_STRING)) {
			return 2;
		} else if (mb.matches(DX_STRING)) {
			return 4;
		} else {
			return 0;
		}
	}

	/**
	 * 匹配中文模板 eg: msg:你好dsafads阿萨德发的sadf的说123多发点
	 * regex:你好([\w\W]*)阿萨德发的([\w\W]*)多发点
	 */

	public static boolean matchTemplet(String msg, String msgTemplet) {
		String string = msgTemplet.replace("[#var#]", "([\\w\\W]*)");
		Pattern patternMobile = Pattern.compile(string);
		return patternMobile.matcher(msg).matches();
	}
	
	public static String getMD5Str(int uid, int channel, int mtype, String content, int len) throws UnsupportedEncodingException {
		if (len == 32) {
			return Md5Util.getMD5(String.valueOf(uid) + channel + mtype + java.net.URLEncoder.encode(content, "utf-8"));
		} else if (len == 16) {
			return Md5Util.getMD5(String.valueOf(uid) + channel + mtype + java.net.URLEncoder.encode(content, "utf-8")).substring(8, 24);
		}
		return null;
	}
}
