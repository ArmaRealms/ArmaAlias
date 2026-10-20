package com.darkblade12.simplealias.plugin;

import org.bukkit.command.CommandSender;
import com.darkblade12.simplealias.util.TextFormat;
import net.kyori.adventure.text.Component;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Logger;

public abstract class PluginBase extends JavaPlugin {
    protected final Logger logger;
    protected final File config;
    protected final MessageManager messageManager;

    protected PluginBase() {
        logger = getLogger();
        config = new File(getDataFolder(), "config.yml");
        messageManager = new MessageManager(this);
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        try {
            messageManager.enable();
        } catch (Exception e) {
            logException("Failed to enable the message manager: {0}", e);
            disablePlugin();
            return;
        }

        if (!enable()) {
            disablePlugin();
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        logInfo("Plugin version {0} enabled! ({1} ms)", getPluginMeta().getVersion(), duration);

    }

    @Override
    public void onDisable() {
        disable();
        messageManager.disable();
        logInfo("Plugin version {0} disabled.", getPluginMeta().getVersion());
    }

    public void onReload() {
        try {
            messageManager.reload();
        } catch (Exception e) {
            logException("Failed to reload the message manager: {0}", e);
            disablePlugin();
            return;
        }

        if (!reload()) {
            disablePlugin();
            return;
        }

        logInfo("Plugin version {0} reloaded!", getPluginMeta().getVersion());
    }

    public abstract boolean enable();

    public abstract void disable();

    public abstract boolean reload();

    public final void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logInfo(String message, Object... args) {
        logger.info(MessageFormat.format(message, args));
    }

    public void logWarning(String message) {
        logger.warning(message);
    }

    public void logWarning(String message, Object... args) {
        logger.warning(MessageFormat.format(message, args));
    }

    public void logException(String message, Exception exception, Object... args) {
        Object[] messageArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, messageArgs, 1, args.length);

        if (exception != null && exception.getMessage() != null) {
            messageArgs[0] = exception.getMessage();
        } else {
            messageArgs[0] = "Undefined";
        }

        logWarning(MessageFormat.format(message, messageArgs));
        if (exception != null) {
            exception.printStackTrace();
        }
    }

    public final String formatMessage(String key, Object... args) {
        return messageManager.formatMessage(key, args);
    }

    public final Component formatComponent(String key, Object... args) {
        Map<String, Component> values = new LinkedHashMap<>();
        Object[] tokens = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            String id = "armavalue" + i;
            values.put(id, args[i] instanceof Component component
                    ? component : TextFormat.external(String.valueOf(args[i] == null ? "" : args[i])));
            tokens[i] = "<" + id + ">";
        }
        return TextFormat.render(messageManager.formatMessage(key, tokens), values);
    }

    public final void sendMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(TextFormat.render(getPrefix()).append(Component.space())
                .append(formatComponent(key, args)));
    }

    public void copyResource(String resourcePath, File outputFile, boolean replace) throws IOException {
        if (!replace && outputFile.exists()) {
            return;
        }

        InputStream stream = getResource(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Resource could not be found.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        }
    }

    public void copyResource(String resourcePath, String outputPath, boolean replace) throws IOException {
        copyResource(resourcePath, new File(outputPath), replace);
    }

    public abstract boolean isDebugEnabled();

    public abstract Locale getCurrentLocale();

    public abstract String getPrefix();

    @Override
    public FileConfiguration getConfig() {
        if (!config.exists()) {
            saveDefaultConfig();
        }
        return super.getConfig();
    }
}
