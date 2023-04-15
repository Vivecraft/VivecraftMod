package org.vivecraft.client_vr;

import com.sun.jna.NativeLibrary;
import jopenvr.JOpenVRLibrary;
import org.vivecraft.client.VRState;
import org.vivecraft.client.utils.Utils;

import java.io.File;

public class VRNatives {

    private static boolean unpackedNatives = false;

    public static void initializeVR() {
        if (!unpackedNatives) {
            unpackPlatformNatives();
            // disable VR if natives failed
            VRState.isVR = !JOpenVRLibrary.isErrored();
            unpackedNatives = true;
        }
    }

    private static boolean unpackPlatformNatives()
    {
        String s = System.getProperty("os.name").toLowerCase();
        String s1 = System.getProperty("os.arch").toLowerCase();
        String s2 = "win";

        if (s.contains("linux"))
        {
            s2 = "linux";
        }
        else if (s.contains("mac"))
        {
            s2 = "osx";
        }

        if (!s.contains("mac"))
        {
            if (s1.contains("64"))
            {
                s2 = s2 + "64";
            }
            else
            {
                s2 = s2 + "32";
            }
        }

        try {
            Utils.unpackNatives(s2);
        } catch (Exception e) {
            System.out.println("Native path not found");
            return false;
        }

        if (!(new File("openvr/" + s2)).exists()) {
            System.out.println("Path does not exist, skipping VR!");
            return false;
        }
        String s3 = (new File("openvr/" + s2)).getAbsolutePath();
        System.out.println("Adding OpenVR search path: " + s3);
        NativeLibrary.addSearchPath("openvr_api", s3);
        return true;
    }

}
