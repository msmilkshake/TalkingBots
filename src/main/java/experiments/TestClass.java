package experiments;

import application.FirebaseDB;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TestClass {
    
    private Scanner scn;
    
    public TestClass() {
        scn = new Scanner(System.in);
    }
    
    public static void main(String[] args) {
        new TestClass().testDB();
    }
    
    private void testDB() {
        FirebaseDB db = new FirebaseDB("admin1.json", "https://startbots-81ecb.firebaseio.com/");
        while (true) {
            db.putMessage("msg", scn.nextLine());
        }
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
