package be.mavicon.intellij.ppimport;

import be.mavicon.intellij.ppimport.ui.ConfigPanel;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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


@State(
        name = "PPImportPlugin",
        storages = {
                @Storage(id = "ppimport", file = "$PROJECT_FILE$"),
                @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/ppimport.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)

public class PPImportPlugin implements ProjectComponent, Configurable, PersistentStateComponent<PPConfiguration> {
    public static final String PLUGIN_COMPONENT_NAME = "PPImportPlugin";
    public static final String PLUGIN_DISPLAY_NAME = "Polopoly Import";

    ConfigPanel configGUI = null;
    PPConfiguration state = new PPConfiguration();

    @Override
    public void initComponent() {
        ActionManager am = ActionManager.getInstance();
        DefaultActionGroup group = (DefaultActionGroup) am.getAction("PPImportGroup");
        group.removeAll();

        List<AnAction> actions = new ArrayList<AnAction>();
        if (state == null) {
            state = new PPConfiguration();
            state.init();
        }
        List<String> includeExtentions = getIncludeExtentions(state.getFileExtensions());
        for (Target target : state.getTargets()) {
            AnAction action = new PPImportAction(target, includeExtentions,state.packMultipleFilesInJar);
            actions.add(action);
            am.unregisterAction(target.getProfile());
            am.registerAction(target.getProfile(), action);
            group.add(action);
        }
    }

    private List<String> getIncludeExtentions(String fileExtentions) {
        List<String> result = new ArrayList<String>();
        if (fileExtentions != null) {
            try {
                String[] splitedUp = fileExtentions.split(",");
                for (String anExtention : splitedUp) {
                    if (anExtention != null && !"".equals(anExtention)) {
                        result.add(anExtention.trim());
                    }
                }
            } catch (Exception e) {

            }
        }
        return result;
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
        if (storedState != null) {
            XmlSerializerUtil.copyBean(storedState, state);
        } else {
            state = new PPConfiguration();
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
            configGUI = new ConfigPanel(this.state);
        }
        return configGUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return !state.equals(configGUI.getConfig());
    }

    @Override
    public void apply() throws ConfigurationException {
        state = configGUI.getConfig();
        initComponent();
    }

    @Override
    public void reset() {
        configGUI.setConfig(this.state);
    }

    @Override
    public void disposeUIResources() {
        configGUI = null;
    }

    public static void doNotify(String message, NotificationType type) {
        Notifications.Bus.notify(new Notification("PPImport", "PPImport", message, type));
    }
}
