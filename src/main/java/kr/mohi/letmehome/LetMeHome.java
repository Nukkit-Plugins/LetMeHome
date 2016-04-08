package kr.mohi.letmehome;

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

public class LetMeHome extends PluginBase implements Listener {
	private int m_version;
	private Config config, messages;
	private LinkedHashMap<String, Object> homeDB;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		this.initDB();
		this.initMessage();
		this.updateMessage();
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase().equals(get("command-sethome"))) {
			if (!(sender instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				alert(sender, get("command.sethome.usage"));
				return true;
			}
			Player player = getServer().getPlayer(sender.getName());
			this.setHome(args[0], player);
		}
		if (command.getName().toLowerCase().equals(get("command-delhome"))) {
			if (!(sender instanceof Player)) {
				getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				alert(sender, get("command.delhome.usage"));
				return true;
			}
			Player player = getServer().getPlayer(sender.getName());
			delHome(args[0], player);
			this.save(this.homeDB, "homeDB.json");

		}
		if (command.getName().toLowerCase().equals(get("command-homelist"))) {
			sender.sendMessage(TextFormat.AQUA + getHomeList(sender, args[0]));
		}
		return false;
	}

	public String getHomeList(CommandSender sender, String command) {
		if (command == "public") {

		}
		if (command == "private") {

		}
		return null;
	}

	@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
	public boolean setHome(final String name, final Player player) {
		for (LinkedHashMap<String, Object> l : (ArrayList<LinkedHashMap>) homeDB.get(player.getName().toLowerCase())) {
			if (l.get("name").equals(name)) {
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
		this.save(this.homeDB, "homeDB.json");
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean delHome(String name, Player player) {
		if (((LinkedHashMap<String, Object>) homeDB.get(name)).get("owner") == player.getName()) {
			homeDB.remove(name);
			return true;
		} else
			return false;
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
		this.config = new Config(getDataFolder() + "/config.yml", Config.YAML);
	}

	public void registerCommands() {

	}

	public void registerCommand(String name, String descript, String usage, String permission) {
		SimpleCommandMap commandMap = getServer().getCommandMap();
		PluginCommand<LetMeHome> command = new PluginCommand<LetMeHome>(name, this);
		command.setDescription(descript);
		command.setUsage(usage);
		command.setPermission(permission);
		commandMap.register(name, command);
	}

	public void save() {
		Config home = new Config(getDataFolder() + "/homeDB.json", Config.JSON);
		home.setAll(this.homeDB);
	}

	public void save(LinkedHashMap<String, Object> config, String fileName) {
		Config save = new Config(getDataFolder() + "/" + fileName, Config.JSON);
		save.setAll(config);
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
