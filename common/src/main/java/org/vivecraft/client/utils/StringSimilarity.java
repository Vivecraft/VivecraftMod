package org.vivecraft.client.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ported from <a href="https://github.com/rapidfuzz/RapidFuzz">RapidFuzz</a> to java<br>
 * RapidFuzz originally licensed under MIT<br>
 * Copyright © 2020-present Max Bachmann<br>
 * Copyright © 2011 Adam Cohen
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class StringSimilarity {

    private static int block_similarity(Map<Character, Integer> block, String s1, String s2) {
        if (s1.isEmpty()) return 0;

        int S = (1 << s1.length()) - 1;

        for (char ch2 : s2.toCharArray()) {
            int Matches = block.getOrDefault(ch2, 0);
            int u = S & Matches;
            S = (S + u) | (S - u);
        }

        // calculate the equivalent of popcount(~S) in C. This breaks for len(s1) == 0
        return 32 - Integer.numberOfLeadingZeros(S) - Integer.bitCount(S);
    }

    private static int block_distance(Map<Character, Integer> block, String s1, String s2) {
        int maximum = s1.length() + s2.length();
        return maximum - 2 * block_similarity(block, s1, s2);
    }

    private static float block_normalized_distance(Map<Character, Integer> block, String s1, String s2) {
        float maximum = s1.length() + s2.length();
        return maximum != 0 ? block_distance(block, s1, s2) / maximum : 0F;
    }

    private static float block_normalized_similarity(
        Map<Character, Integer> block, String s1, String s2, float cutoff)
    {
        float norm_sim = 1F - block_normalized_distance(block, s1, s2);
        return norm_sim >= cutoff ? norm_sim : 0;
    }

    public static float partial_ratio(String s1, String s2) {
        if (s2.length() < s1.length()) {
            // make sure s1 is the shorter string
            String temp = s1;
            s1 = s2;
            s2 = temp;
        }

        Set<Character> s1_char_set = new HashSet<>();
        for (char c : s1.toCharArray()) {
            s1_char_set.add(c);
        }

        int len1 = s1.length();
        int len2 = s2.length();
        float cutoff = 0;

        float res = 0;

        Map<Character, Integer> block = new HashMap<>();
        int x = 1;
        for (char ch1 : s1.toCharArray()) {
            block.put(ch1, block.getOrDefault(ch1, 0) | x);
            x <<= 1;
        }

        for (int i = 1; i < len1; i++) {
            char substr_last = s2.charAt(i - 1);
            if (!s1_char_set.contains(substr_last)) {
                continue;
            }

            float ls_ratio = block_normalized_similarity(block, s1, s2.substring(0, i), cutoff);
            if (ls_ratio > res) {
                res = cutoff = ls_ratio;
                if (res == 1) {
                    return res;
                }
            }
        }


        for (int i = 0; i < len2 - len1; i++) {
            char substr_last = s2.charAt(i + len1 - 1);
            if (!s1_char_set.contains(substr_last)) {
                continue;
            }

            float ls_ratio = block_normalized_similarity(block, s1, s2.substring(i, i + len1), cutoff);
            if (ls_ratio > res) {
                res = cutoff = ls_ratio;
                if (res == 1) {
                    return res;
                }
            }
        }

        for (int i = len2 - len1; i < len2; i++) {
            char substr_first = s2.charAt(i);
            if (!s1_char_set.contains(substr_first)) {
                continue;
            }

            float ls_ratio = block_normalized_similarity(block, s1, s2.substring(i), cutoff);
            if (ls_ratio > res) {
                res = cutoff = ls_ratio;
                if (res == 1) {
                    return res;
                }
            }
        }
        return res;
    }
}
