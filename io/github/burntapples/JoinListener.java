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

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener{
    private ChatMonster pl;
    public JoinListener(ChatMonster plugin){
        pl=plugin;
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void showUpdateOnJoin(PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("chatmonster.update"))
                e.getPlayer().sendMessage(ChatColor.GREEN+"ChatMonster has"+ChatColor.WHITE+"updated"+ChatColor.GREEN+"!"+pl.getLatestVersion() +" is ready to be downloaded. Type "+ChatColor.WHITE+"/update"+ ChatColor.GREEN+"to begin.");
    }
}
