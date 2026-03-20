package me.wrenthen.skyfactions2;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Skyfactions2 extends JavaPlugin implements Listener {
    private static Skyfactions2 INSTANCE;

    private static Economy econ = null;

    public static Skyfactions2 getInstance(){
        if (INSTANCE != null) return INSTANCE;
        return null;
    }

    public DataManager data;

    public FactionsManager factions;

    static HashMap<String, List<String>> warringPlayers = new HashMap<String, List<String>>();

    @Override
    public void onEnable() {

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.data = new DataManager(this);

        this.factions = new FactionsManager(this);

        INSTANCE = this;

        if(!this.getDataFolder().exists()) {
            try {
                this.getDataFolder().mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        getConfig().options().copyDefaults(true);
        saveConfig();


        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this,this);

        getCommand("sf").setExecutor(new IslandCommands());

        getCommand("bal").setExecutor(new IslandCommands());

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static HashMap<String, List<String>> getWarringPlayers() {
        return warringPlayers;
    }

    @EventHandler
    public void pvpOFF(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        Player a = (Player) e.getDamager();

        if (!warringPlayers.containsKey(p.getUniqueId().toString())) {
            e.setCancelled(true);
            a.sendRawMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "PvP disabled as you are not at war with this player.");
        } else {
            List<String> warParties = new ArrayList<String>();
            warParties = warringPlayers.get(p.getUniqueId().toString());
            if (!warParties.contains(a.getUniqueId().toString())) {
                e.setCancelled(true);
                a.sendRawMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "PvP disabled as you are not at war with this player.");
            }
        }
    }

    @EventHandler
    public void pvpOFFArrow(EntityDamageByEntityEvent e){
        if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Arrow arrow)) return;
        Player p = (Player) e.getEntity();

        if (arrow.getShooter() instanceof Player) {
            Player a = (Player) arrow.getShooter();
            if (!warringPlayers.containsKey(p.getUniqueId().toString())) {
                e.setCancelled(true);
                a.sendRawMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "PvP disabled as you are not at war with this player.");
            } else {
                List<String> warParties = new ArrayList<String>();
                warParties = warringPlayers.get(p.getUniqueId().toString());
                if (!warParties.contains(a.getUniqueId().toString())) {
                    e.setCancelled(true);
                    a.sendRawMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "PvP disabled as you are not at war with this player.");
                }
            }
        }


    }

    @EventHandler
    public void pvpOFFPotion(EntityDamageByEntityEvent e){
        if (!(e.getEntity() instanceof Player && e.getDamager() instanceof ThrownPotion potion)) return;
        Player p = (Player) e.getEntity();

        if (potion.getShooter() instanceof Player) {
            Player a = (Player) potion.getShooter();
            if (!warringPlayers.containsKey(p.getUniqueId().toString())) {
                e.setCancelled(true);
                a.sendRawMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "PvP disabled as you are not at war with this player.");
            } else {
                List<String> warParties = new ArrayList<String>();
                warParties = warringPlayers.get(p.getUniqueId().toString());
                if (!warParties.contains(a.getUniqueId().toString())) {
                    e.setCancelled(true);
                    a.sendRawMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "PvP disabled as you are not at war with this player.");
                }
            }
        }
    }

    public void onDisable() {
        saveConfig();
    }

}