package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

public class LoadingScreen extends BaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "loading_screen";

    private final Component message;

    public LoadingScreen(Component message) {
        super(FlowLayout.class, DataSource.asset(Identifier.fromNamespaceAndPath(MOD_ID, SCREEN_ID)));
        this.message = message;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "message").text(message);
    }
}
