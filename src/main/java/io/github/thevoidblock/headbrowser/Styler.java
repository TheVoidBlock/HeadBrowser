package io.github.thevoidblock.headbrowser;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;
import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.HEADS;

public class Styler {
    public static Text StyleHeadTooltip(String name, int categoryId) {
        MutableText nameText = (MutableText) Text.of(name);
        nameText.setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withBold(true));

        MutableText infoText = Text.empty().setStyle(Style.EMPTY.withBold(false).withColor(Formatting.WHITE));

        if(CLIENT.player != null && CLIENT.player.getGameMode() == GameMode.CREATIVE) {
            infoText.append(Text.translatable("tooltip.headbrowser.head.left-click-option", Text.translatable("tooltip.headbrowser.head.get-head-option")));
            infoText.append("\n");
        }

        infoText.append(Text.translatable("tooltip.headbrowser.head.middle-click-option", Text.translatable("tooltip.headbrowser.head.equip-as-skin-option")));

        infoText.append("\n");
        infoText.append(Text.translatable("tooltip.headbrowser.head.right-click-option", Text.translatable("tooltip.headbrowser.head.view-head-info-option")));

        MutableText categoryInfoText = (MutableText) HEADS.getCategoryText(categoryId);
        categoryInfoText.setStyle(Style.EMPTY.withBold(false).withColor(Formatting.DARK_GRAY));

        MutableText tooltipText = Text.empty();
        tooltipText.append(nameText);
        tooltipText.append("\n");
        tooltipText.append(infoText);
        tooltipText.append("\n");
        tooltipText.append(categoryInfoText);

        return tooltipText;
    }
}
