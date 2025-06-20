package dev.onlydarknesss.ODSCore.Utils;

import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

public class PasswordUtils {

    private static Logger logger;


    public static void setLogger(Logger log) {
        logger = log;
    }

    public static String hashPassword(String plainPass) {
        String hashed = BCrypt.hashpw(plainPass, BCrypt.gensalt(13));
        if (logger != null)
            logger.info("[PasswordUtils] Hashed password: " + hashed);
        return hashed;
    }

    public static boolean checkPass(String plainPass, String hashedPass) {
        boolean result = false;
        try {
            result = BCrypt.checkpw(plainPass, hashedPass);
        } catch (Exception e) {
            if (logger != null)
                logger.warning("[PasswordUtils] Error checking password: " + e.getMessage());
        }

        if (logger != null)
            logger.info("[PasswordUtils] Password match result: " + result);

        return result;
    }
}
