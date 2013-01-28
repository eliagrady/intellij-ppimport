package be.mavicon.intellij.ppimport;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/*
 * Copyright 2013 Marc Viaene (Mavicon BVBA)
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

	public String profile;
	public String url;
	public String user;
	public String password;
	public boolean confirm;

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

	public Target(Target another) {
		this.profile = another.profile;
		this.url = another.url;
		this.user = another.user;
		this.password = another.password;
		this.confirm = another.confirm;
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
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

	@Override
	public boolean equals(Object theOther) {
		boolean result = false;
		if (theOther instanceof Target) {
			Target other = (Target) theOther;
			result = this.profile.equals(other.profile) &&
				this.url.equals(other.url) &&
				this.user.equals(other.user) &&
				this.password.equals(other.password) &&
				this.confirm == other.confirm;
		}
		return result;
	}

}
