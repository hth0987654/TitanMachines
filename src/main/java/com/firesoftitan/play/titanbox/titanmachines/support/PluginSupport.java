package com.firesoftitan.play.titanbox.titanmachines.support;

import org.bukkit.Bukkit;

public class PluginSupport {
    private final String pluginName;
    public PluginSupport(String pluginName) {
        this.pluginName = pluginName;
    }
    public boolean isInstalled()
    {
      return Bukkit.getPluginManager().isPluginEnabled(this.pluginName);
    }

    public String getPluginName() {
        return pluginName;
    }
}
