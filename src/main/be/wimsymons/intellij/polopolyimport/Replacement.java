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

/**
 * Replacement contains the basic data of a text replacement.
 */
@SuppressWarnings("WeakerAccess")
public class Replacement {

	public String search;
	public String replacement;

	@SuppressWarnings("UnusedDeclaration")
	public Replacement() {
		// empty default constructor for serialization
	}

	public Replacement(String search, String replacement) {
		this.search = search;
		this.replacement = replacement;
	}

	public Replacement(Replacement replacement) {
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Replacement that = (Replacement) o;

		if (replacement != null ? !replacement.equals(that.replacement) : that.replacement != null) return false;
		if (search != null ? !search.equals(that.search) : that.search != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = search != null ? search.hashCode() : 0;
		result = 31 * result + (replacement != null ? replacement.hashCode() : 0);
		return result;
	}
}
