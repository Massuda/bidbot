package org.bid.thread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Cookie;
import org.bid.gui.BidBot;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public abstract class AbstractBrowser {
    BidBot bb;
    public AbstractBrowser(BidBot bb) {
        this.bb = bb;
    }

    public Page getLoggedPage(Path storageStatePath, Browser browser, String user, String pass, String url, String cookies){
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = size.width/2;
        int height = 800;
//        String currentDirectory = System.getProperty("user.dir");
//        String filePath = currentDirectory + File.separator + stateJsonFileName;
//        Path storageStatePath = Paths.get(filePath);
        Page page;
        if(Files.exists(storageStatePath)){
//            System.out.println("getLoggedPage file exists");
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(storageStatePath).setViewportSize(width, height));
            page = context.newPage();
            page.navigate(url);
        } else {
            page = browser.newPage();
            page.setViewportSize(width, height);
            page.navigate(url);
            page.locator("[class=\"btn btn-xs btn-outline\"]").click();
            page.getByPlaceholder("Username o E-mail").fill(user);//getByPlaceholder
            Page.GetByPlaceholderOptions placeholderOptions = new Page.GetByPlaceholderOptions();
            placeholderOptions.setExact(true);
            page.getByPlaceholder("Password", placeholderOptions).fill(pass);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ENTRA")).click();
            String cookieName = "nfv";
            // Save storage state into the file.
            List<Cookie> arraylist = createCookies(browser, cookies, cookieName);
            browser.contexts().get(0).addCookies(arraylist);
            browser.contexts().get(0).storageState(new BrowserContext.StorageStateOptions().setPath(storageStatePath));
        }
        return page;
    }
    public Page getLoggedPage(Path storageStatePath, BrowserContext context, String user, String pass, String url, String cookies){
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = size.width/2;
        int height = 800;
//        String currentDirectory = System.getProperty("user.dir");
//        String filePath = currentDirectory + File.separator + stateJsonFileName;
//        Path storageStatePath = Paths.get(filePath);
        Page page;
        if(Files.exists(storageStatePath)){
//            System.out.println("getLoggedPage file exists");
            page = context.newPage();
            page.navigate(url);
        } else {
            page = context.newPage();
            page.setViewportSize(width, height);
            page.navigate(url);
            page.locator("[class=\"btn btn-xs btn-outline\"]").click();
            page.getByPlaceholder("Username o E-mail").fill(user);//getByPlaceholder
            Page.GetByPlaceholderOptions placeholderOptions = new Page.GetByPlaceholderOptions();
            placeholderOptions.setExact(true);
            page.getByPlaceholder("Password", placeholderOptions).fill(pass);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ENTRA")).click();
            String cookieName = "nfv";
            // Save storage state into the file.
            List<Cookie> arraylist = createCookies(context, cookies, cookieName);
            context.addCookies(arraylist);
            context.storageState(new BrowserContext.StorageStateOptions().setPath(storageStatePath));
        }
        return page;
    }

    public String getCookieDomainValueFromBrowser(Browser browser, String cookieName) {
        for(Cookie cookie : browser.contexts().get(0).cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.domain;
            }
        }
        return "";
    }

    public String getCookieDomainValueFromBrowser(BrowserContext context, String cookieName) {
        for(Cookie cookie : context.cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.domain;
            }
        }
        return "";
    }

    public Double getCookieExpiresValueFromBrowser(Browser browser, String cookieName) {
        for(Cookie cookie : browser.contexts().get(0).cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.expires;
            }
        }
        return 0.0;
    }

    public Double getCookieExpiresValueFromBrowser(BrowserContext context, String cookieName) {
        for(Cookie cookie : context.cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.expires;
            }
        }
        return 0.0;
    }

    public String getCookiePathValueFromBrowser(Browser browser, String cookieName) {
        for(Cookie cookie : browser.contexts().get(0).cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.path;
            }
        }
        return "";
    }

    public String getCookiePathValueFromBrowser(BrowserContext context, String cookieName) {
        for(Cookie cookie : context.cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.path;
            }
        }
        return "";
    }

    public Boolean getCookieHttpOnlyValueFromBrowser(Browser browser, String cookieName) {
        for(Cookie cookie : browser.contexts().get(0).cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.httpOnly;
            }
        }
        return false;
    }

    public Boolean getCookieSecureValueFromBrowser(Browser browser, String cookieName) {
        for(Cookie cookie : browser.contexts().get(0).cookies()){
            if(cookie.name.equalsIgnoreCase(cookieName)){
                return cookie.secure;
            }
        }
        return false;
    }

    public boolean isCookieExpired(Path storageStatePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File fileToDelete = new File(storageStatePath.toUri());
        if (fileToDelete.exists()) {
            JsonNode rootNode = objectMapper.readTree(new File(storageStatePath.toUri()));
            JsonNode cookies = rootNode.get("cookies");

            if (cookies != null && cookies.isArray()) {
                long currentTime = new Date().getTime() / 1000;  // Timestamp UNIX in secondi
                long nextExpiration = cookies.get(0).get("expires").asLong();
                for (JsonNode cookie : cookies) {
                    String name = cookie.get("name").asText();
//                    if (name.equals(cookieName)) {
                    long expiresTimestamp = cookie.get("expires").asLong();
                    if(nextExpiration > expiresTimestamp){
                        nextExpiration=expiresTimestamp;
                    }
                    if (expiresTimestamp!=-1 && currentTime > expiresTimestamp) {
                        System.out.println("This cookie is expired: "+name);
//                            System.out.println("Cookie not Expired. expiresTimestamp: "+expiresTimestamp+" > currentTime: "+currentTime);
                        return true;  // scaduto
                    }
//                    }
                }
                System.out.println("Cookie next expiration time: "+new java.util.Date(nextExpiration*1000));
            }
        }
        return false;  // nessun cookie è scaduto
    }

    public Map<String, Double> getCookiesMapName2Expire(Browser browser){
        Map<String, Double> name2Expire = new java.util.TreeMap<>();
        for(Cookie cookie : browser.contexts().get(0).cookies()){
            name2Expire.put(cookie.name, cookie.expires);
        }
        return name2Expire;
    }

    public boolean isCookieExpired(BrowserContext context) {
        Cookie minCookie = getMinExpireCookie(context);
        System.out.println("Next cookie to expire: "+minCookie.name+" -> "+new java.util.Date(minCookie.expires.longValue()*1000));
        long currentTime = new Date().getTime() / 1000;
        long expiresTimestamp = minCookie.expires.longValue();
        if(expiresTimestamp != -1 && currentTime > expiresTimestamp){
            System.out.println("Cookie scaduto: "+minCookie.name+" -> "+new java.util.Date(expiresTimestamp*1000));
            return true;
        }
        return false;
    }

    public Cookie getMinExpireCookie(BrowserContext context){
        Cookie cookieToReturn = null;
        Double minExpire = context.cookies().get(0).expires;
        for(Cookie cookie : context.cookies()){
            if(cookie.expires<minExpire)
                cookieToReturn = cookie;
        }
        return cookieToReturn;
    }

    public String cookieInfo(Cookie cookie){
        String name = cookie.name;
        double expiresTimestamp = cookie.expires;
        return "Name: "+name+", expires: "+expiresTimestamp;
    }

    public List<Cookie> createCookies(Browser browser, String cookiesString, String cookieName){
        List<Cookie> arraylist = new ArrayList<>();

        double expiresDate = getCookieExpiresValueFromBrowser(browser, cookieName);
        String domain = getCookieDomainValueFromBrowser(browser, cookieName);
        String path = getCookiePathValueFromBrowser(browser, cookieName);
//        System.out.println("expiresDate "+expiresDate +", domain "+ domain +", path "+ path);
        for (String s : cookiesString.split("; ")) {
            Cookie cookie = new Cookie(s.split("=")[0], s.split("=")[1]);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setExpires(expiresDate);
            cookie.setHttpOnly(false);
            cookie.setSecure(false);
            arraylist.add(cookie);
        }

        return arraylist;
    }

    public List<Cookie> createCookies(BrowserContext context, String cookiesString, String cookieName){
        List<Cookie> arraylist = new ArrayList<>();

        double expiresDate = getCookieExpiresValueFromBrowser(context, cookieName);
        String domain = getCookieDomainValueFromBrowser(context, cookieName);
        String path = getCookiePathValueFromBrowser(context, cookieName);
//        System.out.println("expiresDate "+expiresDate +", domain "+ domain +", path "+ path);
        for (String s : cookiesString.split("; ")) {
            Cookie cookie = new Cookie(s.split("=")[0], s.split("=")[1]);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setExpires(expiresDate);
            cookie.setHttpOnly(false);
            cookie.setSecure(false);
            arraylist.add(cookie);
        }

        return arraylist;
    }
}
