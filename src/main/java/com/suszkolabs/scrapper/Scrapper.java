package com.suszkolabs.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Scrapper {

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver webDriver = new ChromeDriver();
        webDriver.get("https://polwro.com/f,fizycy,7");

        /** login part, requires full xpath in order to work **/
        webDriver.findElement(By.xpath("/html/body/div[6]/div/table/tbody/tr/td/form/table[2]/tbody/tr[2]/td/table/tbody/tr[2]/td[2]/input"))
                .sendKeys(System.getenv("username"));
        webDriver.findElement(By.xpath("/html/body/div[6]/div/table/tbody/tr/td/form/table[2]/tbody/tr[2]/td/table/tbody/tr[3]/td[2]/input"))
                .sendKeys(System.getenv("password"));
        webDriver.findElement(By.xpath("//*[@id=\"rest\"]/div/table/tbody/tr/td/form/table[2]/tbody/tr[2]/td/table/tbody/tr[5]/td/input[2]")).click();

    }
}
