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

import me.theentropyshard.teslauncher.gui.View;

import javax.swing.*;

public class PlayView extends View {
    private final JComboBox<String> groups;
    private final JComboBox<String> accounts;


    public PlayView() {
        this.groups = new JComboBox<>();
        this.accounts = new JComboBox<>();

        JPanel root = this.getRoot();
    }

    public JComboBox<String> getGroups() {
        return this.groups;
    }

    public JComboBox<String> getAccounts() {
        return this.accounts;
    }
}
