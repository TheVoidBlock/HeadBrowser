package io.github.thevoidblock.headbrowser.gui;

import io.github.thevoidblock.headbrowser.MinecraftHeadsAPI;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;
import static java.lang.String.format;

public class HeadInfoScreen extends ChildBaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "head_info_screen";
    private static final int FIELD_WIDTH = 150;
    private static final int FIELD_MARGIN = 5;
    private static final int FIELD_LABEL_OFFSET = 1;

    private final MinecraftHeadsAPI.Head head;

    public HeadInfoScreen(MinecraftHeadsAPI.Head head) {
        super(FlowLayout.class, BaseUIModelScreen.DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
        this.head = head;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        ItemComponent headItem = rootComponent.childById(ItemComponent.class, "head-item");
        headItem.stack(head.toItem());

        LabelComponent headLabel = rootComponent.childById(LabelComponent.class, "head-label");
        headLabel.text(Text.literal(head.name()));

        ButtonComponent confirmButton = rootComponent.childById(ButtonComponent.class, "confirm");
        confirmButton.onPress(button -> this.close());

        FlowLayout fields = rootComponent.childById(FlowLayout.class, "fields");
        addField(fields, "name", head.name());
        addField(fields, "value", head.value());
        addField(fields, "category", MinecraftHeadsAPI.HEADS.getCategoryText(head.category()).getString());
        addField(fields, "category-id", String.valueOf(head.category()));
    }

    private void addField(FlowLayout fields, String field, String value) {
        LabelComponent fieldLabel = Components.label(Text.translatableWithFallback(format("screen.%s.head-info.%s", MOD_ID, field), field));
        fieldLabel.margins(Insets.left(FIELD_LABEL_OFFSET));

        FlowLayout valueLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        valueLayout.verticalAlignment(VerticalAlignment.CENTER);
        valueLayout.margins(Insets.bottom(FIELD_MARGIN));

        TextBoxComponent valueTextBox = Components.textBox(Sizing.fixed(FIELD_WIDTH)).text(value);
        valueTextBox.active = false;

        ButtonComponent copyButton = Components.button(Text.translatable(format("screen.%s.head-info.copy", MOD_ID)), button -> {
            assert this.client != null;
            this.client.keyboard.setClipboard(value);
        });

        valueLayout.child(valueTextBox);
        valueLayout.child(copyButton);

        fields.child(fieldLabel);
        fields.child(valueLayout);
    }
}
