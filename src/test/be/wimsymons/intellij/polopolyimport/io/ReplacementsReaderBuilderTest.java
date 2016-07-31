package be.wimsymons.intellij.polopolyimport.io;

import be.wimsymons.intellij.polopolyimport.Replacement;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ReplacementsReaderBuilder}.
 *
 * @author Wim Symons
 */
public class ReplacementsReaderBuilderTest {

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

  @Test
  public void doFileReplacements() throws IOException {
    Reader in = null;
    Reader replacementsReader = null;
    StringWriter writer = null;
    try {
      final InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("testdata.xml");
      in = new InputStreamReader(resourceAsStream,
        Charset.forName("UTF-8"));
      List<Replacement> replacements = ImmutableList.of(
        new Replacement("dummy_content", "other_content")
      );
      replacementsReader = ReplacementsReaderBuilder.with(in, replacements);
      writer = new StringWriter();
      CharStreams.copy(replacementsReader, writer);
      String actual = writer.toString();
      assertThat(actual, CoreMatchers.containsString("other_content"));
    } finally {
      Closeables.close(replacementsReader, true);
      Closeables.close(in, true);
      Closeables.close(writer, true);
    }
  }

  private String doReplace(String originalString, List<Replacement> replacements) throws IOException {
    Reader reader = null;
    StringWriter writer = null;
    Reader in = null;
    String actual;
    try {
      in = new StringReader(originalString);
      reader = ReplacementsReaderBuilder.with(in, replacements);
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
