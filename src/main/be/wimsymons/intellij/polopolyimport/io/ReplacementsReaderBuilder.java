package be.wimsymons.intellij.polopolyimport.io;

import be.wimsymons.intellij.polopolyimport.Replacement;
import com.google.common.base.Strings;
import sdsu.io.StringReplaceReader;

import java.io.Reader;
import java.util.List;

public class ReplacementsReaderBuilder {

  public static Reader with(final Reader in, final List<Replacement> replacements) {
    if (replacements.isEmpty()) {
      return in;
    } else {
      StringReplaceReader replaceReader = null;
      for (Replacement replacement : replacements) {
        if (!Strings.isNullOrEmpty(replacement.getSearch())) {
          if (replaceReader == null) {
            replaceReader = new StringReplaceReader(in, replacement.getSearch(), replacement.getReplacement());
          } else {
            replaceReader.replace(replacement.getSearch(), replacement.getReplacement());
          }
        }
      }
      return replaceReader;
    }
  }
}
