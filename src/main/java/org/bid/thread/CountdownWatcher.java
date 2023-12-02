package org.bid.thread;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import org.bid.PropertiesLoader;
import org.bid.gui.BidBot;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Random;

public class CountdownWatcher extends AbstractBrowser implements Runnable {

    public CountdownWatcher(BidBot bb) {
        super(bb);
    }

    @Override
    public void run() {
        try (Playwright playwright = Playwright.create()) {

            PropertiesLoader propLoader = new PropertiesLoader();
            String user = propLoader.prop.getProperty("username");
            String url = propLoader.prop.getProperty("url");

            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            int width = size.width/3;
            int height = 500;
            Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(0));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(url);
            page.setDefaultTimeout(10000);
            page.setViewportSize(width, height);
            int counter = 0;
            int bidAvoidedCounter = 0;

            String selector = "[class=\"text-countdown-progressbar\"]";
            bb.isThread1Running = true;
            bb.startThread1Button.setEnabled(false);
            bb.stopThread1Button.setEnabled(true);
            Robot robot = new Robot();
            robot.delay(10);
            while (bb.isThread1Running) {
                try {
                    bb.thread1TextArea.setText("");
                    Cookie minExpireCookie = getMinExpireCookie(context);
                    String name = minExpireCookie.name;
                    double expiresTimestamp = minExpireCookie.expires;
                    bb.appendTextToTextArea(bb.thread1TextArea, new Timestamp(System.currentTimeMillis())+"");
                    bb.appendTextToTextArea(bb.thread1TextArea, "Expiring cookie --> Name:"+name+", expiresTimestamp: "+new Timestamp((long)expiresTimestamp*1000));
                    bb.appendTextToTextArea(bb.thread1TextArea, "Bid #: "+counter);
                    bb.appendTextToTextArea(bb.thread1TextArea, "Bid avoided #:"+bidAvoidedCounter);

                    page.waitForSelector(selector); // Aspetta che l'elemento sia presente
                    page.waitForFunction("() => document.querySelector('.text-countdown-progressbar').textContent === '0'");
                    Random rand = new Random();
                    int int_random = rand.nextInt(100) + 400;
                    Thread.sleep(int_random);
                    Locator value1 = page.locator("[class=\"text-countdown-progressbar\"]");//class="text-countdown-progressbar"
                    String countdown = value1.textContent();
                    if(countdown.equals("0")){
                        String currentWinnerSelector = "p.auction-current-winner";
                        ElementHandle currentWinnerElement = page.querySelector(currentWinnerSelector);
                        if (currentWinnerElement != null && !currentWinnerElement.textContent().equalsIgnoreCase(user)) {
                            // Simula un click del mouse
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                            counter++;
                            bb.soundAlert("/bid-alert-sound.wav");
                        }
                    } else {
                        bidAvoidedCounter++;
                    }
                    Thread.sleep(1000);//if it has bid he must wait for the timer to reset
                } catch (PlaywrightException e) {
                    String iniziaSelector = "a.bid-button.button-default.button-rounded.button-full.ripple-button.button-big-text.auction-btn-bid.button-empty-flat.button-soon-empty-flat.bid-button-empty-soon.hidden-xs";
                    ElementHandle iniziaElement = page.querySelector(iniziaSelector);

                    String winnerSelectorName = "p.text-center strong";
                    ElementHandle winnerSelectorNameElement = page.querySelector(winnerSelectorName);

                    if (iniziaElement != null && iniziaElement.textContent().equals("INIZIA TRA POCO")) {
                        bb.thread1TextArea.setText("");
                        bb.appendTextToTextArea(bb.thread1TextArea, new Timestamp(System.currentTimeMillis())+" -> INIZIA TRA POCO");
                    } else if (winnerSelectorNameElement != null && !winnerSelectorNameElement.textContent().isEmpty()) {
                        String winnerSelectorNameText = winnerSelectorNameElement.textContent();
                        bb.appendTextToTextArea(bb.thread1TextArea, "Winner: " + winnerSelectorNameText);
                        counter = 0;
                        bidAvoidedCounter = 0;
                        //page.close();
                        //browser.close();
                        bb.isThread1Running=false;
                        break;
                    }
                }
            }
            counter = 0;
            bidAvoidedCounter = 0;
            bb.startThread1Button.setEnabled(true);
            bb.stopThread1Button.setEnabled(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
