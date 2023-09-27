package org.vivecraft.client_vr.settings.profile;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileWriter {
    private final String activeProfileName;
    private final String set;
    private Map<String, String> data = new HashMap<>();
    private JsonObject theProfile = null;

    public ProfileWriter(String set) {
        this.activeProfileName = ProfileManager.currentProfileName;
        this.set = set;
        this.data = new HashMap<>();
    }

    public ProfileWriter(String set, JsonObject theProfile) {
        this.activeProfileName = ProfileManager.currentProfileName;
        this.set = set;
        this.theProfile = theProfile;
        this.data = new HashMap<>();
    }

    public void println(String s) {
        String[] astring = ProfileManager.splitKeyValue(s);
        String sa = astring[0];
        String s1 = "";

        if (astring.length > 1) {
            s1 = astring[1];
        }

        this.data.put(sa, s1);
    }

    public void close() {
        if (this.theProfile == null) {
            ProfileManager.setProfileSet(this.activeProfileName, this.set, this.data);
            ProfileManager.save();
        } else {
            ProfileManager.setProfileSet(this.theProfile, this.set, this.data);
        }
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
