package myvertx;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class Functions {

    static void sleep(long milis) {
        try {
            TimeUnit.MILLISECONDS.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static String now() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    static void log(Object... objects) {
        System.err.format("%s %s: %s%n", now(), Thread.currentThread().getName(),
                Arrays.stream(objects).map(Object::toString).collect(Collectors.joining(",")));
    }
}
