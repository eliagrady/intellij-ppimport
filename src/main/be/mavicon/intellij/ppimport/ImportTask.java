package be.mavicon.intellij.ppimport;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
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
class ImportTask extends Task.Backgroundable {

	private final VirtualFile[] virtualFiles;
	private final Target target;
	private final List<String> includeExtensions;
	private final boolean makeJar;
	private final List<Replacement> replacements;

	public ImportTask(final VirtualFile[] virtualFiles, final Target target, final List<String> includeExtensions, final List<Replacement> replacements, final boolean makeJar) {
		super(null, "Polopoly Import progress", true);
		this.virtualFiles = virtualFiles;
		this.target = target;
		this.includeExtensions = includeExtensions;
		this.replacements = replacements;
		this.makeJar = makeJar;
	}

	@Override
	public void run(@NotNull ProgressIndicator progressIndicator) {
		PPImporter importer = new PPImporter(progressIndicator, target, includeExtensions, replacements, makeJar);
		importer.doImport(virtualFiles);
	}

}
