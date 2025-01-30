package io.github.thevoidblock.headbrowser.mixin;

import io.wispforest.owo.ui.container.GridLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GridLayout.class)
public interface GridLayoutAccessor {
    @Accessor
    int getRows();

    @Accessor
    int getColumns();
}
