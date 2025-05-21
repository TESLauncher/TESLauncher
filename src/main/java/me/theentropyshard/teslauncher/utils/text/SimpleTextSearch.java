/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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

package me.theentropyshard.teslauncher.utils.text;

import me.theentropyshard.teslauncher.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleTextSearch implements TextSearch {
    @Override
    public List<TextRange> findOccurrences(String haystack, String needle) {
        if (haystack.isEmpty() || needle.isEmpty()) {
            return Collections.emptyList();
        }

        List<TextRange> occurrences = new ArrayList<>();

        List<Integer> rawOccurrences = SimpleTextSearch.findSequenceOccurrences(haystack, needle);

        for (int rawIndex : rawOccurrences) {
            int line = SimpleTextSearch.findLine(haystack, rawIndex);
            int i = rawIndex - line + 1;
            occurrences.add(new TextRange(i, i + needle.length()));
        }

        return occurrences;
    }

    public static List<Integer> findSequenceOccurrences(String text, String sequence) {
        text = text.toLowerCase();
        sequence = sequence.toLowerCase();

        List<Integer> positions = new ArrayList<>();
        int index = text.indexOf(sequence);

        while (index != -1) {
            positions.add(index);
            index = text.indexOf(sequence, index + sequence.length());
        }

        return positions;
    }

    public static int findLine(String text, int index) {
        String[] lines = text.split("\n");
        int charCount = 0;

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            charCount += lines[lineNumber].length() + 1;
            if (charCount > index) {
                return lineNumber + 1;
            }
        }

        return -1;
    }
}