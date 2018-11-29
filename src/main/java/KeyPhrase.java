import java.util.HashMap;

public class KeyPhrase {

    // the key phrases and their responses
    private static HashMap<String, String> phrases = getHashMap();

    // returns whether a String contains a keyphrase
    public static boolean containsKeyPhrase(String query) {
        for (String key : phrases.keySet()) {
            if (query.contains(key)) {
                return true;
            }
        }
        return false;
    }

    // returns the response of a given Phrase
    public static String get(String query) {
        for (String key : phrases.keySet()) {
            if (query.contains(key)) {
                return phrases.get(key);
            }
        }
        throw new IllegalArgumentException("KeyPhrase didn't find a key: " + query);
    }

    // initializes the HashMap with its values
    private static HashMap<String, String> getHashMap() {
        if (phrases == null) {
            HashMap<String, String> map = new HashMap<>();

            // the response entries
            map.put("realm", "stop");
            map.put("video essay", "stop");
            map.put("lo-fi", "did someone say lo-fi?");
            map.put("lo fi", "did someone say lo fi?");
            map.put("lofi", "did someone say lofi?");
            map.put("shut up", "you shut up");
            map.put("fuck you", "no fuck you");
            map.put("shut the fuck up", "you shut the fuck up");
            map.put("best anime ever created", "miss kobayashi's dragon maid is the best anime ever created");
            map.put("heck off", "you heck off");
            map.put("mission failed", "we'll get em next time");
            map.put("play despacito", "https://open.spotify.com/track/5AgTL2WmiCvoObA8fpncKs?si=U7LLWHWQQB6cH4kgGdWoIA");
            map.put("this is so sad", "https://open.spotify.com/track/5AgTL2WmiCvoObA8fpncKs?si=U7LLWHWQQB6cH4kgGdWoIA");
            map.put("no u", "no u");
            map.put("fuck off", "you fuck off");
            map.put("lewd", "https://i.kym-cdn.com/entries/icons/original/000/017/225/zAp2LzJ.jpg");

            map.put("deja vu", "i've just been in this place before");
            map.put("higher on the streets", "and I know it's my time to go");
            map.put("calling you, and the search is a mystery", "standing on my feet");
            map.put("It's so hard when I try to be me, woah", "deja vu");

            map.put("sentient", "what do you mean \"sentient\"?");
            map.put("waifu", "this is a degeneracy-free zone");
            map.put("fortnite", "get out");
            map.put("tsundere", "I'm like a tsundere, except I don't actually like you");
            map.put("ladies and gentlemen", "we got em");
            map.put("what is love", "baby don't hurt me");
            map.put("depresso espresso", "if you upsetti have some spaghetti");
            map.put("gamers", "**GAMERS RISE UP**");
            map.put("loli", "**FBI OPEN UP**");

            map.put("maybe i'll be tracer", "i'm already tracer");
            map.put("what about widowmaker", "i'm already widowmaker");
            map.put("i'll be bastion", "nerf bastion");
            map.put("you're right. so, winston", "I wanna be winston");
            map.put("i guess i'll be genji", "i'm already genji");
            map.put("then i'll be mcree", "i have an idea");
            map.put("what's your idea", "you should be...");
            map.put("i'm not gonna be mercy", "you should've picked mercy");
            map.put("i'm not gonna be any kind of support", "we ended up losing and it's all your fault");

            map.put("anime is trash", "and so are you");

            return map;
        } else {
            throw new AssertionError("Tried to create a new KeyPhrase phrase HashMap when one already existed");
        }
    }
}
