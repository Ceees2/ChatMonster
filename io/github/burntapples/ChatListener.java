/*  
*   <ChatMonster, here to gobble up all of your unwanted chat.>
*   Copyright (C) <2013>  <Zach Bryant>
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
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package io.github.burntapples;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener
{
  private ChatMonster plugin;
  protected File logFile;
  protected YamlConfiguration log;
  protected FileConfiguration config;
  protected boolean whitelist;
  protected boolean adreplace;
  protected boolean adWarn;
  protected boolean censor;
  protected boolean censorParseAll;
  protected boolean censorWarn;
  protected boolean eatspam;
  protected boolean spamWarn;
  protected boolean blockCaps;
  protected boolean enabled;
  protected boolean noBeginCaps;
  private boolean bes;
  protected ArrayList<String> whitelisted;
  protected ArrayList<String> findCensor;
  protected int frequency;
  protected int warnings;
  protected int adLimit;
  protected long expected;
  protected String toCensor;
  protected CMUtils utils;
  
  public ChatListener(ChatMonster pl)
  {
    plugin = pl;
    config = plugin.getConfig();
    logFile = new File(plugin.getDataFolder() + File.separator + "log.yml");
    log = YamlConfiguration.loadConfiguration(logFile);
    getCMValues();
    utils = new CMUtils(this, pl);
    plugin.getCommand("cm").setExecutor(utils);
    plugin.getCommand("cm clearwarnings").setExecutor(utils);
    plugin.getCommand("cm check").setExecutor(utils);
    plugin.getCommand("cm warn").setExecutor(utils);
    plugin.getCommand("cm reload").setExecutor(utils);
    plugin.getCommand("cm parse").setExecutor(utils);
    plugin.getCommand("cm toggle").setExecutor(utils);   
    plugin.getCommand("cm alias").setExecutor(utils);
    plugin.getCommand("cm help").setExecutor(utils);
  }
  
  protected CMUtils getUtils() { return utils; }
  
  protected final void getCMValues()
  {
    whitelist = config.getBoolean("advertising.enabled");
    adreplace = config.getBoolean("advertising.replace");
    censor = config.getBoolean("censor.enabled");
    censorWarn = config.getBoolean("censor.warn");
    censorParseAll = config.getBoolean("censor.parse-all");
    eatspam = config.getBoolean("eatspam.enabled");
    spamWarn = config.getBoolean("eatspam.warn");
    whitelisted = ((ArrayList)config.getList("advertising.whitelisted"));
    frequency = config.getInt("eatspam.frequency");
    warnings = config.getInt("eatspam.warnings");
    adLimit = config.getInt("advertising.limit");
    adWarn = config.getBoolean("advertising.warn");
    blockCaps = config.getBoolean("eatspam.block-caps");
    toCensor = config.getString("censor.replace");
    findCensor = ((ArrayList)config.getStringList("censor.block"));
    enabled = config.getBoolean("chatmonster-enabled");
    bes = config.getBoolean("eatspam.block-excessive-symbols");
    noBeginCaps = config.getBoolean("eatspam.No-Begin-Caps");
  }
  
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onPlayerChat(AsyncPlayerChatEvent chat) {
    if (!enabled)
      return;
    String name = chat.getPlayer().getName();
    if (!log.contains(name + ".warnings"))
      log.set(name + ".warnings", Integer.valueOf(0));
    if (!log.contains(name + ".second-offense"))
      log.set(name + ".second-offense", Boolean.valueOf(false));
    if (!log.contains(name + ".parseAll"))
      log.set(name + ".parseAll", Boolean.valueOf(false));
    if (!log.contains(name + ".last"))
      log.set(name + ".last", chat.getMessage());
    if (!log.contains(name + ".time"))
      log.set(name + ".time", Long.valueOf(expected));
    if (!log.contains(name + ".failed-last"))
      log.set(name + ".failed-last", Boolean.valueOf(false));
    utils.saveLog();
    utils.reloadLog();
    
    if (!chat.isCancelled())
    {
      if ((whitelist) && (!chat.getPlayer().hasPermission("chatmonster.bypass.ad")))
        chat = findAd(chat);
      if ((!chat.isCancelled()) && (eatspam) && (!chat.getPlayer().hasPermission("chatmonster.bypass.spam")))
        chat = findSpam(chat, log.getLong(name + ".time"));
      if ((!chat.isCancelled()) && (censor) && (!chat.getPlayer().hasPermission("chatmonster.bypass.censor"))) {
        chat = findCensor(chat);
      }
      if (!chat.isCancelled()) {
        boolean failed = log.getBoolean(name + ".failed-last");
        log.set(name + ".last", chat.getMessage());
        if (!failed) {
          log.set(name + ".time", Long.valueOf(System.currentTimeMillis()));
        }
        else
          log.set(name + ".failed-last", Boolean.valueOf(false));
      }
      utils.saveLog();
      utils.reloadLog();
    }
  }
  


  private final AsyncPlayerChatEvent findAd(AsyncPlayerChatEvent c)
  {
    String[] msg = c.getMessage().split("[\\s/]");
    
    Pattern validHostname = Pattern.compile("^(?=(?:.*?[\\.\\,]){1})(?:[a-z][a-z0-9-]*[a-z0-9](?=[\\.,][a-z]|$)[\\.,:;|\\\\]?)+$");
    Pattern validIpAddress = Pattern.compile("^(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?::\\d*)?$", 2);
    
    boolean found = false;
    int rand = (int)(Math.random() * whitelisted.size());
    String replace = (String)whitelisted.get(rand);
    String end ="";
    
    for(int x=0;x<msg.length;x++){
        for (int y = 0; y < whitelisted.size(); y++){
            if (Pattern.compile(Pattern.quote((String)whitelisted.get(y)), 2).matcher(msg[x]).find()){
                return c;
            }
        }
        String temp=msg[x].replaceAll("[\\(\\)]","").replace("(dot|DOT|Dot|dOt|doT|DOt|dOT|DoT)", ".");
        Matcher matchIP = validIpAddress.matcher(temp);
        while (matchIP.find()) {
          if (adreplace) {
            msg[x]=replace;
          } else {
            c.setCancelled(true);
          }
          found = true;
        }

        Matcher matchHost = validHostname.matcher(msg[x]);
        while (matchHost.find()) {
          if (adreplace) {
            msg[x]=replace;
          } else {
            c.setCancelled(true);
          }
          found = true;
        }
        temp =msg[x].toLowerCase();
        if(temp.contains("http"))
            msg[x]="";
        end+=(msg[x]+" ");
    }
    if ((adWarn) && (found))
      utils.warn(c.getPlayer().getName(), c.getPlayer(), 1, "advertising.", "advertising");
    c.setMessage(end.trim());
    if(c.getMessage().length()<=0)
        c.setCancelled(true);
    return c;
  }
  
  private final AsyncPlayerChatEvent findCensor(AsyncPlayerChatEvent c)
  {
    String playernm = c.getPlayer().getName();
    Player player = c.getPlayer();
    String msg = c.getMessage();
    ArrayList<String> msgList = new ArrayList();msgList.addAll(Arrays.asList(msg.split(" ")));
    String adding = "";
    
    for (int x = 0; x < msgList.size(); x++)
    {
      for (int y = 0; y < findCensor.size(); y++)
      {
        String findx = msgList.get(x).toLowerCase().replaceAll("[!@#\\$%\\^&\\*\\(\\)\\-_=\\+\"':;\\?/>\\.<,~`\\|]", "");
        String findy = findCensor.get(y).toLowerCase();
        if (findx.contains(findy))
        {
          if (toCensor.equalsIgnoreCase("false"))
          {
            msgList.remove(x);
            if (msgList.isEmpty())
            {
              c.setCancelled(true);
              if (!censorWarn) {
                return c;
              }
            }
            else {
              String newMsg = "";
              for (String s : msgList) {
                newMsg = newMsg + s + " ";
              }
              c.setMessage(newMsg);
            }
          }
          else {
            msgList.set(x, toCensor);
            String message = "";
            for (String s : msgList)
              message = message + s + " ";
            c.setMessage(message.trim());
          }

          if (censorWarn)
            utils.warn(playernm, player, 1, "speaking wrongly.", "censor");
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
    
    long duration = config.getLong("eatspam.duration");
    expected = (time + duration);
    if (System.currentTimeMillis() < expected) {
      c.getPlayer().sendMessage(ChatColor.RED + "You need to wait " + (expected - System.currentTimeMillis()) / 1000L + " seconds before speaking.");
      c.setCancelled(true);
      log.set(name + ".failed-last", Boolean.valueOf(true));
      return c;
    }
    if (msg.length()<3&&utils.findIfCaps(msg)) {
      c.setMessage(msg.toLowerCase());
      msg = c.getMessage();
    }
    if (noBeginCaps)
    {
      c.setMessage(utils.beginCaps(msg));
      msg = c.getMessage();
    }
    if (bes) {
      c.setMessage(msg.replaceAll("[!?@#_%$^&;:|></\\+,=~`-]{3}", ""));
      msg = c.getMessage();
    }
    if (log.getBoolean(name + ".parseAll")) {
      c.setMessage(msg.replaceAll("[!?@#_%$^&;:|></\\+*,=~`-]", ""));
      msg = c.getMessage();
    }
    
    ArrayList<String> refMsg = utils.refine(msg.split(" "));
    ArrayList<String> refLast = utils.refine(log.getString(name + ".last").split(" "));
    
    int wordsInCmn = 0;
    int big = Math.max(refMsg.size(), refLast.size());
    
    for (int x = 0; x < refMsg.size(); x++) {
      for (int y = 0; y < refLast.size(); y++)
      {
        if (((String)refMsg.get(x)).equalsIgnoreCase((String)refLast.get(y)))
          wordsInCmn++; }
    }
    double percentage = wordsInCmn / big;
    double similarity = config.getDouble("eatspam.similarity");
    if ((similarity < 0.0D) || (similarity > 1.0D))
      similarity = 0.65D;
    if (percentage > similarity)
    {
      if (spamWarn)
        utils.warn(name, player, 1, "spamming", "eatspam");
      c.setCancelled(true);
      log.set(name + ".failed-last", Boolean.valueOf(true));
    }
    
    return c;
  }
}
