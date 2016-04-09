package kr.mohi.simplehome;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getLogger().info("SimpleHome is enabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase().equals(get("command-sethome"))) {
			if (!(sender instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				alert(sender, get("command-sethome-usage"));
				return true;
			}
			this.setHome(args[0], getServer().getPlayer(sender.getName()));
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
			this.delHome(args[0], sender);
			this.save();
		}
		if (command.getName().toLowerCase().equals(get("command-homelist"))) {
			sender.sendMessage(TextFormat.AQUA + "[Home]" + this.get("message-homelist-first"));
			for (LinkedHashMap<String, Object> l : this.getHomeList(sender)) {
				sender.sendMessage("#" + this.getHomeList(sender).indexOf(l) + " " + l.get("name"));
			}
			return true;
		}
		return false;
	}

	@Override
	public void onDisable() {
		this.save();
		this.getLogger().info("SimpleHome is disabled");
	}

	@SuppressWarnings("unchecked")
	public ArrayList<LinkedHashMap<String, Object>> getHomeList(CommandSender sender) {
		return (ArrayList<LinkedHashMap<String, Object>>) homeDB.get(sender.getName().toLowerCase());
	}

	@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
	public boolean setHome(final String name, final Player player) {
		for (Object l : (ArrayList<LinkedHashMap>) homeDB.get(player.getName().toLowerCase())) {
			if (((LinkedHashMap<String, Object>) l).get("name").equals(name)) {
				this.message(player, this.get("message-sethome-failed-reason-overlapping"));
				return false;
			}
		}
		homeDB.put(player.getName().toLowerCase(), new ArrayList<LinkedHashMap>() {
			{
				add(new LinkedHashMap<String, Object>() {
					{
						put("name", name);
						put("x", player.getX());
						put("y", player.getY());
						put("z", player.getZ());
					}
				});
			}
		});
		this.save();
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean delHome(String id, CommandSender player) {
		if (((ArrayList<LinkedHashMap<String, Object>>) homeDB.get(player)).contains(id)) {
			((ArrayList<LinkedHashMap<String, Object>>) homeDB.get(player)).remove(id);
			this.save();
			return true;
		} else {
			this.alert(player, this.get("message-delhome-failed"));
			this.alert(player, "reason : " + this.get("message-delhome-failed-reason-not-found"));
			return false;
		}
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
		Config home = new Config(getDataFolder() + "/homeDB.json", Config.JSON);
		home.setAll(this.homeDB);
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
