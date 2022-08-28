package org.vivecraft.physicalinventory;

import java.util.ArrayList;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

public class ExtendedModelManager
{
    ArrayList<String> models = new ArrayList<>();

    private void putModels(ModelBakery bakery)
    {
        for (String s : this.models)
        {
            new ResourceLocation(s);
        }
    }

    public void registerModel(String baseId)
    {
        this.models.add(baseId);
    }
}
