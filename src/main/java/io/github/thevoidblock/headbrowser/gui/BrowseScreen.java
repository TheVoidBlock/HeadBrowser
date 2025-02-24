package io.github.thevoidblock.headbrowser.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.thevoidblock.headbrowser.MinecraftHeadsAPI;
import io.github.thevoidblock.headbrowser.SkinChanger;
import io.github.thevoidblock.headbrowser.Styler;
import io.github.thevoidblock.headbrowser.mixin.GridLayoutAccessor;
import io.github.thevoidblock.headbrowser.util.ClientTickScheduler;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static io.github.thevoidblock.headbrowser.HeadBrowser.*;
import static io.github.thevoidblock.headbrowser.HeadBrowser.CLIENT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BrowseScreen extends BaseUIModelScreen<FlowLayout> {

    public static final String SCREEN_ID = "browse_screen";

    private final static Gson GSON = new GsonBuilder().create();

    private final static int PAGES_BEFORE_TRUNCATION = 3;

    public BrowseScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(MOD_ID, SCREEN_ID)));
    }

    @Override
    protected void build(FlowLayout rootComponent) {

        GridLayout headsGrid = rootComponent.childById(GridLayout.class, "heads");
        TextBoxComponent searchBox = rootComponent.childById(TextBoxComponent.class, "search-box");
        ButtonComponent searchButton = rootComponent.childById(ButtonComponent.class, "search-button");

        ButtonComponent nextPageButton = rootComponent.childById(ButtonComponent.class, "next-page");
        ButtonComponent previousPageButton = rootComponent.childById(ButtonComponent.class, "previous-page");

        FlowLayout leftPageButtons = rootComponent.childById(FlowLayout.class, "pages-left-section");
        FlowLayout middlePageButtons = rootComponent.childById(FlowLayout.class, "pages-middle-section");
        FlowLayout rightPageButtons = rootComponent.childById(FlowLayout.class, "pages-right-section");

        FlowLayout categories = rootComponent.childById(FlowLayout.class, "categories");

        Filter filter = new Filter();
        BuildData data = new BuildData(
                categories,
                filter,
                headsGrid,
                nextPageButton,
                previousPageButton,
                leftPageButtons,
                middlePageButtons,
                rightPageButtons
        );
        rebuildDynamic(data);
        buildCategories(data);

        searchButton.onPress(button -> {
            filter.setSearchQuery(searchBox.getText());
            rebuildDynamic(data);
        });

        searchBox.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            ClientTickScheduler.schedule(client -> {
                if(CONFIG.autoQuery() || keyCode == GLFW.GLFW_KEY_ENTER) {
                    filter.setSearchQuery(searchBox.getText());
                    rebuildDynamic(data);
                }
            }, 0);
            return false;
        });

        nextPageButton.onPress(button -> {
            filter.page++;
            rebuildDynamic(data);
        });

        previousPageButton.onPress(button -> {
            filter.page--;
            rebuildDynamic(data);
        });
    }

    private static void rebuildDynamic(BuildData data) {
        rebuildHeadGrid(data.headsGrid, data.filter);
        rebuildPages(
                data,
                calculatePages(data.headsGrid, data.filter.filterAll(MinecraftHeadsAPI.HEADS.heads))
        );
    }

    private static int calculatePages(GridLayout headsGrid, List<MinecraftHeadsAPI.Head> filteredHeads) {
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        return (int)Math.ceil((double) filteredHeads.size() / (rows*columns));
    }

    private static void rebuildHeadGrid(GridLayout headsGrid, Filter filter) {
        clearGrid(headsGrid);
        buildHeadGrid(headsGrid, filter);
    }

    private static void clearGrid(GridLayout headsGrid) {
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                headsGrid.removeChild(x, y);
            }
        }
    }

    private static void buildHeadGrid(GridLayout headsGrid, Filter filter) {
        int rows = ((GridLayoutAccessor)headsGrid).getRows();
        int columns = ((GridLayoutAccessor)headsGrid).getColumns();

        List<MinecraftHeadsAPI.Head> heads = new ArrayList<>(MinecraftHeadsAPI.HEADS.heads);
        heads = filter.filterAll(heads);
        heads = filter.filterPage(heads, headsGrid);

        {
            int i = 0;
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < columns; y++) {
                    if(heads.size() > i) {
                        ItemComponent head = getHeadComponent(heads.get(i));
                        headsGrid.child(head, x, y);
                    } else return;
                    i++;
                }
            }
        }
    }

    private static ItemComponent getHeadComponent(MinecraftHeadsAPI.Head head) {
        ItemStack headItem = head.toItem();
        ItemComponent headComponent = Components.item(headItem);
        headComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            switch (button) {
                case 1 -> {
                    String skinValue = head.value();
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

                    if (CLIENT.currentScreen != null) CLIENT.currentScreen.close();
                    if (CLIENT.player != null)
                        CLIENT.player.sendMessage(Text.translatable("chat.headbrowser.skin-equip", head.name()));
                    else CLIENT.setScreen(new AlertScreen(Text.translatable("chat.headbrowser.skin-equip", head.name())));
                }

                case 0 -> {
                    if(CLIENT.currentScreen != null) CLIENT.currentScreen.close();
                    getItem(headItem);
                }
            }
            return true;
        });

        createHeadComponentTooltip(headComponent, head);

        return headComponent;
    }

    private static void createHeadComponentTooltip(ItemComponent headComponent, MinecraftHeadsAPI.Head head) {
        headComponent.tooltip(Styler.StyleHeadTooltip(head.name(), head.category(), head.tags()));
    }

    private static class PageList {
        public List<Integer> left = new ArrayList<>();
        public List<Integer> middle = new ArrayList<>();
        public List<Integer> right = new ArrayList<>();

        public PageList(int page, int pages) {
            if(pages <= 3) {
                for(int i = 1; i <= pages; i++) middle.add(i);
            } else if(page <= PAGES_BEFORE_TRUNCATION) {
                for(int i = 1; i <= page + 1; i++) left.add(i);
                right.add(pages);
            } else if (page >= pages - PAGES_BEFORE_TRUNCATION) {
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

        public Map<MinecraftHeadsAPI.CATEGORY, Boolean> categories = new HashMap<>();

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
                boolean tagMatches = false;
                for(String tag : head.tags()) if(tag.toLowerCase().contains(keywords[keywords.length - 1])) {
                    tagMatches = true;
                    break;
                }
                return matchedKeywords == keywords.length
                        || ((matchedKeywords == keywords.length - 1
                        && !head.name().toLowerCase().contains(keywords[keywords.length - 1])
                        && tagMatches
                ));
            }).toList();
        }

        public List<MinecraftHeadsAPI.Head> filterPage(List<MinecraftHeadsAPI.Head> heads, GridLayout headsGrid) {
            List<MinecraftHeadsAPI.Head> filteredHeads = new ArrayList<>(heads);
            for(
                    int i = 0;
                    i < ((GridLayoutAccessor)headsGrid).getRows() * ((GridLayoutAccessor)headsGrid).getColumns() * (page - 1);
                    i++
            ) {
                filteredHeads.removeFirst();
            }

            return filteredHeads;
        }

        public List<MinecraftHeadsAPI.Head> filterCategories(List<MinecraftHeadsAPI.Head> heads) {
            return heads.stream().filter(head -> categories.getOrDefault(head.category(), true)).toList();
        }
    }

    private static void rebuildPages(
            BuildData data,
            int pages
    ) {
        data.previousPageButton.active(data.filter.page != 1);
        data.nextPageButton.active(!(data.filter.page >= pages));

        PageList pageButtons = new PageList(data.filter.page, pages);
        data.leftPageButtons.clearChildren();
        data.middlePageButtons.clearChildren();
        data.rightPageButtons.clearChildren();
        for(int page : pageButtons.left) data.leftPageButtons.child(createPageButton(page, data));
        for(int page : pageButtons.middle) data.leftPageButtons.child(createPageButton(page, data));
        for(int page : pageButtons.right) data.leftPageButtons.child(createPageButton(page, data));
    }

    private static ButtonComponent createPageButton(
            int page,
            BuildData data
    ) {
        return Components.button(
                Text.of(Integer.toString(page)),
                button -> {
                    data.filter.setPage(page);
                    rebuildDynamic(data);
                }
        ).active(data.filter.page != page);
    }

    private static void buildCategories(BuildData data) {
        data.categories.clearChildren();
        for(MinecraftHeadsAPI.CATEGORY category : MinecraftHeadsAPI.CATEGORY.values()) {
            SmallCheckboxComponent checkbox = Components.smallCheckbox(Text.of(category.asString())).checked(data.filter.categories.getOrDefault(category, true));
            checkbox.onChanged().subscribe(checked -> {
                data.filter.categories.put(category, checked);
                data.filter.page = 1;
                rebuildDynamic(data);
            });
            data.categories.child(checkbox);
        }
    }

    private static class BuildData {
        FlowLayout categories;
        Filter filter;
        GridLayout headsGrid;
        ButtonComponent nextPageButton;
        ButtonComponent previousPageButton;
        FlowLayout leftPageButtons;
        FlowLayout middlePageButtons;
        FlowLayout rightPageButtons;

        public BuildData(FlowLayout categories, Filter filter, GridLayout headsGrid, ButtonComponent nextPageButton, ButtonComponent previousPageButton, FlowLayout leftPageButtons, FlowLayout middlePageButtons, FlowLayout rightPageButtons) {
            this.categories = categories;
            this.filter = filter;
            this.headsGrid = headsGrid;
            this.nextPageButton = nextPageButton;
            this.previousPageButton = previousPageButton;
            this.leftPageButtons = leftPageButtons;
            this.middlePageButtons = middlePageButtons;
            this.rightPageButtons = rightPageButtons;
        }
    }
}
