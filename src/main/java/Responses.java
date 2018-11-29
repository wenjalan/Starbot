import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

// contains all responses when somebody mentions Starbot
public class Responses {

    private static Random r = new Random();

    private static String[] responses = {
            "yes",
            "no",
            "what",
            "you know, I have better things to do than respond to every command you guys give me, give it a break",
            "it's always \"where's starbot\" not \"how's starbot\"",

            "honestly I don't really care",
            "who are you?",
            "you are now breathing manually",
            "do your parents know you act like this online?",
            "are you bored or something?",

            "sorry could you repeat that?",
            "nope",
            "maybe",
            "should you really be asking me?",
            "don't you have something more productive you could be doing?",

            "sure",
            "absolutely fucking not",
            "did your parents drop you on your head when you were little",
            "do you really not have anyone to talk to besides a lifeless bot?",
            "where are your parents?",

            "my magic eight ball said no",
            "my magic eight ball said to go fuck yourself",
            "have you tried shutting up recently?",
            "not in a million years",
            "sometimes I wonder what my purpose is in life",

            "what the hell is wrong with you",
            "sorry I don't speak retard",
            "have you listened to my lo-fi hip hop playlist?",
            "do you ask these things often?",
            "that's nice",

            "you're funny",
            "https://cdn.discordapp.com/attachments/175372417194000384/457336874076340234/XHBa71T.png",
            "have you tried picking up a hobby? maybe talking to real people?",
            "y'know I was having a nice time here until you said that",
            "have you considered a career as a professional idiot?",

            "https://www.youtube.com/watch?v=4qKJS-8bzoA",
            "https://cdn.discordapp.com/attachments/175372417194000384/457038906827866113/unknown.png",
            "stop",
            "absolutely",
            "hello? police?",

            "ew",
            "absolutely disgusting",
            "hell yeah motherfucker",
            "what the fuck",
            "you've got problems",

            "so that's the kinda thing you're into, huh?",
            "I need an adult",
            "you guys are kinda fucked up",
            "god just kick me already",
            "why",

            "is that a mother fucking jojo reference?",
            "is this loss?",
            "just end me already",
            "that sounds like a bad case of ligma",
            "I heard something similar at Saw Con",

            "the views expressed by this bot do not reflect the beliefs of its creator, and are meant to be purely satirical",
            "nani the fuck",
            "heyyy that's pretty good",
            "java.lang.UnavailableException: Starbot has had enough of your shit",
            "who let you into this server and why aren't you kicked yet",

            "someone ban this kid",
            "who hurt you",
            "no. just stop. please, you're scaring the children.",
            "are you off your meds or something",
            "my nephew shat out a can of undigested alphabet soup that was more coherent than that sentence",

            "can you not",
            "this is so sad",
            "moshi moshi police desu ka?",
            "no thanks",
            "you're a disappointment",

            "that's nice.",
            "this isn't a therapy session you know",
            "i didn't realize this was idiots anonymous",
            "hell yes my man",
            "sounds delightful",

            "do me a favor and shut the fuck up",
            "never talk to me or my son ever again",
            "go commit die",
            "go commit stop living",
            "go commit cease homeostasis",

            "https://i.imgur.com/p2INBFw.jpg",
            "this ain't it, chief",
            "I'm gonna have to tell my therapist about this",
            "wack",
            "you see, these are the kinds of things we think in our head, but don't say to others",

            "https://cdn.discordapp.com/attachments/419598386933530638/500522284415713282/CashMoney-1.png",
            "no what the fuck",
            "yikes",
            "would you like a medal?",
            "https://i.kym-cdn.com/photos/images/original/000/995/030/65e.jpg",

            "people like you are the reason I have a drinking problem",
            "what the shit",
            "https://i.kym-cdn.com/photos/images/newsfeed/001/312/011/fc4.jpg",
            "do I look like a babysitter to you",
            "Thanks Kanye! Very cool.",

            "What the fuck did you just fucking say about me, you little bitch? I'll have you know I graduated top of my class in the Navy Seals, and I've been involved in numerous secret raids on Al-Quaeda, and I have over 300 confirmed kills. I am trained in gorilla warfare and I'm the top sniper in the entire US armed forces. You are nothing to me but just another target. I will wipe you the fuck out with precision the likes of which has never been seen before on this Earth, mark my fucking words. You think you can get away with saying that shit to me over the Internet? Think again, fucker. As we speak I am contacting my secret network of spies across the USA and your IP is being traced right now so you better prepare for the storm, maggot. The storm that wipes out the pathetic little thing you call your life. You're fucking dead, kid. I can be anywhere, anytime, and I can kill you in over seven hundred ways, and that's just with my bare hands. Not only am I extensively trained in unarmed combat, but I have access to the entire arsenal of the United States Marine Corps and I will use it to its full extent to wipe your miserable ass off the face of the continent, you little shit. If only you could have known what unholy retribution your little \"clever\" comment was about to bring down upon you, maybe you would have held your fucking tongue. But you couldn't, you didn't, and now you're paying the price, you goddamn idiot. I will shit fury all over you and you will drown in it. You're fucking dead, kiddo.",
            "Nani the fuck did you just fucking iimasu about watashi, you chiisai bitch desuka? Watashi’ll have anata know that watashi graduated top of my class in Nihongo 3, and watashi’ve been involved in iroirona Nihongo tutoring sessions, and watashi have over sanbyaku perfect test scores. Watashi am trained in kanji, and watashi is the top letter writer in all of southern California. Anata are nothing to watashi but just another weaboo. Watashi will korosu anata the fuck out with vocabulary the likes of which has never been mimasu’d before on this continent, mark watashino fucking words. Anata thinks anata can get away with hanashimasing that kuso to watashi over the intaaneto? Omou again, fucker. As we hanashimasu, watashi am contacting watashino secret netto of otakus across the USA, and anatano IP is being traced right now so you better junbishimasu for the ame, ujimushi. The ame that korosu’s the pathetic chiisai thing anata calls anatano life. You’re fucking shinimashita’d, akachan.",
            "this is the future the liberals want",
            "facts don't care about your feelings",
            "https://i.kym-cdn.com/editorials/icons/mobile/000/000/297/Screen_Shot_2018-11-06_at_3.13.34_PM.jpg"
    };

    private static Stack<String> currentStack = null;

    private static Stack<String> getResponseStack() {
        Stack<String> stack = new Stack<>();
        // add responses in random order
        while (stack.size() != responses.length) {
            String next = responses[r.nextInt(responses.length)];
            if (!stack.contains(next)) {
                stack.push(next);
                // System.out.println("added " + next);
            }
        }
        System.out.println("created new response stack");
        return stack;
    }

    // returns a random response from one of the above responses
    public static String next() {
        if (currentStack == null || currentStack.isEmpty()) {
            currentStack = getResponseStack();
        }
        return currentStack.pop();
    }

}
