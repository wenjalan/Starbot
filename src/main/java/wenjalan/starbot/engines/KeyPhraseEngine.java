package wenjalan.starbot.engines;

import java.util.HashMap;

// contains a list of keyphrases and the responses that Starbot prints
public class KeyPhraseEngine {

    // the HashMap to store the phrases and responses
    protected HashMap<String, String> phrases;

    // constructor
    public KeyPhraseEngine() {
        // generate the phraseMap
        this.phrases = generateMap();
    }

    // generates a map of KeyPhrases
    protected HashMap<String, String> generateMap() {
        // map to return
        HashMap<String, String> map = new HashMap<>();

        // the phrases
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
        map.put("yoshi", "**IRS OPEN UP**");
        map.put("dab", "get the fuck out");
        map.put("uwu", "someone get the hose");
        map.put("owo", "talk like a proper human being right now or I will smack you with this here board");
        map.put("baka", "you're a fucking retard");
        map.put("oniichan", "where's my fucking belt");
        map.put("onii-chan", "where's my fucking belt");
        map.put("yuru camp", "(￣▽￣)");
        map.put("java has a function for that", "SHUT THE FUCK UP JASON");

        map.put("furry", "stop it. get some help.");
        map.put("nuzzles", "where're your parents?");
        map.put("hit or miss", "i guess they never miss, huh?");

        // return the map
        return map;
    }

    // returns the response to a query with a keyphrase in it
    public String getResponse(String query) {
        for (String key : this.phrases.keySet()) {
            if (query.toLowerCase().contains(key)) return phrases.get(key);
        }

        // if none was found, report an error and respond with something generic
        System.err.println("Tried to find a response to a keyphrase without a response!");
        System.err.println("\"" + query + "\"");
        return "wait what";
    }

    // returns if a query contains a KeyPhrase
    public Boolean hasKeyPhrase(String query) {
        for (String key : this.phrases.keySet()) {
            if (query.contains(key)) return true;
        }
        return false;
    }

}
