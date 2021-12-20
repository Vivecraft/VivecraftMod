//package org.vivecraft.tweaker;
//
//import cpw.mods.modlauncher.api.IEnvironment;
//import cpw.mods.modlauncher.api.ITransformationService;
//import cpw.mods.modlauncher.api.ITransformer;
//import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import java.util.AbstractMap.SimpleEntry;
//import java.util.Map.Entry;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.vivecraft.asm.VivecraftASMTransformer;
//import org.vivecraft.utils.Utils;
//
//public class VivecraftTransformationService implements ITransformationService
//{
//    private static final Logger LOGGER = LogManager.getLogger();
//    public static URL ZipFileUrl;
//    private static ZipFile ZipFile;
//    private static VivecraftTransformer transformer;
//
//    public String name()
//    {
//        return "Vivecraft";
//    }
//
//    public void initialize(IEnvironment environment)
//    {
//        LOGGER.info("VivecraftTransformationService.initialize");
//    }
//
//    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException
//    {
//        LOGGER.info("VivecraftTransformationService.onLoad");
//
//        try
//        {
//            ZipFileUrl = Utils.getVivecraftZipLocation().toURL();
//            ZipFile = Utils.getVivecraftZip();
//            transformer = new VivecraftTransformer(ZipFile);
//        }
//        catch (Exception exception)
//        {
//            LOGGER.error("Error loading ZIP file: " + ZipFileUrl, (Throwable)exception);
//            throw new IncompatibleEnvironmentException("Error loading ZIP file: " + ZipFileUrl);
//        }
//    }
//
//    public Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalResourcesLocator()
//    {
//        return ITransformationService.super.additionalResourcesLocator();
//    }
//
//    public Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator()
//    {
//        Set<String> set = new HashSet<>();
//        set.add("org.vivecraft.");
//        Supplier<Function<String, Optional<URL>>> supplier = () ->
//        {
//            return this::getResourceUrl;
//        };
//        Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> entry = new SimpleEntry<>(set, supplier);
//        LOGGER.info("additionalClassesLocator: " + set);
//        return entry;
//    }
//
//    public Optional<URL> getResourceUrl(String name)
//    {
//        if (name.endsWith(".class") && !name.startsWith("optifine/"))
//        {
//            name = "srg/" + name;
//        }
//
//        if (transformer == null)
//        {
//            return Optional.empty();
//        }
//        else
//        {
//            ZipEntry zipentry = ZipFile.getEntry(name);
//
//            if (zipentry == null)
//            {
//                return Optional.empty();
//            }
//            else
//            {
//                try
//                {
//                    String s = ZipFileUrl.toExternalForm();
//                    URL url = new URL("jar:" + s + "!/" + name);
//                    return Optional.of(url);
//                }
//                catch (IOException ioexception1)
//                {
//                    LOGGER.error(ioexception1);
//                    return Optional.empty();
//                }
//            }
//        }
//    }
//
//    public List<ITransformer> transformers()
//    {
//        LOGGER.info("VivecraftTransformationService.transformers");
//        List<ITransformer> list = new ArrayList<>();
//
//        if (transformer != null)
//        {
//            list.add(transformer);
//        }
//
//        list.add(new VivecraftASMTransformer());
//        return list;
//    }
//
//    public static VivecraftTransformer getTransformer()
//    {
//        return transformer;
//    }
//}
