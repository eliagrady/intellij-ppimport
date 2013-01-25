package be.mavicon.intellij.ppimport.ui;

import be.mavicon.intellij.ppimport.PPConfiguration;
import be.mavicon.intellij.ppimport.Target;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

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

public class ConfigPanel {

    private PPConfiguration config;

    private JComponent rootPanel;
    private JTextField profile;
    private JTextField url;
    private JTextField user;
    private JTextField password;
    private JCheckBox importNeedsConfirmationCheckBox;
    private JBTable profileTable;
    private JButton addButton;
    private JButton removeButton;
    private JButton upButton;
    private JButton downButton;
    private JTextField fileExtentions;
    private JCheckBox packMultipleFilesInJarCheckBox;
    private int currentSelection = -1;

    private static final String[] COLUMN_NAMES = {"profile", "url"};

    public ConfigPanel(PPConfiguration config) {
        profileTable.setModel(new ProfileTableModel());
        profileTable.getEmptyText().appendText("No profiles defined");
        profileTable.getSelectionModel().addListSelectionListener(new ProfileTableSelectionListener());
        profileTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        profileTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        addButton.addActionListener(new AddActionListener());
        removeButton.addActionListener(new RemoveActionListener());
        upButton.addActionListener(new UpActionListener());
        downButton.addActionListener(new DownActionListener());
        this.setConfig(config);
    }

    public void setConfig(PPConfiguration config) {
        currentSelection = -1;
        try {
            this.config = config.clone();
        } catch (CloneNotSupportedException e) {
            this.config = new PPConfiguration();
        }
        fileExtentions.setText(this.config.getFileExtensions());
        packMultipleFilesInJarCheckBox.setSelected(this.config.isPackMultipleFilesInJar());
        if (this.config.getTargets() != null && this.config.getTargets().size() > 0) {

        }
        setActionStates();
    }

    public JComponent getRootPanel() {
        return this.rootPanel;
    }

    public PPConfiguration getConfig() {
        saveCurrentSelection();
        return this.config;
    }

    private void saveCurrentSelection() {
        if (currentSelection >= 0 && currentSelection < profileTable.getRowCount()) {
            Target target = new Target(
                    profile.getText(),
                    url.getText(),
                    user.getText(),
                    password.getText(),
                    importNeedsConfirmationCheckBox.isSelected()
            );
            this.config.getTargets().set(currentSelection, target);
        }
        this.config.setFileExtensions(fileExtentions.getText());
        this.config.setPackMultipleFilesInJar(packMultipleFilesInJarCheckBox.isSelected());
    }

    private void setTableSelection(int row) {
        if (row >= 0 && row < profileTable.getRowCount()) {
            profileTable.setRowSelectionInterval(row, row);
        }
        setActionStates();
    }

    private void setCurrentSelection(int i) {
        currentSelection = i;
        if (i >= 0 && i < profileTable.getRowCount()) {
            Target target = config.getTargets().get(i);
            if (target != null) {
                profile.setText(target.getProfile());
                url.setText(target.getUrl());
                user.setText(target.getUser());
                password.setText(target.getPassword());
                importNeedsConfirmationCheckBox.setSelected(target.isConfirm());
            }

        } else {
            profile.setText("");
            url.setText("");
            user.setText("");
            password.setText("");
            importNeedsConfirmationCheckBox.setEnabled(false);
        }
        fileExtentions.setText(this.config.getFileExtensions());
        packMultipleFilesInJarCheckBox.setSelected(this.config.isPackMultipleFilesInJar());
    }

    private void setActionStates() {
        if (currentSelection < 0) {
            downButton.setEnabled(false);
            upButton.setEnabled(false);
            removeButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
            downButton.setEnabled(currentSelection < (config.getTargets().size() - 1));
            upButton.setEnabled(currentSelection > 0);
        }
    }

    class ProfileTableSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            saveCurrentSelection();
            int selectedRow = profileTable.getSelectedRow();
            setCurrentSelection(selectedRow);
            setActionStates();
        }
    }

    class ProfileTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return config.getTargets().size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int i) {
            return COLUMN_NAMES[i];
        }

        @Override
        public Class<?> getColumnClass(int i) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int i, int i2) {
            return false;
        }

        @Override
        public Object getValueAt(int i, int i2) {
            Target target = config.getTargets().get(i);
            if (i2 == 1) return target.getUrl();
            return target.getProfile();
        }

    }

    class AddActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Target newTarget = new Target("<new>", "url", "username", "password", true);
            config.getTargets().add(newTarget);
            int newIndex = config.getTargets().size() - 1;
            setCurrentSelection(newIndex);
            setTableSelection(newIndex);
        }
    }

    class RemoveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int[] selectedRows = profileTable.getSelectedRows();
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                config.getTargets().remove(selectedRows[i]);
            }
            int lastSelectedRow = selectedRows[selectedRows.length - 1];
            int lastPossibleSelection = config.getTargets().size() - 1;
            if (lastSelectedRow > lastPossibleSelection) {
                lastSelectedRow = lastPossibleSelection;
            }
            setCurrentSelection(-1);
            setTableSelection(lastSelectedRow);
        }
    }

    class UpActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (currentSelection > 0) {
                Collections.swap(config.getTargets(), currentSelection, currentSelection - 1);
                setCurrentSelection(currentSelection);
            }
            setTableSelection(currentSelection - 1);
        }
    }

    class DownActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (currentSelection < (config.getTargets().size() - 1)) {
                Collections.swap(config.getTargets(), currentSelection, currentSelection + 1);
                setCurrentSelection(currentSelection);
            }
            setTableSelection(currentSelection + 1);
        }
    }
}
