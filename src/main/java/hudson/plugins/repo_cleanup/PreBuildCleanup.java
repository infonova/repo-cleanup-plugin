package hudson.plugins.repo_cleanup;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class PreBuildCleanup extends BuildWrapper {

    private static final String DEFAULT_HOST_FOR_REPO_SYNC = "grzcimaster01.infonova.at";
    private static final String MASTER_JENKINS_FQDN_KEY = "MASTER_JENKINS_FQDN";
    private static final String CLEANUP_CMD = "rsync --delete -avz";

    @DataBoundConstructor
    public PreBuildCleanup() {
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        return new NoopEnv();
    }

    @Override
    public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws AbortException {

        RepoCleanupConfig config = GlobalConfiguration.all().get(RepoCleanupConfig.class);
        Set<LabelAtom> assignedLabels = build.getBuiltOn().getAssignedLabels();

        if (isSkipPreBuildCleanup(assignedLabels, config.getDontExecuteOnLabelsAsList(), build.getBuiltOnStr())) {
            listener.getLogger().append("\nSkipping reset of local repository cache...\n");
            return;
        }

        listener.getLogger().append("\nResetting local repository cache...\n");

        try {
            FilePath homeDirectory = build.getBuiltOn().createPath(build.getEnvironment(listener).get("HOME", ""));
            FilePath m2Repo = homeDirectory.child(".m2/repository");

            String masterFqdn = getEnvVarsFromGlobalNodeProperties().get(MASTER_JENKINS_FQDN_KEY, DEFAULT_HOST_FOR_REPO_SYNC);

            m2Repo.act(new Cleanup(listener, CLEANUP_CMD, config, masterFqdn));

            listener.getLogger().append("done\n\n");

        } catch (Exception ex) {
            Logger.getLogger(PreBuildCleanup.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().append("Cannot reset repository cache: " + ex.getCause() + "\n");
            throw new AbortException("Cannot reset repository cache: " + ex.getCause() + "\n");
        }

    }

    private boolean isSkipPreBuildCleanup(Set<LabelAtom> assignedLabels, Set<String> dontExecuteOnLabelsAsList, String builtOnStr) {
        for (String skipIfThisLabelIsAssigned : dontExecuteOnLabelsAsList) {
            if (assignedLabels.contains(Label.get(skipIfThisLabelIsAssigned))) {
                return true;
            }
            if (StringUtils.equals(builtOnStr, skipIfThisLabelIsAssigned)) {
                return true;
            }
        }
        return false;
    }

    private EnvVars getEnvVarsFromGlobalNodeProperties() {
        List<EnvironmentVariablesNodeProperty> list = Jenkins.getInstance().getGlobalNodeProperties()
                .getAll(EnvironmentVariablesNodeProperty.class);
        EnvVars envVars = new EnvVars();
        for (EnvironmentVariablesNodeProperty environmentVariablesNodeProperty : list) {
            envVars.putAll(environmentVariablesNodeProperty.getEnvVars().descendingMap());
        }
        return envVars;
    }

    @Extension(ordinal = 9999)
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        public String getDisplayName() {
            return Messages.PreBuildCleanup_Reset_Repo();
        }

    }

    class NoopEnv extends Environment {
    }

}
