package ru.khozain.inhgraphics.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.khozain.inhgraphics.Applier;
import ru.khozain.inhgraphics.InhGraphicsPlugin;
import ru.khozain.inhgraphics.PlayerData;

import java.util.List;

/** /igrender <2-32|reset> [ник] */
public final class IgRenderCommand implements CommandExecutor, TabCompleter {

    private final InhGraphicsPlugin plugin;

    public IgRenderCommand(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendRichMessage("<yellow>Использование: /igrender <2-32|reset> [ник]");
            return true;
        }
        Player target = CmdHelper.resolveTarget(sender, args, 1);
        if (target == null) return true;

        PlayerData d = plugin.getStore().get(target.getUniqueId());
        if ("reset".equalsIgnoreCase(args[0])) {
            d.renderDistance = -1;
            Applier.applyRender(plugin, target, d);
            plugin.getStore().save(target.getUniqueId());
            sender.sendRichMessage("<gray>" + CmdHelper.whoName(target, sender)
                    + " — прорисовка снова мировая.");
            return true;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendRichMessage("<red>'" + args[0] + "' — это не число. Радиус 2-32 или reset.");
            return true;
        }
        if (radius < 2 || radius > 32) {
            sender.sendRichMessage("<red>Радиус от 2 до 32 чанков. Меньше — сервер заплачет, больше — клиент.");
            return true;
        }
        d.renderDistance = radius;
        Applier.applyRender(plugin, target, d);
        plugin.getStore().save(target.getUniqueId());
        sender.sendRichMessage("<green>" + CmdHelper.whoName(target, sender)
                + " — сервер шлёт только <gold>" + radius
                + "</gold> чанков вокруг. За границей будет честный обрыв мира: так и задумано.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("2", "4", "8", "16", "32", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return null;
        return List.of();
    }
}
