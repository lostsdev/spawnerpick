package org.lostslvtt.darateria;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class GivePickCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0 || strings.length == 1) {
            final Component msg = MiniMessage.miniMessage().deserialize("<b><gradient:#fa0606:#ffffff>Использование: /givepickaxe <Player> <Chance></gradient></b>");
            commandSender.sendMessage(msg);
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(strings[0]);
        if (targetPlayer == null) {
            commandSender.sendMessage(ChatColor.RED + "Игрок " + strings[0] + " не найден.");
            return true;
        }

        NamespacedKey key = NamespacedKey.minecraft("key");
        NamespacedKey chanceKey = NamespacedKey.minecraft("chance");
        ItemStack item = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "spawnerpick");
        meta.getPersistentDataContainer().set(chanceKey, PersistentDataType.INTEGER, Integer.parseInt(strings[1]));
        item.setItemMeta(meta);

        targetPlayer.getInventory().addItem(item);
        final Component msg = MiniMessage.miniMessage().deserialize("<b><gradient:#fa0606:#ffffff>Кирка для ломания спавнеров выдана игроку "+targetPlayer.getName()+".</gradient></b>");
        commandSender.sendMessage(msg);
        return true;
    }
}
