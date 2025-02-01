package io.github.thevoidblock.headbrowser.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgetsNormal", at = @At("TAIL"))
    private void initWidgetsNormal(int y, int spacingY, CallbackInfo ci) {
        if(CONFIG.modEnabled() && CONFIG.titleButton()) {
            int singlePlayerButtonWidth = 200;
            int singlePlayerButtonX = this.width / 2 - singlePlayerButtonWidth / 2;

            this.addDrawableChild(createSquareBrowseButton(singlePlayerButtonX, singlePlayerButtonWidth, y));
        }
    }
}
