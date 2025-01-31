package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static java.lang.String.format;

public class ErrorScreen extends BaseUIModelScreen<FlowLayout> {

    public static final String SCREEN_ID = "error_screen";

    private final String message;
    private final String error;

    public ErrorScreen(String message, String error) {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
        this.message = message;
        this.error = error;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout lines = rootComponent.childById(FlowLayout.class, "lines");
        for(String line : message.lines().toList()) {
            lines.child(
                    Components.label(Text.of(line))
            );
        }
        for(String line : error.lines().toList()) {
            lines.child(
                    Components.label(Text.of(line)).color(Color.RED)
            );
        }

        rootComponent.childById(ButtonComponent.class, "copy").onPress(button ->
                CLIENT.keyboard.setClipboard(format("%s%n%s", this.message, this.error))
        );

        rootComponent.childById(ButtonComponent.class, "issues").onPress(button ->
                CLIENT.setScreen(new ConfirmLinkScreen(
                        confirmed -> {
                            if(confirmed) Util.getOperatingSystem().open(ISSUES_URL);
                            CLIENT.setScreen(this);
                        },
                        Text.literal("confirm.headbrowser.open-issues"),
                        ISSUES_URL,
                        true
        )));
    }
}
