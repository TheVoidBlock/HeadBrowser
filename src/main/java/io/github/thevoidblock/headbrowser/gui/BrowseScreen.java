package io.github.thevoidblock.headbrowser.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.thevoidblock.headbrowser.MinecraftHeadsAPI;
import io.github.thevoidblock.headbrowser.SkinChanger;
import io.github.thevoidblock.headbrowser.Styler;
import io.github.thevoidblock.headbrowser.mixin.GridLayoutAccessor;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BrowseScreen extends BaseUIModelScreen<FlowLayout> {

    public static final String SCREEN_ID = "browse_screen";

    private final static Gson GSON = new GsonBuilder().create();

    public BrowseScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
    }

    @Override
    protected void build(FlowLayout rootComponent) {

        GridLayout headsGrid = rootComponent.childById(GridLayout.class, "heads");

        populateHeadsGrid(headsGrid);
    }

    public static void populateHeadsGrid(GridLayout headsGrid) {
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        {
            int i = 0;
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < columns; y++) {
                    if(MinecraftHeadsAPI.HEADS.heads.size() > i) {
                        ItemComponent head = getHeadComponent(MinecraftHeadsAPI.HEADS.heads.get(i));
                        headsGrid.child(head, x, y);
                    } else return;
                    i++;
                }
            }
        }
    }

    private static ItemComponent getHeadComponent(MinecraftHeadsAPI.Head head) {
        ItemComponent headComponent = Components.item(head.toItem());
        headComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0) {
                String skinValue = head.value;
                byte[] skinValueDecodedBytes = Base64.getDecoder().decode(skinValue);
                String skinValueDecoded = new String(skinValueDecodedBytes, UTF_8);
                String skinURLString = GSON.fromJson(skinValueDecoded, JsonObject.class)
                        .get("textures").getAsJsonObject()
                        .get("SKIN").getAsJsonObject()
                        .get("url").getAsJsonPrimitive().getAsString();

                URL skinURL;
                try {
                    skinURL = URI.create(skinURLString).toURL();
                } catch (MalformedURLException e) {
                    String errorMessage = "Attempted to equip skin, but the url was malformed";
                    presentError(errorMessage, e.toString());
                    throw new RuntimeException(errorMessage, e);
                }

                SkinChanger.changeSkin(SkinChanger.SKIN_VARIANT.SLIM, skinURL);

                if(CLIENT.currentScreen != null) CLIENT.currentScreen.close();
                if(CLIENT.player != null) CLIENT.player.sendMessage(Text.translatable("chat.headbrowser.skin-equip", head.name));
                else CLIENT.setScreen(new AlertScreen(Text.translatable("chat.headbrowser.skin-equip", head.name)));
            }
            return true;
        });

        createHeadComponentTooltip(headComponent, head);

        return headComponent;
    }

    private static void createHeadComponentTooltip(ItemComponent headComponent, MinecraftHeadsAPI.Head head) {
        headComponent.tooltip(Styler.StyleHeadTooltip(head.name));
    }
}
