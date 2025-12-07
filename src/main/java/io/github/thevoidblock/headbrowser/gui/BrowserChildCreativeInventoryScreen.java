package io.github.thevoidblock.headbrowser.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BrowserChildCreativeInventoryScreen extends CreativeInventoryScreen {
    private final Screen parent;

    public BrowserChildCreativeInventoryScreen(MinecraftClient client, Screen parent) {
        super(Objects.requireNonNull(client.player), client.player.networkHandler.getEnabledFeatures(), client.options.getOperatorItemsTab().getValue());
        this.parent = parent;
    }

    @Override
    public void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);
        close();
    }

    @Override
    public void close() {
        super.close();
        assert this.client != null;
        this.client.setScreen(parent);
    }
}
