package org.vivecraft.client_vr.settings.profile;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

public class ProfileReader
{
    private String set;
    private String profile;
    private Map<String, String> currentProfile = null;
    private Iterator profileSettingsIt = null;
    private JsonObject theProfiles = null;

    public ProfileReader(String set)
    {
        this.profile = ProfileManager.currentProfileName;
        this.set = set;
    }

    public ProfileReader(String set, JsonObject theProfiles)
    {
        this.profile = ProfileManager.currentProfileName;
        this.set = set;
        this.theProfiles = theProfiles;
    }

    public String readLine() throws IOException
    {
        String s = null;

        if (this.currentProfile == null)
        {
            if (this.theProfiles == null)
            {
                this.currentProfile = ProfileManager.getProfileSet(this.profile, this.set);
            }
            else
            {
                this.currentProfile = ProfileManager.getProfileSet(this.theProfiles, this.set);
            }

            this.profileSettingsIt = this.currentProfile.entrySet().iterator();
        }

        if (this.profileSettingsIt.hasNext())
        {
            Entry entry = (Entry)this.profileSettingsIt.next();
            String s1 = (String)entry.getKey();
            String s2 = (String)entry.getValue();

            if (s2 == null)
            {
                s2 = "";
            }

            s = s1 + ":" + s2;
        }

        return s;
    }

    public void close()
    {
    }

    public Map<String, String> getData()
    {
        return this.currentProfile;
    }
}
