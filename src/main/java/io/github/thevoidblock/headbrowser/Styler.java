package io.github.thevoidblock.headbrowser;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CONFIG;
import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.HEADS;

public class Styler {
    public static Text StyleHeadTooltip(String name, int categoryId, boolean favoritesActive) {
        MutableText nameText = (MutableText) Text.of(name);
        nameText.setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withBold(true));

        MutableText hints = Text.empty().setStyle(Style.EMPTY.withBold(false).withColor(Formatting.WHITE));

        if(HeadBrowser.canGetHeadItem()) {
            hints.append(Text.translatable("tooltip.headbrowser.head.left-click-option", Text.translatable("tooltip.headbrowser.head.get-head-option")));
            hints.append("\n");
            hints.append(Text.translatable("tooltip.headbrowser.head.alt-left-click-option", Text.translatable("tooltip.headbrowser.head.get-multiple-option")));
            hints.append("\n");
        }

        hints.append(Text.translatable("tooltip.headbrowser.head.middle-click-option", Text.translatable("tooltip.headbrowser.head.equip-as-skin-option")));

        hints.append("\n");
        hints.append(Text.translatable("tooltip.headbrowser.head.right-click-option", Text.translatable("tooltip.headbrowser.head.view-head-info-option")));

        hints.append("\n");
        hints.append(Text.translatable("tooltip.headbrowser.head.shift-right-click-option", favoritesActive ? Text.translatable("tooltip.headbrowser.head.remove-from-favorites-option") : Text.translatable("tooltip.headbrowser.head.add-to-favorites-option")));

        MutableText categoryInfoText = (MutableText) HEADS.getCategoryText(categoryId);
        categoryInfoText.setStyle(Style.EMPTY.withBold(false).withColor(Formatting.DARK_GRAY));

        MutableText tooltipText = Text.empty();
        tooltipText.append(nameText);

        if(CONFIG.tooltipHints()) {
            tooltipText.append("\n");
            tooltipText.append(hints);
        }

        tooltipText.append("\n");
        tooltipText.append(categoryInfoText);

        return tooltipText;
    }
}
