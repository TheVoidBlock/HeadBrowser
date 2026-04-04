package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

public class ConfirmScreen extends ChildBaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "confirm_screen";

    private final Component message;
    private final Runnable onConfirm;

    public ConfirmScreen(Component message, Runnable onConfirm) {
        super(FlowLayout.class, BaseUIModelScreen.DataSource.asset(Identifier.fromNamespaceAndPath(MOD_ID, SCREEN_ID)));
        this.message = message;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "message").text(message);
        rootComponent.childById(ButtonComponent.class, "cancel").onPress(_ -> this.onClose());
        rootComponent.childById(ButtonComponent.class, "confirm").onPress(_ -> onConfirm.run());
    }
}
