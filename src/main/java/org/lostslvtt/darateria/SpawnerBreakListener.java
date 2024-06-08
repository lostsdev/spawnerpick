package org.lostslvtt.darateria;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Random;

public class SpawnerBreakListener implements Listener {

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.SPAWNER || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(NamespacedKey.minecraft("key"), PersistentDataType.STRING)) {
            return;
        }

        String entityTypeString = meta.getPersistentDataContainer().get(NamespacedKey.minecraft("key"), PersistentDataType.STRING);
        EntityType entityType = EntityType.valueOf(entityTypeString);

        CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
        spawner.setSpawnedType(entityType);
        spawner.update();
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Random rand = new Random();
        int chance = rand.nextInt(4);
        if (block.getType() != Material.SPAWNER) {
            handleInvalidBreak(event);
            return;
        }

        Player player = event.getPlayer();
        if (!canBreakBlock(player, block)) {
            player.sendMessage(ChatColor.RED + "Вы не можете сломать этот спавнер!");
            event.setCancelled(true);
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isSpawnerPickaxe(item)) {
            handleInvalidBreak(event);
            return;
        }
        if (item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("chance"), PersistentDataType.INTEGER) >= chance)
        {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
            ItemMeta spawnerMeta = spawnerItem.getItemMeta();
            spawnerMeta.getPersistentDataContainer().set(NamespacedKey.minecraft("key"), PersistentDataType.STRING, spawner.getSpawnedType().name());
            spawnerItem.setItemMeta(spawnerMeta);

            block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
            item.setAmount(item.getAmount() - 1);
        } else
        {
            final Component msg = MiniMessage.miniMessage().deserialize("<red>Повезет в следующий раз :(</red>");
            player.sendMessage(msg);
            item.setAmount(0);
        }

    }

    private void handleInvalidBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (isSpawnerPickaxe(item)) {
            player.sendMessage(ChatColor.RED + "<red>Этой киркой можно ломать только спавнера!</red>");
            event.setCancelled(true);
        }
    }

    private boolean isSpawnerPickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.WOODEN_PICKAXE || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return Objects.equals(meta.getPersistentDataContainer().get(NamespacedKey.minecraft("key"), PersistentDataType.STRING), "spawnerpick");
    }

    private boolean canBreakBlock(Player player, Block block) {
        WorldGuardPlugin wgPlugin = null;
        try
        {
            wgPlugin = WorldGuardPlugin.inst();
        } catch (Exception e) {
            final Component msg = MiniMessage.miniMessage().deserialize("<b><gradient:#fa0606:#ffffff>Error.</gradient></b>");
            player.sendMessage(msg);
            return true;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));

        if (regions == null) {
            return true;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));
        LocalPlayer localPlayer = wgPlugin.wrapPlayer(player);

        for (ProtectedRegion region : set) {
            if (!region.isMember(localPlayer) && !region.isOwner(localPlayer)) {
                return false;
            }
        }

        return true;
    }
}
