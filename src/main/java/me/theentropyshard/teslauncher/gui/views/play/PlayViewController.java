/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.gui.views.play;

import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class PlayViewController {
    private final PlayView playView;
    private final Map<String, InstancesPanel> instancesPanels;

    public PlayViewController(PlayView playView) {
        this.playView = playView;
        this.instancesPanels = new HashMap<>();
    }

    public void addInstance(String group, String name, Icon icon, Runnable listener) {
        InstancesPanel instancesPanel = this.instancesPanels.get(group);
        if (instancesPanel == null) {
            instancesPanel = new InstancesPanel(new AddInstanceItem());
            this.instancesPanels.put(group, instancesPanel);
        }
        InstanceItem item = new InstanceItem(icon, name);
        item.addListener(e -> listener.run(), true);
        instancesPanel.addInstanceItem(item);
    }
}
