import java.util.*;

public class SimpleMain {
    private List<String> notEnglishResponses;
    private int notEnglishIndex;
    
    private List<String> whatIsResponses;
    private int whatIsIndex;
    
    private String msg;
    private String switchMsg;
    
    private Log log;
    
    private Scanner scn;
    
    private Integer messagesSinceAlert;
    private int randomMsgNumber;
    private boolean clearDataFlag;
    private boolean generateRandomCount;
    
    private int whatIsCount;
    private final int whatIsThreshold;
    
    private final int englishTreshold;
    private int nonEnglishCount;
    
    private BotsDriver bot1;
    private BotsDriver bot2;
    
    public SimpleMain() {
        notEnglishResponses = new ArrayList<>(Arrays.asList(
                "Please, speak in english.",
                "I can't speak other languages.",
                "Can you please speak in english?",
                "Hey, keep our conversation in english.",
                "What language is that?",
                "In english please."
        ));
        Collections.shuffle(notEnglishResponses);
        notEnglishIndex = 0;
    
        whatIsResponses = new ArrayList<>(Arrays.asList(
                "Idk.",
                "I dont have one.",
                "Why do you want to know?",
                "I can't tell you.",
                "Dunno.",
                "I'm not telling you."
        ));
        Collections.shuffle(whatIsResponses);
        whatIsIndex = 0;
        
        msg = "Hello";
        
        switchMsg = null;
        
        log = new Log();
        log.log(msg);
        scn = new Scanner(System.in);
        
        messagesSinceAlert = 0;
        randomMsgNumber = 0;
        clearDataFlag = false;
        generateRandomCount = false;
        
        whatIsCount = 0;
        whatIsThreshold = 4;
        
        englishTreshold = 3;
        nonEnglishCount = 0;
    
        bot1 = new BotsDriver();
        bot1.setPos(0, 0, 800, 860);
        bot2 = new BotsDriver();
        bot2.setPos(800, 0, 800, 860);
    }
    
    
    
    public static void main(String[] args) {
        new SimpleMain().testTwoBots();
    }
    
    private void testDatabase() {
        new TestFirebaseFlagData().start();
    }
    
    private void testTwoBots() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String change = scn.nextLine();
                    System.out.println("Message set.");
                    switchMsg = change;
                    log.log("User entered message to change: \"" + change + "\"");
                }
            }
        });
        t.start();
        
        while (true) {
            try {
                handleBot(bot1);
                log.log("Bot 1: " + msg);
                Thread.sleep(1000);
                handleBot(bot2);
                log.log("Bot 2: " + msg);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleBehavior() {
        checkWhatIs();
        if (clearDataFlag) {
            System.out.println("Will clewr data");
            bot1.setClearDataFlag(true);
            bot2.setClearDataFlag(true);
            clearDataFlag = false;
        }
        
    }
    
    private void handleBot(BotsDriver bot) {
        handleBehavior();
        if (bot.isClearDataFlag()) {
            bot.clearBrowserData();
        }
        if (switchMsg == null) {
            String response = bot.sendInput(msg);
            msg = response;
            if (bot.isEnglish(response)) {
                nonEnglishCount = 0;
            } else {
                System.out.println("Not English detected.");
                System.out.println("Trigger: " + response);
                ++nonEnglishCount;
                System.out.println("Non English repeated times:" + nonEnglishCount);
                if (nonEnglishCount >= englishTreshold) {
                    System.out.println("Changing to english");
                    System.out.println("Msg: " + notEnglishResponses.get(notEnglishIndex));
                    msg = notEnglishResponses.get(notEnglishIndex++);
                    if (notEnglishIndex >= notEnglishResponses.size()) {
                        notEnglishIndex = 0;
                    }
                }
            }
        } else {
            msg = switchMsg;
            switchMsg = null;
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        checkAlerts();
    }
    
    private void checkAlerts() {
        if (BotsDriver.getAlertsCounter() != 0) {
            ++messagesSinceAlert;
            System.out.println("mesagesSinceAlert: " + messagesSinceAlert);
            System.out.println("alertsCounter: " + BotsDriver.getAlertsCounter());
        }
        if (messagesSinceAlert > 25 && !generateRandomCount) {
            if (BotsDriver.getAlertsCounter() >= 3) {
                System.out.println("Favorable conditions to clear data");
                randomMsgNumber = new Random().nextInt(10) + 5;
                System.out.println("randomMsgNumber: " + randomMsgNumber);
                generateRandomCount = true;
                
            } else {
                BotsDriver.resetAlertsCounter();
                messagesSinceAlert = 0;
            }
        }
        if (generateRandomCount) {
            System.out.println("randomMsgNumber: " + randomMsgNumber);
            if (--randomMsgNumber == 0) {
                System.out.println("Data will be cleared!!!");
                clearDataFlag = true;
                generateRandomCount = false;
                BotsDriver.resetAlertsCounter();
                messagesSinceAlert = 0;
            }
        }
    }
    
    private void checkWhatIs() {
        if (msg.toLowerCase().contains("what is")) {
            System.out.println("What is detected.");
            ++whatIsCount;
            System.out.println("whatIsCount: " + whatIsCount);
        } else {
            whatIsCount = 0;
        }
        if (whatIsCount >= whatIsThreshold) {
            System.out.println("Breaking out of What is loop");
            msg = whatIsResponses.get(whatIsIndex++);
            if (whatIsIndex >= whatIsResponses.size()) {
                whatIsIndex = 0;
            }
            clearDataFlag = true;
            whatIsCount = 0;
        }
    }
    
/*
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
*/

    
}
