package be.wimsymons.intellij.polopolyimport.io;

import be.wimsymons.intellij.polopolyimport.Replacement;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.codehaus.swizzle.stream.ReplaceStringsInputStream;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Builder to wrap a normal inputstream with replacements.
 */
public class ReplacementsInputStreamBuilder {

	public static InputStream with(final InputStream in, final List<Replacement> replacements) {
		Map<String, String> tokenMap = Maps.newTreeMap();
		for (Replacement replacement : replacements) {
			if (!Strings.isNullOrEmpty(replacement.getSearch())) {
				tokenMap.put(replacement.getSearch(), replacement.getReplacement());
			}
		}
		return new ReplaceStringsInputStream(in, tokenMap);
	}

}
