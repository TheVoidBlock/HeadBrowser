package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

public class AlertScreen extends BaseUIModelScreen<FlowLayout> {

    public static final String SCREEN_ID = "alert_screen";

    private final Text message;

    public AlertScreen(Text message) {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
        this.message = message;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "message").text(message);
        rootComponent.childById(ButtonComponent.class, "ok").onPress(button -> {
            this.close();
        });
    }
}
