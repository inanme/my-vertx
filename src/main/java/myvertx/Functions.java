package myvertx;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

class Functions {

    static void sleep(long duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static String now() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    static void log(Object log) {
        System.err.format("%s %s: %s \n", now(), Thread.currentThread().getName(), log);
    }
}
