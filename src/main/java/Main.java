import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Driver;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class Main {
    
    public static void main(String[] args) {
        new Main().tests();
    }
    
    private void tests() {
        System.setProperty("webdriver.chrome.driver", "C:/webdrivers/chromedriver.exe");
        ChromeOptions opt = new ChromeOptions();
        opt.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        WebDriver driver = new ChromeDriver(opt);
        WebDriverWait wait = new WebDriverWait(driver, 20);
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        driver.get("chrome://settings/clearBrowserData");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.switchTo().activeElement();
        driver.findElement(By.xpath("//settings-ui")).sendKeys(Keys.ENTER);
        System.out.println("found");
    }
    
}
