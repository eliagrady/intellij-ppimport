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
	private JTextField fileExtensions;
	private JCheckBox packMultipleFilesInJarCheckBox;
	private int currentSelection = -1;

	private static final String[] COLUMN_NAMES = {"profile", "url"};

	public ConfigPanel() {
		config = new PPConfiguration();

		profileTable.setModel(new ProfileTableModel());
		profileTable.getEmptyText().appendText("No profiles defined");
		profileTable.getSelectionModel().addListSelectionListener(new ProfileTableSelectionListener());
		profileTable.getColumnModel().getColumn(0).setPreferredWidth(120);
		profileTable.getColumnModel().getColumn(1).setPreferredWidth(210);
		addButton.addActionListener(new AddActionListener());
		removeButton.addActionListener(new RemoveActionListener());
		upButton.addActionListener(new UpActionListener());
		downButton.addActionListener(new DownActionListener());
		setActionStates();
	}

	public void setConfig(PPConfiguration aConfig) {
		currentSelection = -1;
		this.config = new PPConfiguration(aConfig);
		fileExtensions.setText(this.config.getFileExtensions());
		packMultipleFilesInJarCheckBox.setSelected(this.config.isPackMultipleFilesInJar());
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
		this.config.setFileExtensions(fileExtensions.getText());
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
		fileExtensions.setText(this.config.getFileExtensions());
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

	private class ProfileTableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent listSelectionEvent) {
			saveCurrentSelection();
			int selectedRow = profileTable.getSelectedRow();
			setCurrentSelection(selectedRow);
			setActionStates();
		}
	}

	private class ProfileTableModel extends AbstractTableModel {
		@Override
		public int getRowCount() {
			return config.getTargets().size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Target target = config.getTargets().get(row);
			if (target != null) {
				if (column == 1) {
					return target.getUrl();
				} else {
					return target.getProfile();
				}
			}
			return null;
		}

	}

	private class AddActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			config.addDefaultTarget();
			int newIndex = config.getTargets().size() - 1;
			setCurrentSelection(newIndex);
			setTableSelection(newIndex);
		}
	}

	private class RemoveActionListener implements ActionListener {
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

	private class UpActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (currentSelection > 0) {
				Collections.swap(config.getTargets(), currentSelection, currentSelection - 1);
				setCurrentSelection(currentSelection);
			}
			setTableSelection(currentSelection - 1);
		}
	}

	private class DownActionListener implements ActionListener {

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
