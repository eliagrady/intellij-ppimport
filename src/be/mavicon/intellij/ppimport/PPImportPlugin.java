package be.mavicon.intellij.ppimport;

import be.mavicon.intellij.ppimport.ui.ConfigPanel;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
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


@State(
	name = "PPImportPlugin",
	storages = {
		@Storage(id = "ppimport", file = "$PROJECT_FILE$"),
		@Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/ppimport.xml", scheme = StorageScheme.DIRECTORY_BASED)
	}
)
public class PPImportPlugin implements ProjectComponent, Configurable, PersistentStateComponent<PPConfiguration> {

	private static final Logger LOGGER = Logger.getInstance(PPImportPlugin.class);

	private static final String PLUGIN_COMPONENT_NAME = "PPImportPlugin";
	private static final String PLUGIN_DISPLAY_NAME = "Polopoly Import";

	private ConfigPanel configGUI;
	private PPConfiguration state;

	@Override
	public void initComponent() {
		LOGGER.info(getDisplayName() + " Plugin loaded");
	}

	private void registerActions() {
		ActionManager am = ActionManager.getInstance();

		DefaultActionGroup group = (DefaultActionGroup) am.getAction("PPImportGroup");
		group.removeAll();

		List<String> includeExtensions = getIncludeExtensions(state.getFileExtensions());
		for (Target target : state.getTargets()) {
			AnAction action = new PPImportAction(target, includeExtensions, state.packMultipleFilesInJar);
			am.unregisterAction(target.getProfile());
			am.registerAction(target.getProfile(), action);
			group.add(action);
			LOGGER.info("Registered action for " + target);
		}
	}

	private List<String> getIncludeExtensions(String fileExtensions) {
		return Arrays.asList(StringUtils.split(StringUtils.defaultString(fileExtensions), ','));
	}

	@Override
	public void disposeComponent() {
	}

	@Nls
	@Override
	public String getDisplayName() {
		return PLUGIN_DISPLAY_NAME;
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@NotNull
	@Override
	public String getComponentName() {
		return PLUGIN_COMPONENT_NAME;
	}

	@Nullable
	@Override
	public PPConfiguration getState() {
		return state;
	}

	@Override
	public void loadState(PPConfiguration storedState) {
		state = storedState;
		LOGGER.info("State loaded");
		registerActions();
		if (configGUI != null) {
			configGUI.setConfig(state);
		}
	}

	@Override
	public void projectOpened() {
	}

	@Override
	public void projectClosed() {
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		if (configGUI == null) {
			configGUI = new ConfigPanel();
		}
		return configGUI.getRootPanel();
	}

	@Override
	public boolean isModified() {
		return !(state == null || configGUI == null || configGUI.getConfig() == null) && !state.equals(configGUI.getConfig());
	}

	@Override
	public void apply() throws ConfigurationException {
		if (configGUI != null) {
			state = configGUI.getConfig();
			registerActions();
		}
	}

	@Override
	public void reset() {
		if (configGUI != null && state != null) {
			configGUI.setConfig(state);
		}
	}

	@Override
	public void disposeUIResources() {
		configGUI = null;
	}

	public static void doNotify(String message, NotificationType type) {
		Notifications.Bus.notify(new Notification("PPImport", "PPImport", message, type));
	}
}
