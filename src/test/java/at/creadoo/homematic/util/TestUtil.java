package at.creadoo.homematic.util;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestUtil {
	
	private static final Logger log = Logger.getLogger(TestUtil.class);

	@BeforeMethod
	public void setUp() throws Exception {
		log.debug("\n");
	}

	@AfterMethod
	public void tearDown() throws Exception {
		log.debug("\n\n");
	}
	
	@Test
	public void testToString() {
		Assert.assertEquals(Util.toString("".getBytes()), "");
		Assert.assertEquals(Util.toString("T".getBytes()), "T");
		Assert.assertEquals(Util.toString("TEST".getBytes()), "TEST");
		
		Assert.assertEquals(Util.toString("".getBytes(), 0, 0), "");
		Assert.assertEquals(Util.toString("T".getBytes(), 0, 0), "T");
		Assert.assertEquals(Util.toString("TEST".getBytes(), 0, 0), "T");
	}
	
	@Test
	public void testPad() {
		Assert.assertEquals(Util.padLeft("Test", 8, "0"), "0000Test");
		Assert.assertEquals(Util.padLeft("Test", 8, '0'), "0000Test");

		Assert.assertEquals(Util.padRight("Test", 8, "0"), "Test0000");
		Assert.assertEquals(Util.padRight("Test", 8, '0'), "Test0000");
	}
	
	@Test
	public void testHex1() {
		Assert.assertEquals(Util.isHex("3C1E0F07B8DC6EB70000000000000000"), true);
	}
	
	@Test
	public void testHex2() {
		Assert.assertEquals(Util.isHex("3C1E0F07B8DC6EB7000000000000000X"), false);
	}
	
	@Test
	public void testByteToHex1() {
		Assert.assertEquals(Util.toByteFromHex("3C1E0F07B8DC6EB70000000000000000").length, 16);
	}
	
	@Test
	public void testByteToHex2() {
		Assert.assertEquals(Util.toIntFromHex("3C1E0F0"), 63037680);
	}
}
