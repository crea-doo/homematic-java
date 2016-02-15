package at.creadoo.homematic;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.packets.HomeMaticPacket;
import at.creadoo.homematic.util.CryptoUtil;
import at.creadoo.homematic.util.Util;

public class TestAES {
	
	private static final Logger log = Logger.getLogger(TestAES.class);

	@BeforeMethod
	public void setUp() throws Exception {
		log.debug("\n");
	}

	@AfterMethod
	public void tearDown() throws Exception {
		log.debug("\n\n");
	}
	
	/**
	 * Vectors from:
	 * http://www.inconteam.com/software-development/41-encryption/55-aes-test-vectors
	 * @throws Exception 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	//@Test
	public void testAESVectors() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, Exception {
		log.debug("testAESVectors");

		final byte[] aesKey = Util.toByteFromHex("2b7e151628aed2a6abf7158809cf4f3c");
		final byte[] iV = Util.toByteFromHex("000102030405060708090a0b0c0d0e0f");
		final Cipher cipherEncrypt = CryptoUtil.getAESCipherEncrypt(aesKey, iV);
		final Cipher cipherDecrypt = CryptoUtil.getAESCipherDecrypt(aesKey, iV);
		
		Assert.assertEquals(CryptoUtil.aesEncrypt(cipherEncrypt, Util.toByteFromHex("6bc1bee22e409f96e93d7e117393172a")), Util.toByteFromHex("3b3fd92eb72dad20333449f8e83cfb4a"));
		Assert.assertEquals(CryptoUtil.aesDecrypt(cipherDecrypt, Util.toByteFromHex("3b3fd92eb72dad20333449f8e83cfb4a")), Util.toByteFromHex("6bc1bee22e409f96e93d7e117393172a"));
	}
	
	@Test
	public void testDecryptPacket() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, Exception {
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
