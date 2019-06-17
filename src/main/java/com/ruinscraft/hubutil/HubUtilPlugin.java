package com.ruinscraft.hubutil;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class HubUtilPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("spawn").setExecutor(new SpawnCommand());
    }

    private static HubUtilPlugin instance;

    public static HubUtilPlugin getInstance() {
        return instance;
    }

    public static Location getSpawn() {
        String world = instance.getConfig().getString("spawnworld");
        double x = instance.getConfig().getInt("spawnx") + 0.5D;
        double y = instance.getConfig().getInt("spawny");
        double z = instance.getConfig().getInt("spawnz") + 0.5D;
        float yaw = (float) instance.getConfig().getDouble("spawnyaw");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, 0.0F);
    }

    public static void teleportToSpawn(Player player) {
        player.teleport(getSpawn());
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

        if (location.getY() < 1.0D) {
            teleportToSpawn(player);
            return;
        }

        if (player.hasPermission("group.vip1")) {
            player.setAllowFlight(true);
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

}
