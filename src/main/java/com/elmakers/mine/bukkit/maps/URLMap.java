package com.elmakers.mine.bukkit.maps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class URLMap extends MapRenderer implements com.elmakers.mine.bukkit.api.maps.URLMap {

    // Private and Protected Members
    private final MapController controller;
    private List<BufferedImage> frames = null;
    private List<Long> frameTimes = null;
    private int frame = 0;
    private boolean animated = false;
    private long lastFrameChange = 0;

    protected String world;
    protected Short id;

    protected String url;
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
    protected Set<String> sentToPlayers = new HashSet<String>();
    protected Integer priority;

    protected URLMap(MapController controller, String world, short mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
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
    }

    // Render method override
    @Override
    public void render(MapView mapView, MapCanvas canvas, Player player) {
        if (animated && frameTimes != null)
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
            if (priority != null) {
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

    public boolean matches(String keyword) {
        if (keyword == null || keyword.length() == 0) return true;

        String lowerUrl = url == null ? "" : url.toLowerCase();
        String lowerName = name == null ? "" : name.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        return lowerUrl.contains(lowerKeyword) || lowerName.contains(lowerKeyword);
    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return url;
    }

    public short getId() {
        return id;
    }

    @SuppressWarnings("deprecation")
    protected MapView getMapView() {
        if (!enabled) {
            return null;
        }
        MapView mapView = Bukkit.getMap(id);
        if (mapView == null) {
            controller.remove(getKey());
            enabled = false;
            controller.warning("Failed to get map id " + id + " for key " + getKey() + ", disabled, re-enable in config and fix id");
            controller.save();
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

    protected boolean isEnabled() {
        return enabled;
    }

    protected String getKey() {
        return getKey(world, url, x, y, width, height);
    }

    protected static String getKey(String world, String url, int x, int y, int width, int height) {
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
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        try {
            if (animated) {
                ImageReader reader = ImageIO.getImageReadersBySuffix("GIF").next();
                reader.setInput(in);
                frameTimes = new ArrayList<Long>();
                lastFrameChange = System.currentTimeMillis();
                for (int i = 0, count = reader.getNumImages(true); i < count; i++){
                    BufferedImage image = reader.read(i);
                    images.add(image);

                    IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                    IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
                    frameTimes.add(10 * Long.valueOf(gce.getAttribute("delayTime")));
                }
                reader.dispose();
            } else {
                images.add(ImageIO.read(in));
            }
        } catch (Exception ex) {
           ex.printStackTrace();
        }

        return images;
    }

    protected BufferedImage getImage() {
        if (loading || !enabled) {
            return null;
        }
        if (frames == null) {
            loading = true;
            frames = new ArrayList<BufferedImage>();
            final Plugin plugin = controller.getPlugin();
            if (plugin == null) return null;
            final File cacheFolder = controller.getCacheFolder();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                public void run() {
                    try {
                        animated = url.endsWith(".gif");
                        Collection<BufferedImage> images = null;
                        if (!url.startsWith("http"))
                        {
                            File baseFolder = plugin.getDataFolder().getParentFile().getParentFile();
                            File fileName = new File(baseFolder, url);
                            controller.info("Loading map file: " + fileName.getName());
                            images = loadImages(ImageIO.createImageInputStream(fileName));
                        }
                        else
                        {
                            @SuppressWarnings("deprecation")
                            String cacheFileName = URLEncoder.encode(url);
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
                                    InputStream in = conn.getInputStream();
                                    OutputStream out = new FileOutputStream(cacheFile);
                                    byte[] buffer = new byte[10 * 1024];
                                    int len;
                                    while ((len = in.read(buffer)) != -1) {
                                        out.write(buffer, 0, len);
                                    }
                                    out.close();
                                    in.close();
                                    images = loadImages(ImageIO.createImageInputStream(cacheFile));
                                }
                            } else {
                                controller.info("Loading " + url);
                                URL imageUrl = new URL(url);
                                images = loadImages(ImageIO.createImageInputStream(imageUrl));
                            }
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
                            BufferedImage image = new BufferedImage(128, 128, animated ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
                            Graphics2D graphics = image.createGraphics();

                            // This is hack, not sure how to handled animated gifs with transparency!
                            if (animated) {
                                graphics.setBackground(new Color(0, 0, 0, 0));
                                graphics.clearRect(0, 0, 128, 128);
                            }

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
                        ex.printStackTrace();
                    }
                }
            });
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
