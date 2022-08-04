package org.vivecraft.settings;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class Lang
{
    private static final Splitter splitter = Splitter.on('=').limit(2);
    private static final Pattern pattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

    public static void resourcesReloaded()
    {
        Map map = new HashMap();
        List<String> list = new ArrayList<>();
        String s = "optifine/lang/";
        String s1 = "en_us";
        String s2 = ".lang";
        list.add(s + s1 + s2);
        Minecraft mc = Minecraft.getInstance();
        
        if (!mc.options.languageCode.equals(s1))
        {
            list.add(s + mc.options.languageCode + s2);
        }

        String[] astring = list.toArray(new String[list.size()]);
        loadResources(mc.getClientPackSource().getVanillaPack(), astring, map);
        PackResources[] apackresources = getResourcePacks();

        for (int i = 0; i < apackresources.length; ++i)
        {
            PackResources packresources = apackresources[i];
            loadResources(packresources, astring, map);
        }
    }

    public static PackResources[] getResourcePacks()
    {
        Minecraft mc = Minecraft.getInstance();

        PackRepository packrepository = mc.getResourcePackRepository();
        Collection<Pack> collection = packrepository.getSelectedPacks();
        List list = new ArrayList();

        for (Pack pack : collection)
        {
            PackResources packresources = pack.open();

            if (packresources != mc.getClientPackSource().getVanillaPack())
            {
                list.add(packresources);
            }
        }

        PackResources[] apackresources = (PackResources[]) list.toArray(new PackResources[list.size()]);
        return apackresources;
    }
    
    private static void loadResources(PackResources rp, String[] files, Map localeProperties)
    {
        try
        {
            for (int i = 0; i < files.length; ++i)
            {
                String s = files[i];
                ResourceLocation resourcelocation = new ResourceLocation(s);

                if (rp.hasResource(PackType.CLIENT_RESOURCES, resourcelocation))
                {
                    InputStream inputstream = rp.getResource(PackType.CLIENT_RESOURCES, resourcelocation);

                    if (inputstream != null)
                    {
                        loadLocaleData(inputstream, localeProperties);
                    }
                }
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    public static void loadLocaleData(InputStream is, Map localeProperties) throws IOException
    {
        Iterator iterator = IOUtils.readLines(is, Charsets.UTF_8).iterator();
        is.close();

        while (iterator.hasNext())
        {
            String s = (String)iterator.next();

            if (!s.isEmpty() && s.charAt(0) != '#')
            {
                String[] astring = Iterables.toArray(splitter.split(s), String.class);

                if (astring != null && astring.length == 2)
                {
                    String s1 = astring[0];
                    String s2 = pattern.matcher(astring[1]).replaceAll("%$1s");
                    localeProperties.put(s1, s2);
                }
            }
        }
    }

    public static void loadResources(ResourceManager resourceManager, String langCode, Map<String, String> map)
    {
        try
        {
            String s = "optifine/lang/" + langCode + ".lang";
            ResourceLocation resourcelocation = new ResourceLocation(s);
            Resource resource = resourceManager.getResource(resourcelocation);
            InputStream inputstream = resource.getInputStream();
            loadLocaleData(inputstream, map);
        }
        catch (IOException ioexception)
        {
        }
    }

    public static String get(String key)
    {
        return I18n.get(key);
    }

    public static TranslatableComponent getComponent(String key)
    {
        return new TranslatableComponent(key);
    }

    public static String get(String key, String def)
    {
        String s = I18n.get(key);
        return s != null && !s.equals(key) ? s : def;
    }

    public static String getOn()
    {
        return I18n.get("options.on");
    }

    public static String getOff()
    {
        return I18n.get("options.off");
    }

    public static String getFast()
    {
        return I18n.get("options.graphics.fast");
    }

    public static String getFancy()
    {
        return I18n.get("options.graphics.fancy");
    }

    public static String getDefault()
    {
        return I18n.get("generator.default");
    }
}
