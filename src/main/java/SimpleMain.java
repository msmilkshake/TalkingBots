import java.util.*;

public class SimpleMain {
    private List<String> notEnglishResponses;
    private int notEnglishIndex;
    private String msg;
    
    private Log log;
    
    public SimpleMain() {
        notEnglishResponses = new ArrayList<>(Arrays.asList(
                "Please, speak in english.",
                "I can't speak other languages.",
                "Can you please speak in english?",
                "Hey, keep our conversation in english.",
                "What language is that?",
                "Uhhh, please in english."));
        Collections.shuffle(notEnglishResponses);
        msg = "Hello there!";
        notEnglishIndex = 0;
        
        log = new Log();
        log.log(msg);
    }
    
    public static void main(String[] args) {
        new SimpleMain().testTwoBots();
    }
    
    private void testDatabase() {
        new TestFirebaseFlagData().start();
    }
    
    private void testChrome() {
        new BotsDriver().start();
    }
    
    private void testTwoBots() {
        BotsDriver bot1 = new BotsDriver();
        BotsDriver bot2 = new BotsDriver();
        while (true) {
            try {
                handleBot(bot1);
                log.log("Bot 1: " + msg);
                Thread.sleep(2000);
                handleBot(bot2);
                log.log("Bot 2: " + msg);
                Thread.sleep(2000);
            } catch (Exception e) {
            
            }
        }
    }
    
    private void handleBot(BotsDriver bot) {
        String response = bot.sendInput(msg);
        if (bot.isEnglish(response)) {
            msg = response;
        } else {
            System.out.println("Not English detected.");
            System.out.println("Trigger: " + msg);
            System.out.println("Msg: " + notEnglishResponses.get(notEnglishIndex));
            msg = notEnglishResponses.get(notEnglishIndex++);
            if (notEnglishIndex >= notEnglishResponses.size()) {
                notEnglishIndex = 0;
            }
        }
    }
    
    private void testSendingInput() {
        Scanner scn = new Scanner(System.in);
        BotsDriver bot1 = new BotsDriver();
        BotsDriver bot2 = new BotsDriver();
        while (true) {
            System.out.println("Enter message for bot");
            String input = scn.nextLine();
            String response = bot1.sendInput(input);
            String dbResponse = input
                    .replaceAll("cancel", "can.cel")
                    .replaceAll("goodbye", "goodb.y.e")
                    .replaceAll("bye", "b.y.e")
                    .replaceAll("exit", "ex.it")
                    .replaceAll("nevermimd", "never.mind")
                    .replaceAll("never mind", "never.mind")
                    .replaceAll("quit", "q.u.it");
            System.out.println(response);
            if (!bot1.isEnglish(response)) {
            
            }
        }
    }
}
