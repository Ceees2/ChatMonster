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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class BookListener implements Listener{
    private ChatMonster pl;
    private ChatListener cl;
    private boolean censor,advertising;
    
    public BookListener(ChatMonster plugin,ChatListener chat){
        pl = plugin;
        cl = chat;
        updateValues();
    }
    final void updateValues(){
        pl.config=pl.getConfig();
        censor = pl.config.getBoolean("censor.enabled.books");
        advertising = pl.config.getBoolean("advertising.enabled.books");
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    private void onBookEdit(PlayerEditBookEvent e){
        if (!cl.enabled)
            return;
          String name = e.getPlayer().getName();
          if (!cl.log.contains(name + ".warnings"))
            cl.log.set(name + ".warnings", 0);
          if (!cl.log.contains(name + ".second-offense"))
            cl.log.set(name + ".second-offense", false);
          cl.utils.saveLog();
          cl.utils.reloadLog();
        if(!e.isCancelled()){
        if(censor&&!e.getPlayer().hasPermission("chatmonster.bypass.censor"))
           e=censor(e);
        if(advertising&&!e.getPlayer().hasPermission("chatmonster.bypass.ad"))
           e=advertising(e);
        }
        
    }
    private PlayerEditBookEvent censor(PlayerEditBookEvent e){
        Player p = e.getPlayer();
        String name =p.getName();
        BookMeta info = e.getNewBookMeta();
        List<String> pages = info.getPages();
        boolean found=false;
        
        
        for(int x=0;x<pages.size();x++)
            for(int y=0;y<cl.findCensor.size();y++){
                String findx = msgList.get(x).toLowerCase().replaceAll("[!@#\\$%\\^&\\*\\(\\)\\-_=\\+\"':;\\?/>\\.<,~`\\|]", "");
                String findy = findCensor.get(y).toLowerCase();
                if (findx.contains(findy))
                {
                    found=true;
                  if (toCensor.equalsIgnoreCase("false"))
                  {
                    msgList.remove(x);
                    x--;
                    if (msgList.isEmpty())
                    {
                        c.setCancelled(true);
                        return c;
                    }
                    else {
                      String newMsg = "";
                      for (String s : msgList)
                        newMsg+=s + " ";

                      c.setMessage(newMsg);
                      msgList = new ArrayList();msgList.addAll(Arrays.asList(newMsg.split(" ")));
                    }
                  }
                  else {
                    msgList.set(x, toCensor);
                    String message = "";
                    for (String s : msgList)
                      message+= s + " ";
                    c.setMessage(message.trim());
                    msgList = new ArrayList();msgList.addAll(Arrays.asList(c.getMessage().split(" ")));
                  }  
                }
            }
        return e;
    }
    private PlayerEditBookEvent advertising(PlayerEditBookEvent e){
        
        return e;
    }
}
