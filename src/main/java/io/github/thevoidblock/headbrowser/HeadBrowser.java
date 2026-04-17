package io.github.thevoidblock.headbrowser;

import io.github.thevoidblock.headbrowser.gui.BrowseScreen;
import io.github.thevoidblock.headbrowser.gui.BrowserChildCreativeInventoryScreen;
import io.github.thevoidblock.headbrowser.gui.ErrorScreen;
import io.github.thevoidblock.headbrowser.gui.widget.BrowseHeadsButton;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

public class HeadBrowser implements ClientModInitializer {
    public static final String MOD_ID = "headbrowser";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Minecraft CLIENT = Minecraft.getInstance();
    public static final String ISSUES_URL = "https://github.com/TheVoidBlock/HeadBrowser/issues/new";
    public static final io.github.thevoidblock.headbrowser.HeadBrowserConfig CONFIG = io.github.thevoidblock.headbrowser.HeadBrowserConfig.createAndLoad();
    public static final File MOD_FOLDER = new File(CLIENT.gameDirectory, MOD_ID);

    public static final int BROWSE_BUTTON_OFFSET = 4;
    public static final Dimension BROWSE_BUTTON_DIMENSIONS = new Dimension(20, 20);

    @Override
    public void onInitializeClient() {
        if(CONFIG.modEnabled()) {
            if(!MinecraftHeadsAPI.readHeads()) {
                MinecraftHeadsAPI.downloadAndSaveHeads();
            } else if(currentTimeMillis() - MinecraftHeadsAPI.HEADS.downloadTime > CONFIG.cacheExpirationTime() * 1000L) {
                LOGGER.info("Heads cache expired. Downloading new heads");
                MinecraftHeadsAPI.downloadAndSaveHeads();
            }
        }

        try {
            new ItemStack(() -> Items.PLAYER_HEAD);
        } catch (NullPointerException e) {
            if(e.getMessage().equals("Components not bound yet"))
                loadWorld();
        }

        KeyBindings.registerBindFunctions();
        ClientTickScheduler.register();
    }

    private static void loadWorld() {
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(CLIENT.getResourcePackRepository(), WorldDataConfiguration.DEFAULT, false, true);
        WorldLoader.InitConfig loadConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, LevelBasedPermissionSet.ALL_PERMISSIONS);

        WorldLoader.load(
                loadConfig,
                context -> new WorldLoader.DataLoadOutput<>(Optional.empty(), context.datapackDimensions()),
                (resourceManager, _, _, _) -> {
                    resourceManager.close();
                    return Optional.empty();
                },
                Util.backgroundExecutor(),
                CLIENT
        );
    }

    private static void presentError(String message, String error) {
        CLIENT.setScreen(new ErrorScreen(message, error));
    }

    public static void error(String message, Exception e) {
        LOGGER.error(message, e);
        if(CLIENT.isGameLoadFinished()) presentError(message, e.toString());
    }

    public static boolean canGetHeadItem() {
        return CLIENT.player != null && (CLIENT.player.gameMode() == GameType.CREATIVE || CLIENT.isSingleplayer());
    }

    public static void getItem(ItemStack item) {
        LocalPlayer player = CLIENT.player;
        if(player == null) return;
        if(KeyBindings.isKeyPressed(GLFW.GLFW_KEY_LEFT_ALT)) CLIENT.setScreen(new BrowserChildCreativeInventoryScreen(CLIENT, CLIENT.screen));
        else CLIENT.setScreen(new InventoryScreen(player));
        player.containerMenu.setCarried(item);

        if(CLIENT.isSingleplayer()) {
            MinecraftServer server = CLIENT.getSingleplayerServer();
            assert server != null;
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
            assert serverPlayer != null;
            serverPlayer.containerMenu.setCarried(item);
        }
    }

    public static Button createSquareBrowseButton(int anchorButtonX, int anchorButtonWidth, int y) {
        return new BrowseHeadsButton(
                anchorButtonX + anchorButtonWidth + BROWSE_BUTTON_OFFSET + CONFIG.titleButtonHorizontalOffset() * CONFIG.offsetMultiplier(),
                y + CONFIG.titleButtonVerticalOffset() * CONFIG.offsetMultiplier(),
                BROWSE_BUTTON_DIMENSIONS.width,
                Component.empty(),
                _ -> BrowseScreen.open(CLIENT),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }

    public static Button createWideBrowseButton(int width) {
        return new BrowseHeadsButton(
                0,
                0,
                width,
                Component.translatable(format("menu.%s.wide-browse-button", MOD_ID)),
                _ -> BrowseScreen.open(CLIENT),
                MinecraftHeadsAPI.HEADS.getRandomHead()
        );
    }
}
