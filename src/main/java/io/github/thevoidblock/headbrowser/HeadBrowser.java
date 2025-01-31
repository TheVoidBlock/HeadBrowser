package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.AlertScreen;
import io.github.thevoidblock.headbrowser.gui.ErrorScreen;
import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
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

        var menuBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.examplemod.spook", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.examplemod.test" // The translation key of the keybinding's category.
        ));

        var errorTestBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.examplemod.spooky", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.examplemod.test" // The translation key of the keybinding's category.
        ));

        var headBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.examplemod.spooker", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.examplemod.test" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(menuBind.wasPressed()) {
                client.setScreen(new BrowseScreen());
            }
            if(errorTestBind.wasPressed()) {
                client.setScreen(new ErrorScreen(
                        "Failed to set skin, the JSON string had unsupported encoding. This shouldn't happen.",
                        """
                                /home/void/IdeaProjects/HeadBrowser/src/main/java/io/github/thevoidblock/headbrowser/mixin/GridLayoutAccessor.java:9: warning: Unable to locate obfuscation mapping for @Accessor target rows
                                    @Accessor
                                    ^
                                /home/void/IdeaProjects/HeadBrowser/src/main/java/io/github/thevoidblock/headbrowser/mixin/GridLayoutAccessor.java:12: warning: Unable to locate obfuscation mapping for @Accessor target columns
                                    @Accessor
                                    ^
                                /home/void/IdeaProjects/HeadBrowser/src/main/java/io/github/thevoidblock/headbrowser/HeadBrowser.java:50: error: constructor ErrorScreen in class ErrorScreen cannot be applied to given types;
                                                client.setScreen(new ErrorScreen());
                                                                 ^
                                """
                ));
            }
            if(headBind.wasPressed()) {
                CLIENT.setScreen(new AlertScreen(Text.translatable("chat.headbrowser.skin-fail")));
            }
        });
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
