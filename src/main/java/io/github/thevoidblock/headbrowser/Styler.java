package io.github.thevoidblock.headbrowser;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.HEADS;

public class Styler {
    public static Text StyleHeadTooltip(String name, int categoryId) {
        MutableText nameText = (MutableText) Text.of(name);
        nameText.setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withBold(true));

        MutableText infoText = Text.translatable("tooltip.headbrowser.head-options");
        infoText.setStyle(Style.EMPTY.withBold(false).withColor(Formatting.WHITE));


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
