package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

public class ConfirmScreen extends ChildBaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "confirm_screen";

    private final Text message;
    private final Runnable onConfirm;

    public ConfirmScreen(Text message, Runnable onConfirm) {
        super(FlowLayout.class, BaseUIModelScreen.DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
        this.message = message;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "message").text(message);
        rootComponent.childById(ButtonComponent.class, "cancel").onPress(button -> this.close());
        rootComponent.childById(ButtonComponent.class, "confirm").onPress(button -> onConfirm.run());
    }
}
