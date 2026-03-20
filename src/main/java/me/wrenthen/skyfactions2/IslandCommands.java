package me.wrenthen.skyfactions2;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class IslandCommands implements CommandExecutor {

    int x, z;
    HashMap<String, String> invites = new HashMap<String, String>();
    HashMap<String, Long> invitesLength = new HashMap<String, Long>();
    HashMap<String, List<String>> warApps = new HashMap<String, List<String>>();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            String facName = Skyfactions2.getInstance().data.getConfig().getString(p.getUniqueId() + ".island");

            String userRank = Skyfactions2.getInstance().data.getConfig().getString(p.getUniqueId() + ".rank");
            org.bukkit.World island = Bukkit.getServer().getWorld("island");

            Economy economy = Skyfactions2.getEconomy();

            if (command.getName().equalsIgnoreCase("bal")) {
                p.sendMessage(ChatColor.GREEN + "Balance: " + ChatColor.GOLD + economy.format(economy.getBalance(p)));

            }

            if (command.getName().equalsIgnoreCase("sf")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("create")) {
                        create(args, p, island, economy);
                    }
                    if (args[0].equalsIgnoreCase("help")) {
                        help(p);
                    }
                    if (args[0].equalsIgnoreCase("home")) {
                        home(p, economy);
                    }
                    if (args[0].equalsIgnoreCase("claim")) {
                        Chunk chunk = p.getLocation().getChunk();
                        cmdClaim(p, chunk, facName, userRank, island, economy);
                    }
                    if (args[0].equalsIgnoreCase("invite")) {
                        invite(args, p, facName, userRank);
                    }
                    if (args[0].equalsIgnoreCase("join")) {
                        join(args, p, island);
                    }
                    if (args[0].equalsIgnoreCase("cancelinvite")) {
                        cancelinvite(args, p, facName, userRank);
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        info(args, p, facName);
                    }
                    if (args[0].equalsIgnoreCase("sethome")) {
                        sethome(p, facName, userRank, island);
                    }
                    if (args[0].equalsIgnoreCase("unclaim")) {
                        unclaim(p, facName, userRank, island);
                    }
                    if (args[0].equalsIgnoreCase("leave")) {
                        leave(p, facName, userRank, island);
                    }
                    if (args[0].equalsIgnoreCase("setrank")) {
                        setRank(args, p, facName, userRank);
                    }
                    if (args[0].equalsIgnoreCase("setleader")) {
                        setLeader(args, p, facName, userRank);
                    }
                    if (args[0].equalsIgnoreCase("confirmsetleader")) {
                        confirmSetLeader(args, p, facName, userRank);
                    }
                    if (args[0].equalsIgnoreCase("delete")) {
                        delete(p, facName, userRank);
                    }
                    if (args[0].equalsIgnoreCase("deleteconfirm")) {
                        deleteConfirm(p, facName, userRank, island);
                    }
                    if (args[0].equalsIgnoreCase("declarewar")) {
                        declareWar(p, facName, userRank, args);
                    }
                    if (args[0].equalsIgnoreCase("joinwar")) {
                        joinWar(p, facName, userRank, args);
                    }
                    if (args[0].equalsIgnoreCase("acceptjoin")) {
                        acceptJoin(p, facName, userRank, args);
                    }
                    if (args[0].equalsIgnoreCase("cancelwarapp")) {
                        cancelWarApp(p, facName, userRank, args);
                    }
                    if (args[0].equalsIgnoreCase("endwar")) {
                        endWar(p, facName, userRank, args);
                    }
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Type: /sf help - for help");
                }
            }
        }
        return true;
    }

    public boolean create(String[] args, Player p, org.bukkit.World island, Economy economy) {
        if (args.length > 1) {
            if (!Skyfactions2.getInstance().data.getConfig().contains(String.valueOf(p.getUniqueId()))) {
                if (args[1].toString().length() > 10) {
                    if (!Skyfactions2.getInstance().factions.getConfig().contains(args[1])) {
                        double balance = economy.getBalance(p);
                        if (balance >= 1000) {
                            FileConfiguration config = Skyfactions2.getInstance().getConfig();
                            x = config.getInt("x");
                            z = config.getInt("z");
                            int row = config.getInt("row");
                            int col = config.getInt("col");
                            int targetCol = config.getInt("targetCol");
                            int targetRow = config.getInt("targetRow");
                            int firstIsland = config.getInt("firstIsland");

                            int distance = 120;

                            if (col == 0 && row == 0 && firstIsland == 0) {
                                generateIsland(args, x, z, p, island);
                                firstIsland++;
                                col++;
                            } else {
                                if (row != targetRow) {
                                    row++;
                                    x -= distance;
                                    generateIsland(args, x, z, p, island);
                                } else if (col != targetCol) {
                                    col++;
                                    z -= distance;
                                    generateIsland(args, x, z, p, island);
                                } else {
                                    x = 0;
                                    if (z != 0) {
                                        z = distance * row;
                                    } else {
                                        z = distance;
                                    }

                                    generateIsland(args, x, z, p, island);

                                    row = 0;
                                    col = 0;
                                    targetCol++;
                                    targetRow++;
                                }
                            }

                            Skyfactions2.getInstance().getConfig().set("x", x);
                            Skyfactions2.getInstance().getConfig().set("z", z);
                            Skyfactions2.getInstance().getConfig().set("row", row);
                            Skyfactions2.getInstance().getConfig().set("col", col);
                            Skyfactions2.getInstance().getConfig().set("targetCol", targetCol);
                            Skyfactions2.getInstance().getConfig().set("targetRow", targetRow);
                            Skyfactions2.getInstance().getConfig().set("firstIsland", firstIsland);

                            Skyfactions2.getInstance().data.getConfig().set(p.getUniqueId().toString() + ".island", args[1]);
                            Skyfactions2.getInstance().data.getConfig().set(p.getUniqueId().toString() + ".rank", "leader");
                            Skyfactions2.getInstance().data.saveConfig();

                            List<String> users = new ArrayList<String>();
                            users.add(p.getUniqueId().toString());

                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".users", users);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".leader", p.getUniqueId().toString());
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".motto", "");
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".x", x);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".y", 100);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".z", z);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".spawnX", x);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".spawnY", 100);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".spawnZ", z);
                            Skyfactions2.getInstance().factions.saveConfig();

                            Location loc = new Location(island, x, 100, z, 0, 0);
                            Chunk chunk = loc.getChunk();
                            makeFirstClaim(args, p, chunk, island, economy);

                            EconomyResponse response = economy.withdrawPlayer(p, 900);
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.GOLD + "$1000" + ChatColor.DARK_AQUA + " has been subtracted from your account.");
                        } else {
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You do not have enough money to create a Skyblock Faction. Cost: " + ChatColor.GOLD + "$1000.");
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Type: /balance to check your balance.");

                        }
                    } else {
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Faction name already taken.");
                    }
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Faction name too long.");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You are either already in or own a Skyblock Faction.");
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Leave or destroy it to create a new one.");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Proper usage:");
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf create (name)");
        }
        return true;
    }

    public boolean generateIsland(String[] args, int x, int z, Player p, org.bukkit.World island) {
        Location location = p.getLocation();

        File dataFolder = (Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Skyfactions2"))).getDataFolder();

        File schematic = new File(dataFolder + File.separator + "/island.schem");

        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld (island), -1)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(x, 100, z))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }

        Location loc = new Location(island, x, 100, z, 0, 0);
        p.teleport(loc);
        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Your Skyblock Faction " + ChatColor.GOLD + args[1] + ChatColor.DARK_AQUA + " has been created");
        return true;
    }

    public boolean help(Player p) {
        p.sendMessage(ChatColor.GOLD + "[SF] Help Page");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf help: " + ChatColor.AQUA + "Displays help page");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf create (name): " + ChatColor.AQUA + "Creates a Skyblock Faction with name (name)");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf claim: " + ChatColor.AQUA + "Claims territory for your Skyblock Faction");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf info (name - optional): " + ChatColor.AQUA + "Prints information on your Skyblock Faction - or the Skyblock Faction provided");
        p.sendMessage(" ");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf home: " + ChatColor.AQUA + "Teleports to your Skyblock Faction home");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf sethome: " + ChatColor.AQUA + "Sets your Skyblock Faction home");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf invite (name) " + ChatColor.AQUA + "Invites a player to your Skyblock Faction");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf join (name): " + ChatColor.AQUA + "Joins a Skyblock Faction after being invited");
        p.sendMessage(ChatColor.DARK_AQUA + "/sf cancelinvite: " + ChatColor.AQUA + "Cancels an invitation early");
        return true;
    }

    public boolean home(Player p, Economy economy) {
        double balance = economy.getBalance(p);
        if (balance >= 10) {
            int y = 0;

            String islandName = Skyfactions2.getInstance().data.getConfig().getString(p.getUniqueId().toString() + ".island");

            x = Skyfactions2.getInstance().factions.getConfig().getInt(islandName + ".x");
            y = Skyfactions2.getInstance().factions.getConfig().getInt(islandName + ".y");
            z = Skyfactions2.getInstance().factions.getConfig().getInt(islandName + ".z");

            Location loc = new Location(Bukkit.getWorld("island"), x, y, z, 0, 0);
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Teleporting...");
            p.teleport(loc);
            EconomyResponse response = economy.withdrawPlayer(p, 10);
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.GOLD + "$10" + ChatColor.DARK_AQUA + " has been subtracted from your account.");

            p.sendMessage();
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You do not have enough money to teleport home.");
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Cost: " + ChatColor.GOLD + "$10." + ChatColor.DARK_AQUA + " Type: /balance to check your balance.");
        }
        return true;
    }

    public boolean checkForRegions(Player p, org.bukkit.World island) {
        int x = 0;
        com.sk89q.worldguard.LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(p.getLocation());
        com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(island);

        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(p.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);

        for (ProtectedRegion region : set) {
            if (!region.getId().equalsIgnoreCase("__global__")) {
                x++;
            }
        }
        if (x > 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean cmdClaim(Player p, Chunk chunk, String facName, String userRank, org.bukkit.World island, Economy economy) {
        if (userRank.matches("(?i)officer|leader")) {
            if (checkForRegions(p, island)) {
                checkForRegions(p, island);
                double balance = economy.getBalance(p);
                if (balance >= 100) {
                    claim(p, chunk, facName, userRank, island, economy);

                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Succesfully claimed!");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.GOLD + "$100" + ChatColor.DARK_AQUA + " has been subtracted from your account.");
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You do not have enough money to claim this chunk.");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Cost: " + ChatColor.GOLD + "$100." + ChatColor.DARK_AQUA + " Type: /balance to check your balance.");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You are already in claimed territory");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be officer or leader");
        }

        return true;
    }

    public boolean claim(Player p, Chunk chunk, String facName, String userRank, org.bukkit.World island, Economy economy) {
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;

        int maxX = minX + 15;
        int maxZ = minZ + 15;

        int numOfClaims = Skyfactions2.getInstance().factions.getConfig().getInt(facName + ".numOfClaims");
        numOfClaims++;
        Skyfactions2.getInstance().factions.getConfig().set(facName + ".numOfClaims", numOfClaims);

        String strNumOfClaims = facName + numOfClaims;
        facName = facName + "0";

        BlockVector3 min = BlockVector3.at(minX, 0, minZ);
        BlockVector3 max = BlockVector3.at(maxX, 256, maxZ);

        ProtectedRegion region = new ProtectedCuboidRegion(strNumOfClaims, min, max);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(new BukkitWorld (island));

        ProtectedRegion factionName = regions.getRegion(facName);

        try {
            region.setParent(factionName);
        } catch (ProtectedRegion.CircularInheritanceException e) {
            e.printStackTrace();
        }

        regions.addRegion(region);
        EconomyResponse response = economy.withdrawPlayer(p, 100);

        return true;
    }

    public boolean makeFirstClaim(String[] args, Player p, Chunk chunk, org.bukkit.World island, Economy economy) {
        String facName = args[1];
        String facName0 = facName + "0";
        double dx = p.getLocation().getX();
        double dz = p.getLocation().getZ();

        BlockVector3 min = BlockVector3.at(0, 256, 0);
        BlockVector3 max = BlockVector3.at(1, 255, 1);
        ProtectedRegion region = new ProtectedCuboidRegion(facName0, min, max);
        Skyfactions2.getInstance().factions.getConfig().set(facName + ".numOfClaims", 0);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(new BukkitWorld (island));

        region.setFlag(Flags.GREET_MESSAGE, ChatColor.GOLD + "[SF] " + ChatColor.AQUA + "Now entering " + ChatColor.DARK_AQUA + facName);
        region.setFlag(Flags.FAREWELL_MESSAGE, ChatColor.GOLD + "[SF] " + ChatColor.RED + "Now leaving " + ChatColor.DARK_RED + facName);
        region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        DefaultDomain members = region.getMembers();
        members.addPlayer(p.getUniqueId());

        regions.addRegion(region);

        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Remember to claim the rest of your island");
        claim(p, chunk, facName, "leader", island, economy);

        return true;
    }

    public boolean invite(String[] args, Player p, String facName, String userRank) {
        if (userRank.matches("(?i)officer|leader")) {
            if (args.length > 1) {
                Player invitee = Bukkit.getPlayerExact(args[1]);
                if (invitee == null) {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "The player could not be found.");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Please ensure you are typing their name exactly.");
                } else {
                    String userName = p.getUniqueId().toString();
                    String invitesLengthKey = userName + facName;
                    invites.put(invitee.getUniqueId().toString(), facName);
                    invitesLength.put(invitesLengthKey, System.currentTimeMillis() + (30 * 1000));

                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + args[1] + ChatColor.DARK_AQUA + " has been invited. Please tell them to type:");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf join " + ChatColor.DARK_RED + facName);
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Type: /sf cancelinvite " + ChatColor.DARK_RED + args[1] + ChatColor.DARK_AQUA + " to cancel.");

                    invitee.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You have been invited to " + ChatColor.DARK_RED + facName);
                    invitee.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Type: /sf join " + ChatColor.DARK_RED + facName);
                    invitee.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "This invitation will only last 30 seconds");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Proper usage:");
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf invite (name)");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be officer or leader");
        }
        return true;
    }

    public boolean join(String[] args, Player p, org.bukkit.World island) {
        if (args.length > 1) {
            String facName = args[1];
            String userName = p.getUniqueId().toString();
            if (invites.containsKey(userName)) {
                if (invites.get(userName).equals(facName)) {
                    if (!Skyfactions2.getInstance().data.getConfig().contains(String.valueOf(p.getUniqueId()))) {
                        if (invitesLength.get(userName + facName) > System.currentTimeMillis()) {
                            List<String> users = new ArrayList<String>();
                            users = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".users");
                            users.add(userName);
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".users", users);

                            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                            RegionManager regions = container.get(new BukkitWorld (island));

                            String facName0LC = facName.toLowerCase() + "0";

                            regions.getRegion(facName0LC);
                            ProtectedRegion factionName = regions.getRegion(facName0LC);
                            DefaultDomain members = factionName.getMembers();
                            members.addPlayer(p.getUniqueId());

                            invites.remove(userName);
                            invitesLength.remove(userName + facName);

                            Skyfactions2.getInstance().data.getConfig().set(p.getUniqueId().toString() + ".island", args[1]);
                            Skyfactions2.getInstance().data.getConfig().set(p.getUniqueId().toString() + ".rank", "member");
                            Skyfactions2.getInstance().data.saveConfig();

                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Succesfully joined " + ChatColor.DARK_RED + facName);
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Type: /sf home to teleport to " + ChatColor.DARK_RED + facName + ChatColor.DARK_AQUA + " home");
                        } else {
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Invite expired. Ask " + ChatColor.DARK_RED + facName + ChatColor.DARK_AQUA + " to join again");
                            invitesLength.remove(userName + facName);
                            invites.remove(userName);
                        }
                    } else {
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You are either already in or own a Skyblock Faction.");
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Leave or destroy it to join a new one.");
                    }
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Wrong faction name.");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You have not been invited.");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Proper usage:");
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf join (name)");
        }
        return true;
    }

    public boolean cancelinvite(String[] args, Player p, String facName, String userRank) {
        if (userRank.matches("(?i)officer|leader")) {
            if (args.length > 1) {
                String userName = args[1];
                if (invites.get(userName).equals(facName)) {
                    invites.remove(userName);
                    invitesLength.remove(userName + facName);
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Proper usage:");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf cancelinvite (name)");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Proper usage:");
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf cancelinvite (name)");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be officer or leader");
        }
        return true;
    }

    public boolean info(String[] args, Player p, String facName) {
        if (args.length > 1) {
            facName = args[1];
        }
        processInfo(p, facName);
        return true;
    }

    public boolean processInfo(Player p, String facName) {
        String strLeaderName;
        List<String> users = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".users");
        strLeaderName = Skyfactions2.getInstance().factions.getConfig().getString(facName + ".leader");
        OfflinePlayer leaderName = Bukkit.getPlayer(UUID.fromString(strLeaderName));
        strLeaderName = leaderName.getName().toString();
        OfflinePlayer member;
        String userList = "";

        p.sendMessage(ChatColor.GOLD + "====================[" + facName + "]====================");
        p.sendMessage(ChatColor.GOLD + "Description: ");
        p.sendMessage(ChatColor.GOLD + "Leader: " + ChatColor.AQUA + strLeaderName);
        p.sendMessage(ChatColor.GOLD + "Officers: ");
        p.sendMessage(ChatColor.GOLD + "Citizens: ");
        for (int i = 0; i < users.size(); i++) {
            member = Bukkit.getPlayer(UUID.fromString(users.get(i)));
            userList = member.getName() + ", " + userList;
        }
        p.sendMessage(ChatColor.AQUA + userList);

        return true;
    }


    public boolean sethome(Player p, String facName, String userRank, org.bukkit.World island) {
        if (userRank.equalsIgnoreCase("leader")) {
            double dx = p.getLocation().getX();
            double dy = p.getLocation().getY();
            double dz = p.getLocation().getZ();
            String facNameLC = "";
            String regionName = "";

            com.sk89q.worldguard.LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
            com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(p.getLocation());
            com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(island);

            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(p.getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(loc);

            for (ProtectedRegion region : set) {
                regionName = region.getId().toLowerCase();
                facNameLC = facName.toLowerCase();
                if (regionName.contains(facNameLC)) {
                    Skyfactions2.getInstance().factions.getConfig().set(facName + ".x", dx);
                    Skyfactions2.getInstance().factions.getConfig().set(facName + ".y", dy);
                    Skyfactions2.getInstance().factions.getConfig().set(facName + ".z", dz);

                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Home set");
                }
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean unclaim(Player p, String facName, String userRank, org.bukkit.World island) {
        if (userRank.matches("(?i)officer|leader")) {
            String facNameLC = "";
            String regionName = "";
            int numOfClaims = 0;

            com.sk89q.worldguard.LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
            com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(p.getLocation());
            com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(island);

            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(p.getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(loc);

            RegionManager regions = container.get(new BukkitWorld (island));

            for (ProtectedRegion region : set) {
                regionName = region.getId().toLowerCase();
                facNameLC = facName.toLowerCase();
                if (regionName.contains(facNameLC)) {
                    if (!region.getId().equalsIgnoreCase(facName + "0")) {
                        regions.removeRegion(region.getId());
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Unclaimed");
                        numOfClaims = Skyfactions2.getInstance().factions.getConfig().getInt(facName + ".numOfClaims");
                        numOfClaims--;
                        Skyfactions2.getInstance().factions.getConfig().set(facName + ".numOfClaims", numOfClaims);
                    }
                }
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be officer or leader");
        }
        return true;
    }

    public boolean leave(Player p, String facName, String userRank, org.bukkit.World island) {
        if (userRank.matches("(?i)officer|member")) {
            List<String> users = new ArrayList<String>();
            users = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".users");
            users.remove(p.getUniqueId().toString());
            Skyfactions2.getInstance().factions.getConfig().set(facName + ".users", users);
            Skyfactions2.getInstance().factions.saveConfig();

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(new BukkitWorld (island));

            String facName0LC = facName.toLowerCase() + "0";

            regions.getRegion(facName0LC);
            ProtectedRegion factionName = regions.getRegion(facName0LC);
            DefaultDomain members = factionName.getMembers();
            members.removePlayer(p.getUniqueId());

            Skyfactions2.getInstance().data.getConfig().set(p.getUniqueId().toString(), null);
            Skyfactions2.getInstance().data.saveConfig();
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You have left " + ChatColor.DARK_RED + facName);

            for (int i = 0; i < users.size(); i++) {
                Player user = Bukkit.getPlayerExact(users.get(i));
                user.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_RED + p + ChatColor.DARK_AQUA + " has left the faction.");
            }

        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "You cannot leave your own faction. Please destroy it or give leadership away.");
        }
        return true;
    }

    public boolean setRank(String[] args, Player p, String facName, String userRank) {
        if (!p.getDisplayName().equalsIgnoreCase(args[1])) {
            if (userRank.equalsIgnoreCase("leader")) {
                if (args.length > 1) {
                    Player rankee = Bukkit.getPlayerExact(args[1]);
                    if (Skyfactions2.getInstance().data.getConfig().getString(rankee.getUniqueId().toString() + ".island").equalsIgnoreCase(facName)) {
                        if (args[2].equalsIgnoreCase("member")) {
                            Skyfactions2.getInstance().data.getConfig().set(rankee.getUniqueId().toString() + ".rank", "member");
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + rankee.getDisplayName() + ChatColor.DARK_AQUA + " has been demoted to the rank of " + ChatColor.AQUA + "Member.");
                            rankee.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " You have been demoted to the rank of " + ChatColor.AQUA + "Member.");
                        } else if (args[2].equalsIgnoreCase("officer")) {
                            Skyfactions2.getInstance().data.getConfig().set(rankee.getUniqueId().toString() + ".rank", "officer");
                            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + rankee.getDisplayName() + ChatColor.DARK_AQUA + " Has been promoted to the rank of " + ChatColor.DARK_RED + "Officer.");
                            rankee.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " You have been promoted to the rank of " + ChatColor.DARK_RED + "Officer.");
                        } else {
                            p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " Incorrect rank usage. Use member or officer");
                        }
                    } else {
                        p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " That player could not be found in your faction. Please try again");
                    }
                    Skyfactions2.getInstance().data.saveConfig();
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " Not enough arguments. Correct usage:");
                    p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " /sf setrank (username) (member/officer)");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " You cannot set your own rank!");
        }
        return true;
    }

    public boolean setLeader(String[] args, Player p, String facName, String userRank) {
        if (userRank.equalsIgnoreCase("leader")) {
            Player newLeader = Bukkit.getPlayerExact(args[1]);

            if (Skyfactions2.getInstance().data.getConfig().getString(newLeader.getUniqueId().toString() + ".island").equalsIgnoreCase(facName)) {
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_RED + " You will be giving over your faction to " + ChatColor.DARK_AQUA + newLeader);
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_RED + " Are you SURE about this? You will not be in control of the faction anymore.");
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_RED + " Type /sf confirmsetleader " + newLeader);
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " That player could not be found in your faction. Please try again");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean confirmSetLeader(String[] args, Player p, String facName, String userRank) {
        if (userRank.equalsIgnoreCase("leader")) {
            Player newLeader = Bukkit.getPlayerExact(args[1]);

            if (Skyfactions2.getInstance().data.getConfig().getString(newLeader.getUniqueId().toString() + ".island").equalsIgnoreCase(facName)) {
                List<String> users = new ArrayList<String>();
                users = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".users");

                Skyfactions2.getInstance().data.getConfig().set(newLeader.getUniqueId().toString() + ".rank", "leader");
                Skyfactions2.getInstance().data.getConfig().set(p.getUniqueId().toString() + ".rank", "officer");
                Skyfactions2.getInstance().data.saveConfig();

                Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".leader", newLeader.getUniqueId().toString());
                Skyfactions2.getInstance().factions.saveConfig();

                newLeader.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " Congratulations. You are now the leader of " + ChatColor.DARK_RED + facName);

                for (int i = 0; i < users.size(); i++) {
                    Player user = Bukkit.getPlayerExact(users.get(i));
                    user.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_RED + newLeader + " Is now the faction leader of " + ChatColor.DARK_RED + facName);
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " That player could not be found in your faction. Please try again");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean delete(Player p, String facName, String userRank) {
        if (userRank.equalsIgnoreCase("leader")) {
            if (!Skyfactions2.getInstance().factions.getConfig().getBoolean(facName + ".atwar")) {
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.RED + " You will be " + ChatColor.DARK_RED + "DELETING " + ChatColor.RED + "your faction.");
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.RED + " Are you SURE about this? This is irreversible.");
                p.sendMessage(ChatColor.GOLD + "[SF]" + ChatColor.RED + " Type /sf deleteconfirm if you are sure. ");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean deleteConfirm(Player p, String facName, String userRank, org.bukkit.World island) {
        if (userRank.equalsIgnoreCase("leader")) {
            if (!Skyfactions2.getInstance().factions.getConfig().getBoolean(facName + ".atwar")) {
                List<String> users = new ArrayList<String>();
                users = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".users");

                Skyfactions2.getInstance().factions.getConfig().set(facName, null);
                Skyfactions2.getInstance().factions.saveConfig();

                for (int i = 0; i < users.size(); i++) {
                    Skyfactions2.getInstance().data.getConfig().set(users.get(i), null);
                }
                Skyfactions2.getInstance().data.saveConfig();

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(new BukkitWorld (island));

                String facName0 = facName.toLowerCase() + "0";

                regions.removeRegion(facName0, RemovalStrategy.REMOVE_CHILDREN);

                Bukkit.broadcastMessage(ChatColor.GOLD + "[SF]" + ChatColor.RED + " The Skyblock Faction " + ChatColor.DARK_RED + facName + ChatColor.RED + " has been " + ChatColor.DARK_RED + "deleted.");
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Cannot delete faction whilst at war");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean declareWar(Player p, String facName, String userRank, String[] args) {
        if (userRank.equalsIgnoreCase("leader")) {
            if (Skyfactions2.getInstance().factions.getConfig().contains(args[1])) {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + facName + ChatColor.RED + " has declared WAR on " + ChatColor.GOLD + args[1]);
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " Allies of either faction are urged to type: /sf joinwar (ally name) (enemy name)");
                Bukkit.broadcastMessage("");

                Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".atwar", true);
                Skyfactions2.getInstance().factions.getConfig().set(facName + ".atwar", true);

                List<String> enemyFacsA = new ArrayList<String>();
                List<String> enemyFacsB = new ArrayList<String>();

                if (Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".war") != null) {
                    enemyFacsA = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".war");
                }

                enemyFacsA.add(facName);

                Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".war", enemyFacsA);
                Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".war with." + facName, "defending");

                if (Skyfactions2.getInstance().factions.getConfig().getList(facName + ".war") != null) {
                    enemyFacsB = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".war");
                }

                enemyFacsB.add(args[1]);

                Skyfactions2.getInstance().factions.getConfig().set(facName + ".war", enemyFacsB);
                Skyfactions2.getInstance().factions.getConfig().set(facName + ".war with." + args[1], "attacking");

                Skyfactions2.getInstance().factions.saveConfig();

                HashMap<String, List<String>> warringPlayers = Skyfactions2.getWarringPlayers();

                List<String> facAUsers = new ArrayList<String>();

                List<String> facBUsers = new ArrayList<String>();

                facAUsers = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".users");

                facBUsers = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".users");

                for (int i = 0; i < facAUsers.size(); i++) {
                    warringPlayers.put(facAUsers.get(i), facBUsers);
                }

                for (int i = 0; i < facBUsers.size(); i++) {
                    warringPlayers.put(facBUsers.get(i), facAUsers);
                }



            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find enemy faction. Make sure you spelt it correctly. Case sensitive.");
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Correct usage: /sf declarewar (enemy faction)");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean joinWar(Player p, String facName, String userRank, String[] args) {
        String strLeaderName = "";
        if (userRank.equalsIgnoreCase("leader")) {
            if (Skyfactions2.getInstance().factions.getConfig().contains(args[1])) {
                if (Skyfactions2.getInstance().factions.getConfig().contains(args[2])) {
                    if (Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".war").contains(args[2])) {

                        List<String> warParties = new ArrayList<String>();
                        warParties.add(args[1]);
                        warParties.add(args[2]);

                        warApps.put(facName, warParties);

                        strLeaderName = Skyfactions2.getInstance().factions.getConfig().getString(args[1] + ".leader");
                        Player allyLeader = Bukkit.getPlayer(UUID.fromString(strLeaderName));

                        allyLeader.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + p.getDisplayName() + ChatColor.DARK_AQUA + " of faction " +
                                ChatColor.AQUA + facName + ChatColor.DARK_AQUA + " has requested to join your war against "
                                + ChatColor.DARK_RED + args[2]);

                        allyLeader.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "To accept type: /sf acceptjoin " + ChatColor.DARK_RED + facName + " " + args[2]);

                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Sent request to " + ChatColor.DARK_RED + allyLeader.getDisplayName());
                    } else {
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Factions provided are not at war with each other.");
                    }
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find " + ChatColor.AQUA + args[2] + "." + ChatColor.DARK_AQUA + " Make sure you spelt it correctly. Case sensitive.");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Correct usage: /sf joinwar (ally faction) (enemy faction)");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find " + ChatColor.AQUA + args[1] + "." + ChatColor.DARK_AQUA + " Make sure you spelt it correctly. Case sensitive.");
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Correct usage: /sf joinwar (ally faction) (enemy faction)");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }


        return true;
    }

    public boolean acceptJoin(Player p, String facName, String userRank, String[] args) {
        String ally;
        String enemy;

        if (userRank.equalsIgnoreCase("leader")) {
            if (warApps.containsKey(args[1])) {
                List<String> warParties = new ArrayList<String>();

                warParties = warApps.get(args[1]);

                ally = warParties.get(0);
                enemy = warParties.get(1);

                if (ally.equalsIgnoreCase(facName) && enemy.equalsIgnoreCase(args[2])) {
                    Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".atwar", true);

                    List<String> enemyFacsA = new ArrayList<String>();


                    if (Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".war") != null) {
                        enemyFacsA = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".war");
                    }

                    enemyFacsA.add(args[2]);

                    Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".war", enemyFacsA);
                    Bukkit.broadcastMessage("");

                    if (Skyfactions2.getInstance().factions.getConfig().getString(facName + ".war with." + args[2]).equalsIgnoreCase("defending")) {
                        Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".war with." + args[2], "joining defense");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + args[1] + ChatColor.RED + " has joined " + ChatColor.GOLD + facName + ChatColor.RED + " DEFENDING against " + ChatColor.DARK_RED + args[2]);
                    } else if (Skyfactions2.getInstance().factions.getConfig().getString(facName + ".war with." + args[2]).equalsIgnoreCase("attacking")) {
                        Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".war with." + args[2], "joining attack");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + args[1] + ChatColor.RED + " has joined " + ChatColor.DARK_RED + facName + ChatColor.RED + " ATTACKING " + ChatColor.GOLD + args[2]);
                    }

                    Bukkit.broadcastMessage(ChatColor.GOLD + "[SF]" + ChatColor.DARK_AQUA + " Allies of either faction are urged to type: /sf joinwar (ally name) (enemy name) ");

                    HashMap<String, List<String>> warringPlayers = Skyfactions2.getWarringPlayers();

                    List<String> facAUsers = new ArrayList<String>();

                    List<String> facBUsers = new ArrayList<String>();

                    facAUsers = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".users");

                    facBUsers = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[2] + ".users");

                    for (int i = 0; i < facAUsers.size(); i++) {
                        warringPlayers.put(facAUsers.get(i), facBUsers);
                    }

                    for (int i = 0; i < facBUsers.size(); i++) {
                        warringPlayers.put(facBUsers.get(i), facAUsers);
                    }


                    Skyfactions2.getInstance().factions.saveConfig();
                    warApps.remove(args[1]);

                } else {
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find factions specified. Make sure you spelt it correctly. Case sensitive.");
                    p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Correct usage: /sf acceptjoin (ally faction) (enemy faction)");
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find factions specified. Make sure you spelt it correctly. Case sensitive.");
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Correct usage: /sf acceptjoin (ally faction) (enemy faction)");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader");
        }
        return true;
    }

    public boolean cancelWarApp(Player p, String facName, String userRank, String[] args) {
        if (userRank.equalsIgnoreCase("leader")) {
            if (warApps.containsKey(facName)) {
                warApps.remove(facName);
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Application to join wars removed.");
            }else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find your faction's application to join a war.");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader.");
        }
        return true;
    }

    public boolean endWar(Player p, String facName, String userRank, String[] args) {
        if (args.length > 1) {
            if (userRank.equalsIgnoreCase("leader")) {
                if (Skyfactions2.getInstance().factions.getConfig().getBoolean(facName + ".atwar")) {
                    if (Skyfactions2.getInstance().factions.getConfig().getList(facName + ".war").contains(args[1])) {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_RED + facName + ChatColor.DARK_AQUA + " has sued for peace with " + ChatColor.DARK_RED + args[1]);

                        List<String> enemyFacs = new ArrayList<String>();

                        enemyFacs = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".war");
                        enemyFacs.remove(args[1]);
                        if (enemyFacs.isEmpty()) {
                            Skyfactions2.getInstance().factions.getConfig().set(facName + ".atwar", false);
                        }
                        Skyfactions2.getInstance().factions.getConfig().set(facName + ".war", enemyFacs);

                        enemyFacs = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".war");
                        enemyFacs.remove(facName);
                        if (enemyFacs.isEmpty()) {
                            Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".atwar", false);
                        }
                        Skyfactions2.getInstance().factions.getConfig().set(args[1] + ".war", enemyFacs);


                        HashMap<String, List<String>> warringPlayers = Skyfactions2.getWarringPlayers();

                        List<String> facAUsers = new ArrayList<String>();

                        List<String> facBUsers = new ArrayList<String>();

                        facAUsers = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(facName + ".users");

                        facBUsers = (List<String>) Skyfactions2.getInstance().factions.getConfig().getList(args[1] + ".users");

                        for (int i = 0; i < facAUsers.size(); i++) {
                            warringPlayers.remove(facAUsers.get(i), facBUsers);
                        }

                        for (int i = 0; i < facBUsers.size(); i++) {
                            warringPlayers.remove(facBUsers.get(i), facAUsers);
                        }


                        Skyfactions2.getInstance().factions.saveConfig();
                    } else {
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Could not find " + ChatColor.DARK_RED + args[1] + ChatColor.DARK_AQUA + " please ensure it is spelt correctly. Case sensitive.");
                        p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Looking for: " + ChatColor.DARK_RED + Skyfactions2.getInstance().factions.getConfig().getString(facName + ".war" + "."));
                    }
                }
            } else {
                p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper faction permissions. Must be leader.");
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "Improper arguments. Proper usage:");
            p.sendMessage(ChatColor.GOLD + "[SF] " + ChatColor.DARK_AQUA + "/sf endwar (enemy faction)");
        }
        return true;
    }
}
