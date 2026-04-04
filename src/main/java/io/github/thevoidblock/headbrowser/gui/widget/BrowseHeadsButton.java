package io.github.thevoidblock.headbrowser.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BrowseHeadsButton extends Button.Plain {
    private final static int HEAD_OFFSET_X = 2;
    private final static int HEAD_OFFSET_Y = 1;

    private final ItemStack head;

    public BrowseHeadsButton(int x, int y, int width, MutableComponent message, OnPress onPress, ItemStack head) {
        super(x, y, width, DEFAULT_HEIGHT, message, onPress, DEFAULT_NARRATION);
        this.head = head;
    }

    @Override
    public void extractContents(final @NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, final float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        graphics.item(head, this.getX() + HEAD_OFFSET_X, this.getY() + HEAD_OFFSET_Y);
    }
}
