//package org.vivecraft.tweaker;
//
//import cpw.mods.jarhandling.impl.Jar;
//import cpw.mods.jarhandling.impl.SimpleJarMetadata;
//import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.Set;
//import java.util.jar.Manifest;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipException;
//
//import org.vivecraft.utils.Utils;
//
//public class VivecraftJar extends Jar
//{
//    public VivecraftJar(Path... paths)
//    {
//        super(Manifest::new, (jar) ->
//        {
//            return new SimpleJarMetadata("org.vivecraft", (String)null, jar.getPackages(), new ArrayList<>());
//        }, (s1, s2) ->
//        {
//            return true;
//        }, paths);
//    }
//
//    public Set<String> getPackages()
//    {
//        Set<String> set = new HashSet<>();
//        Enumeration<? extends ZipEntry> enumeration;
//		try {
//			enumeration = LoaderUtils.getVivecraftZip().entries();
//	 
//			while (enumeration.hasMoreElements())
//	        {
//	            ZipEntry zipentry = enumeration.nextElement();
//	            String s = zipentry.getName();
//
//	            if (s.startsWith("org/vivecraft/") && s.endsWith(".clsrg"))
//	            {
//	                set.add(s.substring(s.indexOf("/") + 1, s.lastIndexOf("/")).replace('/', '.'));
//	            }
//	        }
//		} catch (Exception ex) {
//		}
//
//        return set;
//    }
//
//}
