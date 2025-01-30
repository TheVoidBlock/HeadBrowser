package io.github.thevoidblock.headbrowser;

import com.google.gson.*;
import io.github.thevoidblock.headbrowser.gui.AlertScreen;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static java.lang.String.format;

public class SkinChanger {

    public static final String MINECRAFT_SKIN_API = "https://api.minecraftservices.com/minecraft/profile/skins";

    public static void changeSkin(SKIN_VARIANT skinVariant, URL skinURL) {
        HttpPost httpPost = new HttpPost(MINECRAFT_SKIN_API);

        JsonObject jsonBody = new JsonObject();
        jsonBody.add("variant", new JsonPrimitive(skinVariant.asString()));
        jsonBody.add("url", new JsonPrimitive(skinURL.toString()));

        StringEntity body;

        try {
            body = new StringEntity(jsonBody.toString());
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Failed to set skin, the JSON string has unsupported encoding.";
            presentError(errorMessage, e.toString());
            throw new RuntimeException(errorMessage, e);
        }

        httpPost.setEntity(body);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", format("Bearer %s", CLIENT.getSession().getAccessToken()));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            client.execute(httpPost, new BasicResponseHandler());
        } catch (IOException e) {
            CLIENT.setScreen(new AlertScreen(Text.translatable("chat.headbrowser.skin-fail")));
        }
    }

    public static enum SKIN_VARIANT implements StringIdentifiable {
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
