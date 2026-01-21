package io.github.thevoidblock.headbrowser.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.thevoidblock.headbrowser.*;
import io.github.thevoidblock.headbrowser.mixin.GridLayoutAccessor;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static io.github.thevoidblock.headbrowser.MinecraftHeadsAPI.HEADS;

public class BrowseScreen extends BaseUIModelScreen<FlowLayout> {
    public static final String SCREEN_ID = "browse_screen";
    private final static Gson GSON = new GsonBuilder().create();
    private final static int PAGES_BEFORE_TRUNCATION = 3;
    private final Favorites favorites = new Favorites();
    private BuildData buildData;
    private boolean favoritesActive = false;

    private BrowseScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
    }

    public static void open(MinecraftClient client) {
        if(HEADS.data.isEmpty()) {
            client.setScreen(new DownloadingScreen());
            return;
        }

        client.setScreen(new BrowseScreen());
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        GridLayout headsGrid = rootComponent.childById(GridLayout.class, "heads");
        TextBoxComponent searchBox = rootComponent.childById(TextBoxComponent.class, "search-box");
        ButtonComponent searchButton = rootComponent.childById(ButtonComponent.class, "search-button");

        ButtonComponent nextPageButton = rootComponent.childById(ButtonComponent.class, "next-page");
        ButtonComponent previousPageButton = rootComponent.childById(ButtonComponent.class, "previous-page");

        ButtonComponent favoritesButton = rootComponent.childById(ButtonComponent.class, "favorites");

        FlowLayout leftPageButtons = rootComponent.childById(FlowLayout.class, "pages-left-section");
        FlowLayout middlePageButtons = rootComponent.childById(FlowLayout.class, "pages-middle-section");
        FlowLayout rightPageButtons = rootComponent.childById(FlowLayout.class, "pages-right-section");

        FlowLayout categories = rootComponent.childById(FlowLayout.class, "categories");

        Filter filter = new Filter();
        buildData = new BuildData(
                categories,
                filter,
                headsGrid,
                nextPageButton,
                previousPageButton,
                favoritesButton,
                leftPageButtons,
                middlePageButtons,
                rightPageButtons
        );

        rebuildDynamic();
        buildCategories();

        searchButton.onPress(button -> {
            filter.setSearchQuery(searchBox.getText());
            rebuildDynamic();
        });

        searchBox.keyPress().subscribe(keyCode -> {
            ClientTickScheduler.schedule(client -> {
                if(CONFIG.autoQuery() || keyCode.key() == GLFW.GLFW_KEY_ENTER) {
                    filter.setSearchQuery(searchBox.getText());
                    rebuildDynamic();
                }
            }, 0);
            return false;
        });

        nextPageButton.onPress(button -> {
            filter.page++;
            rebuildDynamic();
        });

        previousPageButton.onPress(button -> {
            filter.page--;
            rebuildDynamic();
        });

        favoritesButton.onPress(button -> {
            favoritesActive = !favoritesActive;
            buildData.filter.page = 1;
            if(favoritesActive) favorites.load();
            button.setMessage(favoritesActive ? Text.translatable("screen.headbrowser.browse.browser") : Text.translatable("screen.headbrowser.browse.favorites"));
            rebuildDynamic();
        });
    }

    private void rebuildDynamic() {
        rebuildHeadGrid(buildData.filter);
        rebuildPages(calculatePages(buildData.headsGrid, buildData.filter.filterAll(getHeads())));
    }

    private static int calculatePages(GridLayout headsGrid, List<MinecraftHeadsAPI.Head> filteredHeads) {
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        return (int)Math.ceil((double) filteredHeads.size() / (rows * columns));
    }

    private void rebuildHeadGrid(Filter filter) {
        clearGrid();
        buildHeadGrid(filter);
    }

    private void clearGrid() {
        GridLayout headsGrid = buildData.headsGrid;
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                headsGrid.removeChild(x, y);
            }
        }
    }

    private void buildHeadGrid(Filter filter) {
        GridLayout headsGrid = buildData.headsGrid;
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        List<MinecraftHeadsAPI.Head> heads = getHeads();
        heads = filter.filterAll(heads);
        heads = filter.filterPage(heads, headsGrid);

        for (int x = 0, i = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++, ++i) {
                if(heads.size() > i) {
                    ItemComponent head = getHeadComponent(heads.get(i));
                    headsGrid.child(head, x, y);
                } else return;
            }
        }
    }

    private List<MinecraftHeadsAPI.Head> getHeads() {
        return favoritesActive ? favorites.heads : new ArrayList<>(MinecraftHeadsAPI.HEADS.data);
    }

    private ItemComponent getHeadComponent(MinecraftHeadsAPI.Head head) {
        ItemStack headItem = head.toItem();
        ItemComponent headComponent = UIComponents.item(headItem);
        headComponent.mouseDown().subscribe((click, doubled) -> {
            switch (click.button()) {
                case 0 -> {
                    if(!canGetHeadItem()) break;
                    getItem(headItem);
                }

                case 1 -> {
                    if(KeyBindings.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                        if(favoritesActive) {
                            favorites.deleteHead(head);
                            rebuildDynamic();
                        } else favorites.saveHead(head);
                    } else {
                        CLIENT.setScreen(new HeadInfoScreen(head));
                    }
                }

                case 2 -> CLIENT.setScreen(new ConfirmScreen(Text.translatable("confirm.headbrowser.equip-skin", head.name()), () -> {
                    String skinValue = head.value();
                    byte[] skinValueDecodedBytes = Base64.getDecoder().decode(skinValue);
                    String skinValueDecoded = new String(skinValueDecodedBytes, UTF_8);
                    String skinURLString = GSON.fromJson(skinValueDecoded, JsonObject.class)
                            .get("textures").getAsJsonObject()
                            .get("SKIN").getAsJsonObject()
                            .get("url").getAsJsonPrimitive().getAsString();
                    
                    try {
                        URL skinURL = URI.create(skinURLString).toURL();
                        SkinChanger.changeSkin(SkinChanger.SKIN_VARIANT.SLIM, skinURL, () -> CLIENT.setScreen(new AlertScreen(Text.translatable("alert.headbrowser.skin-equip", head.name()))));
                    } catch (MalformedURLException e) {
                        error("Attempted to equip skin, but the url was malformed", e);
                    }
                }));
            }

            return true;
        });

        createHeadComponentTooltip(headComponent, head);

        return headComponent;
    }

    private void createHeadComponentTooltip(ItemComponent headComponent, MinecraftHeadsAPI.Head head) {
        headComponent.tooltip(Styler.StyleHeadTooltip(head.name(), head.category(), favoritesActive));
    }

    private static class PageList {
        public List<Integer> left = new ArrayList<>();
        public List<Integer> middle = new ArrayList<>();
        public List<Integer> right = new ArrayList<>();

        public PageList(int page, int pages) {
            if(pages <= 3) {
                for(int i = 1; i <= pages; i++) middle.add(i);
            } else if(page < PAGES_BEFORE_TRUNCATION) {
                for(int i = 1; i <= page + 1; i++) left.add(i);
                right.add(pages);
            } else if (page > pages - PAGES_BEFORE_TRUNCATION) {
                left.add(1);
                for(int i = page - 1; i <= pages; i++)
                    right.add(i);
            } else {
                left.add(1);
                middle.add(page - 1);
                middle.add(page);
                middle.add(page + 1);
                right.add(pages);
            }
        }
    }

    private static class Filter {
        public Map<Integer, Boolean> categories = new HashMap<>();

        public int page = 1;

        private String searchQuery = "";

        public void setSearchQuery(String searchQuery) {
            this.page = 1;
            this.searchQuery = searchQuery;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public List<MinecraftHeadsAPI.Head> filterAll(List<MinecraftHeadsAPI.Head> heads) {
            List<MinecraftHeadsAPI.Head> filteredHeads = new ArrayList<>(heads);
            filteredHeads = filterSearchQuery(filteredHeads);
            filteredHeads = filterCategories(filteredHeads);

            return filteredHeads;
        }

        private List<MinecraftHeadsAPI.Head> filterSearchQuery(List<MinecraftHeadsAPI.Head> heads) {
            return heads.stream().filter(head -> {
                String[] keywords = searchQuery.toLowerCase().split(" ");
                int matchedKeywords = 0;
                for(String keyword : keywords) if(head.name().toLowerCase().contains(keyword)) matchedKeywords++;
                return matchedKeywords == keywords.length;
            }).toList();
        }

        public List<MinecraftHeadsAPI.Head> filterPage(List<MinecraftHeadsAPI.Head> heads, GridLayout headsGrid) {
            List<MinecraftHeadsAPI.Head> filteredHeads = new ArrayList<>(heads);
            for(
                    int i = 0;
                    i < ((GridLayoutAccessor)headsGrid).getRows() * ((GridLayoutAccessor)headsGrid).getColumns() * (page - 1);
                    i++
            ) {
                if(!filteredHeads.isEmpty()) filteredHeads.removeFirst();
            }

            return filteredHeads;
        }

        public List<MinecraftHeadsAPI.Head> filterCategories(List<MinecraftHeadsAPI.Head> heads) {
            return heads.stream().filter(head -> categories.getOrDefault(head.category(), true)).toList();
        }
    }

    private void rebuildPages(
            int pages
    ) {
        buildData.previousPageButton.active(buildData.filter.page != 1);
        buildData.nextPageButton.active(!(buildData.filter.page >= pages));

        PageList pageButtons = new PageList(buildData.filter.page, pages);
        buildData.leftPageButtons.clearChildren();
        buildData.middlePageButtons.clearChildren();
        buildData.rightPageButtons.clearChildren();
        for(int page : pageButtons.left) buildData.leftPageButtons.child(createPageButton(page));
        for(int page : pageButtons.middle) buildData.leftPageButtons.child(createPageButton(page));
        for(int page : pageButtons.right) buildData.leftPageButtons.child(createPageButton(page));
    }

    private ButtonComponent createPageButton(int page) {
        return UIComponents.button(
                Text.of(Integer.toString(page)),
                button -> {
                    buildData.filter.setPage(page);
                    rebuildDynamic();
                }
        ).active(buildData.filter.page != page);
    }

    private void buildCategories() {
        Map<Integer, Boolean> categories = buildData.filter.categories;
        buildData.categories.clearChildren();
        for(Map.Entry<Integer, String> category : HEADS.categories.entrySet()) {
            SmallCheckboxComponent checkbox = UIComponents.smallCheckbox(HEADS.getCategoryText(category.getKey()))
                    .checked(categories.getOrDefault(category.getKey(), true));

            checkbox.onChanged().subscribe(checked -> {
                if(KeyBindings.isKeyPressed(GLFW.GLFW_KEY_LEFT_ALT)) {
                    HEADS.categories.forEach((id, name) -> categories.put(id, !categories.getOrDefault(id, true)));
                    categories.put(category.getKey(), true);
                } else {
                    categories.put(category.getKey(), checked);
                }
                buildData.filter.page = 1;
                rebuildDynamic();
                buildCategories();
            });

            buildData.categories.child(checkbox);
        }
    }

    private static class BuildData {
        FlowLayout categories;
        Filter filter;
        GridLayout headsGrid;
        ButtonComponent nextPageButton;
        ButtonComponent previousPageButton;
        ButtonComponent favoritesButton;
        FlowLayout leftPageButtons;
        FlowLayout middlePageButtons;
        FlowLayout rightPageButtons;

        public BuildData(FlowLayout categories, Filter filter, GridLayout headsGrid, ButtonComponent nextPageButton, ButtonComponent previousPageButton, ButtonComponent favoritesButton, FlowLayout leftPageButtons, FlowLayout middlePageButtons, FlowLayout rightPageButtons) {
            this.categories = categories;
            this.filter = filter;
            this.headsGrid = headsGrid;
            this.nextPageButton = nextPageButton;
            this.previousPageButton = previousPageButton;
            this.favoritesButton = favoritesButton;
            this.leftPageButtons = leftPageButtons;
            this.middlePageButtons = middlePageButtons;
            this.rightPageButtons = rightPageButtons;
        }
    }

    private static class Favorites {
        private static final Gson GSON = new GsonBuilder().create();
        private static final File FILE = new File(MOD_FOLDER, "favorites.json");

        public List<MinecraftHeadsAPI.Head> heads = new ArrayList<>();

        private void save() {
            //noinspection ResultOfMethodCallIgnored
            MOD_FOLDER.mkdirs();

            try(FileWriter writer = new FileWriter(FILE)) {
                writer.write(GSON.toJson(heads));
            } catch (IOException e) {
                error("Failed to save favorite heads", e);
            }
        }

        private void load() {
            try {
                heads = GSON.fromJson(Files.readString(FILE.toPath()), new TypeToken<List<MinecraftHeadsAPI.Head>>(){}.getType());
            } catch (NoSuchFileException ignored) {}
            catch (IOException e) {
                error("Failed to read favorite heads", e);
            }
        }

        public void saveHead(MinecraftHeadsAPI.Head head) {
            load();
            heads.add(head);
            save();
        }

        public void deleteHead(MinecraftHeadsAPI.Head head) {
            heads.remove(head);
            save();
        }
    }

    @Override
    public void onDisplayed() {
        if(buildData != null) rebuildHeadGrid(buildData.filter);
    }
}
