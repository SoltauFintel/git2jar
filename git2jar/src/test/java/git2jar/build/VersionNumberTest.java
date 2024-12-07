package git2jar.build;

import org.junit.Assert;
import org.junit.Test;

public class VersionNumberTest {

	@Test
	public void test() {
		VersionNumber v = new VersionNumber("1.2.3");
		
		Assert.assertEquals("1.2.3", v.toString());
		Assert.assertEquals("00001.00002.00003", v.sort());
	}

	@Test
	public void alpha() {
		VersionNumber v = new VersionNumber("1.2.34-A16");
		
		Assert.assertEquals("1.2.34-a16", v.toString());
		Assert.assertEquals("00001.00002.00034-a16", v.sort());
	}

	@Test
	public void one() {
		VersionNumber v = new VersionNumber("1");
		
		Assert.assertEquals("1", v.toString());
		Assert.assertEquals("00001", v.sort());
	}
	
	@Test
	public void five() {
		VersionNumber v = new VersionNumber("1.2.3.4.54710");
		
		Assert.assertEquals("1.2.3.4.54710", v.toString());
		Assert.assertEquals("00001.00002.00003.00004.54710", v.sort());
	}
}
