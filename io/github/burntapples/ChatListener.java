package io.github.burntapples;
/**
 * @author burnt_apples
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
public class ChatListener implements Listener {
    public ChatMonster plugin;
    public File configFile;
    public FileConfiguration config;
    public File logFile;
    public YamlConfiguration log;
    public boolean whitelist;
    public boolean adreplace;
    public boolean blacklist;
    public boolean adParseAll;
    public boolean adWarn;
    public int adLimit;
    public boolean censor;
    public boolean censorParseAll;
    public boolean censorWarn;
    public boolean eatspam;
    public boolean spamWarn;
    public boolean blockCaps;
    public boolean enabled;
    public boolean bes;
    public ArrayList<String> whitelisted;
    public ArrayList<String> blacklisted;
    public ArrayList<String> findCensor;
    public int frequency;
    public int warnings;
    public String toCensor;
    
    CMUtils utils;
    
    //TODO censor doesnt detect things such as test1 when finding test
    //TODO if no permissions, wrong syntax is sent. 
    //TODO find bugs
    public ChatListener(ChatMonster pl)
    {
        plugin = pl;
        config = plugin.getConfig();
        logFile = new File(plugin.getDataFolder()+File.separator+"log.yml");
        log = YamlConfiguration.loadConfiguration(logFile);
        getCMValues(config);
        utils= new CMUtils(this,pl);
        plugin.getCommand("cm").setExecutor(utils);
        plugin.getCommand("cm clearwarnings").setExecutor(utils);
        plugin.getCommand("cm check").setExecutor(utils);
        plugin.getCommand("cm warn").setExecutor(utils);
        plugin.getCommand("cm reload").setExecutor(utils);
        plugin.getCommand("cm parse").setExecutor(utils);
        plugin.getCommand("cm toggle").setExecutor(utils);
        plugin.getCommand("cm conf").setExecutor(utils);
        plugin.getCommand("cm alias").setExecutor(utils);
        plugin.getCommand("cm help").setExecutor(utils);
    }
    
    public final void getCMValues(FileConfiguration conf)
    {
        whitelist = config.getBoolean("advertising.whitelist");
        adreplace = config.getBoolean("advertising.replace");
        blacklist = config.getBoolean("advertising.blacklist");
        adParseAll = config.getBoolean("advertising.parse-all");
        censor = config.getBoolean("censor.enabled");
        censorWarn = config.getBoolean("censor.warn");
        censorParseAll = config.getBoolean("censor.parse-all");
        eatspam = config.getBoolean("eatspam.enabled");
        spamWarn = config.getBoolean("eatspam.use-warnings");
        whitelisted = (ArrayList<String>)config.getList("advertising.whitelisted");
        blacklisted = (ArrayList<String>)config.getList("advertising.blacklisted");
        frequency = config.getInt("eatspam.frequency");
        warnings = config.getInt("eatspam.warnings");
        adLimit = config.getInt("advertising.limit");
        adWarn = config.getBoolean("advertising.warn");
        blockCaps = config.getBoolean("eatspam.block-caps");
        toCensor = config.getString("censor.replace");
        findCensor = (ArrayList<String>)config.getStringList("censor.block");
        enabled = config.getBoolean("chatmonster-enabled");
        bes = config.getBoolean("eatspam.block-excessive-symbols");
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent chat)
    {
        if(!enabled)
            return;
        String name = chat.getPlayer().getName();
        if(!log.contains(name+".warnings"))
            log.set(name+".warnings", 0);
        if(!log.contains(name+".second-offense"))
            log.set(name+".second-offense", false);
        if(!log.contains(name+".last"))
            log.set(name+".last", chat.getMessage());
        if(!log.contains(name+".parseAll"))
            log.set(name+".parseAll",false);
        if(!log.contains(name+".time"))
            log.set(name+".time",System.currentTimeMillis());
        this.saveLog();
        this.reloadLog();

        if(!chat.isCancelled())
        {
            /*if((whitelist || blacklist) && !(whitelist && blacklist) && !chat.getPlayer().hasPermission("chatmonster.bypass.ad"))
            {
                chat=findAd(chat);
            }*/
            if (!chat.isCancelled() && censor && !chat.getPlayer().hasPermission("chatmonster.bypass.censor"))
            {
                chat=findCensor(chat);
            }
            if(!chat.isCancelled() && eatspam && !chat.getPlayer().hasPermission("chatmonster.bypass.spam"))
            {
                chat=findSpam(chat,log.getLong(name+".time"));
            }
            if(!chat.isCancelled()){
                log.set(name+".last", chat.getMessage());
                log.set(name+".time", System.currentTimeMillis());
            }
            this.saveLog();
            this.reloadLog();
        }
    }
    
    public final void reloadLog() 
    {
        if (logFile == null) 
        {
            logFile = new File(plugin.getDataFolder(), "log.yml");
        }
        log = YamlConfiguration.loadConfiguration(logFile);

        // Look for defaults in the jar
        InputStream logStream = plugin.getResource("log.yml");
        if (logStream != null) 
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(logStream);
            log.setDefaults(defConfig);
        }
    }
    
    public final FileConfiguration getLog() 
    {
        if (log == null)
        {
            this.reloadLog();
        }
        return log;
    }
    
    public final void saveLog() 
    {
        if (log == null || logFile == null)
        {
            return;
        }
        try
        {
            getLog().save(logFile);
        } 
        catch (IOException ex) 
        {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + logFile, ex);
        }
    }
    
    public final AsyncPlayerChatEvent findAd(AsyncPlayerChatEvent c)
    {
        //TODO finish this
        String msg = c.getMessage();
        if(whitelist)
        {
            ArrayList<String> find = (ArrayList<String>)config.getStringList("advertising.whitelisted");
        }
        else
            if(blacklist)
            {
                ArrayList<String> find = (ArrayList<String>)config.getStringList("advertising.blacklisted");
            }
        
        return c;
    }
    
    public final AsyncPlayerChatEvent findCensor(AsyncPlayerChatEvent c)
    {
        String playernm = c.getPlayer().getName();
        Player player = c.getPlayer();
        String msg = c.getMessage();
        ArrayList<String> msgList = new ArrayList<String>(); msgList.addAll(Arrays.asList(msg.split(" ")));
        String adding = "";
        
        for(int x=0;x<msgList.size();x++)
        {
            for(int y=0;y<findCensor.size();y++)
            {
                if(log.getBoolean(playernm+".parseAll") || censorParseAll)
                {
                    String findx = msgList.get(x).toLowerCase().replaceAll("[!@#$%^&*()-_=+\"':;?\\/>.<,~`|]","");
                    String findy = findCensor.get(y).toLowerCase().replaceAll("[!@#$%^&*()-_=+\"':;?\\/>.<,~`|]","");
                    for(int a=0;a<findx.length();a++) 
                        for(int b=0;b<findy.length();b++) 
                        {
                            if(findx.charAt(a) == findy.charAt(b))
                            {
                                adding+= findx.charAt(a);
                                if((a<findx.length() && a!=findx.length()-1))
                                    a++;
                            }
                        }
                    if(adding.equalsIgnoreCase(findy))
                    {
                        if(toCensor.equalsIgnoreCase("false"))
                        {
                            msgList.remove(x);
                            if(msgList.isEmpty())
                            {   
                                c.setCancelled(true);
                                if(!censorWarn)
                                    return c;
                            }
                            else
                            {
                                String newMsg = "";
                                for(String s: msgList)
                                    newMsg+=(s+" ");

                                c.setMessage(newMsg);
                            }
                        }
                        else{
                            msgList.set(x,toCensor);
                            String message = "";
                            for(String s: msgList)
                                message+=(s+" ");
                            message=message.substring(0,message.length()-1);
                            c.setMessage(message);
                        }

                        if(censorWarn)
                            utils.warn(playernm, player, 1,"speaking wrongly.","censor");
                    }    
                }
                if(msgList.get(x).equalsIgnoreCase(findCensor.get(y)) )
                {
                    if(toCensor.equalsIgnoreCase("false"))
                    {
                        msgList.remove(x);
                        if(msgList.isEmpty())
                        {   
                            c.setCancelled(true);
                            if(!censorWarn)
                                return c;
                        }
                        else
                        {
                            String newMsg = "";
                            for(String s: msgList)
                                newMsg+=(s+" ");
                            
                            c.setMessage(newMsg);
                        }
                    }
                    else{
                        msgList.set(x,toCensor);
                        String message = "";
                        for(String s: msgList)
                            message+=(s+" ");
                        message=message.substring(0,message.length()-1);
                        c.setMessage(message);
                    }
                    
                    if(censorWarn)
                        utils.warn(playernm, player, 1,"speaking wrongly.","censor");
                }
            }
        }
        return c;
    }
    
    public final AsyncPlayerChatEvent findSpam(AsyncPlayerChatEvent c, long time)
    {
        String msg = c.getMessage();
        if(utils.findIfCaps(msg))
            msg=msg.toLowerCase();
        if(bes)
            msg=utils.chexcessive(msg);
        Player player = c.getPlayer();
        String name = player.getName();
        String[] msgList = msg.split(" ");
        String[] lastMsg = log.getString(name+".last").split(" ");
        ArrayList<String> refMsg = utils.refine(msgList);
        ArrayList<String> refLast = utils.refine(lastMsg);    
        int wordsInCmn =0;
        int big = Math.max(refMsg.size(), refLast.size());
        
        for(int x=0;x<refMsg.size();x++)
            for(int y=0;y<refLast.size();y++)
            {
                if(refMsg.get(x).equalsIgnoreCase(refLast.get(y)))
                    wordsInCmn++;
            }
        double percentage = (double)wordsInCmn/big;
        long duration = config.getInt("eatspam.duration");
        long expected = (time+duration);
        if(percentage > config.getDouble("eatspam.similarity") || System.currentTimeMillis() < expected)
        {
            if(spamWarn)
                utils.warn(name,player,1,"spamming", "eatspam");
            c.setCancelled(true);
        }
        
        return c;
    }
}