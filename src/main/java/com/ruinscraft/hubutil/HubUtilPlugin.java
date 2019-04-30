package com.ruinscraft.hubutil;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HubUtilPlugin extends JavaPlugin implements Listener {

    private static WorldGuard worldGuard;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            worldGuard = WorldGuard.getInstance();
        } catch (NullPointerException e) {
            getLogger().warning("WorldGuard required");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        /* Register event listener */
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean isWithinRegion(Player player, String region) {
        return isWithinRegion(player.getLocation(), region);
    }

    public boolean isWithinRegion(Location loc, String region) {
        BukkitWorld world = new BukkitWorld(loc.getWorld());
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(world);
        ApplicableRegionSet set = regionManager.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));

        for (ProtectedRegion each : set) {
            if (each.getId().equalsIgnoreCase(region)) {
                return true;
            }
        }

        return false;
    }

    public Location getSpawn() {
        String world = getConfig().getString("spawnworld");
        double x = getConfig().getInt("spawnx") + 0.5D;
        double y = getConfig().getInt("spawny");
        double z = getConfig().getInt("spawnz") + 0.5D;
        float yaw = (float) getConfig().getDouble("spawnyaw");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, 0.0F);
    }

    public void teleportToSpawn(Player player) {
        player.teleport(getSpawn());
    }

    public void setPlayerInventory(Player player) {
        player.getInventory().clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setCollidable(false);
        player.setFlying(false);
        player.setGameMode(GameMode.ADVENTURE);

        teleportToSpawn(player);

        if (player.hasPermission("group.vip1")) {
            player.setAllowFlight(true);
        }

        event.setJoinMessage(null);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(getSpawn());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        if (player.getGameMode() == GameMode.SPECTATOR ||
                player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (location.getY() < 32.0D) {
            teleportToSpawn(player);
            return;
        }

        if (isWithinRegion(player, "portals") || isWithinRegion(player, "eventportal")) {
            teleportToSpawn(player);

            if (player.getInventory().contains(Material.DIAMOND_SWORD)) {
                setPlayerInventory(player);
            }

            return;
        }

        if (isWithinRegion(player, "parkour")) {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
            }

            if (player.isFlying()) {
                player.setFlying(false);
            }

            if (player.getInventory().contains(Material.DIAMOND_SWORD)) {
                setPlayerInventory(player);
            }

            return;
        }

        if (player.getLocation().getY() < 57) {
            if (!player.getInventory().contains(Material.DIAMOND_SWORD)) {
                player.sendMessage(ChatColor.RED + "You've entered a PvP zone! " +
                        "Be prepared to fight at any time.");
                player.getInventory().clear();
                player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
            }

            player.setAllowFlight(false);
            player.setFlying(false);

            return;
        }

        if (player.getInventory().contains(Material.DIAMOND_SWORD)) {
            player.sendMessage(ChatColor.GREEN + "You've left the PvP zone.");
            setPlayerInventory(player);
        }

        if (player.hasPermission("group.vip1")) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player damagee = (Player) event.getEntity();

        if (!damager.getInventory().contains(Material.DIAMOND_SWORD)) {
            event.setCancelled(true);
            return;
        }

        if (!damagee.getInventory().contains(Material.DIAMOND_SWORD)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDropItemEvent(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupItemEvent(EntityPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChangeEvent(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

}
