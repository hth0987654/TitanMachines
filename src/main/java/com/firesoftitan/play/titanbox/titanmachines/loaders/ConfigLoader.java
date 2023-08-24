package com.firesoftitan.play.titanbox.titanmachines.loaders;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;


public class ConfigLoader {
    private SaveManager configFile;
    private String material;


    public ConfigLoader() {
        reload();
    }
    public void reload()
    {
        configFile = new SaveManager(TitanMachines.instants.getName(), "config");
        if (!configFile.contains("settings.material"))
        {
            configFile.set("settings.material", "SMOKER");
        }

        this.material = configFile.getString("settings.material").toUpperCase();


        configFile.save();

    }

    public String getMaterial() {
        return material;
    }
}
