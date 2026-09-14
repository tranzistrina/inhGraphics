package ru.khozain.inhgraphics.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.khozain.inhgraphics.ClientModRegistry;
import ru.khozain.inhgraphics.InhGraphicsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** /igmods [игрок] — серверные плагины и добровольный отчёт клиентского мода. */
public final class IgModsCommand implements CommandExecutor, TabCompleter {
    private final InhGraphicsPlugin plugin;

    public IgModsCommand(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendRichMessage("<yellow>Из консоли укажи игрока: /igmods <ник>");
                return true;
            }
            target = player;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendRichMessage("<red>Игрок " + args[0] + " не найден или оффлайн.");
                return true;
            }
        }

        List<String> plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .sorted(Comparator.comparing(Plugin::getName, String.CASE_INSENSITIVE_ORDER))
                .map(p -> p.getName() + " " + p.getDescription().getVersion()
                        + (p.isEnabled() ? " <green>ON" : " <red>OFF"))
                .toList();
        sender.sendRichMessage("<aqua>Плагины сервера (" + plugins.size() + "): <gray>"
                + String.join(", ", plugins));
        sender.sendRichMessage("<aqua>Бренд клиента: <gray>"
                + (target.getClientBrandName() == null ? "не определён" : target.getClientBrandName()));

        ClientModRegistry.ClientReport report = plugin.clientMods().get(target);
        if (report == null) {
            sender.sendRichMessage("<yellow>Список клиентских модов не получен. "
                    + "Нужен необязательный inhClientPATHgraphics; клиентские моды нельзя надёжно перечислить с Paper.");
        } else {
            sender.sendRichMessage("<aqua>Моды клиента " + target.getName() + " ("
                    + report.mods().size() + "): <gray>" + report.summary());
            sender.sendRichMessage("<gray>Отчёт добровольный и не является доказательством для античита.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
