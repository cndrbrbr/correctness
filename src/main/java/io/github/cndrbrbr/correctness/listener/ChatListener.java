package io.github.cndrbrbr.correctness.listener;

import io.github.cndrbrbr.correctness.CorrectnessPlugin;
import io.github.cndrbrbr.correctness.punish.Punisher.Source;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.Optional;

/** Prueft Chat, Chat-Befehle, Schilder, Buecher und Amboss-Namen und blockt Schimpfwoerter. */
public final class ChatListener implements Listener {

    /** Ergebnis-Slot im Amboss. */
    private static final int ANVIL_RESULT_SLOT = 2;

    private final CorrectnessPlugin plugin;

    public ChatListener(CorrectnessPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean enabled(String source) {
        return plugin.getConfig().getBoolean("pruefen." + source, true);
    }

    private boolean exempt(Player player) {
        return player.hasPermission("correctness.bypass");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!enabled("chat") || exempt(event.getPlayer())) {
            return;
        }
        Optional<String> hit = plugin.getFilter().find(event.getMessage());
        if (hit.isEmpty()) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = event.getMessage();
        // Chat kommt asynchron, bestraft (Teleport, Dateien) wird im Main-Thread.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                blocked(player, "Deine Nachricht wurde nicht gesendet.");
                plugin.getPunisher().punish(player, Source.CHAT, hit.get(), message);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!enabled("befehle") || exempt(event.getPlayer())) {
            return;
        }
        String[] parts = event.getMessage().substring(1).split("\\s+");
        if (parts.length < 2) {
            return;
        }
        String label = parts[0].toLowerCase(Locale.ROOT);
        label = label.substring(label.indexOf(':') + 1);
        ConfigurationSection commands = plugin.getConfig().getConfigurationSection("befehle");
        if (commands == null || !commands.contains(label)) {
            return;
        }
        int skip = 1 + Math.max(0, commands.getInt(label));
        if (parts.length <= skip) {
            return;
        }
        String text = String.join(" ", java.util.Arrays.copyOfRange(parts, skip, parts.length));
        Optional<String> hit = plugin.getFilter().find(text);
        if (hit.isPresent()) {
            event.setCancelled(true);
            blocked(event.getPlayer(), "Deine Nachricht wurde nicht gesendet.");
            plugin.getPunisher().punish(event.getPlayer(), Source.BEFEHL, hit.get(), event.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSign(SignChangeEvent event) {
        if (!enabled("schilder") || exempt(event.getPlayer())) {
            return;
        }
        String text = String.join(" ", event.getLines());
        Optional<String> hit = plugin.getFilter().find(text);
        if (hit.isPresent()) {
            event.setCancelled(true);
            blocked(event.getPlayer(), "Dein Schild wurde nicht beschriftet.");
            plugin.getPunisher().punish(event.getPlayer(), Source.SCHILD, hit.get(), text);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBook(PlayerEditBookEvent event) {
        if (!enabled("buecher") || exempt(event.getPlayer())) {
            return;
        }
        BookMeta meta = event.getNewBookMeta();
        StringBuilder text = new StringBuilder();
        if (meta.hasTitle()) {
            text.append(meta.getTitle()).append(' ');
        }
        for (String page : meta.getPages()) {
            text.append(page).append(' ');
        }
        Optional<String> hit = plugin.getFilter().find(text.toString());
        if (hit.isPresent()) {
            event.setCancelled(true);
            blocked(event.getPlayer(), "Dein Buch wurde nicht gespeichert.");
            plugin.getPunisher().punish(event.getPlayer(), Source.BUCH, hit.get(), text.toString().trim());
        }
    }

    /** Erst beim Herausnehmen pruefen, damit Tippen im Amboss nicht schon bestraft wird. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnvil(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory) || event.getRawSlot() != ANVIL_RESULT_SLOT
                || !(event.getWhoClicked() instanceof Player player) || !enabled("amboss") || exempt(player)) {
            return;
        }
        ItemStack result = event.getCurrentItem();
        if (result == null || !result.hasItemMeta()) {
            return;
        }
        ItemMeta meta = result.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }
        Optional<String> hit = plugin.getFilter().find(meta.getDisplayName());
        if (hit.isPresent()) {
            event.setCancelled(true);
            String name = meta.getDisplayName();
            // Inventar schliessen und teleportieren erst nach dem Klick-Event (Bukkit-Vorgabe).
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                if (player.isOnline()) {
                    blocked(player, "Dieser Name ist nicht erlaubt.");
                    plugin.getPunisher().punish(player, Source.AMBOSS, hit.get(), name);
                }
            });
        }
    }

    private static void blocked(Player player, String message) {
        player.sendMessage("§c" + message);
    }
}
