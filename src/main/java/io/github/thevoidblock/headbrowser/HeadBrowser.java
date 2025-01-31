package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.AlertScreen;
import io.github.thevoidblock.headbrowser.gui.ErrorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.currentTimeMillis;

public class HeadBrowser implements ClientModInitializer {

    public static final String MOD_ID = "headbrowser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static final long CACHE_EXPIRATION_PERIOD = 86400000;

    @Override
    public void onInitializeClient() {

        if(!MinecraftHeadsAPI.readHeads()) {
            MinecraftHeadsAPI.downloadAndSaveHeads();
        }

        if(currentTimeMillis() - MinecraftHeadsAPI.HEADS.downloadTime > CACHE_EXPIRATION_PERIOD) {
            LOGGER.info("Heads cache expired. Downloading new heads");
            MinecraftHeadsAPI.downloadAndSaveHeads();
        }
    }

    public static void presentError(String message, String error) {
        CLIENT.setScreen(new ErrorScreen(message, error));
    }

    public static void getItem(ItemStack item) {
        if(CLIENT.player != null) {
            CLIENT.setScreen(new InventoryScreen(CLIENT.player));
            CLIENT.player.currentScreenHandler.setCursorStack(item);
        } else {
            CLIENT.setScreen(new AlertScreen(Text.translatable("alert.headbrowser.head-category-tags")));
        }
    }
}
