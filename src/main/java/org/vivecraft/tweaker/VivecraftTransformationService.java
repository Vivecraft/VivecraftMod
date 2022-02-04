//package org.vivecraft.tweaker;
//
//import cpw.mods.jarhandling.SecureJar;
//import cpw.mods.modlauncher.api.IEnvironment;
//import cpw.mods.modlauncher.api.IModuleLayerManager;
//import cpw.mods.modlauncher.api.ITransformationService;
//import cpw.mods.modlauncher.api.ITransformer;
//import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
//import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;
//import cpw.mods.modlauncher.api.ITransformationService.Resource;
//import optifine.OptiFineResourceLocator;
//import optifine.OptiFineTransformer;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.AbstractMap.SimpleEntry;
//import java.util.Map.Entry;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipException;
//import java.util.zip.ZipFile;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.vivecraft.asm.VivecraftASMTransformer;
//import org.vivecraft.utils.Utils;
//
//public class VivecraftTransformationService implements ITransformationService
//{
//    private static final Logger LOGGER = LogManager.getLogger();
//
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
//    	LOGGER.info("VivecraftTransformationService.onLoad");
//    	try
//    	{
//    		init();
//    	}
//    	catch (Exception exception)
//    	{
//    		LOGGER.error("Error loading ZIP file: " + LoaderUtils.ZipFileUrl, (Throwable)exception);
//    		throw new IncompatibleEnvironmentException("Error loading ZIP file: " + LoaderUtils.ZipFileUrl);
//    	}
//    }
//    
//    private static void init() throws URISyntaxException, ZipException, IOException {        
//            //transformer = new VivecraftTransformer(LoaderUtils.ZipFile);
//    }
//    
//    public List<Resource> completeScan(IModuleLayerManager layerManager)
//    {
//        List<Resource> list = new ArrayList<>();
//        List<SecureJar> list1 = new ArrayList<>();
//        try {
//			list1.add(new VivecraftJar(LoaderUtils.toFile(LoaderUtils.ZipFileUrl.toURI()).toPath()));
//		} catch (URISyntaxException e) {
//		}
//        list.add(new Resource(Layer.GAME, list1));
//        return list;
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
//        if (name.endsWith(".class") && !name.startsWith("org.vivecraft/"))
//        {
//            name = "vcsrg/" + name.replace(".class", ".clsrg");
//        }
//
//        if (transformer == null)
//        {
//            return Optional.empty();
//        }
//        else
//        {
//            ZipEntry zipentry = null;// = LoaderUtils.ZipFile.getEntry(name);
//
//            if (zipentry == null)
//            {
//                return Optional.empty();
//            }
//            else
//            {
//                try
//                {
//                    String s = LoaderUtils.ZipFileUrl.toExternalForm();
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
//    
//
//}
