package be.wimsymons.intellij.polopolyimport.io;

import be.wimsymons.intellij.polopolyimport.Replacement;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link ReplacementsInputStreamBuilder}.
 *
 * @author Wim Symons
 */
public class ReplacementsInputStreamBuilderTest {

	@Test
	public void testNoReplace() throws IOException {
		List<Replacement> replacements = Collections.emptyList();
		String originalString = "R.I.P. James Gandolfini.";

		String actual = doReplace(originalString, replacements);

		assertEquals("R.I.P. James Gandolfini.", actual);
	}

	@Test
	public void testSimpleReplace() throws IOException {
		List<Replacement> replacements = ImmutableList.of(new Replacement("James Gandolfini", "Tony Soprano"));
		String originalString = "R.I.P. James Gandolfini.";

		String actual = doReplace(originalString, replacements);

		assertEquals("R.I.P. Tony Soprano.", actual);
	}

	@Test
	public void testReplaceMultipleInstances() throws IOException {
		List<Replacement> replacements = ImmutableList.of(new Replacement("abc", "xyz"));
		String originalString = "abcdefabcdefabcdef";

		String actual = doReplace(originalString, replacements);

		assertEquals("xyzdefxyzdefxyzdef", actual);
	}

	@Test
	public void testReplaceLargeBuffer() throws IOException {
		List<Replacement> replacements = ImmutableList.of(new Replacement("0123456789", ""));
		String originalString = Strings.repeat("0123456789ABCDEF", 256);

		String actual = doReplace(originalString, replacements);

		assertEquals(Strings.repeat("ABCDEF", 256), actual);
	}

	@Test
	public void testMultipleReplace() throws IOException {
		List<Replacement> replacements = ImmutableList.of(
			new Replacement("{author}", "Wim Symons"),
			new Replacement("{date}", "21-07-2013")
		);
		String originalString = "This example was coded by {author} on {date}.";

		String actual = doReplace(originalString, replacements);

		assertEquals("This example was coded by Wim Symons on 21-07-2013.", actual);
	}

	private String doReplace(String originalString, List<Replacement> replacements) throws IOException {
		Reader reader = null;
		StringWriter writer = null;
		ByteArrayInputStream in = null;
		String actual;
		try {
			in = new ByteArrayInputStream(originalString.getBytes());
			if (replacements.isEmpty()) {
				reader = new InputStreamReader(in);
			} else {
				reader = new InputStreamReader(ReplacementsInputStreamBuilder.with(in, replacements));
			}
			writer = new StringWriter();
			CharStreams.copy(reader, writer);
			actual = writer.toString();
		} finally {
			Closeables.close(reader, true);
			Closeables.close(in, true);
			Closeables.close(writer, true);
		}
		return actual;
	}

}
