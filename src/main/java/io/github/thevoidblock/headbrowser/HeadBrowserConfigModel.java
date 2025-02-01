package io.github.thevoidblock.headbrowser;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;

import static io.github.thevoidblock.headbrowser.HeadBrowser.MOD_ID;

@Modmenu(modId = MOD_ID)
@Config(name = MOD_ID, wrapperName = "HeadBrowserConfig")
public class HeadBrowserConfigModel {
    public boolean modEnabled = true;
    public int titleButtonVerticalOffset = 0;
    public int titleButtonHorizontalOffset = 0;
    public boolean titleButton = true;
    public boolean pauseButton = true;

    @SectionHeader("advanced")
    public long cacheExpirationTime = 86400;
    public int offsetMultiplier = 24;
}
