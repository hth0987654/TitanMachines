package com.firesoftitan.play.titanbox.titanmachines.listeners;


import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TabCompleteListener implements TabCompleter {
    private static final String[] ADMIN_COMMANDS = {"give", "toggle", "reload", "tps"};
    private static final String[] NON_ADMIN_COMMANDS = {};
    private final List<String> pluginNames = new ArrayList<String>();
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> commands = new ArrayList<String>();
        if (args.length == 1) {
            if (TitanMachines.isAdmin(commandSender)) {
                commands.addAll(List.of(ADMIN_COMMANDS));
            }
            commands.addAll(List.of(NON_ADMIN_COMMANDS));

        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give"))
            {
                return null;
            }
            if (args[0].equalsIgnoreCase("toggle"))
            {
                commands.add("pipes");
                commands.add("sorters");
                commands.add("hoppers");
            }
        }
        if (args.length == 3)
        {
            if (args[0].equalsIgnoreCase("give"))
            {
                commands.add("breaker");
                commands.add("trash");
                commands.add("lumberjack");
                commands.add("mobkiller");
                commands.add("pipe");
                commands.add("sorter");
                commands.add("chunkhopper");
                commands.add("areahopper");
                commands.add("junctionbox");


            }

        }
        if (args.length == 4)
        {

        }
        if (args.length == 5)
        {

        }
        if (args.length == 6) {

        }
        if (args.length == 7) {

        }
        //create a new array
        final List<String> completions = new ArrayList<>();
        //copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
        StringUtil.copyPartialMatches(args[args.length - 1], commands, completions);
        //sort the list
        Collections.sort(completions);


        return completions;

    }
}
