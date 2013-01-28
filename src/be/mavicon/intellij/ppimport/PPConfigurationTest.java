package be.mavicon.intellij.ppimport;

import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test serialization done by IntelliJ.
 * {@author Wim Symons}
 */
public class PPConfigurationTest {

	@Test
	public void testSerialize() {
		PPConfiguration config = new PPConfiguration();
		config.addDefaultTarget();
		Element serialized = XmlSerializer.serialize(config);

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		String xml = outputter.outputString(serialized);

		Assert.assertNotNull("no blank xml", xml);
		Assert.assertTrue("fileExtensions not found", xml.contains("fileExtensions"));
		Assert.assertTrue("packMultipleFilesInJar not found", xml.contains("packMultipleFilesInJar"));
		Assert.assertTrue("targets not found", xml.contains("targets"));
		Assert.assertTrue("profile not found", xml.contains("profile"));
		Assert.assertTrue("url not found", xml.contains("url"));
		Assert.assertTrue("user not found", xml.contains("user"));
		Assert.assertTrue("password not found", xml.contains("password"));
		Assert.assertTrue("confirm not found", xml.contains("confirm"));
	}

	@Test
	public void testSerializeTarget() {
		PPConfiguration config = new PPConfiguration();
		config.addDefaultTarget();
		Element serialized = XmlSerializer.serialize(config.getTargets().get(0));

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		String xml = outputter.outputString(serialized);

		Assert.assertNotNull("no blank xml", xml);
		Assert.assertTrue("profile not found", xml.contains("profile"));
		Assert.assertTrue("url not found", xml.contains("url"));
		Assert.assertTrue("user not found", xml.contains("user"));
		Assert.assertTrue("password not found", xml.contains("password"));
		Assert.assertTrue("confirm not found", xml.contains("confirm"));
	}

	@Test
	public void testCopy() {
		PPConfiguration config = new PPConfiguration();
		config.addDefaultTarget();

		PPConfiguration copiedConfig = new PPConfiguration();
		XmlSerializerUtil.copyBean(config, copiedConfig);

		Assert.assertTrue("config not equal after copy: original=" + config + " copy=" + copiedConfig, EqualsBuilder.reflectionEquals(config, copiedConfig));
	}

}
