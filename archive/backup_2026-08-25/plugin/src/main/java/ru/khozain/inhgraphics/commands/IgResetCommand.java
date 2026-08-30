package ru.khozain.inhgraphics.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.khozain.inhgraphics.Applier;
import ru.khozain.inhgraphics.InhGraphicsPlugin;
import ru.khozain.inhgraphics.PlayerData;

import java.util.ArrayList;
import java.util.List;

/** /igreset <ник|all> */
public final class IgResetCommand implements CommandExecutor, TabCompleter {

    private final InhGraphicsPlugin plugin;

    public IgResetCommand(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].isBlank()) {
            sender.sendRichMessage("<yellow>Использование: /igreset <ник|all>");
            return true;
        }
        if ("all".equalsIgnoreCase(args[0])) {
            int n = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData d = plugin.getStore().get(p.getUniqueId());
                boolean hadSomething = d.isDirty();
                plugin.getStore().reset(p.getUniqueId());
                Applier.applyAll(plugin, p, d);
                if (hadSomething) n++;
            }
            sender.sendRichMessage("<gray>Сброшены настройки у всех онлайн (" + n
                    + "). Мир вернулся к заводским.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendRichMessage("<red>Игрок " + args[0] + " не найден или оффлайн."
                    + " Оффлайн-игроку настройки сбросятся при следующем /igreset all.");
            return true;
        }
        plugin.getStore().reset(target.getUniqueId());
        Applier.applyAll(plugin, target, plugin.getStore().get(target.getUniqueId()));
        sender.sendRichMessage("<gray>" + target.getName()
                + ": время, погода, прорисовка и туман сброшены к мировым значениям.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && !args[0].isEmpty()) {
            String prefix = args[0].toLowerCase();
            List<String> out = new ArrayList<>();
            if ("all".startsWith(prefix)) out.add("all");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) out.add(p.getName());
            }
            return out;
        }
        return args.length == 1 ? List.of("all") : List.of();
    }
}
