package org.bid.thread;

import com.microsoft.playwright.*;
import org.bid.PropertiesLoader;
import org.bid.gui.BidBot;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BidCounter extends AbstractBrowser implements Runnable {

    public BidCounter(BidBot bb) {
        super(bb);
    }

    @Override
    public void run() {
        try (Playwright playwright = Playwright.create()) {
            Map<String, String> bidMap = new java.util.HashMap<>(Collections.emptyMap());

            PropertiesLoader propLoader = new PropertiesLoader();
            String url = propLoader.prop.getProperty("url");

            Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(50));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(url);
            page.waitForSelector("table#DStorico");

            bb.isThread2Running = true;
            bb.startThread2Button.setEnabled(false);
            bb.stopThread2Button.setEnabled(true);

            while(bb.isThread2Running) {
                List<ElementHandle> rows = page.querySelectorAll("table#DStorico tbody tr");
                List<String> biddingUsers = new java.util.ArrayList<>();
                if (!rows.isEmpty()) {
                    if(bb.isSelectedManualAlertSound && rows.get(0).textContent().contains("Manuale") && rows.get(1).textContent().contains("Manuale") && rows.get(2).textContent().contains("Manuale")){
                        bb.soundAlert("/manual-sound.wav");
                    }

                    for (ElementHandle row : rows) {
                        String rowData = row.textContent();
                        rowData = rowData.replace("Auto", "-");
                        rowData = rowData.replace("Manuale", "-");
                        String bidPrice = rowData.split("-")[0];
                        String bidTime = rowData.split("-")[1].substring(0, 8);
                        String bidUser = rowData.split("-")[1].substring(8);
                        biddingUsers.add(bidUser);
                        String key = bidPrice + "-" + bidTime;
                        bidMap.put(key, bidUser);
                    }
                    Map<String, Integer> userBids = countUserBids(bidMap);
                    bb.thread2TextArea.setText("");
                    bb.appendTextToTextArea(bb.thread2TextArea, "----------" + new Timestamp(System.currentTimeMillis()) + "----------");
                    Map<String, Integer> lastUsersBids = userBids.entrySet()
                            .stream()
                            .filter(entry -> biddingUsers.contains(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    for (Map.Entry mp : lastUsersBids.entrySet()) {
                        bb.appendTextToTextArea(bb.thread2TextArea, mp.getKey() + "\t" + mp.getValue());
                    }

                    String winnerSelectorName = "p.text-center strong";
                    ElementHandle winnerSelectorNameElement = page.querySelector(winnerSelectorName);
                    if (winnerSelectorNameElement != null && !winnerSelectorNameElement.textContent().isEmpty()) {
                        String winnerSelectorNameText = winnerSelectorNameElement.textContent();
                        bb.appendTextToTextArea(bb.thread2TextArea, "Ha vinto: " + winnerSelectorNameText);
                        page.close();
                        browser.close();
                        bb.isThread2Running=false;
                        bidMap.clear();
                        break;
                    }

                } else {
                    bb.appendTextToTextArea(bb.thread2TextArea, "Nessuna riga nel tbody trovata.");
                }
                Thread.sleep(10000);
            }
            bb.startThread2Button.setEnabled(true);
            bb.stopThread2Button.setEnabled(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> countUserBids(Map<String, String> bidMap){
        Map<String, Integer> userBidsMap = new java.util.TreeMap<>();
        for(String key : bidMap.keySet()){
            String user = bidMap.get(key);
            if(!userBidsMap.containsKey(user)){
                userBidsMap.put(user, 1);
            } else {
                userBidsMap.put(user, userBidsMap.get(user)+1);
            }
        }
        return userBidsMap;
    }
}
