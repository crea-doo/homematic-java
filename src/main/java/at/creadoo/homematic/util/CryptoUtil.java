package at.creadoo.homematic.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtil {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	private CryptoUtil() {
		//
	}
	
	public static Cipher getAESCipher(final byte[] key, final int cipherMode, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		final Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding", new BouncyCastleProvider());
        final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(cipherMode, secretKey, new IvParameterSpec(iv));
        return cipher;
	}
	
	public static Cipher getAESCipherEncrypt(final byte[] key, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
        return getAESCipher(key, Cipher.ENCRYPT_MODE, iv);
	}
	
	public static byte[] aesEncrypt(final Cipher cipher, final byte[] plain) throws Exception {
		return cipher.doFinal(plain);
	}

	public static byte[] aesDecrypt(final Cipher cipher, final byte[] encrypted) throws Exception {
		return cipher.doFinal(encrypted);
	}

	public static Cipher getAESCipherDecrypt(final byte[] key, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		return getAESCipher(key, Cipher.DECRYPT_MODE, iv);
	}

}
