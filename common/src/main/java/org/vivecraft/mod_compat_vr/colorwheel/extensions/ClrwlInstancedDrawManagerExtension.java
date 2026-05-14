package org.vivecraft.mod_compat_vr.colorwheel.extensions;

import dev.djefrey.colorwheel.compile.ClrwlPrograms;
import org.vivecraft.api.client.data.RenderPass;

import java.util.Map;

public interface ClrwlInstancedDrawManagerExtension {
    void vivecraft$setPrograms(Map<RenderPass, ClrwlPrograms> programs);
}
