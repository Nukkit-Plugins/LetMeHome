package kr.mohi.simplehome;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.Lists;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class SimpleHome extends PluginBase implements Listener {
	private int m_version;
	private Config messages;
	private LinkedHashMap<String, Object> homeDB;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		this.initDB();
		this.initMessage();
		this.updateMessage();
		this.registerCommands();
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getLogger().info("SimpleHome is enabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = this.getServer().getPlayer(sender.getName());
		if (command.getName().toLowerCase().equals(get("command-sethome"))) {
			if (!(sender instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0 && args[0] == "") {
				alert(sender, get("command-sethome-usage"));
				return true;
			}
			if(this.getHomePositionByName(player, args[0]) != null) {
				this.alert(sender, this.get("command-sethome-failed"));
				this.alert(sender, this.get("command-sethome-failed-overlapping"));
				return true;
			}
			HomePosition home = new HomePosition(player.getX(), player.getY(), player.getZ(), player.getLevel(), args[0], player.getName().toLowerCase());
			this.setHome(home, sender);
			this.save();
			this.message(sender, this.get("message-sethome-success"));
			return true;
		}
		if (command.getName().toLowerCase().equals(get("command-delhome"))) {
			if (!(sender instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				this.alert(sender, get("command-delhome-usage"));
				return true;
			}
			if (this.homeDB.containsKey(sender.getName().toLowerCase())) {
				this.delHome(args[0], player);
				this.save();
				return true;
			} else {
				this.alert(sender, this.get("command-delhome-failed"));
				this.alert(sender, this.get("command-delhome-failed-not-found"));
			}
			this.save();
			return true;
		}
		if (command.getName().toLowerCase().equals(get("command-homelist"))) {
			sender.sendMessage(TextFormat.AQUA + "[Home]" + this.get("message-homelist-first"));
			if (this.homeDB.containsKey(sender.getName().toLowerCase())) {
				for (HomePosition home : this.getPlayerHomePositions(player)) {
					double x = home.getX();
					double y = home.getY();
					double z = home.getZ();
					String level = home.getLevel().getFolderName();
					sender.sendMessage(TextFormat.AQUA + home.getName() + TextFormat.BOLD + " - " + "X: " + x + ", Y: "
							+ y + ", Z: " + z + ", World : " + level);
				}
			}
			return true;
		}
		if (command.getName().toLowerCase().equals(get("command-home"))) {
			if (!(sender instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				this.alert(sender, this.get("command-home-usage"));
				return true;
			}
			if (this.homeDB.containsKey(sender.getName().toLowerCase())) {
				HomePosition home = this.getHomePositionByName(player, args[0]);
				if (home != null) {
					player.teleport(home.toPosition());
					this.message(sender, this.get("message-home-success"));
					return true;
				} else {
					this.alert(sender, this.get("command-home-failed"));
					this.alert(sender, this.get("command-home-failed-not-found"));
					return true;
				}
			} else {
				this.alert(sender, this.get("command-home-failed"));
				this.alert(sender, this.get("command-home-failed-not-found"));
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDisable() {
		this.save();
		this.getLogger().info("SimpleHome is disabled");
	}

	@SuppressWarnings("unchecked")
	public boolean setHome(final HomePosition position, CommandSender sender) {
		if (!this.homeDB.containsKey(sender.getName().toLowerCase()))
			homeDB.put(sender.getName().toLowerCase(), Lists.newArrayList());
		((List<HomePosition>) homeDB.get(sender.getName().toLowerCase())).add(position);
		this.save();
		return true;
	}

	public boolean delHome(String name, Player player) {
		HomePosition position = this.getHomePositionByName(player, name);
		if (position != null) {
			this.getPlayerHomePositions(player).remove(position);
			this.save();
			return true;
		} else {
			this.alert(player, this.get("message-delhome-failed"));
			this.alert(player, "reason : " + this.get("message-delhome-failed-reason-not-found"));
			return false;
		}
	}

	public HomePosition getHomePositionByName(Player player, String name) {
		Iterator<HomePosition> iterator = getPlayerHomePositions(player).iterator();
		while (iterator.hasNext()) {
			HomePosition position = iterator.next();
			if (position.getName().equals(name)) {
				return position;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<HomePosition> getPlayerHomePositions(Player player) {
		List<HomePosition> positions = (List<HomePosition>) homeDB.get(player.getName());
		return (List<HomePosition>) (positions != null ? positions : Lists.newArrayList());
	}

	/* ---------------------------BasicMethods--------------------------- */
	public void initMessage() {
		saveResource("messages.yml");
		this.messages = new Config(getDataFolder() + "/messages.yml", Config.YAML);
	}

	public void updateMessage() {
		if (messages.get("m_version", 1) < m_version) {
			saveResource("messages.yml", true);
			messages = new Config(getDataFolder() + "/messages.yml");
		}
	}

	public void initDB() {
		this.homeDB = (LinkedHashMap<String, Object>) (new Config(getDataFolder() + "/homeDB.json", Config.JSON))
				.getAll();
	}

	public void registerCommands() {
		registerCommand(get("command-home"), get("command-home-descript"), get("command-home-usage"),
				"simplehome.command.home");
		registerCommand(get("command-sethome"), get("command-sethome-descript"), get("command-sethome-usage"),
				"simplehome.command.sethome");
		registerCommand(get("command-delhome"), get("command-delhome-descript"), get("command-delhome-usage"),
				"simplehome.command.delhome");
		registerCommand(get("command-homelist"), get("command-homelist-descript"), get("command-homelist-usage"),
				"simplehome.command.homelist");
	}

	public void registerCommand(String name, String descript, String usage, String permission) {
		SimpleCommandMap commandMap = getServer().getCommandMap();
		PluginCommand<SimpleHome> command = new PluginCommand<SimpleHome>(name, this);
		command.setDescription(descript);
		command.setUsage(usage);
		command.setPermission(permission);
		commandMap.register(name, command);
	}

	public void save() {
		Config save = new Config(this.getDataFolder() + "/homeDB.json", Config.JSON);
		save.setAll(this.homeDB);
		save.save();
	}

	public String get(String key) {
		return messages.get(messages.get("default-language", "eng") + "-" + key, "default-value");
	}

	public void alert(CommandSender player, String message) {
		player.sendMessage(TextFormat.RED + get("default-prefix") + " " + message);
	}

	public void message(CommandSender player, String message) {
		player.sendMessage(TextFormat.BLUE + get("default-prefix") + " " + message);
	}
}
