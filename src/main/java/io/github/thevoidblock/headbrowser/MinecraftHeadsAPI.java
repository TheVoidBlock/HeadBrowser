package io.github.thevoidblock.headbrowser;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.Stream;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static java.lang.String.format;

public class MinecraftHeadsAPI {

    private static final String APP_UUID = "ecdf3625-9a93-4481-b8be-a32f25ca1ea0";
    public static final File HEADS_FILE = new File(CLIENT.runDirectory, "headbrowser_cache.json");
    private static final Gson GSON = new GsonBuilder().create();
    public static final String MINECRAFT_HEADS_API = "https://minecraft-heads.com/api/heads/custom-heads";

    public static HEADS HEADS = new HEADS();

    private static boolean downloadDatabase() {

        LOGGER.info("Starting Head database download...");

        List<Head> heads = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for(CATEGORY category : CATEGORY.values()) {
                HttpGet request = new HttpGet(format("%s?app_uuid=%s&category_id=%s", MINECRAFT_HEADS_API, APP_UUID, category.ordinal() + 1));
                String response = client.execute(request, new BasicResponseHandler());
                List<JsonElement> categoryHeadsJson = GSON.fromJson(response, JsonObject.class).get("data").getAsJsonArray().asList();

                List<Head> categoryHeads = new ArrayList<>();
                for(JsonElement headUnspecified : categoryHeadsJson) {
                    JsonObject head = headUnspecified.getAsJsonObject();
                    categoryHeads.add(
                            new Head(
                                    head.get("n").getAsString(),
                                    encodeTextureToValue(head.get("u").getAsString()),
                                    category,
                                    UUID.fromString("967e3d4f-c3d3-48b9-9989-79387adcbfec")
                            )
                    );
                }

                heads = Stream.concat(heads.stream(), categoryHeads.stream()).toList();
            }
        } catch (IOException e) {
            String errorMessage = "Failed to execute HTTP request due to an I/O error. You are likely not connected to the internet.";
            LOGGER.error(errorMessage, e);
            LOGGER.info("Failed to download head database");
            return false;
        } catch (IllegalStateException e) {
            String errorMessage = "Minecraft Heads returned an unexpected data structure.";
            LOGGER.error(errorMessage, e);
            return false;
        }

        LOGGER.info("Finished Head database download");

        HEADS.heads = heads;

        return true;
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
        if(downloadDatabase()) saveHeads();
    }

    public record Head(String name, String value, CATEGORY category, UUID uuid) {

        public ItemStack toItem() {
                ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
                GameProfile profile = new GameProfile(this.uuid, "TheVoidBlock");
                profile.getProperties().put("textures", new Property("textures", this.value));
                head.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
                head.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.name()).setStyle(Style.EMPTY.withItalic(false)));
                return head;
        }
    }

    public enum CATEGORY implements StringIdentifiable {
        ALPHABET("Alphabet"),
        ANIMALS("Animals"),
        BLOCKS("Blocks"),
        DECORATION("Decoration"),
        FOOD_DRINKS("Food-Drinks"),
        HUMANS("Humans"),
        HUMANOID("Humanoid"),
        MISCELLANEOUS("Miscellaneous"),
        MONSTERS("Monsters"),
        PLANTS("Plants");

        private final String string;

        CATEGORY(String string) {
            this.string = string;
        }

        @Override
        public String asString() {
            return string;
        }
    }

    public static class HEADS {
        public List<Head> heads = new ArrayList<>();
        public long downloadTime = System.currentTimeMillis();

        public ItemStack getRandomHead() {
            Random random = new Random();

            if(!heads.isEmpty()) return heads.get(random.nextInt(0, heads.size())).toItem();
            else return Items.PLAYER_HEAD.getDefaultStack();
        }
    }
}
