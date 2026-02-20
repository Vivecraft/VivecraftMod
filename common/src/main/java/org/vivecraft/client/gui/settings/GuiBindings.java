package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiGroupedListEditorScreen;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.control.InputAction;

import java.util.LinkedList;
import java.util.List;

public class GuiBindings extends GuiGroupedListEditorScreen<InputAction> {
    public GuiBindings(Screen lastScreen) {
        super(Component.empty(),
            lastScreen, false,
            () -> {
                if (MCVR.get() == null) return List.of();
                List<InputAction> list = new LinkedList<>();
                for (var entry : MCVR.get().getInputActions()) {
                    var input = MCVR.get().getInputActionByName(entry.name);
                    if (input != null) {
                        list.add(input);
                    }
                }
                return list;
            },
            () -> {},
            save -> {},
            action -> action.actionSet.name()
        );

        this.searchable = false;
    }

    @Override
    protected InputAction createNewValue(String category) {
        return null;
    }

    @Override
    protected ValueEntry<InputAction> toEntry(InputAction value, int index) {
        return new RemovableEntry(Component.literal(value.keyBinding.getName()), value, 0);
    }
}
