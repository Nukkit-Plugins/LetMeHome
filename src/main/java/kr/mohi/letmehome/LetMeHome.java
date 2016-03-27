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
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == get("command-sethome")) {
			if (!(sender instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0 || args.length == 1) {
				alert(sender, get("command.sethome.usage"));
				return true;
			}
			Player player = getServer().getPlayer(sender.getName());
			switch (args[1].toLowerCase()) {
			case "public":
				try {
					setHome(args[0], player, true);
				} catch (Exception e) {
					getLogger().alert(e.getMessage());
				}
				break;
			case "private":
				try {
					setHome(args[0], player, false);
				} catch (Exception e) {
					getLogger().alert(e.getMessage());
				}
				break;
			}
		}
		if (command.getName().toLowerCase() == get("command-delhome")) {
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
			save(homeDB);
			
		}
		if (command.getName().toLowerCase() == get("command-homelist")) {
			sender.sendMessage(TextFormat.AQUA + getHomeList(sender, args[0]));
		}
		return false;
	}
	public String getHomeList(CommandSender sender, String args) {
		if(args == "public") {
			for((homeDB.get in )
		}
		if(args == "private") {
			
		}
		return null;
	}
	@SuppressWarnings("serial")
	public boolean setHome(String name, final Player player, Boolean isPublic) {
		if(homeDB.exists(name))
			return false;
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
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean delHome(String name, Player player) {
		if (((LinkedHashMap<String,Object>) homeDB.get(name)).get("owner") == player.getName()) {
			homeDB.remove(name);
			return true;
		}
		else return false;
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
