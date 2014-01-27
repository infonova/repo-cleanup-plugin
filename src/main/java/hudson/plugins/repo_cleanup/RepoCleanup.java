package hudson.plugins.repo_cleanup;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author dvrzalik
 */
public class RepoCleanup extends Notifier implements MatrixAggregatable {

    private boolean cleanupMatrixParent;

    @DataBoundConstructor
    // FIXME can't get repeteable to work with a List<String>
    public RepoCleanup() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().append("\nCleaning local repository cache... \n");
        FilePath homeDirectory = build.getBuiltOn().createPath(build.getEnvironment(listener).get("HOME", ""));
        FilePath m2Repo = homeDirectory.child(".m2/repository");

        try {
            if (m2Repo == null || !m2Repo.exists()) {
                return false;
            } else {
                homeDirectory.act(new Cleanup(listener));
            }
            listener.getLogger().append("done\n\n");
        } catch (Exception ex) {
            Logger.getLogger(RepoCleanup.class.getName()).log(Level.SEVERE, null, ex);
            //            if(notFailBuild) {
            listener.getLogger().append("Cannot delete workspace: " + ex.getCause() + "\n");
            //                listener.getLogger().append("Option not to fail the build is turned on, so let's continue\n");
            //                return true;
            //            }
            return false;
        }
        return true;
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        if(cleanupMatrixParent) {
            return new WsCleanupMatrixAggregator(build, launcher, listener);
        }
        return null;
    }

    public BuildStepMonitor getRequiredMonitorService(){
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    //TODO remove if https://github.com/jenkinsci/jenkins/pull/834 is accepted
    public boolean isMatrixProject(Object o) {
        return o instanceof MatrixProject;
    }

    @Extension(ordinal=-9999)
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(RepoCleanup.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.WsCleanup_Delete_workspace();
        }

        @Override
        public boolean isApplicable(Class clazz) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return super.newInstance(req, formData);
        }
    }


}
