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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener{
    private ChatMonster plugin;
    private ChatListener cl;
    protected boolean censorSigns;
    protected boolean adSigns;
    
    public SignListener(ChatMonster pl, ChatListener chatListener){
        plugin=pl;
        cl=chatListener;
        updateValues();
        
    }
    protected final void updateValues(){
        plugin.config=plugin.getConfig();
        censorSigns=plugin.config.getBoolean("censor.enabled.signs");
        adSigns=plugin.config.getBoolean("advertising.enabled.signs");
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event){
        if (!cl.enabled)
            return;
          String name = event.getPlayer().getName();
          if (!cl.log.contains(name + ".warnings"))
            cl.log.set(name + ".warnings", 0);
          if (!cl.log.contains(name + ".second-offense"))
            cl.log.set(name + ".second-offense", false);
          cl.utils.saveLog();
          cl.utils.reloadLog();
        if(!event.isCancelled()){
            if(censorSigns && !event.getPlayer().hasPermission("chatmonster.bypass.censor"))
                event=findCensor(event);
            if(adSigns && !event.getPlayer().hasPermission("chatmonster.bypass.ad")){
                event=findAd(event);
            }
        }
            
    }
    private final SignChangeEvent findCensor(SignChangeEvent e){
        String playernm = e.getPlayer().getName();
        Player player = e.getPlayer();
        String[] msg = e.getLines();
        boolean found=false;
        for (int x = 0; x < msg.length; x++)
        {
          for (int y = 0; y < cl.findCensor.size(); y++)
          {
            String findx = msg[x].toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
            String findy = cl.findCensor.get(y).toLowerCase();
            if (findx.contains(findy))
            {
                found=true;
              if (cl.toCensor.equalsIgnoreCase("false"))
                msg[x]=msg[x].replaceAll(findy,"");
              else
                msg[x]=msg[x].replaceAll(findy,cl.toCensor);

              for(int i=0;i<4;i++)
                    e.setLine(i,msg[i]);
            }
          }
        }
        if(found){
            if (cl.censorWarn)
                    cl.utils.warn(playernm, player, 1, "speaking wrongly on a sign.", "censor");
            e.getBlock().breakNaturally();
        }
        return e;
    }
    private final SignChangeEvent findAd(SignChangeEvent e){
        
        String[] crude = e.getLines();
        String[] data = new String[crude.length];
        for(int x=0;x<crude.length;x++)
            data[x]=crude[x].replaceAll("(dot|DOT|Dot|dOt|doT|DOt|dOT|DoT)", ".");
        Pattern validHostname = Pattern.compile("^(?=(?:.*?[\\.\\,]){1})(?:[a-z][a-z0-9-]*[a-z0-9](?=[\\.,][a-z]|$)[\\.,:;|\\\\]?)+$");
        Pattern validIpAddress = Pattern.compile("^(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?::\\d*)?$", 2);

        boolean found = false;
        int rand = (int)(Math.random() * cl.whitelisted.size());
        String replace = (String)cl.whitelisted.get(rand);

        for(int x=0;x<data.length;x++){
            for (int y = 0; y < cl.whitelisted.size(); y++){
                if (Pattern.compile(Pattern.quote((String)cl.whitelisted.get(y)), 2).matcher(data[x]).find()){
                    return e;
                }
            }
            String[] temp=data[x].toLowerCase().replaceAll("[\\(\\)!@#\\$%\\^\\&\\*:;\"'\\?><~`,\\\\]","").split(" ");
            for(int v=0;v<temp.length;v++){
                Matcher matchIP = validIpAddress.matcher(temp[v]);
                while (matchIP.find()) {
                  if (cl.adreplace)
                    data[x]=replace;
                  else
                    e.setCancelled(true);
                  found = true;
                }
                Matcher matchHost = validHostname.matcher(temp[v]);
                while (matchHost.find()) {
                  if (cl.adreplace)
                    data[x]=replace;
                  else
                    e.setCancelled(true);
                  found = true;
                }
                if(data[x].toLowerCase().contains("http"))
                    data[x]="";
            }
        }
        if(found){
            if (cl.adWarn){
              cl.utils.warn(e.getPlayer().getName(), e.getPlayer(), 1, "advertising on a sign.", "advertising");
              e.setCancelled(true);
            }
            e.getBlock().breakNaturally();
        }
        for(int x=0;x<data.length;x++){
            if(data[x].length()<15)
                e.setLine(x,data[x]);
            else
                e.setLine(x,data[x].substring(0,15));
        }
        return e;
    }  
}