package io.github.burntapples;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
/**
 * @author burnt_apples
 */
public class ChatMonster extends JavaPlugin{
    /**
     * @TODO advertising, CM player class for all vars
     */
    protected FileConfiguration config;
    protected File logFile = new File(getDataFolder()+File.separator+"log.yml");
    protected YamlConfiguration log;
    protected CMUtils utils;
    protected ChatListener listener;
    
    @Override
    public void onEnable()
    {
        if(!new File(getDataFolder(), "config.yml").exists())
        {
            saveDefaultConfig();
	}
	reloadConfig();
        File cmlog = new File(getDataFolder()+File.separator+"log.yml");
            if(!cmlog.exists())
            {
                getDataFolder().mkdir();
                try{
                    cmlog.createNewFile();
                }
                catch(Exception ioe){ioe.printStackTrace();}

            }
        config = getConfig();
        if(config.getBoolean("chatmonster-enabled"))
        {
            listener=new ChatListener(this);
            utils=listener.getUtils();
            getServer().getPluginManager().registerEvents(listener, this);
            //log = YamlConfiguration.loadConfiguration(logFile);
        }
        else
        {
            getLogger().info("Chatmonster has been disabled through the config.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable()
    {
        saveConfig();
        utils.end();
        HandlerList.unregisterAll(this);
    }
    
    protected void displayHelp(CommandSender sender, int page)
    {
        sender.sendMessage("-=-=-=-=-=-=-=-___"+ChatColor.GREEN+"ChatMonster Help"+ChatColor.WHITE+"___-=-=-=-=-=-=-=-");
        sender.sendMessage(ChatColor.WHITE+"                        ["+ChatColor.GREEN+"optional"+ChatColor.WHITE+"]  <"+ChatColor.GREEN+"required"+ChatColor.WHITE+">");
        if(page<=0 || page >2){
            sender.sendMessage(ChatColor.GREEN+"Woops! Looks like something went wrong. If ChatMonster bit you, please contact your local server adminsitrator for possible treatment.");
        }
        if(page==1){
            sender.sendMessage(ChatColor.GREEN+"/cm [help] [page]"+ChatColor.WHITE+"takes you to this help page.");
            sender.sendMessage(ChatColor.GREEN+"/cm cw <player> "+ChatColor.WHITE+"clears the warnings of a specified player.");
            sender.sendMessage(ChatColor.GREEN+"/cm check <player> "+ChatColor.WHITE+"shows how many warnings a specified player has.");
            sender.sendMessage(ChatColor.GREEN+"/cm add <player> <number> <reason> [punishment]"+ChatColor.WHITE+" add warnings to a player's record.");
        }
        if(page==2){
            sender.sendMessage(ChatColor.GREEN+"/cm bypass <player>"+ChatColor.WHITE+" toggles parse-all feature if the player bypasses the censor.");
            sender.sendMessage(ChatColor.GREEN+"/cm reload"+ChatColor.WHITE+" reloads all ChatMonster files.");
            sender.sendMessage(ChatColor.GREEN+"/cm toggle"+ChatColor.WHITE+" toggles ChatMonster on/off.");
            sender.sendMessage(ChatColor.GREEN+"/cm conf <path> <value>"+ChatColor.WHITE+" configure CM in game.");
            sender.sendMessage(ChatColor.GREEN+"/cm alias <cmd>"+ChatColor.WHITE+" list aliases for a command.");
        }
        sender.sendMessage(ChatColor.GREEN+"Page "+ChatColor.WHITE+page+ChatColor.GREEN+" of "+ChatColor.WHITE+"2");
    }
    
    protected void sendWrongSyntax(CommandSender sender)
    {
        sender.sendMessage(ChatColor.RED+"Unkown command.Try "+ChatColor.GREEN+"/cm help"+ChatColor.RED+" for more info.");
    }
    
    protected void sendNoPerms(CommandSender sender)
    {
        sender.sendMessage(ChatColor.RED+"You do not have sufficient permissions to use ChatMonster.");
    }
    
    public static void main(String[] args) {
    }
}
