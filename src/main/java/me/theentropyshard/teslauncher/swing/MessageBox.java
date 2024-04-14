/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.swing;

import javax.swing.*;
import java.awt.*;

public final class MessageBox {
    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirmMessage(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    private MessageBox() {
        throw new UnsupportedOperationException();
    }
}
