import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;



public class StartProcessOneTimeTest {

    @Test
    public void testName() throws Exception {

        String[] command = new String[] {"ls -alhtr /home/kazesberger/.m2/repository"};

        ProcessBuilder pb = new ProcessBuilder(command[0].split(" "));
        //        System.out.println(f.getAbsolutePath());
        //        System.out.println(f.getAbsoluteFile());
        Process p = pb.start();

        BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s = null;
        System.out.println("Here is the standard output of the command:\n");
        //        listener.getLogger().println("Here is the standard output of the command:\n");
        while ((s = stdOut.readLine()) != null) {
            System.out.println(s);
            //            listener.getLogger().println(s);
        }
        System.out.println("Here is the standard error of the command (if any):\n");
        //        listener.getLogger().println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
            //            listener.getLogger().println(s);
        }

        int val = p.waitFor();
        if (val != 0) {
            throw new IOException("Exception during RSync; return code = " + val);
        }

    }
}
