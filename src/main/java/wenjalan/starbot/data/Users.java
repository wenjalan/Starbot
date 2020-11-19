package wenjalan.starbot.data;

import java.util.List;

// contains information related to users
public class Users {

    // Developer IDs
    public static final long WENTON_ID_LONG = 478706068223164416L;
    public static final long SERIN_ID_LONG = 189852772475207680L;
    public static final long TREB_ID_LONG = 151470394363084800L;
    public static final long ALIQON_ID_LONG = 110137446053736448L;

    // list of approved Starbot developers
    public static final long[] devIds = {
            WENTON_ID_LONG,
            SERIN_ID_LONG,
            TREB_ID_LONG,
            ALIQON_ID_LONG
    };

    // util method to determine if the person is a developer
    public static boolean isDeveloper(long userId) {
        for (long id : devIds) {
            if (id == userId) {
                return true;
            }
        }
        return false;
    }

}
