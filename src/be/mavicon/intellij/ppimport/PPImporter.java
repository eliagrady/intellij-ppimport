package be.mavicon.intellij.ppimport;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.notification.NotificationType;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

	private final ScheduledExecutorService executorService;

	public PPImporter() {
		executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("polopoly-import-%d").build());
	}

	public void doImport(VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions, final boolean makeJar) {
		PPImportPlugin.doNotify("Starting import to " + target.getProfile(), NotificationType.INFORMATION);
		if (virtualFiles.length == 1 && !virtualFiles[0].isDirectory()) {
			VirtualFile virtualFile = virtualFiles[0];
			doAsyncImportSingle(virtualFile, target, includeExtensions);
		} else if (makeJar) {
			doAsyncBuildAndPostJar(virtualFiles, target, includeExtensions);
		} else {
			doAsyncImportMultiple(virtualFiles, target, includeExtensions);
		}
	}

	public void shutdown() {
		executorService.shutdown();
	}

	private void doAsyncImportSingle(final VirtualFile virtualFile, final Target target, final List<String> includeExtensions) {
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					doImportSingleFile(virtualFile, target, includeExtensions);
					PPImportPlugin.doNotify("Import successful.", NotificationType.INFORMATION);
				} catch (IOException e) {
					PPImportPlugin.doNotify("Import failed:\n" + e.getMessage() + "\n\nCheck the server log for more details.", NotificationType.ERROR);
				}
			}
		}, 0, TimeUnit.SECONDS);
	}

	private void doAsyncImportMultiple(final VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions) {
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					doImportMultiple(virtualFiles, target, includeExtensions);
					PPImportPlugin.doNotify("Import successful.", NotificationType.INFORMATION);
				} catch (IOException e) {
					PPImportPlugin.doNotify("Import failed:\n" + e.getMessage() + "\n\nCheck the server log for more details.", NotificationType.ERROR);
				}
			}
		}, 0, TimeUnit.SECONDS);
	}

	private void doAsyncBuildAndPostJar(final VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions) {
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					doBuildAndPostJar(virtualFiles, target, includeExtensions);
					PPImportPlugin.doNotify("Import successful.", NotificationType.INFORMATION);
				} catch (IOException e) {
					PPImportPlugin.doNotify("Import failed:\n" + e.getMessage() + "\n\nCheck the server log for more details.", NotificationType.ERROR);
				}
			}
		}, 0, TimeUnit.SECONDS);
	}

	private void doBuildAndPostJar(VirtualFile[] virtualFiles, Target target, List<String> includeExtensions) throws IOException {
		byte[] jarFile = makeJar(virtualFiles, includeExtensions);
		if (jarFile != null) {
			InputStream dataIS = new ByteArrayInputStream(jarFile);
			String contentType = "application/octet-stream";
			postData("jar-file", dataIS, contentType, "&type=jar", target);
		}
	}

	private void doImportSingleFile(VirtualFile virtualFile, Target target, List<String> includeExtensions) throws IOException {
		if (virtualFile.isInLocalFileSystem() && includeExtensions.contains(virtualFile.getExtension())) {
			InputStream dataIS = new FileInputStream(virtualFile.getCanonicalPath());
			String contentType = "text/xml;charset=" + virtualFile.getCharset();
			postData(virtualFile.getName(), dataIS, contentType, "", target);
		}
	}

	private void doImportMultiple(final VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions) throws IOException {
		for (VirtualFile virtualFile : virtualFiles) {
			if (virtualFile.isDirectory()) {
				doImportDirectory(virtualFile, target, includeExtensions);
			} else {
				doImportSingleFile(virtualFile, target, includeExtensions);
			}
		}
	}

	private void doImportDirectory(VirtualFile virtualFile, final Target target, final List<String> includeExtensions) {
		recurseFiles(
			virtualFile,
			null,
			new ContentIterator() {
				@Override
				public boolean processFile(VirtualFile aVirtualFile) {
					try {
						doImportSingleFile(aVirtualFile, target, includeExtensions);
					} catch (IOException e) {
						PPImportPlugin.doNotify("Import failed:\n" + e.getMessage() + "\n\nCheck the server log for more details.", NotificationType.ERROR);
					}
					return true;
				}
			}
		);
	}

	private byte[] makeJar(VirtualFile[] files, final List<String> includeExtensions) throws IOException {
		JarOutputStream jarOS = null;
		ByteArrayOutputStream byteOS = null;
		try {
			byteOS = new ByteArrayOutputStream();
			jarOS = new JarOutputStream(byteOS);
			for (VirtualFile file : files) {
				final JarOutputStream finalJarOS = jarOS;
				recurseFiles(
					file,
					new VirtualFileFilter() {
						@Override
						public boolean accept(VirtualFile virtualFile) {
							return includeExtensions.contains(virtualFile.getExtension());
						}
					},
					new ContentIterator() {
						@Override
						public boolean processFile(VirtualFile virtualFile) {
							try {
								addToJar(finalJarOS, virtualFile);
							} catch (IOException e) {
								PPImportPlugin.doNotify("Adding " + virtualFile.getName() + " to JAR failed.", NotificationType.ERROR);
							}
							return true;
						}
					}
				);
			}
			jarOS.flush();
			return byteOS.toByteArray();
		} finally {
			Closeables.closeQuietly(jarOS);
			Closeables.closeQuietly(byteOS);
		}
	}

	private void addToJar(JarOutputStream jarOS, VirtualFile file) throws IOException {
		ZipEntry entry = new ZipEntry(file.getCanonicalPath());
		jarOS.putNextEntry(entry);
		Files.copy(new File(file.getCanonicalPath()), jarOS);
	}

	private void postData(final String name, final InputStream dataIS, final String contentType, final String extraParams, final Target target) {
		OutputStream outputStream = null;
		try {
			URL httpURL = buildURL(target, extraParams);
			HttpURLConnection httpConnection = (HttpURLConnection) httpURL.openConnection();
			httpConnection.setDoOutput(true);
			httpConnection.setRequestProperty("Content-Type", contentType);
			httpConnection.setRequestMethod("PUT");
			httpConnection.connect();
			outputStream = httpConnection.getOutputStream();
			ByteStreams.copy(dataIS, outputStream);

			int responseCode = httpConnection.getResponseCode();
			String responseMessage = httpConnection.getResponseMessage();
			if (responseCode < 200 || responseCode >= 300) {
				PPImportPlugin.doNotify("Import of " + name + " failed: " + responseCode + " - " + responseMessage + "\nCheck the server log for more details.", NotificationType.ERROR);
			}
		} catch (Exception e) {
			PPImportPlugin.doNotify("Import failed: " + e.getMessage() + "\nCheck the server log for more details.", NotificationType.ERROR);
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

	private void recurseFiles(@NotNull VirtualFile dir, @Nullable VirtualFileFilter filter, @NotNull ContentIterator iterator) {
		@SuppressWarnings("UnsafeVfsRecursion") VirtualFile[] children = dir.getChildren();
		Arrays.sort(children, new Comparator<VirtualFile>() {
			@Override
			public int compare(VirtualFile f1, VirtualFile f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		for (VirtualFile child : children) {
			if (!child.isSymLink() && !child.isSpecialFile()) {
				if (child.isDirectory()) {
					recurseFiles(child, filter, iterator);
				} else if (filter == null || filter.accept(child)) {
					iterator.processFile(child);
				}
			}
		}
	}
}
