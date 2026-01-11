package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.util.Identifier;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

public class ChangingSkinScreen extends BaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "changing_skin_screen";

    public ChangingSkinScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
    }

    @Override
    protected void build(FlowLayout flowLayout) {}
}
