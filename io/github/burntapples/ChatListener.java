package io.github.burntapples;
/**
 * @author burnt_apples
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
public class ChatListener implements Listener {
    private ChatMonster plugin;
    protected File logFile;
    protected YamlConfiguration log;
    protected FileConfiguration config;
    protected boolean whitelist;
    protected boolean adreplace;
    protected boolean blacklist;
    protected boolean adParseAll;
    protected boolean adWarn;
    protected boolean censor;
    protected boolean censorParseAll;
    protected boolean censorWarn;
    protected boolean eatspam;
    protected boolean spamWarn;
    protected boolean blockCaps;
    protected boolean enabled;
    private boolean bes;
    protected ArrayList<String> whitelisted;
    protected ArrayList<String> blacklisted;
    protected ArrayList<String> findCensor;
    protected int frequency;
    protected int warnings;
    protected int adLimit;
    protected long expected;
    protected String toCensor;
    
    protected CMUtils utils;
    
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
    protected CMUtils getUtils(){
        return utils;
    }
    protected final void getCMValues(FileConfiguration conf)
    {
        whitelist = config.getBoolean("advertising.enabled");
        adreplace = config.getBoolean("advertising.replace");
        blacklist = config.getBoolean("advertising.blacklist");
        adParseAll = config.getBoolean("advertising.parse-all");
        censor = config.getBoolean("censor.enabled");
        censorWarn = config.getBoolean("censor.warn");
        censorParseAll = config.getBoolean("censor.parse-all");
        eatspam = config.getBoolean("eatspam.enabled");
        spamWarn = config.getBoolean("eatspam.warn");
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
        if(!log.contains(name+".parseAll"))
            log.set(name+".parseAll",false);
        if(!log.contains(name+".last"))
            log.set(name+".last", chat.getMessage());
        if(!log.contains(name+".time"))
            log.set(name+".time", System.currentTimeMillis());
        if(!log.contains(name+".failed-last"))
            log.set(name+".failed-last", false);
        utils.saveLog();
        utils.reloadLog();

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
                boolean failed=log.getBoolean(name+".failed-last");
                log.set(name+".last", chat.getMessage());
                if(!failed){
                    log.set(name+".time", System.currentTimeMillis());
                }
                else
                    log.set(name+".failed-last",false);
            }
            utils.saveLog();
            utils.reloadLog();
        }
    }
    
    private final AsyncPlayerChatEvent findAd(AsyncPlayerChatEvent c)
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
    
    private final AsyncPlayerChatEvent findCensor(AsyncPlayerChatEvent c)
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
    
    private final AsyncPlayerChatEvent findSpam(AsyncPlayerChatEvent c, long time)
    {
        String msg = c.getMessage();
        Player player = c.getPlayer();
        String name = player.getName();
        
        if(utils.findIfCaps(msg)){
            msg=msg.toLowerCase();
            c.setMessage(msg);
        }
        if(bes)
            c.setMessage(msg.replaceAll("[!?@#%^&;:></\\=~`]{3}",""));
        if(c.getMessage().length()<2){
            c.setCancelled(true);
            return c;
        }
        
        ArrayList<String> refMsg = utils.refine(msg.split(" "));
        ArrayList<String> refLast = utils.refine(log.getString(name+".last").split(" "));   
        
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
        expected = (time+duration);
        if(System.currentTimeMillis() < expected){
            c.getPlayer().sendMessage(ChatColor.RED+"You need to wait " + ((expected-System.currentTimeMillis())/1000)+ " seconds before speaking.");
            c.setCancelled(true);
            log.set(name+".failed-last", true);
            return c;
        }
        if(percentage > config.getDouble("eatspam.similarity") )
        {
            if(spamWarn)
                utils.warn(name,player,1,"spamming", "eatspam");
            c.setCancelled(true);
            log.set(name+".failed-last",true);
        }
        
        return c;
    }
}