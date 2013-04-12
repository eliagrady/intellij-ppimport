package be.mavicon.intellij.ppimport;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

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

	private final ProgressIndicator progressIndicator;
	private static final Logger LOGGER = Logger.getInstance(PPImporter.class);

	public PPImporter(ProgressIndicator progressIndicator) {
		this.progressIndicator = progressIndicator;
	}

	public void doImport(VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions, final boolean makeJar) {
		PPImportPlugin.doNotify("Starting import into " + target.getProfile(), NotificationType.INFORMATION);
		if (virtualFiles.length == 1 && !virtualFiles[0].isDirectory()) {
			VirtualFile virtualFile = virtualFiles[0];
			doImportSingleFile(virtualFile, target, includeExtensions);
		} else if (makeJar) {
			doBuildAndPostJar(virtualFiles, target, includeExtensions);
		} else {
			doImportMultiple(virtualFiles, target, includeExtensions);
		}
		PPImportPlugin.doNotify("Finished import into " + target.getProfile(), NotificationType.INFORMATION);
	}

	private void doBuildAndPostJar(VirtualFile[] virtualFiles, Target target, List<String> includeExtensions) {
		try {
			byte[] jarFile = makeJar(virtualFiles, includeExtensions);
			if (jarFile != null) {
				InputStream dataIS = new ByteArrayInputStream(jarFile);
				String contentType = "application/octet-stream";
				postData("jar-file", dataIS, contentType, "&type=jar", target);
			}
		} catch (IOException e) {
			PPImportPlugin.doNotify("Import failed: " + e.getMessage(), NotificationType.ERROR);
		}
	}

	private void doImportSingleFile(VirtualFile virtualFile, Target target, List<String> includeExtensions) {
		if (virtualFile.isInLocalFileSystem() && includeExtensions.contains(virtualFile.getExtension())) {
			InputStream dataIS;
			try {
				progressIndicator.setIndeterminate(false);
				progressIndicator.setFraction(0.5D);
				progressIndicator.setText("Importing " + virtualFile.getName() + " ...");
				dataIS = new FileInputStream(virtualFile.getCanonicalPath());
				String contentType = "text/xml;charset=" + virtualFile.getCharset();
				postData(virtualFile.getName(), dataIS, contentType, "", target);
				progressIndicator.setFraction(1.0D);
			} catch (FileNotFoundException e) {
				PPImportPlugin.doNotify("Import of " + virtualFile.getName() + " failed: " + e.getMessage(), NotificationType.ERROR);
			}
		}
	}

	private void doImportMultiple(final VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions) {
		for (VirtualFile virtualFile : virtualFiles) {
			if (virtualFile.isDirectory()) {
				doImportDirectory(virtualFile, target, includeExtensions);
			} else {
				doImportSingleFile(virtualFile, target, includeExtensions);
			}
		}
	}

	private void doImportDirectory(VirtualFile virtualFile, final Target target, final List<String> includeExtensions) {
		progressIndicator.setIndeterminate(true);

		// build list of files to process
		Collection<VirtualFile> filesToProcess = getFileList(virtualFile, null);

		progressIndicator.setIndeterminate(false);
		progressIndicator.setFraction(0.0D);
		double numberFiles = filesToProcess.size();

		int counter = 0;
		for (VirtualFile file : filesToProcess) {
			progressIndicator.setFraction((double) counter / numberFiles);
			progressIndicator.setText("Importing " + file.getName() + " ...");

			doImportSingleFile(file, target, includeExtensions);
		}
	}

	private byte[] makeJar(VirtualFile[] files, final List<String> includeExtensions) throws IOException {
		JarOutputStream jarOS = null;
		ByteArrayOutputStream byteOS = null;
		try {
			progressIndicator.setIndeterminate(true);

			// build list of files to process
			Collection<VirtualFile> filesToProcess = new LinkedHashSet<VirtualFile>();
			for (VirtualFile file : files) {
				filesToProcess.addAll(getFileList(file, new VirtualFileFilter() {
					@Override
					public boolean accept(VirtualFile virtualFile) {
						return includeExtensions.contains(virtualFile.getExtension());
					}
				}));
			}

			progressIndicator.setIndeterminate(false);
			progressIndicator.setFraction(0.0D);
			double numberFiles = filesToProcess.size();

			byteOS = new ByteArrayOutputStream();
			jarOS = new JarOutputStream(byteOS);
			int counter = 0;
			for (VirtualFile file : filesToProcess) {
				progressIndicator.setFraction((double) counter / numberFiles);
				progressIndicator.setText("Adding " + file.getName() + " ...");
				addToJar(jarOS, file);
			}
			jarOS.flush();
			return byteOS.toByteArray();
		} finally {
			Closeables.closeQuietly(jarOS);
			Closeables.closeQuietly(byteOS);
		}
	}

	private void addToJar(JarOutputStream jarOS, VirtualFile file) throws IOException {
		JarEntry entry = new JarEntry(file.getCanonicalPath());
		entry.setTime(file.getTimeStamp());
		jarOS.putNextEntry(entry);
		Files.copy(new File(file.getCanonicalPath()), jarOS);
	}

	private void postData(final String name, final InputStream dataIS, final String contentType, final String extraParams, final Target target) {
		OutputStream outputStream = null;
		LOGGER.info("Doing HTTP POST for " + name);
		try {
			URL httpURL = buildURL(target, extraParams);
			HttpURLConnection httpConnection = (HttpURLConnection) httpURL.openConnection();
			httpConnection.setDoOutput(true);
			httpConnection.setRequestProperty("Content-Type", contentType);
			httpConnection.setRequestMethod("PUT");
			httpConnection.setConnectTimeout(2000);
			httpConnection.setReadTimeout(60000);
			httpConnection.connect();
			outputStream = httpConnection.getOutputStream();
			ByteStreams.copy(dataIS, outputStream);

			int responseCode = httpConnection.getResponseCode();
			String responseMessage = httpConnection.getResponseMessage();
			if (responseCode < 200 || responseCode >= 300) {
				PPImportPlugin.doNotify("Import of " + name + " failed: " + responseCode + " - " + responseMessage + "\nCheck the server log for more details.", NotificationType.ERROR);
			}
		} catch (IOException e) {
			PPImportPlugin.doNotify("Import of " + name + " failed: " + e.getMessage(), NotificationType.ERROR);
		} finally {
			Closeables.closeQuietly(dataIS);
			Closeables.closeQuietly(outputStream);
		}
	}

	private URL buildURL(Target target, String extraParams) throws MalformedURLException {
		StringBuilder url = new StringBuilder();
		url.append(target.getUrl());
		url.append("?result=true");
		url.append("&username=").append(target.getUser());
		url.append("&password=").append(target.getPassword());
		if (StringUtils.isNotBlank(extraParams)) {
			url.append(extraParams);
		}
		return new URL(url.toString());
	}

	@SuppressWarnings("UnsafeVfsRecursion")
	private void recurseFiles(@NotNull VirtualFile dir, @Nullable VirtualFileFilter filter, @NotNull ContentIterator iterator) {
		TreeSet<VirtualFile> sortedDeduplicatedFiles = new TreeSet<VirtualFile>(new Comparator<VirtualFile>() {
			@Override
			public int compare(VirtualFile f1, VirtualFile f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		Collections.addAll(sortedDeduplicatedFiles, dir.getChildren());
		for (VirtualFile child : sortedDeduplicatedFiles) {
			if (!child.isSymLink() && !child.isSpecialFile()) {
				if (child.isDirectory()) {
					recurseFiles(child, filter, iterator);
				} else if (filter == null || filter.accept(child)) {
					iterator.processFile(child);
				}
			}
		}
	}

	private Collection<VirtualFile> getFileList(@NotNull VirtualFile dir, @Nullable VirtualFileFilter filter) {
		LOGGER.info("Getting file list ...");
		final Collection<VirtualFile> result = new LinkedHashSet<VirtualFile>();
		recurseFiles(dir, filter, new ContentIterator() {
			@Override
			public boolean processFile(VirtualFile virtualFile) {
				return result.add(virtualFile);
			}
		});
		return result;
	}
}
