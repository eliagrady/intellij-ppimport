package be.mavicon.intellij.ppimport.io;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link StringReplaceReader}.
 *
 * @author Wim Symons
 */
public class StringReplaceReaderTest {

	@Test
	public void testReplace() throws IOException {
		Reader reader = new StringReplaceReader(new StringReader("R.I.P. James Gandolfini."), "James Gandolfini", "Tony Soprano");
		StringWriter writer = new StringWriter();
		CharStreams.copy(reader, writer);
		assertEquals("R.I.P. Tony Soprano.", writer.toString());
	}

	@Test
	public void testReplaceLongOne() throws IOException {
		Reader reader = new StringReplaceReader(new StringReader(Strings.repeat("0123456789ABCDEF", 256)), "0123456789", "");
		StringWriter writer = new StringWriter();
		CharStreams.copy(reader, writer);
		assertEquals(Strings.repeat("ABCDEF", 256), writer.toString());
	}

}
