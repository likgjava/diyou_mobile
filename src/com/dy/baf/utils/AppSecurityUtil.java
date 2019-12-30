package com.dy.baf.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import com.dy.core.utils.JsonUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.SecurityUtil;
import com.google.gson.Gson;

/**
 * 
 * 
 * @Description: 手机端与服务器通信加密解密工具类
 * @author 波哥
 * @date 2015年9月22日 上午8:55:46
 * @version V1.0
 */
public class AppSecurityUtil {
	private static Logger logger = Logger.getLogger(AppSecurityUtil.class);

	public static final String AES_KEY_IN = PropertiesUtil.getProperty("AES_KEY_IN");
	public static final String AES_KEY_OUT = PropertiesUtil.getProperty("AES_KEY_OUT");
	public static final String SIGN_KEY_IN = PropertiesUtil.getProperty("SIGN_KEY_IN");
	public static final String SIGN_KEY_OUT = PropertiesUtil.getProperty("SIGN_KEY_OUT");

	public static void main(String[] args) throws Exception {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("memberName", "18030041411");
		map.put("password", "a123123");
		map.put("phone_type", "1");
		map.put("type", "2");

		Gson gson = new Gson();
		String jsonText = gson.toJson(map);
		System.out.println(jsonText);
		String base64 = SecurityUtil.encode(jsonText.getBytes());// Base64.encodeToString(jsonText.getBytes());
																	// //
																	// SecurityUtil.encode(jsonText.getBytes());

		String dd = AppSecurityUtil.encrypt(AES_KEY_IN, replaceBlank(base64));
		System.out.println("加密后：" + dd);
		String cc = AppSecurityUtil.decrypt(AES_KEY_IN, dd);
		System.out.println("解密后：" + cc);
		System.out.println("解密后64：" + Base64.decodeToString(cc));

		System.out.println("MD5");
		System.out.println(getMD5(SIGN_KEY_IN + replaceBlank(dd) + SIGN_KEY_IN));
	}

	/**
	 * 对返回给手机端的diyou进行加密
	 * 
	 * @param str
	 * @return
	 */
	public static String encryptDiyou(String str) {
		try {
			String base64 = SecurityUtil.encode(chinaToUnicode(str).getBytes());
			String cipher = AppSecurityUtil.encrypt(AES_KEY_OUT, replaceBlank(base64));
			return cipher;
		} catch (Exception e) {
			logger.error(e.getStackTrace());
		}
		return null;
	}

	/**
	 * 对返回给手机端的xmdy进行加密
	 * 
	 * @param str
	 * @return
	 */
	public static String encryptXmdy(String str) {
		try {
			return getMD5(SIGN_KEY_OUT + str + SIGN_KEY_OUT);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
		}
		return null;
	}

	/**
	 * 获取请求参数
	 * 
	 * @param cipher
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getParamters(String cipher) {
		try {
			// AES解密
			String paramsAES = AppSecurityUtil.decrypt(AES_KEY_IN, cipher);
			// base64解密
			String paramsBase64 = Base64.decodeToString(paramsAES);

			return JsonUtils.fromJson(paramsBase64, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getStackTrace());
		}
		return null;
	}
	
	/**
	 * 获取请求参数
	 * 变量名转换为驼峰
	 * 
	 * @param cipher
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, String> getCamelParamters(String cipher) {
		Map<String,String> map = new HashMap<String, String>();
		try {
			// AES解密
			String paramsAES = AppSecurityUtil.decrypt(AES_KEY_IN, cipher);
			// base64解密
			String paramsBase64 = Base64.decodeToString(paramsAES);

			Map aesMap = JsonUtils.fromJson(paramsBase64, Map.class);
			Iterator entries = aesMap.entrySet().iterator();  
			while (entries.hasNext()) {  
			    Map.Entry entry = (Map.Entry) entries.next(); 
			    if(entry.getValue() != null){
			    	map.put(camelName(entry.getKey().toString()), entry.getValue().toString());
			    }
			}  
		} catch (Exception e) {
			logger.error(e.getStackTrace());
		}
		return map;
	}

	/**
	 * 对手机端发送的数据进行验签名
	 * 
	 * @param sign
	 * @param cipher
	 * @return
	 */
	public static boolean signVerify(String sign, String cipher) {

		if (sign.equals(getMD5(SIGN_KEY_IN + replaceBlank(cipher) + SIGN_KEY_IN))) {
			return true;
		}
		return false;
	}

	private static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll(" ");
		}
		return dest;
	}

	private static String getMD5(String str) {
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		// 16位加密，从第9位到25位
		return md5StrBuff.toString().toLowerCase();
	}

	public static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		}
		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) {
			if ((data[i] & 0xFF) < 16)
				str = str + "0" + Integer.toHexString(data[i] & 0xFF);
			else
				str = str + Integer.toHexString(data[i] & 0xFF);
		}
		return str;
	}

	public static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}

	/**
	 * 本地与服务器AES加密方法
	 * 
	 * @param hexString
	 * @return
	 */
	public static String encrypt(String key, String cipherText) {
		byte[] crypted = null;
		try {
			SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			crypted = cipher.doFinal(cipherText.getBytes("UTF-8"));
		} catch (Exception e) {
			logger.error("",e);
		}
		return new String(Base64.encode(crypted));
	}

	/**
	 * 本地与服务器AES解密方法
	 * 
	 * @param hexString
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decrypt(String key, String cipherText) throws Exception {
		byte[] output = null;
		SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skey);
		output = cipher.doFinal(Base64.decode(cipherText));
		return new String(output, "UTF-8");
	}
	
	

	/**
	 * Unicode转String方法
	 * 
	 * @param str
	 * @return
	 */
	public static String UnicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}

	/**
	 * 中文转unicode
	 * 
	 * @param str
	 * @return
	 */
	public static String chinaToUnicode(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				sb.append("\\u" + Integer.toHexString(c));
			}
		}
		return sb.toString();
	}

	 /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
     * 例如：HELLO_WORLD->HelloWorld
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String camelName(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母小写
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String camels[] = name.split("_");
        for (String camel :  camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
}
