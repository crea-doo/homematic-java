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

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtil {
	
	private static final Logger log = Logger.getLogger(CryptoUtil.class);
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	private CryptoUtil() {
		//
	}
	
	public static Cipher getAESCipher(final byte[] key, final int cipherMode, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		// See https://people.eecs.berkeley.edu/~jonah/bc/org/bouncycastle/crypto/paddings/BlockCipherPadding.html for paddings
		final Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
        final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(cipherMode, secretKey, new IvParameterSpec(iv));
        return cipher;
	}
	
	public static Cipher getAESCipherEncrypt(final byte[] key, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
        return getAESCipher(key, Cipher.ENCRYPT_MODE, iv);
	}
	
	public static byte[] aesCrypt(final Cipher cipher, final byte[] data) throws Exception {
		/* */
		final byte[] temp;
		Util.logPacket(data);
		final int rest = (data.length % 128);
		log.debug("REST: " + rest);
		if (rest > 0) {
			temp = Util.appendItem(data, new byte[128 - rest]);
		} else {
			temp = data;
		}
		Util.logPacket(temp);
		/* */
		return cipher.doFinal(temp);
	}
	
	public static byte[] aesEncrypt(final Cipher cipher, final byte[] plain) throws Exception {
		/*
		final byte[] data;
		Util.logPacket(plain);
		final int rest = (plain.length % 128);
		log.debug("REST: " + rest);
		if (rest > 0) {
			data = Util.appendItem(plain, new byte[128 - rest]);
		} else {
			data = plain;
		}
		Util.logPacket(data);^
		*/
		return cipher.doFinal(plain);
	}

	public static byte[] aesDecrypt(final Cipher cipher, final byte[] encrypted) throws Exception {
		/*
		final byte[] data;
		Util.logPacket(encrypted);
		final int rest = (encrypted.length % 128);
		log.debug("REST: " + rest);
		if (rest > 0) {
			data = Util.appendItem(encrypted, new byte[128 - rest]);
		} else {
			data = encrypted;
		}
		Util.logPacket(data);
		*/
		return cipher.doFinal(encrypted);
	}

	public static Cipher getAESCipherDecrypt(final byte[] key, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		return getAESCipher(key, Cipher.DECRYPT_MODE, iv);
	}

}
