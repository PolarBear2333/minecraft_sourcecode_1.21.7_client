/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 */
package net.minecraft.client.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.data.AtlasProvider;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.WaypointStyleProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;

public class Main {
    @DontObfuscate
    @SuppressForbidden(a="System.out needed before bootstrap")
    public static void main(String[] stringArray) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        AbstractOptionSpec abstractOptionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder optionSpecBuilder = optionParser.accepts("client", "Include client generators");
        OptionSpecBuilder optionSpecBuilder2 = optionParser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo((Object)"generated", (Object[])new String[0]);
        OptionSet optionSet = optionParser.parse(stringArray);
        if (optionSet.has((OptionSpec)abstractOptionSpec) || !optionSet.hasOptions()) {
            optionParser.printHelpOn((OutputStream)System.out);
            return;
        }
        Path path = Paths.get((String)argumentAcceptingOptionSpec.value(optionSet), new String[0]);
        boolean bl = optionSet.has((OptionSpec)optionSpecBuilder2);
        boolean bl2 = bl || optionSet.has((OptionSpec)optionSpecBuilder);
        Bootstrap.bootStrap();
        ClientBootstrap.bootstrap();
        DataGenerator dataGenerator = new DataGenerator(path, SharedConstants.getCurrentVersion(), true);
        Main.addClientProviders(dataGenerator, bl2);
        dataGenerator.run();
    }

    public static void addClientProviders(DataGenerator dataGenerator, boolean bl) {
        DataGenerator.PackGenerator packGenerator = dataGenerator.getVanillaPack(bl);
        packGenerator.addProvider(ModelProvider::new);
        packGenerator.addProvider(EquipmentAssetProvider::new);
        packGenerator.addProvider(WaypointStyleProvider::new);
        packGenerator.addProvider(AtlasProvider::new);
    }
}

