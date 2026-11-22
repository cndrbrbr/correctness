package io.github.cndrbrbr.correctness.punish;

import io.github.cndrbrbr.correctness.CorrectnessPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/** Verstoss zaehlen, protokollieren und bestrafen (Verwarnung oder Gefaengnis). Nur im Main-Thread aufrufen. */
public final class Punisher {

    /** Woher der Text kam, fuer Meldungen und Protokoll. */
    public enum Source {
        CHAT("Chat"), BEFEHL("Befehl"), SCHILD("Schild"), BUCH("Buch"), AMBOSS("Amboss");

        private final String label;

        Source(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CorrectnessPlugin plugin;
    private final OffenseTracker tracker;
    private final File offensesFile;
    private final File logFile;

    public Punisher(CorrectnessPlugin plugin) {
        this.plugin = plugin;
        this.tracker = new OffenseTracker(List.of(0), 24, System::currentTimeMillis);
        this.offensesFile = new File(plugin.getDataFolder(), "offenses.yml");
        this.logFile = new File(plugin.getDataFolder(), "verstoesse.log");
        configure();
        load();
    }

    /** Strafstufen und Verfall aus der config.yml (neu) lesen. */
    public void configure() {
        List<Integer> steps = plugin.getConfig().getIntegerList("strafe.stufen");
        tracker.configure(steps, plugin.getConfig().getDouble("strafe.verfall-stunden", 24));
    }

    public OffenseTracker tracker() {
        return tracker;
    }

    public void punish(Player player, Source source, String word, String text) {
        int count = tracker.record(player.getUniqueId());
        int minutes = tracker.minutesFor(count);
        save();

        String reason = "Beleidigungen und Schimpfwörter sind hier verboten.";
        boolean jailedElsewhere = false;
        if (minutes <= 0) {
            int next = tracker.minutesFor(count + 1);
            player.sendMessage("§c§lVerwarnung! §r§c" + reason
                    + (next > 0 ? " Beim nächsten Mal geht es für " + next + " Min. ins Gefängnis." : ""));
        } else if (!plugin.getJailManager().jail(player, minutes, reason)) {
            // Sitzt schon in einem anderen Gefaengnis (z. B. BlockWatch): nur zaehlen, nicht doppelt einsperren.
            player.sendMessage("§c" + reason + " Der Verstoß wurde gezählt.");
            minutes = 0;
            jailedElsewhere = plugin.getJailManager().jailedElsewhere(player);
        }

        log(player, source, word, text, count, minutes, jailedElsewhere);

        String notice = "§6Correctness §7" + player.getName() + " (" + source.label() + ", " + count + ". Verstoß): §f"
                + word + (minutes > 0 ? " §7→ " + minutes + " Min. Gefängnis"
                        : jailedElsewhere ? " §7→ sitzt schon in einem anderen Gefängnis" : " §7→ Verwarnung");
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != player && online.hasPermission("correctness.admin")) {
                online.sendMessage(notice);
            }
        }
    }

    private void log(Player player, Source source, String word, String text, int count, int minutes,
            boolean jailedElsewhere) {
        String result = minutes > 0 ? minutes + " Min. Gefängnis"
                : jailedElsewhere ? "sitzt schon in anderem Gefängnis" : "Verwarnung";
        plugin.getLogger().info(player.getName() + " (" + source.label() + "): \"" + word + "\" → " + result);
        String line = ZonedDateTime.now().format(TIME) + " | " + player.getName() + " | " + source.label()
                + " | " + word + " | " + count + ". Verstoß | " + result + " | " + text.replace('\n', ' ')
                + System.lineSeparator();
        try {
            Files.writeString(logFile.toPath(), line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            plugin.getLogger().warning("verstoesse.log konnte nicht geschrieben werden: " + exception.getMessage());
        }
    }

    public void forgive(UUID player) {
        tracker.forgive(player);
        save();
    }

    private void load() {
        if (!offensesFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offensesFile);
        for (String key : yaml.getKeys(false)) {
            try {
                tracker.load(UUID.fromString(key), yaml.getLongList(key));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Ungültiger Eintrag in offenses.yml: " + key);
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        tracker.snapshot().forEach((id, times) -> yaml.set(id.toString(), times));
        try {
            yaml.save(offensesFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("offenses.yml konnte nicht gespeichert werden: " + exception.getMessage());
        }
    }
}
