package kr.mohi.letmehome;

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
	private LinkedHashMap<String, Object> homeDB;
	private int m_version;
	private Config messages, config;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		this.initDB();
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender player, Command command, String label, String[] args) {
		if (command.getName().toLowerCase() == this.get("command-sethome")) {
			if (!(player instanceof Player)) {
				this.getLogger().info("Do not use this command on console");
				return true;
			}
			if (args.length == 0) {
				this.get("command.sethome.usage");
				return true;
			}
			try {
				this.setHome((Player) player, args[0], args[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void setHome(final Player player, String name, final String isPublic) {
		this.homeDB.put(player.getName(), new LinkedHashMap<String, Object>() {
			{
				put("level", player.getLevel().toString());
				put("x", (int) player.getX());
				put("y", (int) player.getY());
				put("z", (int) player.getZ());
				put("isPublic", isPublic);
			}
		});
		this.save();
	}

	public void initMessage() {
		this.saveResource("messages.yml");
		this.messages = new Config(this.getDataFolder() + "/messages.yml", Config.YAML);
	}

	public void updateMessage() {
		if (this.messages.get("m_version", 1) < this.m_version) {
			this.saveResource("messages.yml", true);
			messages = new Config(this.getDataFolder() + "/messages.yml");
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
		
	}
	public String get(String key) {
		return this.messages.get(this.messages.get("default-language", "kor") + "-" + key, "default-value");
	}

	public void alert(CommandSender player, String message) {
		player.sendMessage(TextFormat.RED + get("default-prefix") + " " + message);
	}

	public void message(CommandSender player, String message) {
		player.sendMessage(TextFormat.BLUE + get("default-prefix") + " " + message);
	}
}
