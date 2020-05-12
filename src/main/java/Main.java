import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class Main {
    
    public static int tabPosition = 0;
    public static int tabCount = 1;
    
    public static void main(String[] args) {
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (driver == null) {
            throw new NullPointerException("RemoteWebDriver was null.");
        }
        
        WebDriverWait wait = new WebDriverWait(driver, 20);
        
        try {
            driver.get("https://www.cleverbot.com/");
            WebElement textInput = driver.findElement(By.name("stimulus"));
            textInput.sendKeys("Pianoforte canzone amore." + Keys.ENTER);
            wait.until(presenceOfElementLocated(By.id("snipTextIcon")));
            WebElement text = driver.findElement(By.id("line1"));
            System.out.println(text.getText());
    
            String currentHandle= driver.getWindowHandle();
            try {
                Thread.sleep(1000);
                newTab(driver);
                Thread.sleep(2000);
                Set<String> handles=driver.getWindowHandles();
                for(String actual: handles)
                {
        
                    if(actual.equals(currentHandle))
                    {
                        driver.switchTo().window(actual);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
        }
    }
    
    public static void goToTab(WebDriver driver, int pos) {
        if (tabPosition == pos) {
            return;
        }
        if (tabPosition < pos) {
            while (tabPosition != pos) {
                driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "\t");
                ++tabPosition;
            }
        } else {
            while (tabPosition != pos) {
                driver.findElement(By.cssSelector("body")).sendKeys("\ue009" + "\ue008" + "\t");
                --tabPosition;
            }
        }
    }
    
    public static void newTab(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.open();");
        ++tabCount;
        ++tabPosition;
    }
}
