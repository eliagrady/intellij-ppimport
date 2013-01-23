package be.mavicon.intellij.ppimport.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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

public class ConfirmDialog extends DialogWrapper {

    String profile;
    JPanel contentPane;
    private JLabel areYouSureLabel;

    public ConfirmDialog(String profile) {
        super(false);
        this.profile = profile;
        this.init();
        this.setModal(true);
        this.setTitle("Confirm your action.");
        this.setOKActionEnabled(true);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        areYouSureLabel.setText("Are you sure you want to import the selected files to " + profile + "?");
        return contentPane;
    }


    public void setData(ConfirmDialog data) {
    }

    public void getData(ConfirmDialog data) {
    }

    public boolean isModified(ConfirmDialog data) {
        return false;
    }
}
