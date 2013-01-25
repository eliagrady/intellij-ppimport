package be.mavicon.intellij.ppimport;

import java.util.ArrayList;
import java.util.List;

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

public class PPConfiguration implements Cloneable {

    List<Target> targets = new ArrayList<Target>();
    String fileExtensions = "xml";
    boolean packMultipleFilesInJar = false;

    void init() {
        if (targets.isEmpty()) {
            targets.add(new Target("localhost", "http://localhost:8080/polopoly/import", "sysadmin", "password",false));
        }
        fileExtensions = "xml";
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
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
        boolean result = false;
        if (theOther instanceof PPConfiguration) {
            boolean allEquals = true;
            PPConfiguration other = (PPConfiguration) theOther;
            if(!this.getFileExtensions().equals(other.getFileExtensions())) {
                allEquals = false;
            }   else if (this.packMultipleFilesInJar != other.packMultipleFilesInJar) {
                allEquals = false;
            } else if (targets.size() == other.targets.size() ) {
                for (int i = 0; i < targets.size(); i++) {
                    if(!targets.get(i).equals(other.targets.get(i))) {
                        allEquals = false;
                    }
                }
            } else {
                allEquals = false;
            }
            result = allEquals;
        }
        return result;
    }

    @Override
    public PPConfiguration clone() throws CloneNotSupportedException {
        PPConfiguration clone = new PPConfiguration();
        List<Target> cloneTargets = new ArrayList<Target>();
        clone.setTargets(cloneTargets);
        for (Target target : this.targets) {
            clone.getTargets().add(new Target(target.getProfile(), target.getUrl(),target.getUser(),target.getPassword(), target.isConfirm()));
            clone.setFileExtensions(fileExtensions);
            clone.setPackMultipleFilesInJar(packMultipleFilesInJar);
        }
        return clone;
    }


}
