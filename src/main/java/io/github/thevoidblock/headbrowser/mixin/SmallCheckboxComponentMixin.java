package io.github.thevoidblock.headbrowser.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import net.minecraft.client.gui.Click;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmallCheckboxComponent.class)
public class SmallCheckboxComponentMixin {
    @ModifyExpressionValue(method = "onMouseDown", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Click;isLeft()Z"))
    public boolean onMouseDown(boolean original, Click click) {
        return click.button() == 0;
    }
}
