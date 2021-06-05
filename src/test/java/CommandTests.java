import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import wenjalan.starbot.engine.command.SeekCommand;

import java.util.concurrent.TimeUnit;

public class CommandTests {

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(MarkovLanguageTests.class);
        for (Failure fail : result.getFailures()) {
            System.out.println(fail.toString());
        }
        System.out.println(result.wasSuccessful());
    }

    @Test
    public void testConvertTimestampToLongMillis() {
        String timestamp = "0:00:00";
        long pos = SeekCommand.jonoGetPosition(timestamp);
        assert pos == 0;

        timestamp = "0:00:01";
        pos = SeekCommand.jonoGetPosition(timestamp);
        assert pos == 1000;

        timestamp = "0:01";
        pos = SeekCommand.jonoGetPosition(timestamp);
        assert pos == 1000;

        timestamp = "1000:00";
        pos = SeekCommand.jonoGetPosition(timestamp);
        assert pos == TimeUnit.MINUTES.toMillis(1000);

        timestamp = "00:1000";
        pos = SeekCommand.jonoGetPosition(timestamp);
        assert pos == TimeUnit.SECONDS.toMillis(1000);
    }

}
