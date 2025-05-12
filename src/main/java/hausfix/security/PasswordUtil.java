package hausfix.security;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;   // 10-12 ist für Web-Apps üblich

    private PasswordUtil() { }

    /** Hash mit Salt & Work-Factor erzeugen. */
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Prüfen, ob plain-Text zu gespeichertem Hash passt. */
    public static boolean verify(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
