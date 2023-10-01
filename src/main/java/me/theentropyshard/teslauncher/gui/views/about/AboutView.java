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

package me.theentropyshard.teslauncher.gui.views.about;

import me.theentropyshard.teslauncher.gui.View;

import javax.swing.*;
import java.awt.*;

public class AboutView extends View {
    public AboutView() {
        JLabel line1 = new JLabel("Yet another launcher for Minecraft");
        JLabel line2 = new JLabel("Made by TheEntropyShard");

        JPanel lines = new JPanel();
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));

        lines.add(line1);
        lines.add(line2);

        JPanel root = this.getRoot();
        root.add(lines, BorderLayout.CENTER);
    }
}
