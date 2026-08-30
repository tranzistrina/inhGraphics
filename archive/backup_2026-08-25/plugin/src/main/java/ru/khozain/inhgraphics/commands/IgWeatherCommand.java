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

/** /igweather clear|rain|thunder|reset [ник] */
public final class IgWeatherCommand implements CommandExecutor, TabCompleter {

    private static final List<String> VARIANTS = List.of("clear", "rain", "thunder", "reset");
    private final InhGraphicsPlugin plugin;

    public IgWeatherCommand(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendRichMessage("<yellow>Использование: /igweather <clear|rain|thunder|reset> [ник]");
            return true;
        }
        Player target = CmdHelper.resolveTarget(sender, args, 1);
        if (target == null) return true;

        PlayerData d = plugin.getStore().get(target.getUniqueId());
        switch (args[0].toLowerCase()) {
            case "clear" -> d.weather = "CLEAR";
            case "rain" -> d.weather = "RAIN";
            case "thunder" -> d.weather = "THUNDER";
            case "reset" -> d.weather = "NONE";
            default -> {
                sender.sendRichMessage("<red>Не поняла аргумент '" + args[0] + "'. clear/rain/thunder/reset.");
                return true;
            }
        }
        Applier.applyWeather(target, d);
        plugin.getStore().save(target.getUniqueId());

        String who = CmdHelper.whoName(target, sender);
        switch (d.weather) {
            case "CLEAR" -> sender.sendRichMessage("<green>" + who + " — персональная ясность. Ни капли, ни облачка.");
            case "RAIN" -> sender.sendRichMessage("<green>" + who + " — персональный дождик. Зонт в инвентаре не предусмотрен.");
            case "THUNDER" -> sender.sendRichMessage("<green>" + who + " — персональная гроза! Небо потемнело"
                    + (plugin.thunderSoundEnabled()
                       ? ", раскатами грома обещали."
                       : ". Звук грома отключён в конфиге (thunder-sound: false), тишина грозная."));
            default -> sender.sendRichMessage("<gray>" + who + " — погода снова мировая. Пусть сама решает.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return VARIANTS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return null;
        return List.of();
    }
}
