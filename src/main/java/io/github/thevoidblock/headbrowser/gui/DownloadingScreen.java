package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;
import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.COMPLETED;
import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.STEPS;
import static java.lang.String.format;

public class DownloadingScreen extends BaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "downloading_screen";

    public DownloadingScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
    }

    FlowLayout progressBarBackground;
    BoxComponent progressBarForeground;

    @Override
    protected void build(FlowLayout rootComponent) {
        progressBarBackground = rootComponent.childById(FlowLayout.class, "progress-bar-background");
        progressBarForeground = rootComponent.childById(BoxComponent.class, "progress-bar-foreground");
    }

    @Override
    public void tick() {
        super.tick();

        if(COMPLETED == -1 && this.client != null) this.client.setScreen(new AlertScreen(Text.translatable(format("alert.%s.download-failed", MOD_ID))));
        if(STEPS == 0) return;

        if(progressBarBackground != null && progressBarForeground != null) progressBarForeground.horizontalSizing(Sizing.fixed(progressBarBackground.horizontalSizing().get().value / STEPS * COMPLETED));
        if(STEPS == COMPLETED) BrowseScreen.open(this.client);
    }
}
