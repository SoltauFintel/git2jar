package git2jar.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.util.FileUtils;
import org.pmw.tinylog.Logger;

/**
 * Execute Windows batch file
 */
public class ShellScriptExecutor {
    private String prefix = "CommandFileExecutor";
    /** in minutes */
    private int timeout = 60;
    private File log;
    
    /**
     * @param cmd shell script multi-line content
     * @param workDir working directory
     */
    public void execute(String cmd, File workDir) {
        Logger.debug("ShellScriptExecutor.execute: cmd=" + cmd + " | workDir=" + workDir.getAbsolutePath());
        try {
            File batch = File.createTempFile(prefix, isWindows() ? ".bat" : ".sh");
            try (FileWriter w = new FileWriter(batch)) {
                w.write(cmd);
            }
            execute(getProcessBuilder(batch.getAbsolutePath()), workDir);
            batch.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String executeAndGetLog(String cmd, File workDir) {
        execute(cmd, workDir);
        String ret = getLogText();
        return ret;
    }

    protected ProcessBuilder getProcessBuilder(String batch) {
        if (isWindows()) {
            return new ProcessBuilder("cmd", "/c", batch);
        } else {
            return new ProcessBuilder("sh", batch);
        }
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    protected final void execute(ProcessBuilder pb, File workDir) {
        try {
            log = File.createTempFile(prefix + "-" + timestamp(), ".log");
            deleteOldFiles(log.getParentFile());
            pb.directory(workDir);
// TODO Ich will Live-Ausgaben nach sysout!            
            pb.redirectErrorStream(true);
            pb.redirectOutput(log);
            Process p = pb.start();
            boolean exit = p.waitFor(timeout, TimeUnit.MINUTES);
            if (!exit) {
                throw new RuntimeException("Batch execution lasts too long! Timeout: " + timeout + "'");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteOldFiles(File parent) {
        File[] files = parent.listFiles();
        if (files != null) {
            final long nowMinus2d = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
            
            for (File file : files) {
                if (file.exists() && file.isFile() && file.getName().startsWith(prefix) && file.lastModified() < nowMinus2d) {
                    try {
                        FileUtils.delete(file);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public File getLog() {
        return log;
    }
    
    public String getLogText() {
        return loadTextFile(log);
    }
    
    public static String loadTextFile(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Error loading file " + file.getAbsolutePath(), e);
        }
    }

    public void cleanup() {
        if (log != null) {
            log.delete();
            log = null;
        }
    }

    public static String timestamp() {
        return timestamp(0);
    }
    
    public static String timestamp(int dayModifier) {
        return LocalDateTime.now().plusDays(dayModifier).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_";
    }
}
