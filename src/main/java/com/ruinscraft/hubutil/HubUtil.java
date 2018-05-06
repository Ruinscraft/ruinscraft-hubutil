package com.ruinscraft.hubutil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HubUtil extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
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

		event.setJoinMessage(null);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();

		if (location.getY() < 32.0D) {
			teleportToSpawn(player);
			return;
		}

		Block blockUnderPlayer = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (blockUnderPlayer == null || blockUnderPlayer.getType() == Material.AIR) {
			return;
		}

		if ((blockUnderPlayer.getType() == Material.STAINED_GLASS) && (blockUnderPlayer.getData() == 3)) {
			if (!player.getInventory().contains(Material.DIAMOND_SWORD)) {
				player.getInventory().clear();
				player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
				player.setAllowFlight(false);
				player.setFlying(false);
			}
		}

		else if (player.getInventory().contains(Material.DIAMOND_SWORD)) {
			setPlayerInventory(player);

			if (player.hasPermission("group.vip1")) {
				player.setAllowFlight(true);
			}
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

}
