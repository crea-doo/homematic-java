package at.creadoo.homematic;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
	
	@Test
	public void testDecryptPacket1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException, Exception {
		log.debug("testDecryptPacket");

		final byte[] aesKey = Util.toByteFromHex("00112233445566778899AABBCCDDEEFF");
		//final byte[] aesRemoteIV = Util.toByteFromHex("30180C06C2E170B80000000000000000");
		final byte[] aesLocalIV = Util.toByteFromHex("86ED37816BD71C4B2D0E7092B1D8364C");
		final Cipher cipherDecrypt = CryptoUtil.getAESCipherDecrypt(aesKey, aesLocalIV);
		
		log.debug("\n\ntestDecryptPacket: 01\n");
		// Should be "HHM-LAN-IF,03C4,JEQ0706166,1EA2B9,FD666A,0000B721,0000,00"
		Util.logPacket("HHM-LAN-IF,03C4,JEQ0706166,1EA2B9,FD666A,0000B721,0000,00".getBytes());
		Util.logPacket(CryptoUtil.aesCrypt(cipherDecrypt, Util.toByteFromHex("6DB0CFF905BD91A299B861BBE503659F7B37FADAB4DD30ECE3ABAF6D2D75E2799DA5A3443C3301D9A6106280FD63465231AD9A53E3E872AC0BC8AB")));
		

		log.debug("\n\ntestDecryptPacket: 02\n");
		// Should be "HHM-LAN-IF,03C4,JEQ0706166,1EA2B9,FD666A,0000F1DD,0000,00"
		Util.logPacket("HHM-LAN-IF,03C4,JEQ0706166,1EA2B9,FD666A,0000F1DD,0000,00".getBytes());
		Util.logPacket(CryptoUtil.aesCrypt(cipherDecrypt, Util.toByteFromHex("F931D53FFC21BB5B58226FB971176BD0D44F5476C99E1274EDB84F42B9F1F3B5619029E899F269E97175B854A38379A1CB0A454C78038A692C7708")));
		

		log.debug("\n\ntestDecryptPacket: 03\n");
		// Should be "E31E00F,0000,00038E03,FF,FFCA,2D844131E00F000000010BC8"
		Util.logPacket("E31E00F,0000,00038E03,FF,FFCA,2D844131E00F000000010BC8".getBytes());
		Util.logPacket(CryptoUtil.aesCrypt(cipherDecrypt, Util.toByteFromHex("5707B416045BB9B6D81D0E1638BC69EF12838193379599372FCE56A9468958BA4F743E30923852EAE23CFA658F15C8C4ADFB40D5BE2B9F39")));
	}

	//@Test(timeOut=5000)
	public void testAESWithDummyLink() throws InterruptedException {
		log.debug("testAESWithDummyLink");
		
		final AtomicBoolean success = new AtomicBoolean(false);
		
		final String aesRemoteIV = "30180C06C2E170B80000000000000000";
		final String aesLocalIV = "86ED37816BD71C4B2D0E7092B1D8364C";
		
		log.debug("RemoteIV: " + aesRemoteIV);
		log.debug("LocalIV: " + aesLocalIV);
		
		log.error("RemoteIV Packet: " + "V" + aesRemoteIV);
		
		final DummySocketLink dummyLink = new DummySocketLink(aesRemoteIV, aesLocalIV);
		dummyLink.setAESEnabled(true);
		dummyLink.setAESLANKey("00112233445566778899AABBCCDDEEFF");
		dummyLink.setAESRFKey("00112233445566778899AABBCCDDEEFF");
		dummyLink.addLinkListener(new ILinkListener() {
			
			@Override
			public void received(final HomeMaticPacket packet) {
				/*
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
				*/
			}
			
			@Override
			public void close() {
				//
			}
		});
		dummyLink.start();
		
		Thread.sleep(1000);
		
		//dummyLink.received(new String("V" + Util.toHex(aesRemoteIV)).getBytes());
		dummyLink.received(Util.toByteFromHex("6DB0CFF905BD91A299B861BBE503659F7B37FADAB4DD30ECE3ABAF6D2D75E2799DA5A3443C3301D9A6106280FD63465231AD9A53E3E872AC0BC8AB"));
		
		/*
		while (!success.get()) {
			Thread.sleep(100);
		}
		*/
	}
}
