package org.example.features;

import java.io.*;
import java.lang.management.*;
import java.text.DecimalFormat;

public class SystemInfoFeature implements Feature {

    @Override
    public boolean canHandle(String input) {
        String lower = input.toLowerCase();
        return lower.contains("system info") || lower.contains("cpu") || lower.contains("ram")
                || lower.contains("memory") || lower.contains("disk space") || lower.contains("storage")
                || lower.contains("battery") || lower.contains("ip address") || lower.contains("my ip")
                || lower.contains("computer info") || lower.contains("pc info")
                || lower.equals("status") || lower.contains("system status");
    }

    @Override
    public void execute(String input, ChatCallback callback) {
        String lower = input.toLowerCase();

        if (lower.contains("cpu")) {
            showCpuInfo(callback);
        } else if (lower.contains("ram") || lower.contains("memory")) {
            showMemoryInfo(callback);
        } else if (lower.contains("disk") || lower.contains("storage")) {
            showDiskInfo(callback);
        } else if (lower.contains("battery")) {
            showBatteryInfo(callback);
        } else if (lower.contains("ip")) {
            showIpAddress(callback);
        } else {
            // Full system report
            showFullSystemInfo(callback);
        }
    }

    private void showCpuInfo(ChatCallback callback) {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        int cpuCount = Runtime.getRuntime().availableProcessors();
        String sb = "🖥️ CPU Information:\n\n" +
                "• Processors: " + cpuCount + " cores\n" +
                "• OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\n" +
                "• OS Version: " + System.getProperty("os.version") + "\n" +
                "• JVM: " + System.getProperty("java.version");
        callback.onMessage("💻 System Info", sb);
    }

    private void showMemoryInfo(ChatCallback callback) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        DecimalFormat df = new DecimalFormat("#.##");
        String sb = "🧠 Memory Information:\n\n" +
                "• Used: " + df.format(usedMemory / 1024.0 / 1024.0) + " MB\n" +
                "• Free: " + df.format(freeMemory / 1024.0 / 1024.0) + " MB\n" +
                "• Total JVM: " + df.format(totalMemory / 1024.0 / 1024.0) + " MB\n" +
                "• Max JVM: " + df.format(maxMemory / 1024.0 / 1024.0) + " MB";
        callback.onMessage("💻 System Info", sb);
    }

    private void showDiskInfo(ChatCallback callback) {
        File[] roots = File.listRoots();
        StringBuilder sb = new StringBuilder("💾 Disk Information:\n\n");
        DecimalFormat df = new DecimalFormat("#.##");

        for (File root : roots) {
            long total = root.getTotalSpace();
            long free  = root.getFreeSpace();
            long used  = total - free;

            if (total > 0) {
                double usedPct = (double) used / total * 100;
                sb.append("• Drive ").append(root.getAbsolutePath()).append("\n")
                        .append("  Total: ").append(df.format(total / 1024.0 / 1024.0 / 1024.0)).append(" GB\n")
                        .append("  Used:  ").append(df.format(used  / 1024.0 / 1024.0 / 1024.0)).append(" GB (")
                        .append(df.format(usedPct)).append("%)\n")
                        .append("  Free:  ").append(df.format(free  / 1024.0 / 1024.0 / 1024.0)).append(" GB\n\n");
            }
        }
        callback.onMessage("💻 System Info", sb.toString());
    }

    private void showBatteryInfo(ChatCallback callback) {
        String os = System.getProperty("os.name").toLowerCase();
        new Thread(() -> {
            try {
                String result;
                if (os.contains("win")) {
                    Process p = Runtime.getRuntime().exec(new String[]{"powershell", "-Command",
                            "Get-WmiObject Win32_Battery | Select-Object EstimatedChargeRemaining, BatteryStatus | Format-List"});
                    result = new String(p.getInputStream().readAllBytes()).trim();
                    if (result.isEmpty()) result = "No battery detected (desktop PC).";
                } else if (os.contains("mac")) {
                    Process p = Runtime.getRuntime().exec(new String[]{"pmset", "-g", "batt"});
                    result = new String(p.getInputStream().readAllBytes()).trim();
                } else {
                    result = "Battery info not supported on this OS.";
                }
                callback.onMessage("🔋 Battery", result.isEmpty() ? "Could not read battery info." : result);
            } catch (Exception e) {
                callback.onMessage("❌ Error", "Battery check failed: " + e.getMessage());
            }
        }).start();
    }

    private void showIpAddress(ChatCallback callback) {
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec("hostname -I");
                String result = new String(p.getInputStream().readAllBytes()).trim();
                if (result.isEmpty()) {
                    // Windows fallback
                    p = Runtime.getRuntime().exec("ipconfig");
                    result = new String(p.getInputStream().readAllBytes());
                    // Extract IPv4
                    for (String line : result.split("\n")) {
                        if (line.contains("IPv4")) {
                            result = line.split(":")[1].trim();
                            break;
                        }
                    }
                }
                callback.onMessage("🌐 Network", "Your IP Address: " + result);
            } catch (Exception e) {
                callback.onMessage("❌ Error", "Could not get IP: " + e.getMessage());
            }
        }).start();
    }

    private void showFullSystemInfo(ChatCallback callback) {
        Runtime runtime = Runtime.getRuntime();
        DecimalFormat df = new DecimalFormat("#.##");

        long totalMem = runtime.totalMemory();
        long freeMem  = runtime.freeMemory();
        long usedMem  = totalMem - freeMem;

        File mainDrive = File.listRoots()[0];
        long diskTotal = mainDrive.getTotalSpace();
        long diskFree  = mainDrive.getFreeSpace();

        String info = "═══════════════════════════\n" +
                "  💻 SYSTEM STATUS REPORT\n" +
                "═══════════════════════════\n\n" +
                "🖥️  OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")\n" +
                "☕  Java: " + System.getProperty("java.version") + "\n" +
                "👤  User: " + System.getProperty("user.name") + "\n\n" +
                "🧠  RAM Used: " + df.format(usedMem / 1024.0 / 1024.0) + " MB\n" +
                "🧠  RAM Free: " + df.format(freeMem / 1024.0 / 1024.0) + " MB\n\n" +
                "💾  Disk Total: " + df.format(diskTotal / 1024.0 / 1024.0 / 1024.0) + " GB\n" +
                "💾  Disk Free:  " + df.format(diskFree  / 1024.0 / 1024.0 / 1024.0) + " GB\n\n" +
                "⚙️  CPU Cores: " + runtime.availableProcessors() + "\n" +
                "🏠  Home: " + System.getProperty("user.home");

        callback.onMessage("💻 System Status", info);
    }
}