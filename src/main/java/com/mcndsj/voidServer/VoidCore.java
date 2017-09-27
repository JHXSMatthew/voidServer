package com.mcndsj.voidServer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Matthew on 11/06/2016.
 */
public class VoidCore extends JavaPlugin implements Listener{

    HashMap<String,Location> map = new HashMap<String,Location>();

    public void onEnable(){
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "LobbyConnect");
        getServer().getWorld("lobby").setGameRuleValue("doDaylightCycle","false");


        new BukkitRunnable(){

            @Override
            public void run() {
                if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 4 && Calendar.getInstance().get(Calendar.MINUTE) == 0  )
                    Bukkit.shutdown();

                Iterator<Map.Entry<String,Location>> iterator=  map.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,Location> next = iterator.next();
                    if(Bukkit.getPlayer(next.getKey()) == null || !Bukkit.getPlayer(next.getKey()).isOnline()){
                        iterator.remove();
                    }else{
                        if(Bukkit.getPlayer(next.getKey()).getLocation().getX() == next.getValue().getX() && Bukkit.getPlayer(next.getKey()).getLocation().getZ() == next.getValue().getZ()){
                            continue;
                        }else{
                            Send(Bukkit.getPlayer(next.getKey()));
                            iterator.remove();
                        }
                    }
                }
                for(Player p : Bukkit.getOnlinePlayers()){
                    map.put(p.getName(),p.getLocation());
                }
            }
        }.runTaskTimer(this,0,60);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt){
        evt.setQuitMessage("");
    }

    @EventHandler
    public void weather(WeatherChangeEvent evt){
        evt.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt){
        for(Player p : getServer().getOnlinePlayers()){
            p.hidePlayer(evt.getPlayer());
            evt.getPlayer().hidePlayer(p);
        }
        evt.setJoinMessage("");
        evt.getPlayer().setGameMode(GameMode.ADVENTURE);
        evt.getPlayer().teleport(Bukkit.getWorld("lobby").getSpawnLocation());
        Location l = evt.getPlayer().getLocation();
        l.setPitch(-64);
        l.setYaw(114);
        evt.getPlayer().teleport(l);

        Title title = new Title(ChatColor.RED + "YourCraft-挂机锁屏",ChatColor.GREEN + "←←←移动解锁←←←",0,60 * 60,0);
        title.send(evt.getPlayer());
    }

    public void Send(Player p){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(p.getName());
        p.sendPluginMessage(this, "LobbyConnect", out.toByteArray());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getCause() == EntityDamageEvent.DamageCause.VOID){
            e.getEntity().teleport(Bukkit.getWorld("lobby").getSpawnLocation());
        }
        e.setCancelled(true);

    }
    @EventHandler
    public void onTouch(PlayerInteractEvent e){
        if(e.getPlayer().isOp()){
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        e.setCancelled(true);
        e.getPlayer().sendMessage(ChatColor.RED + "请保持安静哟~.");
    }


}
