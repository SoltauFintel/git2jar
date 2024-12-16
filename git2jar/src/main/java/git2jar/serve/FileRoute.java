package git2jar.serve;

import java.io.File;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import git2jar.Git2jarApp;
import git2jar.base.Config;
import spark.Request;
import spark.Response;
import spark.Route;

public class FileRoute implements Route {
    // TODO Man könnte die file usage dauerhaft speichern. Ziel: rausfinden was weg kann
	
    @Override
    public Object handle(Request req, Response res) throws Exception {
        String p = req.ip() + " | " + req.requestMethod() + " | ";
        String path = req.pathInfo();
        if ("/".equals(path)) {
            return "git2jar " + Git2jarApp.VERSION;
        } else if (path == null || path.contains("..") || path.replace("\\", "/").contains("//")/* ~UNC */) {
            Logger.error(p + "Illegal path: " + path);
        } else {
            File repositoryDir = Config.config.getRepositoryDir();
            File file = new File(repositoryDir, path);
            if (!file.exists()) {
                String s = path.replace("\\", "/");
                if (s.startsWith("/")) s = s.substring(1);
                File file2 = search(repositoryDir, s.split("/"));
                if (file2 != null) {
                    file = file2;
                }
            }
            if (file.isFile()) {
                try {
                    if ("HEAD".equalsIgnoreCase(req.requestMethod())) {
                        Logger.info(p + file.getAbsolutePath());
                        return "";
                    } else {
                        Logger.info(p + file.getAbsolutePath());
                        res.type("application/octet-stream");
                        return Files.readAllBytes(file.toPath());
                    }
                } catch (Exception e) {
                    Logger.error(e, p + "Error loading file: " + file.getAbsolutePath());
                }
            } else {
                Logger.error(p + "File not found: " + file.getAbsolutePath());
            }
        }
        res.status(404);
        return "File not found";
    }

    // case-insensitive search
    private File search(File repo, String[] path) {
        File folder = repo;
        for (int index = 0; index < path.length - 1; index++) {
            File x = search2(folder, true, path[index]);
            if (x == null) { // not found
                return null;
            }
            folder = x;
        }
        return search2(folder, false, path[path.length - 1]);
    }
    
    private File search2(File folder, boolean mustDir, String name) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() == mustDir && file.getName().equalsIgnoreCase(name)) {
                    return file;
                }
            }
        }
        return null;
    }
}
