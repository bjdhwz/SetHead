package main.java.com.sethead;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getCommand("sethead").setExecutor(new SetHead());
    }

    @Override
    public void onDisable(){

    }
}