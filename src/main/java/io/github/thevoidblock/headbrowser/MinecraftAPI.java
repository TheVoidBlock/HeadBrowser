package io.github.thevoidblock.headbrowser;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import io.github.thevoidblock.headbrowser.gui.AlertScreen;
import io.github.thevoidblock.headbrowser.gui.ChangingSkinScreen;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;

public class MinecraftAPI {
    public static final String CHANGE_SKIN_API = "https://api.minecraftservices.com/minecraft/profile/skins/";
    public static final String QUERY_SKIN_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    public static final String QUERY_NAME_API = "https://api.mojang.com/minecraft/profile/lookup/name/";

    public static void changeSkin(SKIN_VARIANT skinVariant, URL skinURL, Runnable onSuccess) {
        CLIENT.setScreen(new ChangingSkinScreen());

        OkHttpClient client = new OkHttpClient();

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("variant", skinVariant.getSerializedName());
        jsonBody.addProperty("url", skinURL.toString());

        Request request = new Request.Builder()
                .url(CHANGE_SKIN_API)
                .post(RequestBody.create(jsonBody.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + CLIENT.getUser().getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                CLIENT.execute(() -> CLIENT.setScreen(new AlertScreen(Component.translatable("alert.headbrowser.skin-fail-network"))));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                int code = response.code();
                if(code == 200) CLIENT.execute(onSuccess);
                else CLIENT.execute(() -> CLIENT.setScreen(new AlertScreen(
                        code == 401 ? Component.translatable("alert.headbrowser.skin-fail-session") : Component.translatable("alert.headbrowser.skin-fail-code", code)
                )));
            }
        });
    }

    public static CompletableFuture<Skin> downloadSkin(String name) {
        if(!name.matches("^\\w{1,16}$")) return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid username"));
        return getUuid(name).thenComposeAsync(gameProfile -> CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(QUERY_SKIN_API + gameProfile.id().toString().replaceAll("-", ""))
                    .get()
                    .build();

            try(Response response = client.newCall(request).execute()) {
                if(!response.isSuccessful()) throw new Exception();
                JsonObject textures = null;
                for(JsonElement element : JsonParser.parseString(response.body().string()).getAsJsonObject().get("properties").getAsJsonArray()) {
                    if(element.getAsJsonObject().get("name").getAsString().equals("textures")) textures = element.getAsJsonObject();
                }
                return new Skin(Objects.requireNonNull(textures).get("value").getAsString(), gameProfile);
            } catch (Exception e) {
                LOGGER.warn("Skin not found for {} ({})", name, gameProfile);
                throw new CompletionException(e);
            }
        }));
    }

    public record Skin(String value, GameProfile gameProfile) {}

    public static CompletableFuture<GameProfile> getUuid(String name) {
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(QUERY_NAME_API + name)
                    .get()
                    .build();

            try(Response response = client.newCall(request).execute()) {
                if(!response.isSuccessful()) throw new Exception();
                JsonObject responseJson = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return new GameProfile(parseUnformattedUUID(responseJson.get("id").getAsString()), responseJson.get("name").getAsString());
            } catch (Exception e) {
                LOGGER.warn("Profile not found for {}", name);
                throw new CompletionException(e);
            }
        });
    }

    private static UUID parseUnformattedUUID(String uuid) {
        StringBuilder builder = new StringBuilder(uuid);
        builder.insert(8, "-");
        builder.insert(13, "-");
        builder.insert(18, "-");
        builder.insert(23, "-");
        return UUID.fromString(builder.toString());
    }

    public enum SKIN_VARIANT implements StringRepresentable {
        CLASSIC("classic"),
        SLIM("slim");

        private final String string;

        SKIN_VARIANT(String value) {
            this.string = value;
        }

        @Override
        public @NonNull String getSerializedName() {
            return string;
        }
    }
}
