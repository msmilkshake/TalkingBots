import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class BotsDriver {
    private static boolean propertySet = false;
    
    private WebDriver mDriver;
    private WebDriverWait mWait;
    
    private Clipboard clipboard;
    
    public Map<String, String> mTabHandles;
    private String currentTab;
    
    public BotsDriver() {
        setChromeDriverProperty();
        ChromeOptions opt = new ChromeOptions();
        opt.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        mDriver = new ChromeDriver(opt);
        mDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        
        mWait = new WebDriverWait(mDriver, 20);
        mTabHandles = new HashMap<>();
        newTab("cleverbot", "https://www.cleverbot.com", true);
        newTab("translator", "https://translate.google.com");
        mDriver.switchTo().window(mTabHandles.get("cleverbot"));
    }
    
    private static void setChromeDriverProperty() {
        if (!propertySet) {
            System.setProperty("webdriver.chrome.driver", "C:/webdrivers/chromedriver.exe");
            propertySet = true;
        }
    }
    
    public void start() {
        speakItalian();
    }
    
    public void newTab(String tabName, String url, Boolean flag) {
        for (String handle : mDriver.getWindowHandles()) {
            if (!mTabHandles.containsValue(handle)) {
                currentTab = handle;
            }
        }
        mDriver.switchTo().window(currentTab);
        mTabHandles.put(tabName, currentTab);
        mDriver.get(url);
    }
    
    public void newTab(String tabName, String url) {
        JavascriptExecutor js = (JavascriptExecutor) mDriver;
        js.executeScript("window.open();");
        newTab(tabName, url, true);
    }
    
    public String sendInput(String input) {
        mDriver.switchTo().window(mTabHandles.get("cleverbot"));
        WebElement textInput = mWait.until(presenceOfElementLocated(By.name("stimulus")));
        StringSelection ss = new StringSelection(input);
        clipboard.setContents(ss, null);
        try {
            Thread.sleep(50);
            textInput.sendKeys(Keys.CONTROL + "v");
            Thread.sleep(50);
            try {
                textInput.sendKeys(Keys.ENTER);
                Thread.sleep(150);
            } catch (UnhandledAlertException e) {
                System.out.println("ALERTBOX detected!!");
                Thread.sleep(2000);
                mDriver.switchTo().alert().accept();
                Thread.sleep(100);
                mDriver.findElement(By.name("thinkformebutton")).click();
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        mWait.until(presenceOfElementLocated(By.id("snipTextIcon")));
        String response = mDriver.findElement(By.id("line1")).getText();
        return response;
    }
    
    public boolean isAlertPresent() {
        try {
            mDriver.switchTo().alert();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public boolean isEnglish(String text) {
        mDriver.switchTo().window(mTabHandles.get("translator"));
        WebElement translateInput = mWait.until(presenceOfElementLocated(By.id("source")));
        
        WebElement clearBtn = mDriver.findElement(By.className("clear-wrap"));
        clearBtn.click();
        
        StringSelection ss = new StringSelection(text);
        clipboard.setContents(ss, null);
        translateInput.sendKeys(Keys.CONTROL + "v");
        GetLang getLang = new GetLang();
        
        Thread t = new Thread(getLang);
        t.start();
        try {
            t.join();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String lang = getLang.getLang();
        mDriver.navigate().to("https://translate.google.com");
        return lang.contains("english");
    }
    
    private void speakItalian() {
        //WebElement textInput = mDriver.findElement(By.name("stimulus"));
        //textInput.sendKeys("Pianoforte canzone amore." + Keys.ENTER);
        //mWait.until(presenceOfElementLocated(By.id("snipTextIcon")));
        //WebElement text = mDriver.findElement(By.id("line1"));
        //System.out.println(text.getText());
        
        try {
            mWait.until(presenceOfElementLocated(By.id("source")));
            System.out.println("Found");
            WebElement translateInput = mDriver.findElement(By.id("source"));
            
            String ita = "Pianoforte canzone amore.";
            String eng = "2hatr you do, just yes.";
            StringSelection ss = new StringSelection(eng);
            
            clipboard.setContents(ss, null);
            translateInput.sendKeys(Keys.CONTROL + "v");
            
            GetLang getLang = new GetLang();
            Thread t = new Thread(getLang);
            
            t.start();
            t.join();
            
            String lang = getLang.getLang();
            System.out.println(lang);
            if (!lang.contains("english")) {
                System.out.println("Text was not in english.");
            } else {
                System.out.println("Text was in english");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private class GetLang implements Runnable {
        private volatile String lang;
        private WebElement detectedLaguage;
        
        public GetLang() {
            lang = null;
            detectedLaguage = mDriver.findElement(By
                    .cssSelector("div.goog-inline-block.jfk-button.jfk-button-standard" +
                            ".jfk-button-collapse-right.jfk-button-checked"));
        }
        
        @Override
        public void run() {
            while (!detectedLaguage.getText().toLowerCase().contains("detected")) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lang = detectedLaguage.getText().toLowerCase();
            
        }
        
        public String getLang() {
            return lang;
        }
    }
}
