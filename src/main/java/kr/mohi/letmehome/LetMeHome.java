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
	private Config messages, config, homeDB;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		this.initDB();
		this.initMessage();
		this.updateMessage();
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender player, Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == get("command-sethome")) {
			if (!(player instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0 || args.length == 1) {
				alert(player, get("command.sethome.usage"));
				return true;
			}
			switch (args[1].toLowerCase()) {
			case "true":
			case "on":
			case "y":
			case "yes":
				try {
					this.setHome(args[0], (Player) player, true);
				} catch (Exception e) {
					getLogger().alert(e.getMessage());
				}
				break;
			case "false":
			case "off":
			case "n":
			case "no":
				try {
					setHome(args[0], (Player) player, false);
				} catch (Exception e) {
					getLogger().alert(e.getMessage());
				}
				break;
			}
		}
		if (command.getName().toLowerCase() == get("command-delhome")) {
			if (!(player instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				alert(player, get("command.delhome.usage"));
				return true;
			}

		}
		return false;
	}

	@SuppressWarnings("serial")
	public void setHome(String name, final Player player, Boolean isPublic) {
		homeDB.set(name, new LinkedHashMap<String, Object>() {
			{
				{
					put("owner", new ArrayList<String>().add(player.getName()));
					put("level", player.getLevel().toString());
					put("x", (int) player.getX());
					put("y", (int) player.getY());
					put("z", (int) player.getZ());
					put("isPublic", isPublic);
				}
			}
		});
		save(homeDB);
	}

	@SuppressWarnings("unchecked")
	public void delHome(String name, Player player) {
		if (((ArrayList<String>) ((LinkedHashMap<String, Object>) homeDB.get(name)).get("owner"))
				.contains((player.getName()))) {

		}
	}

	/* ---------------------------BasicMethods--------------------------- */
	public void initMessage() {
		saveResource("messages.yml");
		messages = new Config(getDataFolder() + "/messages.yml", Config.YAML);
	}

	public void updateMessage() {
		if (messages.get("m_version", 1) < m_version) {
			saveResource("messages.yml", true);
			messages = new Config(getDataFolder() + "/messages.yml");
		}
	}

	public void initDB() {
		homeDB = new Config(getDataFolder() + "/homeDB.json", Config.JSON);
		config = new Config(getDataFolder() + "/config.yml", Config.YAML);
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
		save(homeDB);
		save(config);
	}

	public void save(Config config) {
		config.save();
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
