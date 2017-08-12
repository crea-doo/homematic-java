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
