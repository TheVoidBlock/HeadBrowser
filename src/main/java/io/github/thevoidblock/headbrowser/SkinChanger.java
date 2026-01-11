package io.github.thevoidblock.headbrowser;

import com.google.gson.*;
import io.github.thevoidblock.headbrowser.gui.AlertScreen;
import io.github.thevoidblock.headbrowser.gui.ChangingSkinScreen;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;

public class SkinChanger {

    public static final String MINECRAFT_SKIN_API = "https://api.minecraftservices.com/minecraft/profile/skins";

    public static void changeSkin(SKIN_VARIANT skinVariant, URL skinURL, Runnable onSuccess) {
        CLIENT.setScreen(new ChangingSkinScreen());

        OkHttpClient client = new OkHttpClient();

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("variant", skinVariant.asString());
        jsonBody.addProperty("url", skinURL.toString());

        Request request = new Request.Builder()
                .url(MINECRAFT_SKIN_API)
                .post(RequestBody.create(jsonBody.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + CLIENT.getSession().getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                CLIENT.execute(() -> CLIENT.setScreen(new AlertScreen(Text.translatable("alert.headbrowser.skin-fail-network"))));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                int code = response.code();
                if(code == 200) CLIENT.execute(onSuccess);
                else CLIENT.execute(() -> CLIENT.setScreen(new AlertScreen(
                        code == 401 ? Text.translatable("alert.headbrowser.skin-fail-session") : Text.translatable("alert.headbrowser.skin-fail-code", code)
                )));
            }
        });
    }

    public enum SKIN_VARIANT implements StringIdentifiable {
        CLASSIC("classic"),
        SLIM("slim");

        private final String string;

        SKIN_VARIANT(String value) {
            this.string = value;
        }

        @Override
        public String asString() {
            return string;
        }
    }
}
