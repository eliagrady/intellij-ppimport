package be.wimsymons.intellij.polopolyimport;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

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

public class PPConfiguration {

	private List<Target> targets = new ArrayList<Target>();
	private List<Replacement> replacements = new ArrayList<Replacement>();
	private String fileExtensions = "xml";
	private boolean packMultipleFilesInJar = false;

	public PPConfiguration() {
	}

	public PPConfiguration(PPConfiguration another) {
		for (Target target : another.targets) {
			targets.add(new Target(target));
		}
		for (Replacement replacement : another.replacements) {
			replacements.add(new Replacement(replacement));
		}
		fileExtensions = another.fileExtensions;
		packMultipleFilesInJar = another.packMultipleFilesInJar;
	}

	public void addDefaultTarget() {
		targets.add(new Target("<new>", "http://<host>/polopoly/import", "sysadmin", "", false));
	}

	public List<Target> getTargets() {
		return targets;
	}

	public void setTargets(List<Target> targets) {
		this.targets = targets;
	}

	public void setReplacements(List<Replacement> replacements) {
		this.replacements = replacements;
	}

	public List<Replacement> getReplacements() {
		return replacements;
	}

	public String getFileExtensions() {
		return fileExtensions;
	}

	public void setFileExtensions(String fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public boolean isPackMultipleFilesInJar() {
		return packMultipleFilesInJar;
	}

	public void setPackMultipleFilesInJar(boolean packMultipleFilesInJar) {
		this.packMultipleFilesInJar = packMultipleFilesInJar;
	}

	@Override
	public boolean equals(Object theOther) {
		if (theOther == null) {
			return false;
		}
		if (theOther == this) {
			return true;
		}
		if (theOther.getClass() != getClass()) {
			return false;
		}
		PPConfiguration rhs = (PPConfiguration) theOther;
		return new EqualsBuilder()
			.append(targets, rhs.targets)
			.append(replacements, rhs.replacements)
			.append(fileExtensions, rhs.fileExtensions)
			.append(packMultipleFilesInJar, rhs.packMultipleFilesInJar)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(targets)
			.append(replacements)
			.append(fileExtensions)
			.append(packMultipleFilesInJar)
			.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(ToStringStyle.SHORT_PREFIX_STYLE)
			.append("targets", targets)
			.append("replacements", replacements)
			.append("fileExtensions", fileExtensions)
			.append("packMultipleFilesInJar", packMultipleFilesInJar)
			.toString();
	}
}
