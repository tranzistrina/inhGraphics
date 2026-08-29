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

/** /igtime day|night|noon|midnight|<тики>|reset [ник] */
public final class IgTimeCommand implements CommandExecutor, TabCompleter {

    private static final List<String> PRESETS = List.of("day", "noon", "night", "midnight", "reset");
    private final InhGraphicsPlugin plugin;

    public IgTimeCommand(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendRichMessage("<yellow>Использование: /igtime <day|night|noon|midnight|тики|reset> [ник]");
            return true;
        }
        Player target = CmdHelper.resolveTarget(sender, args, 1);
        if (target == null) return true;

        PlayerData d = plugin.getStore().get(target.getUniqueId());
        long targetTicks;
        switch (args[0].toLowerCase()) {
            case "day" -> targetTicks = 1000L;      // рассвет
            case "noon" -> targetTicks = 6000L;     // полдень
            case "night" -> targetTicks = 13000L;   // закат/ночь
            case "midnight" -> targetTicks = 18000L;
            case "reset" -> targetTicks = -1L;
            default -> {
                try {
                    long ticks = Long.parseLong(args[0]);
                    if (ticks < 0 || ticks > 23999) {
                        sender.sendRichMessage("<red>Тики от 0 до 23999. В сутках их 24000, больше не запасено.");
                        return true;
                    }
                    targetTicks = ticks;
                } catch (NumberFormatException e) {
                    sender.sendRichMessage("<red>Не поняла аргумент '" + args[0] + "'. day/night/noon/midnight/reset или число тиков.");
                    return true;
                }
            }
        }
        if (targetTicks < 0) {
            d.timeTicks = -1L;
            Applier.applyTime(target, d);
            plugin.getStore().save(target.getUniqueId());
            sender.sendRichMessage("<gray>" + CmdHelper.whoName(target, sender)
                    + " снова живёт по мировому времени. Скучновато, зато честно.");
            return true;
        }
        // время ставится «прямо сейчас» и дальше течёт вместе с мировым
        Applier.applyTimeAt(plugin, target, d, targetTicks);
        sender.sendRichMessage("<green>" + CmdHelper.whoName(target, sender)
                + " — персональное время: <gold>" + targetTicks
                + "</gold> тик(ов), дальше идёт синхронно с миром.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return PRESETS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            return null; // ники онлайн по умолчанию
        }
        return List.of();
    }
}
