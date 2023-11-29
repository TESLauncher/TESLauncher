/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.io.IOException;

public class Log4jConfigurator {
    public static void configure() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.WARN);

        Log4jConfigurator.configureConsole(builder);
        Log4jConfigurator.configureFile(builder);
        Log4jConfigurator.configureLogger(builder);
        Log4jConfigurator.configureRoot(builder);

        @SuppressWarnings({"resource", "unused"})
        LoggerContext context = Configurator.initialize(builder.build());
    }

    private static void configureConsole(ConfigurationBuilder<BuiltConfiguration> builder) {
        LayoutComponentBuilder patternLayout = builder.newLayout("PatternLayout");
        patternLayout.addAttribute("pattern", "%highlight{%d{HH:mm:ss} [%t] %level %logger{36} - %msg%n}{INFO=default}");
        patternLayout.addAttribute("disableAnsi", "false");

        AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
        console.add(patternLayout);

        builder.add(console);
    }

    private static void configureFile(ConfigurationBuilder<BuiltConfiguration> builder) {
        LayoutComponentBuilder patternLayout = builder.newLayout("PatternLayout");
        patternLayout.addAttribute("pattern", "%d{HH:mm:ss} [%t] %level %logger{36} - %msg%n");
        patternLayout.addAttribute("charset", "866");

        ComponentBuilder<?> policies = builder.newComponent("Policies")
                .addComponent(builder.newComponent("TimeBasedTriggeringPolicy"))
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                        .addAttribute("size", "10 MB"));

        ComponentBuilder<?> rolloverStrategy = builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", "10");

        AppenderComponentBuilder rollingFile = builder.newAppender("rollingFile", "RollingRandomAccessFile");
        rollingFile.addAttribute("fileName", "${sys:teslauncher.workDir}/logs/latest.log");
        rollingFile.addAttribute("filePattern", "${sys:teslauncher.workDir}/logs/$${date:MM-yyyy}/app-%d{dd-MM-yyyy}-%i.log.gz");
        rollingFile.addComponent(policies);
        rollingFile.addComponent(rolloverStrategy);
        rollingFile.add(patternLayout);

        builder.add(rollingFile);
    }

    private static void configureLogger(ConfigurationBuilder<BuiltConfiguration> builder) {
        LoggerComponentBuilder mainLogger = builder.newLogger("me.theentropyshard.teslauncher", Level.DEBUG);
        mainLogger.addAttribute("additivity", false);
        mainLogger.add(builder.newAppenderRef("stdout"));
        mainLogger.add(builder.newAppenderRef("rollingFile"));

        builder.add(mainLogger);
    }

    private static void configureRoot(ConfigurationBuilder<BuiltConfiguration> builder) {
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger();

        builder.add(rootLogger);
    }
}
