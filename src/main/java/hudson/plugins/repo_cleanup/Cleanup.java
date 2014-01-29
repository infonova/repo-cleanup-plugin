package hudson.plugins.repo_cleanup;

import hudson.FilePath.FileCallable;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

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

        List<String> commandWithArgs = new ArrayList<String>();
        commandWithArgs.addAll(Arrays.asList(command.split(" ")));

        commandWithArgs.add(user + "@" + masterFqdn + ":" + pathToMasterRepo);
        commandWithArgs.add(f.getPath());

        listener.getLogger().println("synchronizing with command: " + StringUtils.join(commandWithArgs, " "));

        ProcessBuilder pb = new ProcessBuilder(commandWithArgs);
        Process p = pb.start();

        BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s = null;
        System.out.println("Here is the standard output of the command:\n");
        listener.getLogger().println("Here is the standard output of the command:\n");
        while ((s = stdOut.readLine()) != null) {
            System.out.println(s);
            listener.getLogger().println(s);
        }
        System.out.println("Here is the standard error of the command (if any):\n");
        listener.getLogger().println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
            listener.getLogger().println(s);
        }

        int val = p.waitFor();
        if (val != 0) {
            throw new IOException("Exception during RSync; return code = " + val);
        }
        return null;
    }

}
