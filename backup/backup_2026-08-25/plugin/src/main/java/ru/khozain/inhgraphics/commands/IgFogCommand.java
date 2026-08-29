package ru.khozain.inhgraphics.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.khozain.inhgraphics.InhGraphicsPlugin;
import ru.khozain.inhgraphics.PlayerData;

import java.util.List;

/** /igfog on|off [ник]  |  /igfog density <1-10> [ник] */
public final class IgFogCommand implements CommandExecutor, TabCompleter {

    private static final List<String> VARIANTS = List.of("on", "off", "density");
    private final InhGraphicsPlugin plugin;

    public IgFogCommand(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendRichMessage("<yellow>Использование: /igfog <on|off|density 1-10> [ник]");
            return true;
        }
        boolean isDensity = "density".equalsIgnoreCase(args[0]);
        int nameIdx = isDensity ? 2 : 1;
        Player target = CmdHelper.resolveTarget(sender, args, nameIdx);
        if (target == null) return true;

        PlayerData d = plugin.getStore().get(target.getUniqueId());
        switch (args[0].toLowerCase()) {
            case "on" -> {
                d.fogOn = true;
                plugin.getStore().save(target.getUniqueId());
                sender.sendRichMessage("<green>" + CmdHelper.whoName(target, sender)
                        + " — туман накрыл. Частицы видишь только ты, окружающие дышат свободно.");
            }
            case "off" -> {
                d.fogOn = false;
                plugin.getStore().save(target.getUniqueId());
                sender.sendRichMessage("<gray>" + CmdHelper.whoName(target, sender)
                        + " — туман рассеялся.");
            }
            case "density" -> {
                if (args.length < 2) {
                    sender.sendRichMessage("<yellow>Укажи плотность: /igfog density <1-10> [ник]");
                    return true;
                }
                try {
                    int dens = Integer.parseInt(args[1]);
                    if (dens < 1 || dens > 10) throw new NumberFormatException();
                    d.fogDensity = dens;
                    d.fogOn = true;
                    plugin.getStore().save(target.getUniqueId());
                    sender.sendRichMessage("<green>" + CmdHelper.whoName(target, sender)
                            + " — плотность тумана <gold>" + dens + "</gold>/10."
                            + " Туман включён, если был выключен.");
                } catch (NumberFormatException e) {
                    sender.sendRichMessage("<red>Плотность — целое число от 1 до 10.");
                }
            }
            default -> sender.sendRichMessage("<red>Не поняла аргумент '" + args[0] + "'. on/off/density.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return VARIANTS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && "density".equalsIgnoreCase(args[0])) {
            return List.of("3", "5", "7", "10").stream()
                    .filter(s -> s.startsWith(args[1])).toList();
        }
        if ((args.length == 2 && !"density".equalsIgnoreCase(args[0]))
                || (args.length == 3 && "density".equalsIgnoreCase(args[0]))) {
            return null; // ники
        }
        return List.of();
    }
}
