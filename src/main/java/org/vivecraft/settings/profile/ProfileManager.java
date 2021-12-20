package org.vivecraft.settings.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ProfileManager
{
    public static final String DEFAULT_PROFILE = "Default";
    public static final String PROFILE_SET_OF = "Of";
    public static final String PROFILE_SET_MC = "Mc";
    public static final String PROFILE_SET_VR = "Vr";
    public static final String PROFILE_SET_CONTROLLER_BINDINGS = "Controller";
    static final String KEY_PROFILES = "Profiles";
    static final String KEY_SELECTED_PROFILE = "selectedProfile";
    static String currentProfileName = "Default";
    static File vrProfileCfgFile = null;
    static JSONObject jsonConfigRoot = null;
    static JSONObject profiles = null;
    static boolean loaded = false;
    public static final String[] DEFAULT_BINDINGS = new String[] {"key.playerlist:b:6:Button 6", "axis.updown:a:2:-:Y Rotation", "walk.forward:a:0:-:Y ", "gui.axis.leftright:a:3:-:X Rotation", "gui.axis.updown:a:2:-:Y Rotation", "key.sneak:b:9:Button 9", "gui.Left:px:-", "key.itemright:b:5:Button 5", "gui.Right:px:+", "key.left:a:1:-:X ", "gui.Select:b:0:Button 0", "key.aimcenter:b:8:Button 8", "key.pickItem:b:2:Button 2", "key.menu:b:7:Button 7", "key.attack:a:4:-:Z ", "gui.Up:py:-", "key.use:a:4:+:Z ", "axis.leftright:a:3:-:X Rotation", "gui.Down:py:+", "key.right:a:1:+:X ", "key.back:a:0:+:Y ", "key.inventory:b:3:Button 3", "key.jump:b:0:Button 0", "key.drop:b:1:Button 1", "gui.Back:b:1:Button 1", "key.itemleft:b:4:Button 4"};

    public static synchronized void init(File dataDir)
    {
        vrProfileCfgFile = new File(dataDir, "optionsviveprofiles.txt");
        load();
    }

    public static synchronized void load()
    {
        try
        {
            if (!vrProfileCfgFile.exists())
            {
                vrProfileCfgFile.createNewFile();
            }

            InputStreamReader inputstreamreader = new InputStreamReader(new FileInputStream(vrProfileCfgFile), "UTF-8");

            try
            {
                JSONTokener jsontokener = new JSONTokener(inputstreamreader);
                jsonConfigRoot = new JSONObject(jsontokener);
            }
            catch (Exception exception)
            {
                jsonConfigRoot = new JSONObject();
            }

            inputstreamreader.close();

            if (jsonConfigRoot.has("selectedProfile"))
            {
                currentProfileName = jsonConfigRoot.getString("selectedProfile");
            }
            else
            {
                jsonConfigRoot.put("selectedProfile", "Default");
            }

            if (jsonConfigRoot.has("Profiles"))
            {
                profiles = jsonConfigRoot.getJSONObject("Profiles");
            }
            else
            {
                profiles = new JSONObject();
                jsonConfigRoot.put("Profiles", profiles);
            }

            if (!profiles.has("Default"))
            {
                JSONObject jsonobject = new JSONObject();
                profiles.put("Default", jsonobject);
            }

            validateProfiles();
            loaded = true;
        }
        catch (Exception exception1)
        {
            System.out.println("FAILED to read VR profile settings!");
            exception1.printStackTrace();
            loaded = false;
        }
    }

    private static void validateProfiles() throws Exception
    {
        for (Object object : profiles.keySet())
        {
            String s = (String)object;
            Object object1 = profiles.get(s);

            if (object1 instanceof JSONObject)
            {
                JSONObject jsonobject = (JSONObject)object1;
                JSONObject jsonobject1 = null;
                JSONObject jsonobject2 = null;
                JSONObject jsonobject3 = null;
                JSONObject jsonobject4 = null;

                for (Object object2 : jsonobject.keySet())
                {
                    String s1 = (String)object2;
                    Object object3 = jsonobject.get(s1);

                    if (object3 instanceof JSONObject)
                    {
                        if (s1.equals("Mc"))
                        {
                            jsonobject1 = (JSONObject)object3;
                        }

                        if (s1.equals("Of"))
                        {
                            jsonobject2 = (JSONObject)object3;
                        }

                        if (s1.equals("Vr"))
                        {
                            jsonobject3 = (JSONObject)object3;
                        }

                        if (s1.equals("Controller"))
                        {
                            jsonobject4 = (JSONObject)object3;
                        }
                    }
                }
            }
        }
    }

    private static synchronized boolean loadLegacySettings(File settingsFile, JSONObject theProfile, String set) throws Exception
    {
        if (!settingsFile.exists())
        {
            return false;
        }
        else
        {
            FileReader filereader = new FileReader(settingsFile);
            BufferedReader bufferedreader = new BufferedReader(filereader);
            Map<String, String> map = new HashMap<>();
            String s;
            int i;

            for (i = 0; (s = bufferedreader.readLine()) != null; ++i)
            {
                String[] astring = splitKeyValue(s);
                String s1 = astring[0];
                String s2 = "";

                if (astring.length > 1)
                {
                    s2 = astring[1];
                }

                map.put(s1, s2);
            }

            setProfileSet(theProfile, set, map);
            return i != 0;
        }
    }

    private static synchronized boolean loadLegacySettings(String settingStr, JSONObject theProfile, String set) throws Exception
    {
        StringReader stringreader = new StringReader(settingStr);
        BufferedReader bufferedreader = new BufferedReader(stringreader);
        Map<String, String> map = new HashMap<>();
        String s;
        int i;

        for (i = 0; (s = bufferedreader.readLine()) != null; ++i)
        {
            String[] astring = splitKeyValue(s);
            String s1 = astring[0];
            String s2 = "";

            if (astring.length > 1)
            {
                s2 = astring[1];
            }

            map.put(s1, s2);
        }

        setProfileSet(theProfile, set, map);
        return i != 0;
    }

    private static synchronized boolean loadLegacySettings(String[] settingStr, JSONObject theProfile, String set) throws Exception
    {
        Map<String, String> map = new HashMap<>();
        int i = 0;

        for (String s : settingStr)
        {
            if (s != null)
            {
                String[] astring = splitKeyValue(s);
                String s1 = astring[0];
                String s2 = "";

                if (astring.length > 1)
                {
                    s2 = astring[1];
                }

                map.put(s1, s2);
                ++i;
            }
        }

        setProfileSet(theProfile, set, map);
        return i != 0;
    }

    public static synchronized Map<String, String> getProfileSet(String profile, String set)
    {
        Map<String, String> map = new HashMap<>();

        if (profiles.has(profile))
        {
            JSONObject jsonobject = profiles.getJSONObject(profile);

            if (jsonobject.has(set))
            {
                JSONObject jsonobject1 = jsonobject.getJSONObject(set);

                for (String s : (Set<String>)jsonobject1.keySet())
                {
                    String s1 = jsonobject1.getString(s);
                    map.put(s, s1);
                }
            }
        }

        return map;
    }

    public static synchronized Map<String, String> getProfileSet(JSONObject theProfile, String set)
    {
        Map<String, String> map = new HashMap<>();

        if (theProfile.has(set))
        {
            JSONObject jsonobject = theProfile.getJSONObject(set);

            for (String s : (Set<String>)jsonobject.keySet())
            {
                String s1 = jsonobject.getString(s);
                map.put(s, s1);
            }
        }

        return map;
    }

    public static synchronized void setProfileSet(String profile, String set, Map<String, String> settings)
    {
        JSONObject jsonobject = null;
        JSONObject jsonobject1 = new JSONObject();

        if (profiles.has(profile))
        {
            jsonobject = profiles.getJSONObject(profile);
        }
        else
        {
            jsonobject = new JSONObject();
            profiles.put(profile, jsonobject);
        }

        for (String s : settings.keySet())
        {
            String s1 = settings.get(s);
            jsonobject1.put(s, s1);
        }

        jsonobject.remove(set);
        jsonobject.put(set, jsonobject1);
    }

    public static synchronized void setProfileSet(JSONObject theProfile, String set, Map<String, String> settings)
    {
        JSONObject jsonobject = new JSONObject();

        for (String s : settings.keySet())
        {
            String s1 = settings.get(s);
            jsonobject.put(s, s1);
        }

        theProfile.remove(set);
        theProfile.put(set, jsonobject);
    }

    public static synchronized void save()
    {
        try
        {
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(vrProfileCfgFile), "UTF-8");
            String s = jsonConfigRoot.toString(3);
            outputstreamwriter.write(s);
            outputstreamwriter.flush();
            outputstreamwriter.close();
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    public static synchronized boolean profileExists(String profileName)
    {
        return profiles.has(profileName);
    }

    public static synchronized SortedSet<String> getProfileList()
    {
        Set<String> set = profiles.keySet();
        return new TreeSet<>(set);
    }

    private static JSONObject getCurrentProfile()
    {
        if (!profiles.has(currentProfileName))
        {
            return null;
        }
        else
        {
            Object object = profiles.get(currentProfileName);
            return object != null && object instanceof JSONObject ? (JSONObject)object : null;
        }
    }

    public static synchronized String getCurrentProfileName()
    {
        return currentProfileName;
    }

    public static synchronized boolean setCurrentProfile(String profileName, StringBuilder error)
    {
        if (!profiles.has(profileName))
        {
            error.append("Profile '" + profileName + "' not found.");
            return false;
        }
        else
        {
            currentProfileName = profileName;
            jsonConfigRoot.put("selectedProfile", currentProfileName);
            return true;
        }
    }

    public static synchronized boolean createProfile(String profileName, StringBuilder error)
    {
        if (profiles.has(profileName))
        {
            error.append("Profile '" + profileName + "' already exists.");
            return false;
        }
        else
        {
            JSONObject jsonobject = new JSONObject();
            profiles.put(profileName, jsonobject);
            return true;
        }
    }

    public static synchronized boolean renameProfile(String existingProfileName, String newProfileName, StringBuilder error)
    {
        if (existingProfileName.equals("Default"))
        {
            error.append("Cannot rename Default profile.");
            return false;
        }
        else if (!profiles.has(existingProfileName))
        {
            error.append("Profile '" + existingProfileName + "' not found.");
            return false;
        }
        else if (profiles.has(newProfileName))
        {
            error.append("Profile '" + newProfileName + "' already exists.");
            return false;
        }
        else
        {
            JSONObject jsonobject = new JSONObject(profiles.getJSONObject(existingProfileName));
            profiles.remove(existingProfileName);
            profiles.put(newProfileName, jsonobject);

            if (existingProfileName.equals(currentProfileName))
            {
                setCurrentProfile(newProfileName, error);
            }

            return true;
        }
    }

    public static synchronized boolean duplicateProfile(String profileName, String duplicateProfileName, StringBuilder error)
    {
        if (!profiles.has(profileName))
        {
            error.append("Profile '" + profileName + "' not found.");
            return false;
        }
        else if (profiles.has(duplicateProfileName))
        {
            error.append("Profile '" + duplicateProfileName + "' already exists.");
            return false;
        }
        else
        {
            JSONObject jsonobject = new JSONObject(profiles.getJSONObject(profileName));
            profiles.put(duplicateProfileName, jsonobject);
            return true;
        }
    }

    public static synchronized boolean deleteProfile(String profileName, StringBuilder error)
    {
        if (profileName.equals("Default"))
        {
            error.append("Cannot delete Default profile.");
            return false;
        }
        else if (!profiles.has(profileName))
        {
            error.append("Profile '" + profileName + "' not found.");
            return false;
        }
        else
        {
            profiles.remove(profileName);

            if (profileName.equals(currentProfileName))
            {
                setCurrentProfile("Default", error);
            }

            return true;
        }
    }

    public static void loadControllerDefaults()
    {
        if (loaded)
        {
            JSONObject jsonobject = getCurrentProfile();

            if (jsonobject != null)
            {
                try
                {
                    loadLegacySettings(DEFAULT_BINDINGS, jsonobject, "Controller");
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

    public static String[] splitKeyValue(String s)
    {
        return s.split(":", 2);
    }
}
