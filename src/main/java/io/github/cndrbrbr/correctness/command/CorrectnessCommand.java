package io.github.cndrbrbr.correctness.command;

import io.github.cndrbrbr.correctness.CorrectnessPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** /correctness jail|frei|setjail|test|verstoesse|vergeben|reload */
public final class CorrectnessCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("jail", "frei", "setjail", "test", "verstoesse",
            "vergeben", "reload");
    private static final List<String> PLAYER_ARG = List.of("jail", "frei", "verstoesse", "vergeben");

    private final CorrectnessPlugin plugin;

    public CorrectnessCommand(CorrectnessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "jail" -> handleJail(sender, rest);
            case "frei" -> handleRelease(sender, rest);
            case "setjail" -> handleSetJail(sender);
            case "test" -> handleTest(sender, rest);
            case "verstoesse", "verstöße" -> handleOffenses(sender, rest);
            case "vergeben" -> handleForgive(sender, rest);
            case "reload" -> {
                plugin.reload();
                sender.sendMessage("§aCorrectness neu geladen (" + plugin.getFilter().size() + " Wörter).");
            }
            default -> help(sender);
        }
        return true;
    }

    private static void help(CommandSender sender) {
        sender.sendMessage("§6Correctness §7- Chat-Kontrolle");
        sender.sendMessage("§e/correctness jail §7<spieler> [minuten] §f/ §efrei §7<spieler> §f- Gefängnis");
        sender.sendMessage("§e/correctness setjail §f- Gefängniszelle an deine Position setzen");
        sender.sendMessage("§e/correctness test §7<text> §f- prüfen, ob ein Text gefiltert würde");
        sender.sendMessage("§e/correctness verstoesse §7<spieler> §f/ §evergeben §7<spieler> §f- Verstöße anzeigen/löschen");
        sender.sendMessage("§e/correctness reload §f- config.yml und words.yml neu laden");
    }

    private void handleJail(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§eBenutzung: /correctness jail <spieler> [minuten]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cSpieler ist nicht online: " + args[0]);
            return;
        }
        int minutes = plugin.getConfig().getInt("jail-minuten", 5);
        if (args.length > 1) {
            try {
                minutes = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage("§cUngültige Minutenzahl: " + args[1]);
                return;
            }
        }
        if (minutes <= 0) {
            sender.sendMessage("§cMinuten müssen größer als 0 sein.");
            return;
        }
        plugin.getJailManager().jail(target, minutes, "Eingesperrt von " + sender.getName() + ".");
        sender.sendMessage("§a" + target.getName() + " ist für " + minutes + " Minuten im Gefängnis.");
    }

    private void handleRelease(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§eBenutzung: /correctness frei <spieler>");
            return;
        }
        Player online = Bukkit.getPlayerExact(args[0]);
        Optional<UUID> id = online != null ? Optional.of(online.getUniqueId())
                : plugin.getJailManager().findByName(args[0]);
        if (id.isPresent() && plugin.getJailManager().release(id.get())) {
            sender.sendMessage("§a" + args[0] + " ist frei" + (online == null ? " (beim nächsten Login)." : "."));
        } else {
            sender.sendMessage("§c" + args[0] + " ist nicht im Gefängnis.");
        }
    }

    private void handleSetJail(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur im Spiel.");
            return;
        }
        plugin.getJailManager().setCell(player.getLocation());
        sender.sendMessage("§aGefängniszelle ist jetzt hier. Achte darauf, dass man hier nicht herauskommt.");
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eBenutzung: /correctness test <text>");
            return;
        }
        String text = String.join(" ", args);
        Optional<String> hit = plugin.getFilter().find(text);
        sender.sendMessage(hit.map(word -> "§cGefiltert: §f" + word).orElse("§aErlaubt."));
    }

    @SuppressWarnings("deprecation") // Name → OfflinePlayer fuer Spieler, die gerade nicht online sind
    private Optional<OfflinePlayer> player(CommandSender sender, String[] args, String usage) {
        if (args.length < 1) {
            sender.sendMessage("§eBenutzung: /correctness " + usage + " <spieler>");
            return Optional.empty();
        }
        Player online = Bukkit.getPlayerExact(args[0]);
        OfflinePlayer target = online != null ? online : Bukkit.getOfflinePlayer(args[0]);
        if (online == null && !target.hasPlayedBefore()) {
            sender.sendMessage("§cUnbekannter Spieler: " + args[0]);
            return Optional.empty();
        }
        return Optional.of(target);
    }

    private void handleOffenses(CommandSender sender, String[] args) {
        player(sender, args, "verstoesse").ifPresent(target -> {
            int count = plugin.getPunisher().tracker().count(target.getUniqueId());
            String jailed = plugin.getJailManager().isJailed(target.getUniqueId()) ? " §c(im Gefängnis)" : "";
            sender.sendMessage("§e" + target.getName() + "§7: " + count + " zählende Verstöße" + jailed
                    + "§7, nächster: " + describe(plugin.getPunisher().tracker().minutesFor(count + 1)));
        });
    }

    private void handleForgive(CommandSender sender, String[] args) {
        player(sender, args, "vergeben").ifPresent(target -> {
            plugin.getPunisher().forgive(target.getUniqueId());
            sender.sendMessage("§aVerstöße von " + target.getName() + " gelöscht.");
        });
    }

    private static String describe(int minutes) {
        return minutes > 0 ? minutes + " Min. Gefängnis" : "Verwarnung";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }
        if (args.length == 2 && PLAYER_ARG.contains(args[0].toLowerCase(Locale.ROOT))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
        }
        return List.of();
    }
}
