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
package at.creadoo.homematic;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import at.creadoo.homematic.util.CryptoUtil;
import at.creadoo.homematic.util.Util;

public class TestAES {
	
	private static final Logger log = Logger.getLogger(TestAES.class);

	@BeforeTest
	public void setUpTest() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	@AfterTest
	public void tearDownTest() throws Exception {
		//
	}
	
	@BeforeMethod
	public void setUpMethod() throws Exception {
		log.debug("\n");
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
		log.debug("\n\n");
	}
	
	/**
	 * Vectors from:
	 * http://www.inconteam.com/software-development/41-encryption/55-aes-test-vectors#aes-cfb-128
	 * @throws InvalidKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchProviderException 
	 * @throws Exception
	 */
	@Test
	public void testAESVectors() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		final byte[] encryptionKey = Util.toByteFromHex("2b7e151628aed2a6abf7158809cf4f3c".toLowerCase());
		
		/*
		log.debug("Providers:");
		for (Provider provider : Security.getProviders()) {
			log.debug(provider.getName());
			for (String key : provider.stringPropertyNames()) {
				log.debug("\t" + key + "\t" + provider.getProperty(key));
			}
		}
		*/
		
		log.debug("testAESVectors [01]");
		final byte[] iV1 = Util.toByteFromHex("000102030405060708090a0b0c0d0e0f".toLowerCase());
		Assert.assertEquals(AESEncrypt(encryptionKey, iV1, Util.toByteFromHex("6bc1bee22e409f96e93d7e117393172a")), Util.toByteFromHex("3b3fd92eb72dad20333449f8e83cfb4a"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV1, Util.toByteFromHex("3b3fd92eb72dad20333449f8e83cfb4a")), Util.toByteFromHex("6bc1bee22e409f96e93d7e117393172a"));

		log.debug("testAESVectors [02]");
		final byte[] iV2 = Util.toByteFromHex("3B3FD92EB72DAD20333449F8E83CFB4A".toLowerCase());
		Assert.assertEquals(AESEncrypt(encryptionKey, iV2, Util.toByteFromHex("ae2d8a571e03ac9c9eb76fac45af8e51")), Util.toByteFromHex("c8a64537a0b3a93fcde3cdad9f1ce58b"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV2, Util.toByteFromHex("c8a64537a0b3a93fcde3cdad9f1ce58b")), Util.toByteFromHex("ae2d8a571e03ac9c9eb76fac45af8e51"));

		log.debug("testAESVectors [03]");
		final byte[] iV3 = Util.toByteFromHex("C8A64537A0B3A93FCDE3CDAD9F1CE58B".toLowerCase());
		Assert.assertEquals(AESEncrypt(encryptionKey, iV3, Util.toByteFromHex("30c81c46a35ce411e5fbc1191a0a52ef")), Util.toByteFromHex("26751f67a3cbb140b1808cf187a4f4df"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV3, Util.toByteFromHex("26751f67a3cbb140b1808cf187a4f4df")), Util.toByteFromHex("30c81c46a35ce411e5fbc1191a0a52ef"));

		log.debug("testAESVectors [04]");
		final byte[] iV4 = Util.toByteFromHex("26751F67A3CBB140B1808CF187A4F4DF".toLowerCase());
		Assert.assertEquals(AESEncrypt(encryptionKey, iV4, Util.toByteFromHex("f69f2445df4f9b17ad2b417be66c3710")), Util.toByteFromHex("c04b05357c5d1c0eeac4c66f9ff7f2e6"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV4, Util.toByteFromHex("c04b05357c5d1c0eeac4c66f9ff7f2e6")), Util.toByteFromHex("f69f2445df4f9b17ad2b417be66c3710"));
		
	}
	
	private byte[] AESEncrypt(final byte[] encryptionKey, final byte[] initializationVector, final byte[] testVector) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		final Cipher cipherEncrypt = CryptoUtil.getAESCipherEncrypt(encryptionKey, initializationVector);
		return CryptoUtil.aesCrypt(cipherEncrypt, testVector);
	}
	
	private byte[] AESDecrypt(final byte[] encryptionKey, final byte[] initializationVector, final byte[] testVector) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		final Cipher cipherDecrypt = CryptoUtil.getAESCipherDecrypt(encryptionKey, initializationVector);
		return CryptoUtil.aesCrypt(cipherDecrypt, testVector);
	}

}
