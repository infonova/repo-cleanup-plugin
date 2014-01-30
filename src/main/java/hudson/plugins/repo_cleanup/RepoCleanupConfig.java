package hudson.plugins.repo_cleanup;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;

import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class RepoCleanupConfig extends GlobalConfiguration {


    @CheckForNull
    private String pathToMasterRepo;

    @CheckForNull
    private String user;

    @CheckForNull
    private String dontExecuteOnLabels;

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

    public void setPathToMasterRepo(@CheckForNull String pathToMasterRepo) {
        this.pathToMasterRepo = pathToMasterRepo;
    }

    public String getUser() {
        return user;
    }

    public void setUser(@CheckForNull String user) {
        this.user = user;
    }

    public void setDontExecuteOnLabels(@CheckForNull String dontExecuteOnLabels) {
        this.dontExecuteOnLabels = dontExecuteOnLabels;
    }

    public Set<String> getDontExecuteOnLabelsAsList() {
        if (StringUtils.isBlank(dontExecuteOnLabels)) {
            return new HashSet<String>();
        }
        Set<String> set = new HashSet<String>(Arrays.asList(dontExecuteOnLabels.split(",")));

        Set<String> result = new HashSet<String>();
        for (String string : set) {
            result.add(string.trim());
        }

        return result;
    }

    public FormValidation doCheckUser(@QueryParameter String user) {
        if (StringUtils.isBlank(user)) {
            return FormValidation.error("mandatory field");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckPathToMasterRepo(@QueryParameter String pathToMasterRepo) {
        if (StringUtils.isBlank(pathToMasterRepo)) {
            return FormValidation.error("mandatory field");
        }
        return FormValidation.ok();
    }

    public String getDontExecuteOnLabels() {
        return dontExecuteOnLabels;
    }

}
