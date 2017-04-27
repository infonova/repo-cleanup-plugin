package hudson.plugins.repo_cleanup;

import hudson.FilePath.FileCallable;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

class Cleanup implements FileCallable<Object> {

    private String command;
    private BuildListener listener;
    private String user;
    private String pathToMasterRepo;
    private String masterFqdn;

    public Cleanup(BuildListener listener, String command, RepoCleanupConfig config, String masterFqdn) {
        this.listener = listener;
        this.command = command;
        this.user = config.getUser();
        this.pathToMasterRepo = config.getPathToMasterRepo();
        this.masterFqdn = masterFqdn;
    }

    public Cleanup(BuildListener listener, String command, String pathToMasterRepo, String user, String masterFqdn) {
        this.listener = listener;
        this.command = command;
        this.user = user;
        this.pathToMasterRepo = pathToMasterRepo;
        this.masterFqdn = masterFqdn;
    }

    // Can't use FileCallable<Void> to return void
    public Object invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        // Currently uses passwordless SSH keys to login to sword
        // String[] cmd = new String[]{"rsync", "-r", USER + "@" + HOST + ":" + REMOTE_FILE_ROOT, LOCAL_FILE_ROOT};

        // String[] cmd = new String[]{"ls", "-lathr"};

        String remote = user + "@" + masterFqdn + ":" + pathToMasterRepo;
        String commandWithRemoteAndSource = StringUtils.join(new String[]{command, remote, f.getPath()}, " ");

        listener.getLogger().println("[repo-cleanup] synchronizing with command: " + commandWithRemoteAndSource);

        ProcessBuilder pb = new ProcessBuilder(new String[] {"bash", "-c", commandWithRemoteAndSource});
        Process p = pb.start();

        BufferedReader stdOut = null;
        BufferedReader stdError = null;

        try {
            stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s = null;

            listener.getLogger().println("[repo-cleanup] Here is the standard output of the command:");
            while ((s = stdOut.readLine()) != null) {
                listener.getLogger().println("[repo-cleanup] " + s);
            }
            listener.getLogger().println("[repo-cleanup] Here is the standard error of the command (if any):");
            while ((s = stdError.readLine()) != null) {
                listener.getLogger().println("[repo-cleanup] " + s);
            }

            int val = p.waitFor();
            if (val != 0) {
                throw new IOException("Exception during rsync; return code = " + val);
            }
        } finally {
            // normally not needed, but class Process closes streams when gc is invoked,
            // so it is generally a good idea to close them
            IOUtils.closeQuietly(stdOut);
            IOUtils.closeQuietly(stdError);
            IOUtils.closeQuietly(p.getOutputStream());
        }
        return null;
    }

}
