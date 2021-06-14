package com.elmakers.mine.bukkit.maps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.ProfileResponse;

public class URLMap extends MapRenderer implements com.elmakers.mine.bukkit.api.maps.URLMap {

    // Private and Protected Members
    private final MapController controller;
    private List<BufferedImage> frames = null;
    private List<Long> frameTimes = null;
    private int frame = 0;
    private boolean animated = false;
    private long lastFrameChange = 0;

    protected String world;
    protected Integer id;

    protected String url;
    protected String playerName;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Integer xOverlay;
    protected Integer yOverlay;
    protected String name;
    protected boolean enabled = true;
    protected boolean rendered = false;
    protected volatile boolean loading = false;
    protected Set<String> sentToPlayers = new HashSet<>();
    protected Integer priority;

    private class GetImageTask implements Runnable {
        @Override
        public void run() {
            try {
                final Plugin plugin = controller.getPlugin();
                final File cacheFolder = controller.getCacheFolder();
                animated = url.endsWith(".gif");
                Collection<BufferedImage> images = null;
                if (!url.startsWith("http"))
                {
                    File fileName;
                    if (!url.startsWith("/")) {
                        File baseFolder = plugin.getDataFolder().getParentFile().getParentFile();
                        fileName = new File(baseFolder, url);
                    } else {
                        fileName = new File(url);
                    }
                    controller.info("Loading map file: " + fileName.getName());
                    images = loadImages(ImageIO.createImageInputStream(fileName));
                }
                else
                {
                    String cacheFileName = URLEncoder.encode(url, "UTF-8");
                    File cacheFile = cacheFolder != null ? new File(cacheFolder, cacheFileName) : null;
                    if (cacheFile != null) {
                        if (cacheFile.exists()) {
                            controller.info("Loading from cache: " + cacheFile.getName());
                            images = loadImages(ImageIO.createImageInputStream(cacheFile));
                        } else {
                            controller.info("Loading " + url);
                            URL imageUrl = new URL(url);
                            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
                            conn.setConnectTimeout(30000);
                            conn.setReadTimeout(30000);
                            conn.setInstanceFollowRedirects(true);
                            try (InputStream in = conn.getInputStream();
                                    OutputStream out = new FileOutputStream(cacheFile)) {
                                byte[] buffer = new byte[10 * 1024];
                                int len;

                                while ((len = in.read(buffer)) != -1) {
                                    out.write(buffer, 0, len);
                                }
                            }

                            images = loadImages(ImageIO.createImageInputStream(cacheFile));
                        }
                    } else {
                        controller.info("Loading " + url);
                        URL imageUrl = new URL(url);
                        images = loadImages(ImageIO.createImageInputStream(imageUrl));
                    }
                }

                if (images.size() == 0)
                {
                    enabled = false;
                    controller.warning("Failed to load map " + url);
                }
                for (BufferedImage rawImage : images)
                {
                    int imageWidth = width <= 0 ? rawImage.getWidth() + width : width;
                    int imageHeight = height <= 0 ? rawImage.getHeight() + height : height;
                    if (imageWidth > rawImage.getWidth()) {
                        imageWidth = rawImage.getWidth();
                    }
                    if (imageHeight > rawImage.getHeight()) {
                        imageHeight = rawImage.getHeight();
                    }
                    int imageX = x + rawImage.getMinX();
                    int imageY = y + rawImage.getMinY();

                    BufferedImage croppedImage = rawImage.getSubimage(imageX, imageY, imageWidth, imageHeight);
                    BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = image.createGraphics();

                    AffineTransform transform = AffineTransform.getScaleInstance((float)128 / imageWidth, (float)128 / imageHeight);
                    graphics.drawRenderedImage(croppedImage, transform);

                    if (xOverlay != null && yOverlay != null) {
                        BufferedImage croppedOverlay = rawImage.getSubimage(xOverlay, yOverlay, imageWidth, imageHeight);
                        graphics.drawRenderedImage(croppedOverlay, transform);
                    }

                    frames.add(image);
                }
                loading = false;
            } catch (Exception ex) {
                controller.warning("Failed to load map " + url + ": " + ex.getMessage());
            }
        }
    }

    protected URLMap(MapController controller, String world, int mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority, String playerName) {
        this.controller = controller;
        this.world = world;
        this.url = url;
        this.name = name;
        this.x = x;
        this.y = y;
        this.xOverlay = xOverlay;
        this.yOverlay = yOverlay;
        this.width = width;
        this.height = height;
        this.id = mapId;
        this.priority = priority;
        this.playerName = playerName;
    }

    // Render method override
    @Override
    public void render(MapView mapView, MapCanvas canvas, Player player) {
        if (animated && frameTimes != null && frameTimes.size() > 0 && controller.isAnimationAllowed())
        {
            long now = System.currentTimeMillis();
            long delay = frameTimes.get(frame);
            if (now > lastFrameChange + delay)
            {
                frame = (frame + 1) % frameTimes.size();
                sentToPlayers.clear();
                rendered = false;
                lastFrameChange = now;
            }
        }

        if (rendered) {
            if (priority != null && player != null) {
                sendToPlayer(player, mapView);
            }
            return;
        }

        BufferedImage image = getImage();
        if (image != null) {
            canvas.drawImage(0, 0, image);
            rendered = true;
        }
    }

    @Override
    public void initialize(MapView mapView) {
        // This is here mainly as a hack to be able to force render to canvas.
        rendered = false;
    }

    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.length() == 0) return true;

        String lowerUrl = url == null ? "" : url.toLowerCase();
        String lowerName = name == null ? "" : name.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        return lowerUrl.contains(lowerKeyword) || lowerName.contains(lowerKeyword);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean fix(World world, int maxIds) {
        if (enabled) return true;

        MapView mapView = CompatibilityLib.getDeprecatedUtils().getMap(id);
        if (mapView != null) {
            enabled = true;
            return true;
        }
        int retry = 0;
        boolean matched = false;
        while (!matched && retry < maxIds) {
            MapView newView = Bukkit.createMap(world);
            short newId = CompatibilityLib.getDeprecatedUtils().getMapId(newView);
            matched = newId == id;
            if (newId < 0 || newId > id) break;
            retry++;
        }

        mapView = getMapView();
        if (mapView == null) {
            controller.warning("Failed to fix map id " + id + " for key " + getKey());
        } else {
            enabled = true;
        }

        return enabled;
    }

    @Nullable
    protected MapView getMapView() {
        if (!enabled) {
            return null;
        }
        MapView mapView = CompatibilityLib.getDeprecatedUtils().getMap(id);
        if (mapView == null) {
            enabled = false;
            controller.warning("Failed to get map id " + id + " for key " + getKey() + ", disabled, use 'mmap fix' to re-enable");
            return null;
        }
        List<MapRenderer> renderers = mapView.getRenderers();
        boolean needsRenderer = false;
        for (MapRenderer renderer : renderers) {
            if (!(renderer instanceof URLMap)) {
                mapView.removeRenderer(renderer);
                needsRenderer = true;
            }
        }
        if (needsRenderer) {
            mapView.addRenderer(this);
        }
        return mapView;
    }

    protected void disable() {
        enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    protected String getKey() {
        return getKey(world, url, playerName, x, y, width, height);
    }

    protected static String getKey(String world, String url, String playerName, int x, int y, int width, int height) {
        if (url == null) {
            url = playerName;
        }
        return world + "|" + x + "," + y + "|" + width + "," + height + "|" + url;
    }

    protected void resendTo(String playerName) {
        sentToPlayers.remove(playerName);
    }

    protected void reload() {
        sentToPlayers.clear();
        rendered = false;
        loading = false;
        frames = null;
    }

    protected Collection<BufferedImage> loadImages(ImageInputStream in)
    {
        List<BufferedImage> images = new ArrayList<>();
        try {
            if (animated) {
                ImageReader reader = ImageIO.getImageReadersBySuffix("GIF").next();
                reader.setInput(in);
                frameTimes = new ArrayList<>();
                lastFrameChange = System.currentTimeMillis();
                loadGIFImages(reader, images);
                reader.dispose();
            } else {
                BufferedImage frame = ImageIO.read(in);
                if (frame != null)
                {
                    images.add(frame);
                }
            }
        } catch (Exception ex) {
           ex.printStackTrace();
        }

        return images;
    }

    private void loadGIFImages(ImageReader reader, Collection<BufferedImage> images) throws IOException {
        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();
        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
            NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }
        }

        BufferedImage master = null;
        Graphics2D masterGraphics = null;
        List<String> frameDisposals = new ArrayList<>();

        for (int frameIndex = 0;; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }
            if (image == null) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            int delay = Integer.parseInt(gce.getAttribute("delayTime"));
            String disposal = gce.getAttribute("disposalMethod");

            int x = 0;
            int y = 0;

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else {
                NodeList children = root.getChildNodes();
                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }
            }
            masterGraphics.drawImage(image, x, y, null);

            BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
            images.add(copy);
            frameTimes.add((long)10 * delay);
            frameDisposals.add(disposal);

            if (disposal.equals("restoreToPrevious")) {
                BufferedImage from = null;
                for (int i = frameIndex - 1; i >= 0; i--) {
                    if (!frameDisposals.get(i).equals("restoreToPrevious") || frameIndex == 0) {
                        from = frames.get(i);
                        break;
                    }
                }

                if (from == null) break;
                master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else if (disposal.equals("restoreToBackgroundColor")) {
                masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
            }
        }
    }

    @Nullable
    protected BufferedImage getImage() {
        if (loading || !enabled) {
            return null;
        }
        if (url == null) {
            if (playerName != null) {
                loading = true;
                CompatibilityLib.getSkinUtils().fetchProfile(playerName, new ProfileCallback() {
                    @Override
                    public void result(ProfileResponse response) {
                        url = response == null ? null : response.getSkinURL();
                        if (url == null) {
                            enabled = false;
                        }
                        controller.save();
                        loading = false;
                    }
                });
            }
            return null;
        }
        if (frames == null) {
            loading = true;
            frames = new ArrayList<>();
            final Plugin plugin = controller.getPlugin();
            if (plugin == null) return null;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new GetImageTask());
            return null;
        }
        return frames.get(frame);
    }

    protected void reset() {
        frames = null;
        rendered = false;
        loading = false;
        sentToPlayers.clear();
    }

    protected void sendToPlayer(Player player, MapView mapView) {
        // Safety check
        if (priority == null || !enabled) {
            return;
        }

        String playerName = player.getName();

        // Randomly stagger sending to avoid a big hit on login
        if (!sentToPlayers.contains(playerName) && (Math.random() * priority) <= 1) {
            sentToPlayers.add(playerName);
            player.sendMap(mapView);
        }
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
