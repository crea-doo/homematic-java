package at.creadoo.homematic.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
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
	
	private static Cipher getAESCipher(final byte[] key, final int cipherMode, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		final Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
        final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(cipherMode, secretKey, new IvParameterSpec(iv));
        return cipher;
	}
	
	public static Cipher getAESCipherEncrypt(final byte[] key, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
        return getAESCipher(key, Cipher.ENCRYPT_MODE, iv);
	}

	public static Cipher getAESCipherDecrypt(final byte[] key, final byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		return getAESCipher(key, Cipher.DECRYPT_MODE, iv);
	}

	public static byte[] aesCrypt(final Cipher cipher, final byte[] plain) throws GeneralSecurityException, IOException {
		return cipher.doFinal(plain);
		//return aesCrypt(cipher, new ByteArrayInputStream(plain));
	}

	/*
	public static byte[] aesCrypt(final Cipher cipher, final InputStream input) throws GeneralSecurityException, IOException {
		byte[] byteBuffer = new byte[64 * 1024];
		int n;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final CipherOutputStream cos = new CipherOutputStream(baos, cipher);
		try {
			while ((n = input.read(byteBuffer)) > 0) {
				cos.write(byteBuffer, 0, n);
			}
		} finally {
			cos.close();
			baos.close();
			input .close();
		}
		return baos.toByteArray();
	}
	*/

}
