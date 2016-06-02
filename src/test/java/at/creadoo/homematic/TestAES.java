package at.creadoo.homematic;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.atomic.AtomicBoolean;

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

import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.packets.HomeMaticPacket;
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
		final byte[] encryptionKey = Util.toByteFromHex("2b7e151628aed2a6abf7158809cf4f3c");
		
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
		final byte[] iV1 = Util.toByteFromHex("000102030405060708090a0b0c0d0e0f");
		Assert.assertEquals(AESEncrypt(encryptionKey, iV1, Util.toByteFromHex("6bc1bee22e409f96e93d7e117393172a")), Util.toByteFromHex("3b3fd92eb72dad20333449f8e83cfb4a"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV1, Util.toByteFromHex("3b3fd92eb72dad20333449f8e83cfb4a")), Util.toByteFromHex("6bc1bee22e409f96e93d7e117393172a"));

		log.debug("testAESVectors [02]");
		final byte[] iV2 = Util.toByteFromHex("3B3FD92EB72DAD20333449F8E83CFB4A");
		Assert.assertEquals(AESEncrypt(encryptionKey, iV2, Util.toByteFromHex("ae2d8a571e03ac9c9eb76fac45af8e51")), Util.toByteFromHex("c8a64537a0b3a93fcde3cdad9f1ce58b"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV2, Util.toByteFromHex("c8a64537a0b3a93fcde3cdad9f1ce58b")), Util.toByteFromHex("ae2d8a571e03ac9c9eb76fac45af8e51"));

		log.debug("testAESVectors [03]");
		final byte[] iV3 = Util.toByteFromHex("C8A64537A0B3A93FCDE3CDAD9F1CE58B");
		Assert.assertEquals(AESEncrypt(encryptionKey, iV3, Util.toByteFromHex("30c81c46a35ce411e5fbc1191a0a52ef")), Util.toByteFromHex("26751f67a3cbb140b1808cf187a4f4df"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV3, Util.toByteFromHex("26751f67a3cbb140b1808cf187a4f4df")), Util.toByteFromHex("30c81c46a35ce411e5fbc1191a0a52ef"));

		log.debug("testAESVectors [04]");
		final byte[] iV4 = Util.toByteFromHex("26751F67A3CBB140B1808CF187A4F4DF");
		Assert.assertEquals(AESEncrypt(encryptionKey, iV4, Util.toByteFromHex("f69f2445df4f9b17ad2b417be66c3710")), Util.toByteFromHex("c04b05357c5d1c0eeac4c66f9ff7f2e6"));
		Assert.assertEquals(AESDecrypt(encryptionKey, iV4, Util.toByteFromHex("c04b05357c5d1c0eeac4c66f9ff7f2e6")), Util.toByteFromHex("f69f2445df4f9b17ad2b417be66c3710"));
		
	}
	
	private byte[] AESEncrypt(final byte[] encryptionKey, final byte[] initializationVector, final byte[] testVector) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		final Cipher cipherEncrypt = CryptoUtil.getAESCipherEncrypt(encryptionKey, initializationVector);
		return CryptoUtil.aesEncrypt(cipherEncrypt, testVector);
	}
	
	private byte[] AESDecrypt(final byte[] encryptionKey, final byte[] initializationVector, final byte[] testVector) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		final Cipher cipherDecrypt = CryptoUtil.getAESCipherDecrypt(encryptionKey, initializationVector);
		return CryptoUtil.aesDecrypt(cipherDecrypt, testVector);
	}
	
	@Test
	public void testDecryptPacket() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		log.debug("testDecryptPacket");

		final byte[] aesKey = Util.toByteFromHex("D2F2A4C5F823661C00349FFCA38E0625");
		final byte[] encryptIV = Util.toByteFromHex("1B0D06036A351A0D150A050201000000");
		final byte[] decryptIV = Util.toByteFromHex("7AC0DC0BDAE15CAA4B6D912CCB502762");
		final Cipher cipherEncrypt = CryptoUtil.getAESCipherEncrypt(aesKey, encryptIV);
		final Cipher cipherDecrypt = CryptoUtil.getAESCipherDecrypt(aesKey, decryptIV);
		
		log.debug(Util.toHex(CryptoUtil.aesDecrypt(cipherDecrypt,Util.toByteFromHex("EFBFBD2BEFBFBDDD8CEFBFBD241901EFBFBDEFBFBD226EEFBFBD6F35EFBFBD09EFBFBDEFBFBD6F4767EFBFBD261BEFBFBDEFBFBDEFBFBDEFBFBD31EFBFBDDEAAEFBFBDEFBFBDEFBFBDEFBFBD1F6C152B64DAB425EFBFBD0F44EFBFBD5F40696AEFBFBD09EFBFBDEFBFBDEFBFBDEFBFBD0827427750EFBFBD"))));
		log.debug(Util.toStringFromHex(CryptoUtil.aesDecrypt(cipherDecrypt, Util.toByteFromHex("EFBFBD2BEFBFBDDD8CEFBFBD241901EFBFBDEFBFBD226EEFBFBD6F35EFBFBD09EFBFBDEFBFBD6F4767EFBFBD261BEFBFBDEFBFBDEFBFBDEFBFBD31EFBFBDDEAAEFBFBDEFBFBDEFBFBDEFBFBD1F6C152B64DAB425EFBFBD0F44EFBFBD5F40696AEFBFBD09EFBFBDEFBFBDEFBFBDEFBFBD0827427750EFBFBD"))));
		log.debug(Util.toStringFromHex(CryptoUtil.aesDecrypt(cipherDecrypt, "EFBFBD2BEFBFBDDD8CEFBFBD241901EFBFBDEFBFBD226EEFBFBD6F35EFBFBD09EFBFBDEFBFBD6F4767EFBFBD261BEFBFBDEFBFBDEFBFBDEFBFBD31EFBFBDDEAAEFBFBDEFBFBDEFBFBDEFBFBD1F6C152B64DAB425EFBFBD0F44EFBFBD5F40696AEFBFBD09EFBFBDEFBFBDEFBFBDEFBFBD0827427750EFBFBD".getBytes())));
		log.debug(Util.toHex(CryptoUtil.aesDecrypt(cipherDecrypt, Util.toByteFromHex("2BDD8C241901226E6F35096F4767261B31DEAA1F6C152B64DAB4250F445F40696A090827427750"))));
		log.debug(Util.toHex(CryptoUtil.aesDecrypt(cipherDecrypt, "2BDD8C241901226E6F35096F4767261B31DEAA1F6C152B64DAB4250F445F40696A090827427750".getBytes())));
		log.debug(Util.toString(CryptoUtil.aesDecrypt(cipherDecrypt, "2BDD8C241901226E6F35096F4767261B31DEAA1F6C152B64DAB4250F445F40696A090827427750".getBytes())));
		//log.debug(Util.toStringFromHex(CryptoUtil.aesDecrypt(cipherDecrypt, Util.toByteFromHex("2BDD8C241901226E6F35096F4767261B31DEAA1F6C152B64DAB4250F445F40696A090827427750"))));
	}

	//@Test(timeOut=5000)
	public void testAESWithDummyLink() throws InterruptedException {
		log.debug("testAESWithDummyLink");
		
		final AtomicBoolean success = new AtomicBoolean(false);
		
		final DummySocketLink dummyLink = new DummySocketLink();
		dummyLink.setAESEnabled(true);
		dummyLink.setAESLANKey(Util.toByteFromHex("D2F2A4C5F823661C00349FFCA38E0625"));
		dummyLink.setAESRFKey(Util.toByteFromHex("B263E6ABEF373A47B125204E95DF13BB"));
		dummyLink.addLinkListener(new ILinkListener() {
			
			@Override
			public void received(final HomeMaticPacket packet) {
				 if (dummyLink.getAESEnabled()) {
		        	char c = (char) packet.getData()[0];
		        	if (c == 'V') {
		        		final byte[] subset = Util.subset(packet.getData(), 1);
		    			log.debug("Received remoteIV: [" + Util.toString(subset) + "]");
		        	}
		        } else {
					Util.logPacket(dummyLink, packet.getData());
		        }
				
				success.getAndSet(true);
			}
			
			@Override
			public void close() {
				//
			}
		});
		dummyLink.start();
		
		while (!success.get()) {
			Thread.sleep(100);
		}
	}
}
