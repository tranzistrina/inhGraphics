package ru.khozain.inhgraphics.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Общие помощники для команд: поиск целевого игрока, сообщения.
 */
final class CmdHelper {

    private CmdHelper() {
    }

    /**
     * Определяет цель по аргументу [ник]; без аргумента — сам отправитель.
     *
     * @param nameIdx индекс аргумента с ником
     * @return игрок или null (ошибка уже отправлена)
     */
    static Player resolveTarget(CommandSender sender, String[] args, int nameIdx) {
        if (args.length <= nameIdx || args[nameIdx].isBlank()) {
            if (sender instanceof Player p) return p;
            sender.sendRichMessage("<red>Из консоли указывай ник: /<команда> <аргумент> <ник>");
            return null;
        }
        Player target = Bukkit.getPlayerExact(args[nameIdx]);
        if (target == null) {
            sender.sendRichMessage("<red>Игрок " + args[nameIdx] + " не найден или оффлайн.");
        }
        return target;
    }

    static String who(Player target, CommandSender sender) {
        return target.equals(sender) ? "тебе" : target.getName();
    }

    static String whoName(Player target, CommandSender sender) {
        return target.equals(sender) ? "Тебе" : target.getName();
    }
}
