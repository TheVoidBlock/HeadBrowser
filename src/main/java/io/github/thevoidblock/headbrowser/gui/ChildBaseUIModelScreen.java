package io.github.thevoidblock.headbrowser.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.core.ParentUIComponent;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;

public abstract class ChildBaseUIModelScreen<R extends ParentUIComponent> extends BaseUIModelScreen<R> {
    private final Screen parent;

    protected ChildBaseUIModelScreen(Class<R> rootComponentClass, @NotNull BaseUIModelScreen.DataSource source, Screen parent) {
        super(rootComponentClass, source);
        this.parent = parent;
    }

    protected ChildBaseUIModelScreen(Class<R> rootComponentClass, @NotNull BaseUIModelScreen.DataSource source) {
        this(rootComponentClass, source, CLIENT.gui.screen());
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
