package io.protostuff.jetbrains.plugin.util;


import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ProtocUtil {


    private static final ProcessBuilder PROCESS_BUILDER = new ProcessBuilder();

    private static final String COMMAND_TEMPLATE = "%s -I=%s --java_out=%s %s";

    private static final boolean isWindowsOS = System.getProperty("os.name").startsWith("Windows");

    private ProtocUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    /**
     * @param protocPath    protoc (absolute) path
     * @param workDir       work (absolute) directory
     * @param javaOutDir    generate java file to one (absolute) directory
     * @param protoFilePath origin proto file (absolute) path
     * @param consumer      callback to handle command result
     */
    public static int generate(String protocPath, String workDir, String javaOutDir, String protoFilePath,
                               Consumer<String> consumer) throws InterruptedException, IOException {
        String command = String.format(COMMAND_TEMPLATE, protocPath, workDir, javaOutDir, protoFilePath);
        if (!isWindowsOS) {
            PROCESS_BUILDER.command("sh", "-c", command);
        } else {
            PROCESS_BUILDER.command("cmd.exe", "/c", command);
        }
        PROCESS_BUILDER.redirectErrorStream(true);
        Process process = PROCESS_BUILDER
                .directory(new File(System.getProperty("user.home")))
                .start();
        new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
                .forEach(consumer);

        return process.waitFor();
    }

}
