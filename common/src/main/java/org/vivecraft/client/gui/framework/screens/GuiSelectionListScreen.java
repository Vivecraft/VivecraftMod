package org.vivecraft.client.gui.framework.screens;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.widgets.SettingsList;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuiSelectionListScreen<T> extends GuiListScreen {

    private final Supplier<List<T>> valuesSupplier;
    private final Function<T, Component> componentSupplier;
    private final Function<T, String> categorySupplier;
    private final Consumer<T> consumer;
    private final boolean resettable;
    private final Function<T, AbstractWidget> widgetSupplier;

    /**
     * @param title             title to show on the screen
     * @param lastScreen        previous screen to go back to when done
     * @param valuesSupplier    provides a list of objects to show in the list. objects are shown in the order provided
     * @param componentSupplier function that supplies a Component for the provided object to be able to show them in the list
     * @param categorySupplier  function that supplies a translation string that is used for the category the given object is in
     * @param consumer          called with the selected object, is called with {@code null} if it should reset
     * @param resettable        when true, the left button will say 'reset', else 'clear'
     * @param widgetSupplier    supplies an optional widget shown with the object
     */
    public GuiSelectionListScreen(
        Component title, Screen lastScreen, Supplier<List<T>> valuesSupplier, Function<T, Component> componentSupplier,
        @Nullable Function<T, String> categorySupplier, Consumer<T> consumer, boolean resettable,
        @Nullable Function<T, AbstractWidget> widgetSupplier)
    {
        super(title, lastScreen);
        this.valuesSupplier = valuesSupplier;
        this.componentSupplier = componentSupplier;
        this.categorySupplier = categorySupplier != null ? categorySupplier : item -> "";
        this.consumer = consumer;
        this.resettable = resettable;
        this.widgetSupplier = widgetSupplier != null ? widgetSupplier : item -> null;
    }

    @Override
    protected void addLowerButtons(int top) {
        this.addRenderableWidget(
            new Button.Builder(Component.translatable(this.resettable ? "controls.reset" : "vivecraft.gui.clear"),
                p -> {
                    this.consumer.accept(null);
                    this.onClose();
                })
                .bounds(this.width / 2 - 155, top, 150, 20)
                .build());

        this.addRenderableWidget(
            new Button.Builder(Component.translatable("gui.cancel"), p -> onClose())
                .bounds(this.width / 2 + 5, top, 150, 20)
                .build());
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        String currentCategory = "";
        SettingsList.GroupedEntry activeCategory = null;
        for (T item : this.valuesSupplier.get()) {
            String newCategory = this.categorySupplier.apply(item);
            if (!currentCategory.equals(newCategory)) {
                currentCategory = newCategory;
                activeCategory = new SettingsList.GroupedEntry(Component.translatable(newCategory));
                entries.add(activeCategory);
            }
            SettingsList.BaseEntry entry = new SettingsList.SelectableEntry(this.componentSupplier.apply(item),
                this.widgetSupplier.apply(item), () -> {
                this.consumer.accept(item);
                this.onClose();
            });
            if (activeCategory != null) {
                activeCategory.add(entry);
            } else {
                entries.add(entry);
            }
        }
        return entries;
    }
}
