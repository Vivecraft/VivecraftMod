package org.vivecraft.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class LangHelper
{
    public static final String YES_KEY = "vivecraft.options.yes";
    public static final String NO_KEY = "vivecraft.options.no";
    public static final String ON_KEY = "options.on";
    public static final String OFF_KEY = "options.off";

    public static void loadLocaleData(String code, Map<String, String> map)
    {
        String s = "lang/" + code + ".lang";
        InputStream inputstream = Utils.getAssetAsStream(s, false);

        if (inputstream != null)
        {
            try
            {
                StringBuilder stringbuilder = new StringBuilder();
                String s1 = null;

                for (String s2 : IOUtils.readLines(inputstream, StandardCharsets.UTF_8))
                {
                    if (!s2.isEmpty() && s2.charAt(0) != '#')
                    {
                        if (s2.charAt(s2.length() - 1) == '\\')
                        {
                            s2 = s2.substring(0, s2.length() - 1);

                            if (s1 == null)
                            {
                                String[] astring = s2.split("=", 2);

                                if (astring.length == 2)
                                {
                                    stringbuilder.append(StringEscapeUtils.unescapeJava(astring[1]));
                                }

                                s1 = astring[0];
                            }
                            else
                            {
                                stringbuilder.append(StringEscapeUtils.unescapeJava(s2));
                            }
                        }
                        else if (s1 != null)
                        {
                            stringbuilder.append(StringEscapeUtils.unescapeJava(s2));
                            map.put(s1, stringbuilder.toString());
                            stringbuilder.setLength(0);
                            s1 = null;
                        }
                        else
                        {
                            String[] astring1 = s2.split("=", 2);
                            map.put(astring1[0], astring1.length == 2 ? StringEscapeUtils.unescapeJava(astring1[1]) : "");
                        }
                    }
                }

                inputstream.close();
            }
            catch (Exception exception)
            {
                System.out.println("Failed reading locale data: " + s);
                exception.printStackTrace();
            }
        }
    }

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
