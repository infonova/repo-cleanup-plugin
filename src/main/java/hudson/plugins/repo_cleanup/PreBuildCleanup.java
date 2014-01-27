package hudson.plugins.repo_cleanup;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

public class PreBuildCleanup extends BuildWrapper {

    @DataBoundConstructor
    public PreBuildCleanup() {
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Override
    public Environment setUp( AbstractBuild build, Launcher launcher, BuildListener listener ) throws IOException, InterruptedException{
        return new NoopEnv();
    }

    @Override
    public void preCheckout(AbstractBuild build, Launcher launcher,
            BuildListener listener) throws AbortException {

        listener.getLogger().append("\nResetting local repository cache... ");
    }

    @Extension(ordinal=9999)
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        public String getDisplayName() {
            return Messages.PreBuildCleanup_Delete_workspace();
        }

    }

    class NoopEnv extends Environment{
    }


}
