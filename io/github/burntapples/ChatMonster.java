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

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;

public class ChatMonster extends JavaPlugin{
    
    protected FileConfiguration config;
    protected CMUtils utils;
    protected ChatListener listener;
    protected SignListener sign;
    protected Updater updater;
    public static boolean update=false;
    public static String name="";
    public static long size=0;
    
    @Override
    public void onEnable()
    {
        updater= new Updater(this, "chatmonster", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
        update =updater.getResult()== Updater.UpdateResult.UPDATE_AVAILABLE;
        name=updater.getLatestVersionString();
        size=updater.getFileSize();
        
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
        
        if(config.getBoolean("auto-update.notify")&&update)
                utils.notifyAdmins();
        
        if(config.getBoolean("auto-update.download")){
            updater = new Updater(this, "chatmonster", this.getFile(), Updater.UpdateType.DEFAULT, true);
            update=false;
            size=0;
        }  
        
        listener=new ChatListener(this);
        sign=new SignListener(this, listener);
        utils=listener.getUtils();
        
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(sign, this);
        
        if(!config.getBoolean("chatmonster-enabled"))
            getLogger().info("Chatmonster has been disabled through the config.");
        
        getCommand("cm").setExecutor(utils);
        getCommand("cm clearwarnings").setExecutor(utils);
        getCommand("cm check").setExecutor(utils);
        getCommand("cm warn").setExecutor(utils);
        getCommand("cm reload").setExecutor(utils);
        getCommand("cm parse").setExecutor(utils);
        getCommand("cm toggle").setExecutor(utils);   
        getCommand("cm alias").setExecutor(utils);
        getCommand("cm help").setExecutor(utils);
        getCommand("cm update").setExecutor(utils);
    }
    
    @Override
    public void onDisable()
    {
        saveConfig();
        utils.saveLog();
        utils.end();
        HandlerList.unregisterAll(this);
    }
    
    void update(){
        updater = new Updater(this, "chatmonster", this.getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
    }
    
    public String getUpdateName(){
        return name;
    }
    
    public boolean getUpdateStatus(){
        name=updater.getLatestVersionString();
        size=updater.getFileSize();
        return updater.getResult()== Updater.UpdateResult.UPDATE_AVAILABLE;
    }
    
    public long getUpdateSize(){
        return size;
    }
    public String getLatestVersion(){
        return updater.getLatestVersionString();
    }
    
    protected void displayHelp(CommandSender sender, int page)
    {
        sender.sendMessage("-=-=-=-=-=-=-=-___"+ChatColor.GREEN+"ChatMonster Help"+ChatColor.WHITE+"___-=-=-=-=-=-=-=-");
        sender.sendMessage(ChatColor.WHITE+"                        ["+ChatColor.GREEN+"optional"+ChatColor.WHITE+"]  <"+ChatColor.GREEN+"required"+ChatColor.WHITE+">");
        if(page<=0 || page >3){
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
        }
        if(page==3){
            sender.sendMessage(ChatColor.GREEN+"/cm update"+ChatColor.WHITE+" checks for an update and updates the plugin accordingly.");
        }
        sender.sendMessage(ChatColor.GREEN+"Page "+ChatColor.WHITE+page+ChatColor.GREEN+" of "+ChatColor.WHITE+"3");
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
