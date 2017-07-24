/*
 * Copyright 2017 crea-doo.at
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package at.creadoo.homematic.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtil {

    public static final String DEFAULT_ENCODING = "UTF-8";
	
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
		return cipher.update(plain);
	}

    private static String toHash(final String raw, final String algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchProviderException {
        if (raw == null) {
            return null;
        }

        final MessageDigest md = MessageDigest.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        final StringBuilder result = new StringBuilder();

        for (byte b : md.digest(raw.getBytes(DEFAULT_ENCODING))) {
            String h  = Integer.toHexString(0xFF & b);

            while (h.length() < 2) {
                h = "0" + h;
            }

            result.append(h);
        }

        return result.toString();
    }

    public static String toMD5(final String raw) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchProviderException {
        return toHash(raw, "MD5");
    }

    public static String toSHA256(final String raw) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchProviderException {
        return toHash(raw, "SHA-256");
    }

}
