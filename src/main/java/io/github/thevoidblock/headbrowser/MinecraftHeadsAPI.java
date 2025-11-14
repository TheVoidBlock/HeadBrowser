package io.github.thevoidblock.headbrowser;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.thevoidblock.headbrowser.util.ThrowingConsumer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static java.lang.String.format;

public class MinecraftHeadsAPI {

    private static final String APP_UUID = "ecdf3625-9a93-4481-b8be-a32f25ca1ea0";
    private static final File HEADS_FILE = new File(CLIENT.runDirectory, "headbrowser_cache.json");
    private static final Gson GSON = new GsonBuilder().create();
    private static final String MINECRAFT_HEADS_API = "https://minecraft-heads.com/api/heads/";
    private static final String CUSTOM_HEADS_ENDPOINT = "custom-heads";
    private static final String CATEGORIES_ENDPOINT = "categories";
    private static final UUID DEFAULT_UUID = UUID.fromString("967e3d4f-c3d3-48b9-9989-79387adcbfec");

    public static HEADS HEADS = new HEADS();

    private static boolean downloadDatabase() throws IOException, IllegalStateException {
        LOGGER.info("Starting Head database download...");

        List<Head> heads = new ArrayList<>();
        Map<Integer, String> categories = new HashMap<>();
        OkHttpClient client = new OkHttpClient();

        // Request categories
        sendRequest(client, getRequest(builder -> builder, CATEGORIES_ENDPOINT), response -> {
            JsonArray data = response.getAsJsonArray("data");
            for(JsonElement entry : data) {
                JsonObject category = entry.getAsJsonObject();
                int id = category.get("id").getAsInt();
                String name = category.get("n").getAsString();
                categories.put(id, name);
            }
        });

        // Request head data
        AtomicInteger pages = new AtomicInteger(1);
        for(AtomicInteger page = new AtomicInteger(1); page.get() <= pages.get(); page.incrementAndGet()) {
            Request request = getRequest(builder -> builder.addQueryParameter("page", String.valueOf(page.get())),
                    CUSTOM_HEADS_ENDPOINT
            );

            sendRequest(client, request, response -> {
                JsonObject pagination = response.getAsJsonObject("pagination");
                pages.set(pagination.get("last_page").getAsInt());

                JsonArray data = response.getAsJsonArray("data");

                for(JsonElement entry : data) {
                    JsonObject head = entry.getAsJsonObject();
                    heads.add(new Head(
                            head.get("n").getAsString(),
                            encodeTextureToValue(head.get("u").getAsString()),
                            head.get("c").getAsInt(),
                            DEFAULT_UUID
                    ));
                }
            });
        }

        HEADS.data = heads;
        HEADS.categories = categories;
        LOGGER.info("Finished Head database download");
        return true;
    }

    private static void sendRequest(OkHttpClient client, Request request, ThrowingConsumer<JsonObject> onResponse) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            onResponse.accept(JsonParser.parseString(response.body().string()).getAsJsonObject());
        } catch (JsonParseException e) {
            throw new IOException("Head Browser API did not return a JSON object", e);
        } catch (ClassCastException e) {
            throw new IOException("Failed to parse Head Browser API response JSON", e);
        } catch (Exception e) {
            throw new IOException("Failed to send a request to Head Browser API", e);
        }
    }

    private static Request getRequest(@NotNull Function<HttpUrl.Builder, HttpUrl.Builder> builder, String endpoint) throws IOException {
        try {
            HttpUrl url = builder.apply(Objects.requireNonNull(HttpUrl.parse(MINECRAFT_HEADS_API + endpoint)).newBuilder())
                    .addQueryParameter("app_uuid", APP_UUID)
                    .build();
            return new Request.Builder()
                    .url(url)
                    .build();
        } catch (NullPointerException e) {
            throw new IOException("Malformed API endpoint");
        }
    }

    private static String encodeTextureToValue(String texture) {
        String json = format("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/%s\"}}}", texture);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static void saveHeads() {
        LOGGER.info("Saving heads cache at {}", HEADS_FILE.getPath());
        try (FileWriter writer = new FileWriter(HEADS_FILE)) {
            writer.write(GSON.toJson(HEADS));
            LOGGER.info("Saved heads cache successfully!");
        } catch (IOException e) {
            String errorMessage = "Error saving heads cache";
            presentError(errorMessage, e.toString());
            throw new RuntimeException(errorMessage, e);
        }
    }

    public static boolean readHeads() {
        LOGGER.info("Reading heads cache at {}", HEADS_FILE.getPath());
        try {
            HEADS = GSON.fromJson(Files.readString(HEADS_FILE.toPath()), MinecraftHeadsAPI.HEADS.class);
            LOGGER.info("Read heads cache successfully!");
            return true;
        } catch (NoSuchFileException e) {
            LOGGER.info("No heads cache was found.");
            return false;
        } catch (IOException e) {
            String errorMessage = "Error reading heads cache";
            presentError(errorMessage, e.toString());
            throw new RuntimeException(errorMessage, e);
        }
    }

    public static void downloadAndSaveHeads() {
        try {
            if(downloadDatabase()) saveHeads();
        } catch (IOException e) {
            LOGGER.warn("Failed to download Minecraft Heads database", e);
        }
    }

    public record Head(String name, String value, int category, UUID uuid) {
        public ItemStack toItem() {
            ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
            GameProfile profile = new GameProfile(this.uuid, "TheVoidBlock");
            profile.getProperties().put("textures", new Property("textures", this.value));
            head.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
            head.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.name()).setStyle(Style.EMPTY.withItalic(false)));
            head.set(DataComponentTypes.LORE, new LoreComponent(Collections.singletonList(Text.literal("Head Browser mod by TheVoidBlock").formatted(Formatting.DARK_GRAY))));
            return head;
        }
    }

    public static class HEADS {
        public List<Head> data = new ArrayList<>();
        public long downloadTime = System.currentTimeMillis();
        public Map<Integer, String> categories = new HashMap<>();

        public ItemStack getRandomHead() {
            Random random = new Random();

            if(!data.isEmpty()) return data.get(random.nextInt(0, data.size())).toItem();
            else return Items.PLAYER_HEAD.getDefaultStack();
        }

        public Text getCategoryText(int id) {
            String name = categories.get(id);

            String translationKey = format("screen.%s.browse.category.%s", MOD_ID, name
                    .toLowerCase()
                    .replace(" ", "")
                    .replace("&", "-")
            );

            return Language.getInstance().hasTranslation(translationKey) ? Text.translatable(translationKey) : Text.literal(name);
        }
    }
}
