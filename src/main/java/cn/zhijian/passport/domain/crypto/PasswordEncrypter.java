package cn.zhijian.passport.domain.crypto;

import java.nio.charset.Charset;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class PasswordEncrypter {

	private static HashFunction hashFunc = Hashing.sha256();
	private static Charset UTF8 = Charset.forName("UTF-8");

	public static String encrypt(String plainText) {
		if (plainText == null) {
			return null;
		}
		return hashFunc.hashString(plainText, UTF8).toString();
	}
}
