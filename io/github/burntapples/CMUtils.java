/*  
*   <ChatMonster, here to gobble up all of your unwanted chat.>
*   Copyright (C) 2013  Zach Bryant
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see http://www.gnu.org/licenses/.
*/
package io.github.burntapples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author burnt_apples
 */
public class CMUtils implements CommandExecutor {
    protected ChatListener cl;
    protected ChatMonster plugin;
    protected boolean suppOut;
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
            if(args.length>=1){
                if(args[0].equalsIgnoreCase("toggle")&&(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.toggle"))){
                    cl.enabled = !cl.enabled;
                    cl.config.set("chatmonster-enabled", cl.enabled);
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    cl.config=plugin.getConfig();
                    cl.getCMValues();
                    String res;
                    if(cl.enabled)
                        res = "enabled.";
                    else
                        res =ChatColor.RED+"disabled.";
                    sender.sendMessage(ChatColor.GREEN+"ChatMonster has been "+res);
                    return true;
                }
                if(args.length>0 && args[0].equalsIgnoreCase("reload")||args[0].equalsIgnoreCase("r")){
                    if(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.reload")){
                        reloadLog();
                        saveLog();
                        plugin.reloadConfig();
                        plugin.saveConfig();
                        cl.config=plugin.getConfig();
                        cl.getCMValues();
                        if(!suppOut)
                        sender.sendMessage(ChatColor.GREEN+"ChatMonster files were successfully refreshed!");
                        return true;
                    }
                    else{plugin.sendNoPerms(sender); return true;}
                    
                }
            }
            if(!cl.enabled){
                sender.sendMessage(ChatColor.GREEN+"ChatMonster has been "+ChatColor.RED+ "disabled "+ChatColor.GREEN+ "through the config.");
                return true;
            }
            if(args.length>=2 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("checkw") || args[0].equalsIgnoreCase("warns")) && args[1]!=null)
                if(args[1].equalsIgnoreCase(sender.getName())){
                    sender.sendMessage(ChatColor.GREEN+"Warnings: "+ChatColor.WHITE+cl.log.get(args[1]+".warnings"));
                    sender.sendMessage(ChatColor.GREEN+"Second Offense: "+ChatColor.WHITE+cl.log.get(args[1]+".second-offense"));
                    return true;
                }
            boolean permcheck =(sender instanceof ConsoleCommandSender || (player.hasPermission("chatmonster.update")||player.hasPermission("chatmonster.clearwarnings") || player.hasPermission("chatmonster.check") || player.hasPermission("chatmonster.reload") || player.hasPermission("chatmonster.add") || player.hasPermission("chatmonster.*") ||player.hasPermission("chatmonster.warn") ||player.hasPermission("chatmonster.togglestate") ||player.hasPermission("chatmonster.configure") ||player.hasPermission("chatmonster.alias") ));
            if(!permcheck){
                plugin.sendNoPerms(sender);
                return true;
            }
            if(args.length==0 && permcheck){
                plugin.displayHelp(sender,1);
                return true;
            }
            if(args.length==1){
                if(permcheck&& args[0].equalsIgnoreCase("help")){
                    plugin.displayHelp(sender,1);
                    return true;
                }
                if(args[0].equalsIgnoreCase("update") &&(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.update"))){
                    if(!cl.config.getBoolean("auto-update.download")){
                        sender.sendMessage(ChatColor.RED+"Updating has been disabled through the config.");
                        return true;
                    }
                    plugin.updateCheck();
                    if(plugin.updater.getResult()== Updater.UpdateResult.NO_UPDATE){
                        sender.sendMessage(ChatColor.GREEN+"There is no update ready.");
                        return true;
                    }
                    sender.sendMessage(ChatColor.GREEN+plugin.getUpdateName()+" downloading...");
                    plugin.update();
                    if(plugin.updater.getResult()== Updater.UpdateResult.SUCCESS)
                        sender.sendMessage(ChatColor.GREEN+"Done! Restart your server to complete the update.");
                    else
                        sender.sendMessage(ChatColor.RED+"There was a problem while updating ChatMonster.");
                    return true;
                }
            }
            if(permcheck&& args[0].equalsIgnoreCase("help") && args[1]!=null){
                try{
                    plugin.displayHelp(sender,Integer.parseInt(args[1]));
                }
                catch(Exception e){sender.sendMessage(ChatColor.RED+"Page number must be numeric");}
                return true;
            }
            if(args.length<2 && permcheck){
                plugin.sendWrongSyntax(sender);
                return true;
            }
            if((args[0].equalsIgnoreCase("parse")||args[0].equalsIgnoreCase("strict")||args[0].equalsIgnoreCase("sp")||args[0].equalsIgnoreCase("parsetoggle") && args[1]!=null) ){
                if(sender instanceof ConsoleCommandSender || player.hasPermission("chatmonster.parsetoggle")){
                    boolean state = cl.log.getBoolean(args[1]+".parseAll");
                    cl.log.set(args[1]+".parseAll",!state);
                    saveLog();
                    reloadLog();
                    String res;
                    if(state)
                        res="strict";
                    else
                        res="lax";
                    sender.sendMessage(ChatColor.GREEN+"Successfully set "+args[1]+"'s parse state to "+res);
                    return true;
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
            if((args[0].equalsIgnoreCase("conf")||args[0].equalsIgnoreCase("config")||args[0].equalsIgnoreCase("configure")||args[0].equalsIgnoreCase("setval")) && args[1]!=null){
                if(args.length==2){
                    if(sender instanceof ConsoleCommandSender || sender.hasPermission("chatmonster.configure")){
                        sender.sendMessage(iGConf(args[1],null));
                        return true;
                    }
                    else{
                        plugin.sendNoPerms(sender);
                        return true;
                    }
                }
                if(args[2]!=null){
                    if(sender instanceof ConsoleCommandSender || sender.hasPermission("chatmonster.configure")){
                        String go = "";
                        for(int x=2;x<args.length;x++)
                            go+=args[x]+" ";
                        sender.sendMessage(iGConf(args[1],go.trim()));
                        return true;
                    }
                    else{
                        plugin.sendNoPerms(sender);
                        return true;
                    }
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
    protected String beginCaps(String s){
        String[] msg=s.split(" ");
        int count =0;
        for(int x=1;x<msg.length;x++){
            String temp=msg[x].substring(1,msg[x].length());
            if(Character.isUpperCase(msg[x].charAt(0))&& !(StringUtils.isAllUpperCase(temp)) ){
                count++;
            }
        }
        if(count>2){
            String temp = msg[0];
            for(int x=1;x<msg.length;x++){
                temp+=(" "+StringUtils.uncapitalize(msg[x]));
            }
            return temp;
        }
        return s;
    }
    protected void end(){
        Player[] players = plugin.getServer().getOnlinePlayers();
        for(int x=0;x<players.length;x++){
            cl.log.set(players[x].getName()+".time",cl.expected);
        }
    }
    protected String iGConf(String where, String what)
    {
        boolean toBool = false;
        int toInt =0;
        double toDoub = 0.0;
        long toLong = 0;  
        String done;
        if(cl.config.contains(where))
        {
            if(what==null){
                return (ChatColor.GREEN+where+" contains "+cl.config.get(where));
            }
            done =(ChatColor.GREEN+"Successfully set "+where+" to "+what);
            List<String> list;
            if(where.equalsIgnoreCase("censor.block") || where.equalsIgnoreCase("advertising.whitelisted")|| where.equalsIgnoreCase("advertising.blacklisted")){
                list=cl.config.getStringList(where);
                list.add((String)what);
                cl.config.set(where,list);
                plugin.saveConfig();
                plugin.reloadConfig();
                cl.config=plugin.getConfig();
                cl.getCMValues();
                plugin.sign.updateValues();
                return done;
            }
            else{
                if(cl.config.isString(where)){
                    try{
                        cl.config.set(where,what);
                    }
                    catch(Exception e){return (ChatColor.RED+"Could not parse "+what);}
                }
                if(cl.config.isBoolean(where)){
                    try{
                        toBool=Boolean.parseBoolean(what);
                        cl.config.set(where,toBool);
                    }
                    catch(Exception e){return (ChatColor.RED+"You must enter a boolean value (true/false).");}
                }
                if(cl.config.isInt(where)){
                    try{
                        toInt=Integer.parseInt(what);
                        cl.config.set(where,toInt);
                    }
                    catch(Exception e){return (ChatColor.RED+"You must enter a number.");}
                }
                if(cl.config.isDouble(where)){
                    try{
                        toDoub=Double.parseDouble(what);
                        cl.config.set(where,toDoub);
                    }
                    catch(Exception e){return (ChatColor.RED+"You must enter a decimal.");}
                }
                if(cl.config.isLong(where)){
                    try{
                        toLong=Long.parseLong(what);
                        cl.config.set(where,toLong);
                    }
                    catch(Exception e){return (ChatColor.RED+"You must enter a number.");}
                }
            }
            plugin.saveConfig();
            plugin.reloadConfig();
            cl.config=plugin.getConfig();
            cl.getCMValues();
            plugin.sign.updateValues();
            return done;
        }
        else{
            Set<String> keys = cl.config.getKeys(true);
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(keys);
            String send ="";
            for(int x=1;x<list.size();x++)
            {
                if(x==1)
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
                BukkitTask task= new CMDispatcher(secArgs).runTask(plugin);
                if(suppOut)
                    plugin.getLogger().log(Level.INFO, "ChatMonster issued the command: {0}", secArgs);
                cl.log.set(playernm+".warnings", 0);
                saveLog();
                reloadLog();
            }
            else
            {
                BukkitTask task= new CMDispatcher(onLimitArgs).runTask(plugin);
                if(suppOut)
                    plugin.getLogger().log(Level.INFO, "ChatMonster issued the command: {0}", onLimitArgs);
                cl.log.set(playernm+".warnings", 0);
                cl.log.set(playernm+".second-offense", true);
                saveLog();
                reloadLog();
            }

        }
        
    }
    
    protected final ArrayList<Character> refine(char[] msg)
    {
        ArrayList<Character> refined = new ArrayList<Character>();
        for(int x=0;x<msg.length;x++)
        {
            char temp = msg[x];
            if(Character.isLetter(temp))
                refined.add(Character.toLowerCase(temp));
            if(Character.isDigit(temp))
                refined.add(temp);
        }
        return refined; 
    }
    
    protected final boolean findIfCaps(String msg)
    {
        char[] temp=msg.replaceAll("[\\W]","").toCharArray();
        double count =0;
        for(int x=0;x<temp.length;x++)
            if(Character.isUpperCase(temp[x]))
                count++;
        double percent = count/(double)(temp.length);
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
            plugin.getLogger().log(Level.SEVERE, "Could not save log");
            return;

        }
        try
        {
            getLog().save(cl.logFile);
        } 
        catch (IOException ex) 
        {
            plugin.getLogger().log(Level.SEVERE, "Could not save log \n{0}",ex);
        }
    }
}
