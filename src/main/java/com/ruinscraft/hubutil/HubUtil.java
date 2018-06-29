package com.ruinscraft.hubutil;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class HubUtil extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	// https://bukkit.org/threads/solved-worldguard-get-the-region-a-player-is-standing-in.50800/
	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	public boolean isWithinRegion(Player player, String region) { 
		return isWithinRegion(player.getLocation(), region); 
	}

	public boolean isWithinRegion(Location loc, String region) {
		WorldGuardPlugin guard = getWorldGuard();
		Vector v = toVector(loc);
		RegionManager manager = guard.getRegionManager(loc.getWorld());
		ApplicableRegionSet set = manager.getApplicableRegions(v);

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

		teleportToSpawn(player);

		if (player.hasPermission("group.vip1")) {
			player.setAllowFlight(true);
		}

		player.setCollidable(false);

		player.setFlying(false);

		event.setJoinMessage(null);

		player.setGameMode(GameMode.ADVENTURE);
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

}
