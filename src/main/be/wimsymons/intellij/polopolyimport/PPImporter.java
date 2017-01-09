package be.wimsymons.intellij.polopolyimport;

import be.wimsymons.intellij.polopolyimport.io.ReplacementsReaderBuilder;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.CharSet;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/*
 * Copyright 2013 Marc Viaene (Mavicon BVBA)
 * Copyright 2013 Wim Symons (wim.symons@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
class PPImporter {

	private static final Logger LOGGER = Logger.getInstance(PPImporter.class);
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private final ProgressIndicator progressIndicator;
	private final Target target;
	private final List<String> includeExtensions;
	private final List<Replacement> replacements;
	private final boolean makeJar;

	private int successCount = 0;
	private int failureCount = 0;
	private int totalCount = 0;
	private int skippedCount = 0;

	public PPImporter(final ProgressIndicator progressIndicator, final Target target, final List<String> includeExtensions, final List<Replacement> replacements, final boolean makeJar) {
		this.progressIndicator = progressIndicator;
		this.target = target;
		this.includeExtensions = includeExtensions;
		this.replacements = replacements;
		this.makeJar = makeJar;
	}

	public void doImport(VirtualFile[] virtualFiles) {
		if (makeJar) {
			doBuildAndPostJar(virtualFiles);
		} else {
			doImportMultiple(virtualFiles);
		}
		if (progressIndicator.isCanceled()) {
			PPImportPlugin.doNotify("Import into " + target.getProfile() + " cancelled. " + getStats(), NotificationType.INFORMATION);
		} else {
			PPImportPlugin.doNotify("Finished import into " + target.getProfile() + ". " + getStats(), NotificationType.INFORMATION);
		}
	}

	private boolean canImport(final VirtualFile virtualFile) {
		return !virtualFile.isDirectory() && includeExtensions.contains(virtualFile.getExtension());
	}

	private String getStats() {
		return String.format("Total: %d, Success: %d, Failures: %d, Skipped: %d", totalCount, successCount, failureCount, skippedCount);
	}

	private void doBuildAndPostJar(VirtualFile[] virtualFiles) {
		try {
			byte[] jarFile = makeJar(virtualFiles);
			if (jarFile != null) {
				InputStream dataIS = new ByteArrayInputStream(jarFile);
				String contentType = "application/octet-stream";
				postData("jar-file", new InputStreamReader(dataIS), contentType, "&type=jar");
				progressIndicator.setFraction(1.0D);
			}
		} catch (IOException e) {
			PPImportPlugin.doNotify("Import failed: " + e.getMessage(), NotificationType.ERROR);
		}
	}

	private void doImportSingleFile(VirtualFile virtualFile) {
		if (canImport(virtualFile)) {
			InputStream dataIS;
			try {
				dataIS = virtualFile.getInputStream();
				String contentType = "text/xml&charset=" + virtualFile.getCharset();
				postData(virtualFile.getName(), wrapWithReplacements(dataIS, virtualFile.getCharset()), contentType, "");
//				postData(virtualFile.getName(), dataIS, contentType, "");
			} catch (IOException e) {
				PPImportPlugin.doNotify("Import of " + virtualFile.getName() + " failed: " + e.getMessage(), NotificationType.ERROR);
			}
		} else {
			skippedCount++;
		}
	}

	private void doImportMultiple(final VirtualFile[] virtualFiles) {
		progressIndicator.setIndeterminate(true);

		// build list of files to process
		Collection<VirtualFile> filesToProcess = new LinkedHashSet<VirtualFile>();
		for (VirtualFile virtualFile : virtualFiles) {
			filesToProcess.addAll(getFileList(virtualFile));
		}

		progressIndicator.setIndeterminate(false);
		progressIndicator.setFraction(0.0D);
		totalCount = filesToProcess.size();

		int counter = 0;
		for (VirtualFile file : filesToProcess) {
			if (progressIndicator.isCanceled()) {
				break;
			}

			counter++;
			progressIndicator.setFraction((double) counter / (double) totalCount);
			progressIndicator.setText("Importing " + file.getName() + " ...");

			LOGGER.info("Importing file " + (counter + 1) + "/" + totalCount);

			doImportSingleFile(file);
		}
	}

	private byte[] makeJar(VirtualFile[] files) throws IOException {
		JarOutputStream jarOS = null;
		ByteArrayOutputStream byteOS = null;
		try {
			progressIndicator.setIndeterminate(true);

			// build list of files to process
			Collection<VirtualFile> filesToProcess = new LinkedHashSet<VirtualFile>();
			for (VirtualFile virtualFile : files) {
				filesToProcess.addAll(getFileList(virtualFile));
			}

			progressIndicator.setIndeterminate(false);
			progressIndicator.setFraction(0.0D);

			totalCount = filesToProcess.size();

			byteOS = new ByteArrayOutputStream();
			jarOS = new JarOutputStream(byteOS);
			int counter = 0;
			for (VirtualFile file : filesToProcess) {
				if (progressIndicator.isCanceled()) {
					break;
				}

				counter++;
				progressIndicator.setFraction((double) counter / (double) totalCount * 0.5D);
				progressIndicator.setText("Adding " + file.getName() + " ...");

				if (canImport(file)) {
					LOGGER.info("Adding file " + (counter + 1) + "/" + totalCount);
					addToJar(jarOS, file);
				} else {
					skippedCount++;
				}
			}
			jarOS.flush();
			return progressIndicator.isCanceled() ? null : byteOS.toByteArray();
		} finally {
			Closeables.close(jarOS, true);
			Closeables.close(byteOS, true);
		}
	}

	private void addToJar(JarOutputStream jarOS, VirtualFile file) throws IOException {
		JarEntry entry = new JarEntry(file.getCanonicalPath());
		entry.setTime(file.getTimeStamp());
		jarOS.putNextEntry(entry);
		Reader reader = wrapWithReplacements(file.getInputStream(), file.getCharset());
		Writer writer = new OutputStreamWriter(jarOS);
		try {
			CharStreams.copy(reader, writer);
		} finally {
			Closeables.close(reader, true);
			Closeables.close(writer, true);
		}
	}

  private void postData(final String name, final Reader reader, final String contentType, final String extraParams) throws IOException {
    Writer writer = null;
    LOGGER.info("Doing HTTP POST for " + name);
    try {
      URL httpURL = buildURL(target, extraParams);

      HttpURLConnection httpConnection = (HttpURLConnection) httpURL.openConnection();
      httpConnection.setDoInput(true);
      httpConnection.setDoOutput(true);
      httpConnection.setUseCaches(false);
      httpConnection.setRequestMethod("PUT");
      httpConnection.setRequestProperty("Content-Type", contentType);
      httpConnection.setConnectTimeout(2000);
      httpConnection.setReadTimeout(60000);
      httpConnection.connect();

      if(contentType.contains("UTF-8")) {
        copyAndFlush(reader, httpConnection.getOutputStream());
      }
      else {
        writer = new OutputStreamWriter(httpConnection.getOutputStream());
        CharStreams.copy(reader, writer);
        writer.flush();
      }

      int responseCode = httpConnection.getResponseCode();
      String responseMessage = httpConnection.getResponseMessage();
      if (responseCode < 200 || responseCode >= 300) {
        failureCount++;
        PPImportPlugin.doNotify("Import of " + name + " failed: " + responseCode + " - " + responseMessage + "\nCheck the server log for more details.", NotificationType.ERROR);
      } else {
        successCount++;
      }
    } catch (IOException e) {
      failureCount++;
      PPImportPlugin.doNotify("Import of " + name + " failed: " + e.getMessage(), NotificationType.ERROR);
    } finally {
      Closeables.close(reader, true);
      Closeables.close(writer, true);
    }
  }

	private URL buildURL(Target target, String extraParams) throws MalformedURLException {
		StringBuilder url = new StringBuilder();
		url.append(target.getUrl());
		url.append("?result=true");
		url.append("&username=").append(target.getUser());
		url.append("&password=").append(target.getPassword());
		if (!Strings.isNullOrEmpty(extraParams)) {
			url.append(extraParams);
		}
		return new URL(url.toString());
	}

	@SuppressWarnings("UnsafeVfsRecursion")
	private void recurseFiles(@NotNull VirtualFile virtualFile, @NotNull ContentIterator iterator) {
		TreeSet<VirtualFile> sortedDeduplicatedFiles = new TreeSet<VirtualFile>(new Comparator<VirtualFile>() {
			@Override
			public int compare(VirtualFile f1, VirtualFile f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		if (virtualFile.isDirectory()) {
			Collections.addAll(sortedDeduplicatedFiles, virtualFile.getChildren());
			for (VirtualFile child : sortedDeduplicatedFiles) {
				recurseFiles(child, iterator);
			}
		} else {
			iterator.processFile(virtualFile);
		}
	}

	private Collection<VirtualFile> getFileList(@NotNull VirtualFile dir) {
		LOGGER.info("Getting file list ...");
		final Collection<VirtualFile> result = new LinkedHashSet<VirtualFile>();
		recurseFiles(dir, new ContentIterator() {
			@Override
			public boolean processFile(VirtualFile virtualFile) {
				return result.add(virtualFile);
			}
		});
		return result;
	}

	private Reader wrapWithReplacements(InputStream in, Charset charset) {
		if (replacements.isEmpty()) {
			return new InputStreamReader(in, charset);
		} else {
			return ReplacementsReaderBuilder.with(new InputStreamReader(in, charset), replacements);
		}
	}

  private void copyAndFlush(Reader reader, OutputStream writeTo) throws IOException {
    InputStream stream = new ReaderInputStream(reader, "UTF-8");
    byte[] bytes = new byte[1024];
    int len;

    while ((len = stream.read(bytes)) > 0) {
      writeTo.write(bytes, 0, len);
    }

    writeTo.flush();
  }
}
