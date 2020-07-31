package application;

import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class BotsDriver {
    private static boolean propertySet = false;
    
    private WebDriver mDriver;
    private WebDriverWait mWait;
    private WebDriverWait mWaitForLang;
    
    private ClipboardManager clipboard;
    
    private GetLang getLang;
    
    public Map<String, String> mTabHandles;
    private String currentTab;
    
    private static int alertsCounter = 0;
    
    private boolean clearDataFlag;
    
    public BotsDriver() {
        setChromeDriverProperty();
        ChromeOptions opt = new ChromeOptions();
        opt.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        mDriver = new ChromeDriver(opt);
        mDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
    
        clipboard = new ClipboardManager();
        getLang = new GetLang();
        
        clearDataFlag = false;
    
        mWait = new WebDriverWait(mDriver, 12);
        mWaitForLang = new WebDriverWait(mDriver, 5);
        mTabHandles = new HashMap<>();
        newTab("cleverbot", "https://www.cleverbot.com", true);
        newTab("translator", "https://translate.google.com");
        newTab("clearData", "chrome://settings/clearBrowserData");
        mDriver.switchTo().window(mTabHandles.get("cleverbot"));
        try {
            mDriver.findElement(By.cssSelector("#noteb > form > input[type=submit]")).click();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static void setChromeDriverProperty() {
        if (!propertySet) {
            System.setProperty("webdriver.chrome.driver", "C:/webdrivers/chromedriver.exe");
            propertySet = true;
        }
    }
    
    public void setPos(int x, int y, int w, int h) {
        mDriver.manage().window().setPosition(new Point(x, y));
        mDriver.manage().window().setSize(new Dimension(w, h));
    }
    
    public void setClearDataFlag(boolean clearDataFlag) {
        this.clearDataFlag = clearDataFlag;
    }
    public boolean isClearDataFlag() {
        return clearDataFlag;
    }
    
    public void clearBrowserData() {
        mDriver.switchTo().window(mTabHandles.get("clearData"));
        mDriver.findElement(By.xpath("//settings-ui")).sendKeys(Keys.ENTER);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDriver.navigate().to("chrome://settings/clearBrowserData");
        mDriver.switchTo().window(mTabHandles.get("cleverbot"));
        mDriver.navigate().to("https://www.cleverbot.com");
        clearDataFlag = false;
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
        clipboard.putInClipboard(input);
        try {
            Thread.sleep(10);
            textInput.sendKeys(Keys.CONTROL + "v");
            clipboard.restoreClipboard();
            Thread.sleep(10);
            try {
                textInput.sendKeys(Keys.ENTER);
                Thread.sleep(100);
            } catch (UnhandledAlertException alert) {
                System.out.println("ALERTBOX detected!!");
                ++alertsCounter;
                Thread.sleep(20);
                mDriver.switchTo().alert().accept();
                Thread.sleep(50);
                mDriver.findElement(By.name("thinkformebutton")).click();
            }
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            mWait.until(presenceOfElementLocated(By.id("snipTextIcon")));
        } catch (TimeoutException e) {
            System.out.println("Caught no scisors error!");
            mDriver.switchTo().window(mTabHandles.get("cleverbot"));
            mWait.until(presenceOfElementLocated(By.name("thinkformebutton"))).click();
            mWait.until(presenceOfElementLocated(By.id("snipTextIcon")));
        }
        return mDriver.findElement(By.id("line1")).getText();
    }
    
    public boolean isEnglish(String text) {
        mDriver.switchTo().window(mTabHandles.get("translator"));

        clipboard.putInClipboard(text);
        
        WebElement translateInput = mWait.until(presenceOfElementLocated(By.id("source")));
        translateInput.sendKeys(Keys.CONTROL + "v");
        
        clipboard.restoreClipboard();
        
        Thread t = new Thread(getLang);
        t.start();
        try {
            t.join();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String lang = getLang.getLang();
        mDriver.navigate().to("https://translate.google.com");
        mDriver.switchTo().window(mTabHandles.get("cleverbot"));
        boolean isEnglish = lang.contains("eng");
        if (!isEnglish) {
            System.out.println("Not english: " + lang + " ( in isEnglish() )");
            System.out.println("Text: \"" + text + "\"");
        }
        return isEnglish;
    }
    
    private class GetLang implements Runnable {
        private volatile String lang;
        
        public GetLang() {
            lang = null;
        }
        
        @Override
        public void run() {
            WebElement detectedLaguage = mWait.until(presenceOfElementLocated(By
                    .cssSelector("div.goog-inline-block.jfk-button.jfk-button-standard" +
                            ".jfk-button-collapse-right.jfk-button-checked")));
            int timeout = 0;
            while (!detectedLaguage.getText().toLowerCase().contains("detected")) {
                try {
                    Thread.sleep(50);
                    ++timeout;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (timeout == 60) {
                    System.out.println("Lang check timed out.");
                    lang = "eng";
                    break;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lang = detectedLaguage.getText().toLowerCase();
        }
        
        public String getLang() {
            return lang;
        }
    }
    
    public static int getAlertsCounter() {
        return alertsCounter;
    }
    
    public static void resetAlertsCounter() {
        alertsCounter = 0;
    }
    
}
