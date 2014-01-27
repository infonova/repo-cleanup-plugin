package hudson.plugins.repo_cleanup;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WsCleanupMatrixAggregator extends MatrixAggregator {

    public WsCleanupMatrixAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        super(build, launcher, listener);
    }

    public boolean endBuild() throws InterruptedException, IOException {
        return doWorkspaceCleanup();
    }

    private boolean doWorkspaceCleanup() throws IOException, InterruptedException {
        listener.getLogger().append("\nCleaning local repository cache of matrix project node... \n");
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

}
