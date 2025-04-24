package spigot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class SpigotUtils implements com.elmakers.mine.bukkit.utility.platform.SpigotUtils {
    private final Platform platform;

    public SpigotUtils(Platform platform) {
        this.platform = platform;
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.spigot().sendMessage(parseChatComponents(message));
        } else {
            sender.sendMessage(ChatUtils.getSimpleMessage(message));
        }
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        if (ChatUtils.hasJSON(message)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, parseChatComponents(message));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
        return true;
    }

    @Override
    public boolean sendActionBar(Player player, String message, String font) {
        BaseComponent[] components;
        if (ChatUtils.hasJSON(message)) {
            components = parseChatComponents(message);
        } else {
            components = new BaseComponent[]{new TextComponent(message)};
        }
        BaseComponent[] fontComponent = new ComponentBuilder("").font(font).append(components).create();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, fontComponent);
        return true;
    }

    @Override
    @Nullable
    public String serializeBossBar(String title, String font) {
        if (ChatUtils.isDefaultFont(font)) {
            return null;
        }
        BaseComponent[] components;
        if (ChatUtils.hasJSON(title)) {
            components = parseChatComponents(title);
        } else {
            components = new BaseComponent[]{new TextComponent(title)};
        }
        BaseComponent[] fontComponent = new ComponentBuilder("").font(font).append(components).create();
        return serializeBossBar(fontComponent);
    }

    @Override
    @Nullable
    public String serializeBossBar(String title) {
        if (ChatUtils.hasJSON(title)) {
            BaseComponent[] components = parseChatComponents(title);
            return serializeBossBar(components);
        }
        return null;
    }

    @Nullable
    private String serializeBossBar(BaseComponent[] components) {
        if (components.length == 0) {
            return null;
        }

        BaseComponent component = components.length == 1 ? components[0] : collapseComponents(Arrays.asList(components));
        return ComponentSerializer.toString(component);
    }

    @Override
    public String getHexColor(String hexCode) {
        return ChatColor.of(hexCode).toString();
    }

    @Override
    public String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public String stripColor(String message) {
        return ChatColor.stripColor(message);
    }

    @Override
    public List<String> serializeLore(List<String> lore) {
        List<String> serializedLore = new ArrayList<>(lore.size());
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (ChatUtils.hasJSON(line)) {
                List<BaseComponent> components = new ArrayList<>();
                List<BaseComponent> addToComponents = components;
                BaseComponent addToComponent = null;
                String[] pieces = ChatUtils.getComponents(line);
                for (String component : pieces) {
                    try {
                        List<BaseComponent> addComponents;
                        if (component.startsWith("{")) {
                            addComponents = Arrays.asList(ComponentSerializer.parse(component));
                        } else {
                            addComponents = Arrays.asList(fromLegacyText(component));
                        }
                        if (!addComponents.isEmpty()) {
                            addToComponents.addAll(addComponents);
                            if (addToComponent != null) {
                                addToComponent.setExtra(addToComponents);
                            }

                            addToComponent = addToComponents.get(addToComponents.size() - 1);
                            addToComponents = addToComponent.getExtra();
                            if (addToComponents == null) {
                                addToComponents = new ArrayList<>();
                            }
                        }
                    } catch (Exception ex) {
                        platform.getLogger().log(Level.SEVERE, "Error parsing chat components from: " + component, ex);
                    }
                }
                // resetItalics is done implicitly as part of collapsing
                serializedLore.add(ComponentSerializer.toString(collapseComponents(components)));
            } else {
                // Reproduce some oddly specific spigot behavior I didn't realize was a thing,
                // but was forcing all the wand and spell lore from being italicized
                BaseComponent[] components = fromLegacyText(line);
                components = resetItalics(components);
                serializedLore.add(ComponentSerializer.toString(components));
            }
        }
        return serializedLore;
    }

    protected BaseComponent collapseComponents(List<BaseComponent> list) {
        if (list.isEmpty()) {
            return new ComponentBuilder("").create()[0];
        }
        BaseComponent single = list.get(0);

        // See if we need to reproduce the "reset vanilla italics in lore" behavior
        // that is normally handled in CraftChatMessage.StringMessage
        boolean needsReset = single.hasFormatting() && !single.isItalic();

        // If this is just a single component and doesn't need a format reset,
        // we can just return it.
        if (list.size() == 1 && !needsReset) {
            return single;
        }

        BaseComponent wrapper = new ComponentBuilder("").create()[0];
        if (needsReset) {
            wrapper.setItalic(false);
        }
        wrapper.setExtra(list);
        return wrapper;
    }

    protected BaseComponent[] resetItalics(BaseComponent[] components) {
        // Apparently spigot has some behavior where it automatically adds a reset at the start
        // of item lore if you have specified a color there.
        // So we need to reproduce this behavior for compatibility.
        // Unfortunate, fromLegacyText does not seem to handle &r correctly, it only
        // resets color but none of the other formatting, so just prepending &r is not sufficient.
        // This is why we always handle it at the component level.
        boolean needsReset = false;
        if (components.length == 0) return components;
        BaseComponent first = components[0];
        if (first instanceof TextComponent) {
            TextComponent text = (TextComponent)first;
            boolean isItalic = text.isItalic();
            if (!isItalic) {
                needsReset = true;
            }
        }
        if (needsReset) {
            BaseComponent[] reset = new ComponentBuilder("").italic(false).create();
            if (reset.length > 0) {
                reset[reset.length - 1].setExtra(Arrays.asList(components));
                components = reset;
            }
        }
        return components;
    }

    protected BaseComponent[] parseChatComponents(String containsJson) {
        List<BaseComponent> components = new ArrayList<>();
        List<BaseComponent> addToComponents = components;
        String[] pieces = ChatUtils.getComponents(containsJson);
        BaseComponent addToComponent = null;
        for (String component : pieces) {
            try {
                List<BaseComponent> addComponents;
                boolean isJson = component.startsWith("{");
                if (isJson) {
                    addComponents = Arrays.asList(ComponentSerializer.parse(component));
                } else {
                    addComponents = Arrays.asList(fromLegacyText(component));
                }
                if (!addComponents.isEmpty()) {
                    addToComponents.addAll(addComponents);
                    if (addToComponent != null) {
                        addToComponent.setExtra(addToComponents);
                    }

                    // If this is a legacy text string, append to it to keep the legacy
                    // behavior of formatting affecting everything after it.
                    // if this is a json block, formatting only affects what is in that block.
                    if (!isJson) {
                        addToComponent = addToComponents.get(addToComponents.size() - 1);
                        addToComponents = addToComponent.getExtra();
                        if (addToComponents == null) {
                            addToComponents = new ArrayList<>();
                        }
                    }
                }
            } catch (Exception ex) {
                platform.getLogger().log(Level.SEVERE, "Error parsing chat components from: " + component, ex);
            }
        }
        return components.toArray(new BaseComponent[0]);
    }

    /**
     * Straight up copying this method out of bungee-chat
     * There was a change in this commit:
     * https://github.com/SpigotMC/BungeeCord/commit/2a716bbc7f91887e31bbc37fb608581673893605#diff-84a270b9ce33820f05d3564ab384e29478185dd8302836fbdc17ec17cd04b949
     * That broke using legacy text with a font via this function by preventing the resulting component from caring
     * about the font (via the new reset flag)
     * I don't understand what this commit was trying to fix well enough to know if my use-case was exploiting a bug,
     * nor do I really have time nor care enough to try to convince someone at Spigot that it's broken, so I'm just
     * going to copy the "works for me" version of this method an dmove on.
     */
    @SuppressWarnings("checkstyle:parenpad")
    private static final Pattern url = Pattern.compile( "^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$" );

    @SuppressWarnings("checkstyle:parenpad")
    public static BaseComponent[] fromLegacyText(String message)
    {
        return fromLegacyText( message, ChatColor.WHITE );
    }

    @SuppressWarnings("checkstyle:parenpad")
    public static BaseComponent[] fromLegacyText(String message, ChatColor defaultColor)
    {
        ArrayList<BaseComponent> components = new ArrayList<BaseComponent>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher( message );

        for ( int i = 0; i < message.length(); i++ )
        {
            char c = message.charAt( i );
            if ( c == ChatColor.COLOR_CHAR )
            {
                if ( ++i >= message.length() )
                {
                    break;
                }
                c = message.charAt( i );
                if ( c >= 'A' && c <= 'Z' )
                {
                    c += 32;
                }
                ChatColor format;
                if ( c == 'x' && i + 12 < message.length() )
                {
                    StringBuilder hex = new StringBuilder( "#" );
                    for ( int j = 0; j < 6; j++ )
                    {
                        hex.append( message.charAt( i + 2 + ( j * 2 ) ) );
                    }
                    try
                    {
                        format = ChatColor.of( hex.toString() );
                    } catch ( IllegalArgumentException ex )
                    {
                        format = null;
                    }

                    i += 12;
                } else
                {
                    format = ChatColor.getByChar( c );
                }
                if ( format == null )
                {
                    continue;
                }
                if ( builder.length() > 0 )
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    components.add( old );
                }
                if ( format == ChatColor.BOLD )
                {
                    component.setBold( true );
                } else if ( format == ChatColor.ITALIC )
                {
                    component.setItalic( true );
                } else if ( format == ChatColor.UNDERLINE )
                {
                    component.setUnderlined( true );
                } else if ( format == ChatColor.STRIKETHROUGH )
                {
                    component.setStrikethrough( true );
                } else if ( format == ChatColor.MAGIC )
                {
                    component.setObfuscated( true );
                } else if ( format == ChatColor.RESET )
                {
                    format = defaultColor;
                    component = new TextComponent();
                    component.setColor( format );
                } else
                {
                    component = new TextComponent();
                    component.setColor( format );
                }
                continue;
            }
            int pos = message.indexOf( ' ', i );
            if ( pos == -1 )
            {
                pos = message.length();
            }
            if ( matcher.region( i, pos ).find() )
            { //Web link handling

                if ( builder.length() > 0 )
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    components.add( old );
                }

                TextComponent old = component;
                component = new TextComponent( old );
                String urlString = message.substring( i, pos );
                component.setText( urlString );
                component.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL,
                        urlString.startsWith( "http" ) ? urlString : "http://" + urlString ) );
                components.add( component );
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append( c );
        }

        component.setText( builder.toString() );
        components.add( component );

        return components.toArray( new BaseComponent[ 0 ] );
    }
}
