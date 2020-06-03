import application.BotsDriver;
import application.FirebaseDB;
import application.Log;

import java.util.*;

public class TalkingBots {
    private List<String> notEnglishResponses;
    private int notEnglishIndex;
    
    private List<String> whatIsResponses;
    private int whatIsIndex;
    
    private String msg;
    
    private volatile List<String> messageBuffer;
    private final int MSG_BUFFER_SIZE;
    
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
    
    private static FirebaseDB db;
    
    private final Object BUFFER_LOCK;
    private final Object MESSAGE_LOCK;
    
    public TalkingBots() {
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
        
        messageBuffer = new ArrayList<>();
        MSG_BUFFER_SIZE = 10;
        
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
        
        BUFFER_LOCK = new Object();
        MESSAGE_LOCK = new Object();
        
        initBufferList();
    }
    
    public static void initFirebase(String filename, String url) {
        db = new FirebaseDB(filename, url);
    }
    
    private void initBufferList() {
        for (int i = 0; i < MSG_BUFFER_SIZE; ++i) {
            messageBuffer.add("");
        }
    }
    
    public static void main(String[] args) {
        initFirebase("admin1.json", "https://startbots-81ecb.firebaseio.com/");
        new TalkingBots().testTwoBots();
    }
    
    private void testDatabase() {
        new FirebaseDB("admin1.json", "https://startbots-81ecb.firebaseio.com/").start();
    }
    
    private void asyncMessageInjector() {
        Thread t = new Thread(() -> {
            while (true) {
                String change = scn.nextLine();
                if (change.equals(".consume")) {
                    messageBuffer.set(MSG_BUFFER_SIZE - 1, "");
                    System.out.println(consumeMessage());
                    continue;
                }
                System.out.println("Message set.");
                switchMsg = change;
                log.log("User entered message to change: \"" + change + "\"");
            }
        });
        t.start();
    }
    
    private void asyncBotsLoop() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    handleBot(bot1);
                    log.log("Test 1: " + msg);
                    Thread.sleep(100);
                    handleBot(bot2);
                    log.log("Test 2: " + msg);
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
    
    private void asyncBufferGenerator() {
        Thread t = new Thread(() -> {
            while (true) {
                synchronized (MESSAGE_LOCK) {
                    if (messageBuffer.get(MSG_BUFFER_SIZE - 1).equals("")) {
                        synchronized (BUFFER_LOCK) {
                            BUFFER_LOCK.notify();
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        });
        t.start();
    }
    
    private void asyncDatabaseHandler() {
        Thread t = new Thread(() -> {
            boolean startFlag = true;
            
            while (startFlag) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (MESSAGE_LOCK) {
                    if (messageBuffer.get(MSG_BUFFER_SIZE - 1).isEmpty()) {
                        continue;
                    }
                }
                startFlag = false;
                db.putMessage("msg1", consumeMessage());
                db.putMessage("msg2", consumeMessage());
                db.putMessage("msg1", consumeMessage(), "bufferMsg");
                db.putMessage("msg2", consumeMessage(), "bufferMsg");
                //db.putMessage(consumeMessage(), "bufferMsg");
            }
            System.out.println("Ready to listen for requests.");
            db.setListening(true);
            while (true) {
                if (db.getRequestNumber() == 1) {
                    db.putMessage("msg1", consumeMessage(), "bufferMsg");
                    db.disableFlag();
                }
                if (db.getRequestNumber() == 2) {
                    db.putMessage("msg2", consumeMessage(), "bufferMsg");
                    db.disableFlag();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
    
    private void testTwoBots() {
        asyncMessageInjector();
        asyncBotsLoop();
        asyncBufferGenerator();
        asyncDatabaseHandler();
    }
    
    private void handleBehavior() {
        checkWhatIs();
        if (clearDataFlag) {
            System.out.println("Will clear data");
            bot1.setClearDataFlag(true);
            bot2.setClearDataFlag(true);
            clearDataFlag = false;
        }
        
    }
    
    private void handleBot(BotsDriver bot) {
        synchronized (BUFFER_LOCK) {
            try {
                BUFFER_LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        checkAlerts();
        updateBuffer();
        //changeMsgChain();
    }
    
    private String consumeMessage() {
        String message = "";
        synchronized (MESSAGE_LOCK) {
            for (int i = MSG_BUFFER_SIZE - 1; i >= 0; --i) {
                if (messageBuffer.get(i).isEmpty()) {
                    continue;
                }
                message = messageBuffer.get(i);
                messageBuffer.set(i, "");
                break;
            }
        }
        message = validateMessage(message);
        return message;
    }
    
    private String validateMessage(String message) {
        //if (message.replaceAll("\\p{Punct}", "").replaceAll("\\p{Blank}", "").length() < 10) {
        //    message = "Short response: " + message;
        //    message = "<speak><break time=\"0.1s\"/><prosody volume=\"x-loud\">" + message + "</prosody><break time=\"0.5s\"/></speak>";
        //}
        if (message.equals("Oh.")) {
            message = "Oh ok.";
        }
        if (message.equals("How?")) {
            message = "How is it?";
        }
    
        if (message.equals("Who?")) {
            message = "Who is it?";
        }
    
        if (message.equals("Why?")) {
            message = "Why is that?";
        }
        
        if (message.toLowerCase().contains("bye")
                || message.toLowerCase().contains("stop")
                || message.toLowerCase().contains("close")) {
            message = message.toLowerCase()
                    .replaceAll("bye", " b b y y e")
                    .replaceAll("stop", "s t t o p")
                    .replaceAll("close", "c l l o s e e");
        }
        if (message.toLowerCase().contains("lol")) {
            message = message.toLowerCase()
                    .replaceAll("lol", " *laughting out loud* ");
        }
        if (message.toLowerCase().replaceAll("\\p{Blank}", "").contains("nevermind")) {
            message = message.toLowerCase().replaceAll("mind", "m i i n d");
        }
        return message;
    }
    
    private void updateBuffer() {
        synchronized (MESSAGE_LOCK) {
            for (int i = MSG_BUFFER_SIZE - 1; i > 0; --i) {
                messageBuffer.set(i, messageBuffer.get(i - 1));
            }
            messageBuffer.set(0, msg);
        }
    }
    
    private void checkAlerts() {
        if (BotsDriver.getAlertsCounter() != 0) {
            ++messagesSinceAlert;
            System.out.println("mesagesSinceAlert: " + messagesSinceAlert);
            System.out.println("alertsCounter: " + BotsDriver.getAlertsCounter());
        }
        if (messagesSinceAlert > 50 && !generateRandomCount) {
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
        application.BotsDriver bot1 = new application.BotsDriver();
        application.BotsDriver bot2 = new application.BotsDriver();
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
