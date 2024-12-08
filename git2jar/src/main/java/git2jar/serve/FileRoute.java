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
            File file = new File(Config.config.getFilesDir(), path);
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
}
