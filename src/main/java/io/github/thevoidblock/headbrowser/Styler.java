package io.github.thevoidblock.headbrowser;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CONFIG;
import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.HEADS;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class Styler {
    public static Component StyleHeadTooltip(String name, int categoryId, boolean favoritesActive) {
        MutableComponent nameText = (MutableComponent) Component.nullToEmpty(name);
        nameText.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true));

        MutableComponent hints = Component.empty().setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE));

        if(HeadBrowser.canGetHeadItem()) {
            hints.append(Component.translatable("tooltip.headbrowser.head.left-click-option", Component.translatable("tooltip.headbrowser.head.get-head-option")));
            hints.append("\n");
            hints.append(Component.translatable("tooltip.headbrowser.head.alt-left-click-option", Component.translatable("tooltip.headbrowser.head.get-multiple-option")));
            hints.append("\n");
        }

        hints.append(Component.translatable("tooltip.headbrowser.head.middle-click-option", Component.translatable("tooltip.headbrowser.head.equip-as-skin-option")));

        hints.append("\n");
        hints.append(Component.translatable("tooltip.headbrowser.head.right-click-option", Component.translatable("tooltip.headbrowser.head.view-head-info-option")));

        hints.append("\n");
        hints.append(Component.translatable("tooltip.headbrowser.head.shift-right-click-option", favoritesActive ? Component.translatable("tooltip.headbrowser.head.remove-from-favorites-option") : Component.translatable("tooltip.headbrowser.head.add-to-favorites-option")));

        MutableComponent categoryInfoText = (MutableComponent) HEADS.getCategoryText(categoryId);
        categoryInfoText.setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.DARK_GRAY));

        MutableComponent tooltipText = Component.empty();
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
