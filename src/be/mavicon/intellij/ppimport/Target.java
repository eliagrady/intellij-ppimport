package be.mavicon.intellij.ppimport;

import java.io.Serializable;

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
public class Target implements Serializable {

    private String profile;
    private String url;
    private String user;
    private String password;
    private boolean confirm;

    public Target() {
    }

    public Target(String profile, String url, String user, String password, boolean confirm) {
        this.profile = profile;
        this.url = url;
        this.user = user;
        this.password = password;
        this.confirm = confirm;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    @Override
    public String toString() {
        return "Target[" + profile + "; " + url + "; " + user + "]";
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
                    this.isConfirm() == other.isConfirm();
        }
        return result;
    }

}
