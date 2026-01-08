package org.vivecraft;


import joptsimple.internal.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vivecraft.mod_compat_vr.shaders.ShaderPatcher;
import org.vivecraft.mod_compat_vr.shaders.patches.Patch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ShaderPatchingTest {

    @Test
    public void runTests() {
        for (Patch p : ShaderPatcher.getPatches()) {
            String orig = p.getTestString();

            String patched = p.patch(orig);

            for (String line : orig.split("\n")) {
                boolean contains = patched.contains(line) && !line.contains("// don't patch");
                Assertions.assertFalse(contains, p.getClass().getSimpleName() +
                    ": Patched output: \n'%s'\n still contained original line \n'%s'\n patterns: \n%s"
                        .formatted(patched, line,
                            Strings.join(Arrays.stream(p.getPatterns()).map(Pattern::pattern).toList(), "\n")));
            }
            // check that there are no open brackets left
            Map<Character, Integer> charmap = new HashMap<>();
            for (char c : patched.toCharArray()) {
                charmap.put(c, charmap.getOrDefault(c, 0) + 1);
            }
            String msg = p.getClass().getSimpleName() + ": unequal amounts of '%s' and '%s'";
            Assertions.assertEquals(charmap.get('('), charmap.get(')'), msg.formatted("(", ")"));
            Assertions.assertEquals(charmap.get('['), charmap.get(']'), msg.formatted("[", "]"));
            Assertions.assertEquals(charmap.get('{'), charmap.get('}'), msg.formatted("{", "}"));
        }
    }
}
