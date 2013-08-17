package io.github.burntapples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author burnt_apples
 */
public class CMUtils implements CommandExecutor {
    protected ChatListener cl;
    protected ChatMonster plugin;
    protected boolean suppOut;
    protected File pyaml;
    public CMUtils(ChatListener cmcl, ChatMonster pl)
    {
        cl = cmcl;
        plugin = pl;
        suppOut=cl.config.getBoolean("suppress-output");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        if(sender instanceof Player)
            player = (Player)sender;
 
        if(cmd.getName().equalsIgnoreCase("cm"))
        {
            boolean permcheck =(sender instanceof ConsoleCommandSender || (player.hasPermission("chatmonster.clearwarnings") || player.hasPermission("chatmonster.check") || player.hasPermission("chatmonster.reload") || player.hasPermission("chatmonster.add") || player.hasPermission("chatmonster.*") ||player.hasPermission("chatmonster.warn") ||player.hasPermission("chatmonster.togglestate") ||player.hasPermission("chatmonster.configure") ||player.hasPermission("chatmonster.alias") ));
            if(!permcheck){
                plugin.sendNoPerms(sender);
                return true;
            }
            if(args.length==0 && permcheck){
                plugin.displayHelp(sender,1);
                return true;
            }
            if(permcheck&& args[0].equalsIgnoreCase("help") && args.length==1){
                plugin.displayHelp(sender,1);
                return true;
            }
            if(permcheck&& args[0].equalsIgnoreCase("help") && args[1]!=null){
                try{
                plugin.displayHelp(sender,Integer.parseInt(args[1]));
                }
                catch(Exception e){sender.sendMessage(ChatColor.RED+"Page number must be numeric");}
                return true;
            }
            if(args[0].equalsIgnoreCase("toggle")){
                cl.enabled = !cl.enabled;
                cl.config.set("chatmonster-enabled", cl.enabled);
                plugin.saveConfig();
                String res ="";
                if(cl.enabled)
                    res = "enabled.";
                else
                    res ="disabled.";
                sender.sendMessage(ChatColor.GREEN+"ChatMonster has been "+res);
                return true;
            }
            if(args[0].equalsIgnoreCase("reload")){
                if(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.reload")){
                    reloadLog();
                    plugin.reloadConfig();
                    cl.getCMValues(cl.config);
                    sender.sendMessage(ChatColor.GREEN+"ChatMonster files were successfully refreshed!");
                    return true;
                }
                else{plugin.sendNoPerms(sender); return true;}
            }
            if(args.length<2 && permcheck){
                plugin.sendWrongSyntax(sender);
                return true;
            }
            if((args[0].equalsIgnoreCase("parse")||args[0].equalsIgnoreCase("strict")||args[0].equalsIgnoreCase("sp")||args[0].equalsIgnoreCase("parsetoggle") && args[1]!=null) ){
                if(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.parsetoggle")){
                    boolean state = cl.log.getBoolean(args[1]+".parseAll");
                    cl.log.set(args[1]+".parseAll",!state);
                    String res = "";
                    if(state)
                        res="strict";
                    else
                        res="lax";
                    sender.sendMessage(ChatColor.GREEN+"Successfully set "+args[1]+"'s parse state to "+res);
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase("alias")){
                if(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.alias")){
                    sender.sendMessage(ChatColor.RED+"This function has not been finished. Coming soon.");
                }
            }
            if((args[0].equalsIgnoreCase("cw") || args[0].equalsIgnoreCase("clearw") || args[0].equalsIgnoreCase("clearwarnings")) && args[1] !=null){
                if(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.clearwarnings")){
                    if(cl.log.contains(args[1]+".warnings")){
                        cl.log.set(args[1]+".warnings",0);
                        sender.sendMessage(ChatColor.GREEN+"Successfully set " + args[1]+"'s warnings to 0");
                        return true;
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"ChatMonster was unable to find "+ChatColor.GREEN+args[1]+ChatColor.RED+" in the log.");
                        return true;
                    }
                }
                else{plugin.sendNoPerms(sender); return true;}
            }
            if(((args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("checkw") || args[0].equalsIgnoreCase("warns")) && args[1]!=null)){
                if(sender instanceof ConsoleCommandSender || sender.hasPermission("chatmonster.check")){
                    if(cl.log.contains(args[1]+".warnings") && cl.log.contains(args[1]+".second-offense")){
                        sender.sendMessage(ChatColor.GREEN+"Warnings: "+ChatColor.WHITE+cl.log.get(args[1]+".warnings"));
                        sender.sendMessage(ChatColor.GREEN+"Second Offense: "+ChatColor.WHITE+cl.log.get(args[1]+".second-offense"));
                        return true;
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"ChatMonster was unable to find "+ChatColor.GREEN+args[1]+ChatColor.RED+" in the log.");
                    }
                }
                else{plugin.sendNoPerms(sender); return true;}
            }
            if(args.length<3){
                plugin.sendWrongSyntax(sender);
                return true;
            }
            if((args[0].equalsIgnoreCase("conf")||args[0].equalsIgnoreCase("config")||args[0].equalsIgnoreCase("configure")||args[0].equalsIgnoreCase("setval")) && args[1]!=null&&args[2] !=null){
                if(sender instanceof ConsoleCommandSender || sender.hasPermission("chatmonster.configure")){
                    sender.sendMessage(iGConf(args[1],args[2]));
                    return true;
                }
            }
            if(args.length<4){
                plugin.sendWrongSyntax(sender);
                return true;
            }
            if(args[3] !=null && args[2] != null && args[1] != null && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("warn") || args[0].equalsIgnoreCase("addwarn") || args[0].equalsIgnoreCase("addwarning") || args[0].equalsIgnoreCase("a")) ){
                if(sender instanceof ConsoleCommandSender || sender.hasPermission("chatmonster.add")){
                    if(cl.log.contains(args[1]+".warnings") && cl.log.contains(args[1]+".second-offense"))
                    {
                        String r = "";
                        int theSize=0;
                        if(args[4]!=null)
                            theSize= args.length-1;
                        else
                            theSize = args.length;
                        for(int x =3;x<theSize;x++)
                            r+=(args[x]+" ");
                        r=r.substring(0,r.length()-1);
                        try{
                            warn(args[1], Bukkit.getServer().getPlayer(args[1]), Integer.parseInt(args[2]),r, "censor");
                        }
                        catch(Exception e){sender.sendMessage(ChatColor.RED+"Warning points must be numeric"); return true;}
                        sender.sendMessage(ChatColor.GREEN+"Added "+args[2]+" warnings to "+args[1]+"'s record for "+ r);
                        return true;
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"ChatMonster was unable to find "+ChatColor.GREEN+args[1]+ChatColor.RED+" in the log.");
                        return true;
                    }
                }
                else{plugin.sendNoPerms(sender); return true;}
            }
            else{plugin.sendWrongSyntax(sender); return true;}
        }
        return true;
    }
    protected void end(){
        List<Player> players = Arrays.asList(plugin.getServer().getOnlinePlayers());
        for(int x=0;x<players.size();x++){
            cl.log.set(players.get(x).getName()+".time",cl.expected);
        }
    }
    protected String iGConf(String where, String what)
    {
        if(cl.config.contains(where))
        {
            ArrayList<String> list;
            if(where.equalsIgnoreCase("censor.block") || where.equalsIgnoreCase("advertising.whitelisted")|| where.equalsIgnoreCase("advertising.blacklisted")){
                list=(ArrayList<String>)cl.config.getList(where);
                list.add(what);
                cl.config.set(where,list);
            }
            else
                cl.config.set(where,what);
            plugin.saveConfig();
            plugin.reloadConfig();
            cl.getCMValues(cl.config);
            return (ChatColor.GREEN+"Successfully set "+where+" to "+what);
        }
        else{
            Set<String> keys = cl.config.getKeys(true);
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(keys);
            String send ="";
            for(int x=0;x<list.size();x++)
            {
                if(x==0)
                    send+=ChatColor.WHITE+"/";
                send+=ChatColor.GREEN+list.get(x)+ChatColor.WHITE+"/";
            }
            return send;
        }
        
    }
    protected void warn(String playernm, Player play, int amt, String reason,String who)
    {
        who = who.toLowerCase();
        if(!(who.equals("eatspam") || who.equals("censor") || who.equals("advertising")))
            who="censor";
        if(Bukkit.getServer().getPlayer(playernm) !=null)
            play.sendMessage(ChatColor.RED+"You have gained "+amt+" warning(s) from ChatMonster for "+reason);
        if(suppOut)
            plugin.getLogger().log(Level.INFO, "{0} recieved "+amt+" ChatMonster warning(s).", playernm);
        int playerWarns = cl.log.getInt(playernm+".warnings");
        cl.log.set(playernm+".warnings", (playerWarns+amt));
        saveLog();
        reloadLog();
        playerWarns=cl.log.getInt(playernm+".warnings");
        boolean second = cl.log.getBoolean(playernm+".second-offense");
        int limit = cl.config.getInt(who+".limit");
        String[] onLimit = cl.config.getString(who+".on-limit.punishment").split(" ");
        String onLimitArgs ="";
        String secArgs = "";

        for(String s: onLimit)
        {
            if(s.compareTo("%player%") == 0 || s.compareTo("%PLAYER%") == 0)
                onLimitArgs+=(playernm+" ");
            else
                onLimitArgs+=(s+" ");   
        }

        if(playerWarns >= limit)
        {
            if(second)
            {
                String[] secOff = cl.config.getString(who+".on-limit.second-offense").split(" ");
                for(String s: secOff)
                {
                    if(s.compareTo("%player%") == 0 || s.compareTo("%PLAYER%") == 0)
                        secArgs+=(playernm+" ");
                    else
                        secArgs+=(s+" "); 
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), secArgs);
                if(suppOut)
                    plugin.getLogger().log(Level.INFO, "ChatMonster issued the command: {0}", secArgs);
                cl.log.set(playernm+".warnings", 0);
                saveLog();
                reloadLog();
            }
            else
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), onLimitArgs);
                if(suppOut)
                    plugin.getLogger().log(Level.INFO, "ChatMonster issued the command: {0}", onLimitArgs);
                cl.log.set(playernm+".warnings", 0);
                cl.log.set(playernm+".second-offense", true);
                saveLog();
                reloadLog();
            }

        }
        
    }
    
    protected final ArrayList<String> refine(String[] msg)
    {
        ArrayList<String> refined = new ArrayList<String>();
        for(int x=0;x<msg.length;x++)
        {
            String temp = msg[x];
            temp =temp.replaceAll("\\W","");
            refined.add(temp);
        }
        return refined; 
    }
    
    protected final boolean findIfCaps(String msg)
    {
        msg=msg.trim();
        int orig=msg.length();
        if(orig<1)
            return false;
        char[] temp=msg.replaceAll("[\\W\\s]","").toCharArray();
        int count =0;
        for(int x=0;x<temp.length;x++)
            if(Character.isUpperCase(temp[x]))
                count++;
        double percent = (double)count/temp.length;
        if(percent>=0.5)
            return true;
        return false;
    }
    protected final void reloadLog() 
    {
        if (cl.logFile == null) 
        {
            cl.logFile = new File(plugin.getDataFolder(), "log.yml");
        }
        cl.log = YamlConfiguration.loadConfiguration(cl.logFile);

        // Look for defaults in the jar
        InputStream logStream = plugin.getResource("log.yml");
        if (logStream != null) 
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(logStream);
            cl.log.setDefaults(defConfig);
        }
    }
    
    protected final FileConfiguration getLog() 
    {
        if (cl.log == null)
        {
            this.reloadLog();
        }
        return cl.log;
    }
    
    protected final void saveLog() 
    {
        if (cl.log == null || cl.logFile == null)
        {
            return;
        }
        try
        {
            getLog().save(cl.logFile);
        } 
        catch (IOException ex) 
        {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + cl.logFile, ex);
        }
    }
}
