package hudson.plugins.repo_cleanup;

import hudson.FilePath.FileCallable;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

class Cleanup implements FileCallable<Object> {

    public Cleanup(BuildListener listener) {
    }

    // Can't use FileCallable<Void> to return void
    public Object invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        // Currently uses passwordless SSH keys to login to sword
        //        String[] cmd = new String[]{"rsync", "-r", USER + "@" + HOST + ":" + REMOTE_FILE_ROOT, LOCAL_FILE_ROOT};
        String[] cmd = new String[]{"ls", "-lathr"};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();
        int val = p.waitFor();
        if (val != 0) {
            throw new IOException("Exception during RSync; return code = " + val);
        }
        return null;
    }
}
