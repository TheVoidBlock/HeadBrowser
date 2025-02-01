package io.github.thevoidblock.headbrowser.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class BrowseHeadsButton extends ButtonWidget {

    private final static int HEAD_OFFSET_X = 2;
    private final static int HEAD_OFFSET_Y = 1;

    private final ItemStack head;

    public BrowseHeadsButton(int x, int y, int width, Text message, PressAction onPress, ItemStack head) {
        super(x, y, width, DEFAULT_HEIGHT, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.head = head;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.drawItem(head, this.getX() + HEAD_OFFSET_X, this.getY() + HEAD_OFFSET_Y);
    }
}
