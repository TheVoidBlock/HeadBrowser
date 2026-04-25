package io.github.thevoidblock.headbrowser.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createNormalMenuOptions", at = @At("TAIL"))
    private void init(int topPos, int spacing, CallbackInfoReturnable<Integer> cir) {
        if(CONFIG.modEnabled() && CONFIG.titleButton() && componentsBound()) {
            int singlePlayerButtonWidth = 200;
            int singlePlayerButtonX = this.width / 2 - singlePlayerButtonWidth / 2;

            this.addRenderableWidget(createSquareBrowseButton(singlePlayerButtonX, singlePlayerButtonWidth, topPos));
        }
    }
}
