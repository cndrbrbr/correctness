package io.github.cndrbrbr.correctness;

import io.github.cndrbrbr.correctness.command.CorrectnessCommand;
import io.github.cndrbrbr.correctness.filter.WordFilter;
import io.github.cndrbrbr.correctness.jail.JailManager;
import io.github.cndrbrbr.correctness.listener.ChatListener;
import io.github.cndrbrbr.correctness.punish.Punisher;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/** Chat-Kontrolle: Schimpfwoerter blocken, verwarnen, einsperren. Siehe README.md. */
public final class CorrectnessPlugin extends JavaPlugin {

    private volatile WordFilter filter;
    private JailManager jailManager;
    private Punisher punisher;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Neue Einstellungen aus Updates in eine bestehende config.yml uebernehmen.
        getConfig().options().copyDefaults(true);
        saveConfig();
        getDataFolder().mkdirs();
        if (!new File(getDataFolder(), "words.yml").exists()) {
            saveResource("words.yml", false);
        }
        loadWords();

        jailManager = new JailManager(this);
        punisher = new Punisher(this);
        getServer().getPluginManager().registerEvents(jailManager, this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        CorrectnessCommand command = new CorrectnessCommand(this);
        getCommand("correctness").setExecutor(command);
        getCommand("correctness").setTabCompleter(command);
        getLogger().info("Correctness aktiviert (" + filter.size() + " Wörter).");
    }

    /** config.yml und words.yml neu einlesen. */
    public void reload() {
        reloadConfig();
        loadWords();
        punisher.configure();
    }

    private void loadWords() {
        YamlConfiguration words = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "words.yml"));
        filter = new WordFilter(words.getStringList("woerter"), words.getStringList("ganze-woerter"),
                words.getStringList("ausnahmen"));
    }

    /** Wird auch aus dem asynchronen Chat-Thread gelesen, daher volatile und unveraenderlich. */
    public WordFilter getFilter() {
        return filter;
    }

    public JailManager getJailManager() {
        return jailManager;
    }

    public Punisher getPunisher() {
        return punisher;
    }
}
