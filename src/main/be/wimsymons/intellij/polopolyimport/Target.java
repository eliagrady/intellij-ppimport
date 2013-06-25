package be.wimsymons.intellij.polopolyimport;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

/**
 * Target contains the basic data of a target place to where files can be sent.
 */
public class Target {

	private String profile;
	private String url;
	private String user;
	private String password;
	private boolean confirm;

	@SuppressWarnings("UnusedDeclaration")
	public Target() {
		// empty default constructor for serialization
	}

	public Target(String profile, String url, String user, String password, boolean confirm) {
		this.profile = profile;
		this.url = url;
		this.user = user;
		this.password = password;
		this.confirm = confirm;
	}

	public Target(final Target another) {
		this.profile = another.profile;
		this.url = another.url;
		this.user = another.user;
		this.password = another.password;
		this.confirm = another.confirm;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConfirm(boolean confirm) {
		this.confirm = confirm;
	}

	public String getProfile() {
		return profile;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public boolean isConfirm() {
		return confirm;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("profile", profile)
			.append("url", url)
			.append("user", user)
			.append("password", password)
			.append("confirm", confirm)
			.toString();
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
		Target rhs = (Target) theOther;
		return new EqualsBuilder()
			.append(profile, rhs.profile)
			.append(url, rhs.url)
			.append(user, rhs.user)
			.append(password, rhs.password)
			.append(confirm, rhs.confirm)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(profile)
			.append(url)
			.append(user)
			.append(password)
			.append(confirm)
			.toHashCode();
	}

}
