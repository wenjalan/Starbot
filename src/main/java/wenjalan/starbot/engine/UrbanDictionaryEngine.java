package wenjalan.starbot.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UrbanDictionaryEngine {

    // defines a given query on Urban Dictionary
    // returns a list of definitions
    // code courtesy of jonathan (Discord: Serin#6268)
    public static List<String> define(String query) {
        List<String> ret = new ArrayList<>();
        try {
            String term =  query;
            URL mainLink = new URL("http://api.urbandictionary.com/v0/define?term=" + term);
            BufferedReader br = new BufferedReader(new InputStreamReader(mainLink.openStream()));
            String input = br.readLine();
            if (input.equals("{\"list\":[]}")) {
                return Arrays.asList("idiot");
            }
            Scanner justForYouAlan = new Scanner(input);
            String firstWord = justForYouAlan.next();
            firstWord = firstWord.substring(23);
            String token = justForYouAlan.next();
            for (int i = 0; i < input.length(); i++) {
                if (token.charAt(0) == '[' && token.charAt(token.length()-1) == ']' ) {
                    token = token.substring(1,token.length()-1);
                }
                else if (token.charAt(0) == '[' && token.charAt(token.length()-2) == ']' ) {
                    char s = token.charAt(token.length()-1);
                    token = token.substring(1, token.length()-2);
                    token = token + s;
                }
                if (token.length() < 13) {
                    firstWord = firstWord + " " + token;
                }
                else {
                    if (token.contains("\",\"permalink\"")) {
                        char[] chars = token.toCharArray();
                        String local = "";
                        for (int j = 0; j < token.indexOf("\",\"permalink\""); j++) {
                            local = local + chars[j];
                        }
                        if (local.charAt(0) == '[' && local.charAt(local.length()-2) == ']') {
                            char s = local.charAt(local.length()-1);
                            local = local.substring(1,local.length()-2);
                            local = local + s;
                        }
                        ret.add(firstWord + " " + local + "\"");
                        i = input.length();
                    }
                    else {
                        firstWord = firstWord + " " + token;
                    }
                }
                token = justForYouAlan.next();
            }
            br.close();
        } catch (Exception e) {
            System.err.println("jonathan's code fucked up");
            e.printStackTrace();
        }
        return ret;
    }

}
