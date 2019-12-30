package com.dy.baf.utils;

import java.util.regex.Pattern;

public class HtmlFilterUtil {

	private static final Pattern regEx_html = Pattern.compile("<[^>\u4E00-\u9FA5]+>");
	private static final Pattern regEx_blank = Pattern.compile("[\\\\n\\s　]+");

	public static String getTextFromHtml(String htmlStr) {
		if (htmlStr != null && htmlStr.trim().length() > 0) {
			htmlStr = regEx_html.matcher(htmlStr).replaceAll("");// 过滤hmtl标签
			htmlStr = regEx_blank.matcher(htmlStr).replaceAll("").replaceAll("&nbsp;", "").replaceAll("&bsp;", "");;// 过滤空白字符
		} else {
			htmlStr = "";
		}
		return htmlStr;
	}
}
