package io.github.thevoidblock.headbrowser;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class Styler {
    public static Text StyleHeadTooltip(String name) {
        MutableText nameText = (MutableText) Text.of(name);
        nameText.setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withBold(true));

        MutableText infoText = Text.translatable("tooltip.headbrowser.head");
        infoText.setStyle(Style.EMPTY.withBold(false).withColor(Formatting.WHITE));

        MutableText tooltipText = Text.empty();
        tooltipText.append(nameText);
        tooltipText.append("\n");
        tooltipText.append(infoText);

        return tooltipText;
    }
}
