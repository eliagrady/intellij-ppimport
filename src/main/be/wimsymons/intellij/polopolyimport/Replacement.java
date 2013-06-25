package be.wimsymons.intellij.polopolyimport;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Replacement contains the basic data of a text replacement.
 */
public class Replacement {

	private String search;
	private String replacement;

	@SuppressWarnings("UnusedDeclaration")
	public Replacement() {
		// empty default constructor for serialization
	}

	public Replacement(String search, String replacement) {
		this.search = search;
		this.replacement = replacement;
	}

	public Replacement(final Replacement replacement) {
		this.search = replacement.search;
		this.replacement = replacement.replacement;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
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
		Replacement rhs = (Replacement) theOther;
		return new EqualsBuilder()
			.append(search, rhs.search)
			.append(replacement, rhs.replacement)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(search)
			.append(replacement)
			.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("search", search)
			.append("replacement", replacement)
			.toString();
	}
}
