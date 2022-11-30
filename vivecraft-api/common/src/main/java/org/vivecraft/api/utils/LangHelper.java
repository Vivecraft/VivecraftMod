package org.vivecraft.api.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import net.minecraft.client.resources.language.I18n;

public class LangHelper
{
    public static final String YES_KEY = "vivecraft.options.yes";
    public static final String NO_KEY = "vivecraft.options.no";
    public static final String ON_KEY = "options.on";
    public static final String OFF_KEY = "options.off";

    public static String get(String key, Object... params)
    {
        return I18n.get(key, params);
    }

    public static String getYes()
    {
        return I18n.get(YES_KEY);
    }

    public static String getNo()
    {
        return I18n.get(NO_KEY);
    }
}
