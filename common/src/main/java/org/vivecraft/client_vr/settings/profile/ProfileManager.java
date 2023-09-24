package org.vivecraft.client_vr.settings.profile;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.vivecraft.common.utils.Utils.logger;

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
    static File vrProfileCfgFile;
    static JsonObject jsonConfigRoot;
    static JsonObject profiles;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    static boolean loaded = false;
    public static final String[] DEFAULT_BINDINGS = {"key.playerlist:b:6:Button 6", "axis.updown:a:2:-:Y Rotation", "walk.forward:a:0:-:Y ", "gui.axis.leftright:a:3:-:X Rotation", "gui.axis.updown:a:2:-:Y Rotation", "key.sneak:b:9:Button 9", "gui.Left:px:-", "key.itemright:b:5:Button 5", "gui.Right:px:+", "key.left:a:1:-:X ", "gui.Select:b:0:Button 0", "key.aimcenter:b:8:Button 8", "key.pickItem:b:2:Button 2", "key.menu:b:7:Button 7", "key.attack:a:4:-:Z ", "gui.Up:py:-", "key.use:a:4:+:Z ", "axis.leftright:a:3:-:X Rotation", "gui.Down:py:+", "key.right:a:1:+:X ", "key.back:a:0:+:Y ", "key.inventory:b:3:Button 3", "key.jump:b:0:Button 0", "key.drop:b:1:Button 1", "gui.Back:b:1:Button 1", "key.itemleft:b:4:Button 4"};

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

            InputStreamReader inputstreamreader = new InputStreamReader(new FileInputStream(vrProfileCfgFile), StandardCharsets.UTF_8);

            try
            {
                jsonConfigRoot = JsonParser.parseReader(inputstreamreader).getAsJsonObject();
            }
            catch (Exception exception)
            {
                jsonConfigRoot = new JsonObject();
            }

            inputstreamreader.close();

            if (jsonConfigRoot.has("selectedProfile"))
            {
                currentProfileName = jsonConfigRoot.get("selectedProfile").getAsString();
            }
            else
            {
                jsonConfigRoot.add("selectedProfile", new JsonPrimitive("Default"));
            }

            if (jsonConfigRoot.has("Profiles"))
            {
                profiles = jsonConfigRoot.get("Profiles").getAsJsonObject();
            }
            else
            {
                profiles = new JsonObject();
                jsonConfigRoot.add("Profiles", profiles);
            }

            if (!profiles.has("Default"))
            {
            	JsonObject JsonObject = new JsonObject();
                profiles.add("Default", JsonObject);
            }

            validateProfiles();
            loaded = true;
        }
        catch (Exception exception1)
        {
            logger.error("FAILED to read VR profile settings!");
            exception1.printStackTrace();
            loaded = false;
        }
    }

    private static void validateProfiles() throws Exception
    {
        for (String s : profiles.keySet())
        {
            if (profiles.get(s) instanceof JsonObject JsonObject)
            {
                JsonObject JsonObject1 = null;
                JsonObject JsonObject2 = null;
                JsonObject JsonObject3 = null;
                JsonObject JsonObject4 = null;

                for (String s1 : JsonObject.keySet())
                {
                    if (JsonObject.get(s1) instanceof JsonObject jsonobject)
                    {
                        if ("Mc".equals(s1))
                        {
                            JsonObject1 = jsonobject;
                        }

                        if ("Of".equals(s1))
                        {
                            JsonObject2 = jsonobject;
                        }

                        if ("Vr".equals(s1))
                        {
                            JsonObject3 = jsonobject;
                        }

                        if ("Controller".equals(s1))
                        {
                            JsonObject4 = jsonobject;
                        }
                    }
                }
            }
        }
    }

    private static synchronized boolean loadLegacySettings(File settingsFile, JsonObject theProfile, String set) throws Exception
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

    private static synchronized boolean loadLegacySettings(String settingStr, JsonObject theProfile, String set) throws Exception
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

    private static synchronized boolean loadLegacySettings(String[] settingStr, JsonObject theProfile, String set) throws Exception
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
            JsonObject JsonObject = profiles.get(profile).getAsJsonObject();

            if (JsonObject.has(set))
            {
                JsonObject JsonObject1 = JsonObject.get(set).getAsJsonObject();

                for (String s : JsonObject1.keySet())
                {
                    String s1 = JsonObject1.get(s).getAsString();
                    map.put(s, s1);
                }
            }
        }

        return map;
    }

    public static synchronized Map<String, String> getProfileSet(JsonObject theProfile, String set)
    {
        Map<String, String> map = new HashMap<>();

        if (theProfile.has(set))
        {
            JsonObject JsonObject = theProfile.get(set).getAsJsonObject();

            for (String s : JsonObject.keySet())
            {
                String s1 = JsonObject.get(s).getAsString();
                map.put(s, s1);
            }
        }

        return map;
    }

    public static synchronized void setProfileSet(String profile, String set, Map<String, String> settings)
    {
        JsonObject JsonObject = null;
        JsonObject JsonObject1 = new JsonObject();

        if (profiles.has(profile))
        {
            JsonObject = profiles.get(profile).getAsJsonObject();
        }
        else
        {
            JsonObject = new JsonObject();
            profiles.add(profile, JsonObject);
        }

        for (String s : settings.keySet())
        {
            String s1 = settings.get(s);
            JsonObject1.add(s, new JsonPrimitive(s1));
        }

        JsonObject.remove(set);
        JsonObject.add(set, JsonObject1);
    }

    public static synchronized void setProfileSet(JsonObject theProfile, String set, Map<String, String> settings)
    {
        JsonObject JsonObject = new JsonObject();

        for (String s : settings.keySet())
        {
            String s1 = settings.get(s);
            JsonObject.add(s, new JsonPrimitive(s1));
        }

        theProfile.remove(set);
        theProfile.add(set, JsonObject);
    }

    public static synchronized void save()
    {
        try
        {
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(vrProfileCfgFile), StandardCharsets.UTF_8);
            String s = gson.toJson(jsonConfigRoot);
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

    private static JsonObject getCurrentProfile()
    {
        return (profiles.get(currentProfileName) instanceof JsonObject objectJSON) ? objectJSON : null;
    }

    public static synchronized String getCurrentProfileName()
    {
        return currentProfileName;
    }

    public static synchronized boolean setCurrentProfile(String profileName, StringBuilder error)
    {
        if (!profiles.has(profileName))
        {
            error.append("Profile '").append(profileName).append("' not found.");
            return false;
        }
        else
        {
            currentProfileName = profileName;
            jsonConfigRoot.add("selectedProfile",new JsonPrimitive(currentProfileName));
            return true;
        }
    }

    public static synchronized boolean createProfile(String profileName, StringBuilder error)
    {
        if (profiles.has(profileName))
        {
            error.append("Profile '").append(profileName).append("' already exists.");
            return false;
        }
        else
        {
            JsonObject JsonObject = new JsonObject();
            profiles.add(profileName, JsonObject);
            return true;
        }
    }

    public static synchronized boolean renameProfile(String existingProfileName, String newProfileName, StringBuilder error)
    {
        if ("Default".equals(existingProfileName))
        {
            error.append("Cannot rename Default profile.");
            return false;
        }
        else if (!profiles.has(existingProfileName))
        {
            error.append("Profile '").append(existingProfileName).append("' not found.");
            return false;
        }
        else if (profiles.has(newProfileName))
        {
            error.append("Profile '").append(newProfileName).append("' already exists.");
            return false;
        }
        else
        {
            JsonObject JsonObject = profiles.get(existingProfileName).getAsJsonObject().deepCopy();
            profiles.remove(existingProfileName);
            profiles.add(newProfileName, JsonObject);

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
            error.append("Profile '").append(profileName).append("' not found.");
            return false;
        }
        else if (profiles.has(duplicateProfileName))
        {
            error.append("Profile '").append(duplicateProfileName).append("' already exists.");
            return false;
        }
        else
        {
            JsonObject JsonObject = profiles.get(profileName).getAsJsonObject().deepCopy();
            profiles.add(duplicateProfileName, JsonObject);
            return true;
        }
    }

    public static synchronized boolean deleteProfile(String profileName, StringBuilder error)
    {
        if ("Default".equals(profileName))
        {
            error.append("Cannot delete Default profile.");
            return false;
        }
        else if (!profiles.has(profileName))
        {
            error.append("Profile '").append(profileName).append("' not found.");
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
            JsonObject JsonObject = getCurrentProfile();

            if (JsonObject != null)
            {
                try
                {
                    loadLegacySettings(DEFAULT_BINDINGS, JsonObject, "Controller");
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
