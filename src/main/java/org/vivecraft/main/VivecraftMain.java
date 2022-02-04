package org.vivecraft.main;

import com.google.common.base.Throwables;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.vivecraft.tweaker.MinecriftClassTransformer;

public class VivecraftMain
{
    private static final String[] encapsulatedTransformers = new String[0];
    private static final String[] removedTransformers = new String[] {"guichaguri.betterfps.transformers.PatcherTransformer", "sampler.asm.Transformer"};

    public static void main(String[] p_main_0_)
    {
        LaunchClassLoader launchclassloader = (LaunchClassLoader)Thread.currentThread().getContextClassLoader();

        try
        {
            Field field = launchclassloader.getClass().getDeclaredField("transformers");
            field.setAccessible(true);
            List<IClassTransformer> list = (List)field.get(launchclassloader);
            List<IClassTransformer> list1 = new ArrayList<>();
            List<IClassTransformer> list2 = new ArrayList<>();
            boolean flag = false;
            System.out.println("************** Vivecraft classloader pre-filter ***************");
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                IClassTransformer iclasstransformer = (IClassTransformer)iterator.next();
                System.out.println(iclasstransformer.getClass().getName());

                if (iclasstransformer.getClass().getName().equals("net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer"))
                {
                    flag = true;
                }

                for (String s : encapsulatedTransformers)
                {
                    if (iclasstransformer.getClass().getName().equals(s) || iclasstransformer.getClass().getName().equals("$wrapper." + s))
                    {
                        if (flag)
                        {
                            list2.add(iclasstransformer);
                        }
                        else
                        {
                            list1.add(iclasstransformer);
                        }

                        iterator.remove();
                        break;
                    }
                }

                for (String s2 : removedTransformers)
                {
                    if (iclasstransformer.getClass().getName().equals(s2) || iclasstransformer.getClass().getName().equals("$wrapper." + s2))
                    {
                        iterator.remove();
                        break;
                    }
                }
            }

            list.add(2, new MinecriftClassTransformer(MinecriftClassTransformer.Stage.MAIN, (Map<String, byte[]>)null));
            int i = 0;

            for (int j = 0; j < list.size(); ++j)
            {
                IClassTransformer iclasstransformer1 = list.get(j);

                if (iclasstransformer1.getClass().getName().equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriberTransformer"))
                {
                    i = j + 1;
                    break;
                }
            }

            if (list1.size() > 0)
            {
                HashMap<String, byte[]> hashmap = new HashMap<>();
                list.add(i, new MinecriftClassTransformer(MinecriftClassTransformer.Stage.CACHE, hashmap));
                list.addAll(i + 1, list1);
                list.add(i + list1.size() + 1, new MinecriftClassTransformer(MinecriftClassTransformer.Stage.REPLACE, hashmap));
                int k = i + list1.size() + 2;
            }

            if (list2.size() > 0)
            {
                HashMap<String, byte[]> hashmap1 = new HashMap<>();
                list.add(list.size() - 1, new MinecriftClassTransformer(MinecriftClassTransformer.Stage.CACHE, hashmap1));
                list.addAll(list.size() - 1, list2);
                list.add(list.size() - 1, new MinecriftClassTransformer(MinecriftClassTransformer.Stage.REPLACE, hashmap1));
            }

            System.out.println("************** Vivecraft classloader filter ***************");

            for (IClassTransformer iclasstransformer2 : list)
            {
                System.out.println(iclasstransformer2.getClass().getName());
            }
        }
        catch (Exception exception1)
        {
            System.out.println("************** Vivecraft filter error ***************");
            exception1.printStackTrace();
        }

        try
        {
            String s1 = "net.minecraft.client.main.Main";
            Class<?> oclass = Class.forName("net.minecraft.client.main.Main", false, launchclassloader);
            Method method = oclass.getMethod("main", String[].class);
            method.invoke((Object)null, p_main_0_);
        }
        catch (Exception exception)
        {
            System.out.println("************** Vivecraft critical error ***************");
            Throwables.throwIfUnchecked(exception);
            throw new RuntimeException(exception);
        }
    }
}
