package io.github.thevoidblock.headbrowser.gui;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BrowserChildCreativeInventoryScreen extends CreativeModeInventoryScreen {
    private final Screen parent;

    public BrowserChildCreativeInventoryScreen(Minecraft client, Screen parent) {
        super(Objects.requireNonNull(client.player), client.player.connection.enabledFeatures(), client.options.operatorItemsTab().get());
        this.parent = parent;
    }

    @Override
    protected void slotClicked(final @Nullable Slot slot, final int slotId, final int buttonNum, @NonNull ContainerInput containerInput) {
        super.slotClicked(slot, slotId, buttonNum, containerInput);
        onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(parent);
    }
}
