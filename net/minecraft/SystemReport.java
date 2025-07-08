/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 *  oshi.hardware.CentralProcessor$ProcessorIdentifier
 *  oshi.hardware.GlobalMemory
 *  oshi.hardware.GraphicsCard
 *  oshi.hardware.HardwareAbstractionLayer
 *  oshi.hardware.PhysicalMemory
 *  oshi.hardware.VirtualMemory
 */
package net.minecraft;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;

public class SystemReport {
    public static final long BYTES_PER_MEBIBYTE = 0x100000L;
    private static final long ONE_GIGA = 1000000000L;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String OPERATING_SYSTEM = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
    private static final String JAVA_VERSION = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
    private static final String JAVA_VM_VERSION = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor");
    private final Map<String, String> entries = Maps.newLinkedHashMap();

    public SystemReport() {
        this.setDetail("Minecraft Version", SharedConstants.getCurrentVersion().name());
        this.setDetail("Minecraft Version ID", SharedConstants.getCurrentVersion().id());
        this.setDetail("Operating System", OPERATING_SYSTEM);
        this.setDetail("Java Version", JAVA_VERSION);
        this.setDetail("Java VM Version", JAVA_VM_VERSION);
        this.setDetail("Memory", () -> {
            Runtime runtime = Runtime.getRuntime();
            long l = runtime.maxMemory();
            long l2 = runtime.totalMemory();
            long l3 = runtime.freeMemory();
            long l4 = l / 0x100000L;
            long l5 = l2 / 0x100000L;
            long l6 = l3 / 0x100000L;
            return l3 + " bytes (" + l6 + " MiB) / " + l2 + " bytes (" + l5 + " MiB) up to " + l + " bytes (" + l4 + " MiB)";
        });
        this.setDetail("CPUs", () -> String.valueOf(Runtime.getRuntime().availableProcessors()));
        this.ignoreErrors("hardware", () -> this.putHardware(new SystemInfo()));
        this.setDetail("JVM Flags", () -> {
            List list = Util.getVmArguments().collect(Collectors.toList());
            return String.format(Locale.ROOT, "%d total; %s", list.size(), String.join((CharSequence)" ", list));
        });
    }

    public void setDetail(String string, String string2) {
        this.entries.put(string, string2);
    }

    public void setDetail(String string, Supplier<String> supplier) {
        try {
            this.setDetail(string, supplier.get());
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to get system info for {}", (Object)string, (Object)exception);
            this.setDetail(string, "ERR");
        }
    }

    private void putHardware(SystemInfo systemInfo) {
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        this.ignoreErrors("processor", () -> this.putProcessor(hardwareAbstractionLayer.getProcessor()));
        this.ignoreErrors("graphics", () -> this.putGraphics(hardwareAbstractionLayer.getGraphicsCards()));
        this.ignoreErrors("memory", () -> this.putMemory(hardwareAbstractionLayer.getMemory()));
        this.ignoreErrors("storage", this::putStorage);
    }

    private void ignoreErrors(String string, Runnable runnable) {
        try {
            runnable.run();
        }
        catch (Throwable throwable) {
            LOGGER.warn("Failed retrieving info for group {}", (Object)string, (Object)throwable);
        }
    }

    public static float sizeInMiB(long l) {
        return (float)l / 1048576.0f;
    }

    private void putPhysicalMemory(List<PhysicalMemory> list) {
        int n = 0;
        for (PhysicalMemory physicalMemory : list) {
            String string = String.format(Locale.ROOT, "Memory slot #%d ", n++);
            this.setDetail(string + "capacity (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB(physicalMemory.getCapacity()))));
            this.setDetail(string + "clockSpeed (GHz)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf((float)physicalMemory.getClockSpeed() / 1.0E9f)));
            this.setDetail(string + "type", () -> ((PhysicalMemory)physicalMemory).getMemoryType());
        }
    }

    private void putVirtualMemory(VirtualMemory virtualMemory) {
        this.setDetail("Virtual memory max (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB(virtualMemory.getVirtualMax()))));
        this.setDetail("Virtual memory used (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB(virtualMemory.getVirtualInUse()))));
        this.setDetail("Swap memory total (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB(virtualMemory.getSwapTotal()))));
        this.setDetail("Swap memory used (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB(virtualMemory.getSwapUsed()))));
    }

    private void putMemory(GlobalMemory globalMemory) {
        this.ignoreErrors("physical memory", () -> this.putPhysicalMemory(globalMemory.getPhysicalMemory()));
        this.ignoreErrors("virtual memory", () -> this.putVirtualMemory(globalMemory.getVirtualMemory()));
    }

    private void putGraphics(List<GraphicsCard> list) {
        int n = 0;
        for (GraphicsCard graphicsCard : list) {
            String string = String.format(Locale.ROOT, "Graphics card #%d ", n++);
            this.setDetail(string + "name", () -> ((GraphicsCard)graphicsCard).getName());
            this.setDetail(string + "vendor", () -> ((GraphicsCard)graphicsCard).getVendor());
            this.setDetail(string + "VRAM (MiB)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf(SystemReport.sizeInMiB(graphicsCard.getVRam()))));
            this.setDetail(string + "deviceId", () -> ((GraphicsCard)graphicsCard).getDeviceId());
            this.setDetail(string + "versionInfo", () -> ((GraphicsCard)graphicsCard).getVersionInfo());
        }
    }

    private void putProcessor(CentralProcessor centralProcessor) {
        CentralProcessor.ProcessorIdentifier processorIdentifier = centralProcessor.getProcessorIdentifier();
        this.setDetail("Processor Vendor", () -> ((CentralProcessor.ProcessorIdentifier)processorIdentifier).getVendor());
        this.setDetail("Processor Name", () -> ((CentralProcessor.ProcessorIdentifier)processorIdentifier).getName());
        this.setDetail("Identifier", () -> ((CentralProcessor.ProcessorIdentifier)processorIdentifier).getIdentifier());
        this.setDetail("Microarchitecture", () -> ((CentralProcessor.ProcessorIdentifier)processorIdentifier).getMicroarchitecture());
        this.setDetail("Frequency (GHz)", () -> String.format(Locale.ROOT, "%.2f", Float.valueOf((float)processorIdentifier.getVendorFreq() / 1.0E9f)));
        this.setDetail("Number of physical packages", () -> String.valueOf(centralProcessor.getPhysicalPackageCount()));
        this.setDetail("Number of physical CPUs", () -> String.valueOf(centralProcessor.getPhysicalProcessorCount()));
        this.setDetail("Number of logical CPUs", () -> String.valueOf(centralProcessor.getLogicalProcessorCount()));
    }

    private void putStorage() {
        this.putSpaceForProperty("jna.tmpdir");
        this.putSpaceForProperty("org.lwjgl.system.SharedLibraryExtractPath");
        this.putSpaceForProperty("io.netty.native.workdir");
        this.putSpaceForProperty("java.io.tmpdir");
        this.putSpaceForPath("workdir", () -> "");
    }

    private void putSpaceForProperty(String string) {
        this.putSpaceForPath(string, () -> System.getProperty(string));
    }

    private void putSpaceForPath(String string, Supplier<String> supplier) {
        String string2 = "Space in storage for " + string + " (MiB)";
        try {
            String string3 = supplier.get();
            if (string3 == null) {
                this.setDetail(string2, "<path not set>");
                return;
            }
            FileStore fileStore = Files.getFileStore(Path.of(string3, new String[0]));
            this.setDetail(string2, String.format(Locale.ROOT, "available: %.2f, total: %.2f", Float.valueOf(SystemReport.sizeInMiB(fileStore.getUsableSpace())), Float.valueOf(SystemReport.sizeInMiB(fileStore.getTotalSpace()))));
        }
        catch (InvalidPathException invalidPathException) {
            LOGGER.warn("{} is not a path", (Object)string, (Object)invalidPathException);
            this.setDetail(string2, "<invalid path>");
        }
        catch (Exception exception) {
            LOGGER.warn("Failed retrieving storage space for {}", (Object)string, (Object)exception);
            this.setDetail(string2, "ERR");
        }
    }

    public void appendToCrashReportString(StringBuilder stringBuilder) {
        stringBuilder.append("-- ").append("System Details").append(" --\n");
        stringBuilder.append("Details:");
        this.entries.forEach((string, string2) -> {
            stringBuilder.append("\n\t");
            stringBuilder.append((String)string);
            stringBuilder.append(": ");
            stringBuilder.append((String)string2);
        });
    }

    public String toLineSeparatedString() {
        return this.entries.entrySet().stream().map(entry -> (String)entry.getKey() + ": " + (String)entry.getValue()).collect(Collectors.joining(System.lineSeparator()));
    }
}

