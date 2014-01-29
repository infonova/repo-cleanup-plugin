package hudson.plugins.repo_cleanup;

import hudson.Extension;
import hudson.model.Descriptor;

import java.text.SimpleDateFormat;

import javax.annotation.CheckForNull;

import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class RepoCleanupConfig extends GlobalConfiguration {


    /**
     * The chosen format for displaying the system clock time, as recognised by {@link SimpleDateFormat}.
     */
    @CheckForNull
    private String pathToMasterRepo;

    /**
     * The chosen format for displaying the elapsed time, as recognised by {@link DurationFormatUtils}.
     */
    @CheckForNull
    private String user;

    /**
     * Constructor.
     */
    public RepoCleanupConfig() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public String getPathToMasterRepo() {
        return pathToMasterRepo;
    }

    public void setPathToMasterRepo(String pathToMasterRepo) {
        this.pathToMasterRepo = pathToMasterRepo;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
