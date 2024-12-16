package git2jar.base;

import github.soltaufintel.amalia.auth.simple.SimpleAuth;
import github.soltaufintel.amalia.web.config.AppConfig;

public final class Git2jarAuth extends SimpleAuth {

    public Git2jarAuth(AppConfig config) {
        super(config);
    }
    
    @Override
    protected boolean isProtected(String uri) {
        if (uri == null) {
            return true;
        }
        uri += "/";
        return uri.startsWith("/project/") || uri.startsWith("/job/") || uri.startsWith("/login/");
    }
}
