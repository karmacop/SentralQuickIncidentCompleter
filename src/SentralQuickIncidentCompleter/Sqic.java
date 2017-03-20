/*
 * Copyright (C) 2017 Glenn Tester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package SentralQuickIncidentCompleter;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 *
 * @author Glenn Tester
 */
public class Sqic {
    
    static String proxyUrl;
    static String proxyPort;
    static String ernUser;
    static String ernPass;
    static String sentralUser;
    static String sentralPass;
    static File ernFile;
    
    static String responseCSV;
    
    static Gson gson;
    static GsonBuilder gsonBuilder;
    static String inputJson;
    final static String config = "Sqic.json";
    static SqicConfig SqicConfig;
    static Handler fileLoggerHandler;
    
    static String reportName;
    
    final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    
    final static Logger sqicLogger = Logger.getLogger(Sqic.class.getName());
    
    static HtmlPage mainPage;
    static HtmlPage framePage;
    
    public static void main(String[] args) {
        
        ConsoleHandler handler = new ConsoleHandler();
        sqicLogger.setLevel(Level.ALL);
        
        SqicConfig = new SqicConfig();
        
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.setPrettyPrinting().create();
        
        reportName = "Sqic-"+dateFormat.format(new Date());
        
        loadConfig();
        sqicLogger.log(Level.INFO, "Config Loaded");
        
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
        if(SqicConfig.useProxy) {
            
            sqicLogger.log(Level.INFO, "Using Proxy\nhost:{0}\nport:{1}\nusername:{2}",new String[]{SqicConfig.proxyHost,""+SqicConfig.proxyPort,SqicConfig.proxyUsername});
            ProxyConfig proxyConfig = new ProxyConfig(SqicConfig.proxyHost, SqicConfig.proxyPort);
            webClient.getOptions().setProxyConfig(proxyConfig);
            DefaultCredentialsProvider defaultCredentialsProvider = new DefaultCredentialsProvider();
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            Credentials credentials = new UsernamePasswordCredentials(SqicConfig.proxyUsername, SqicConfig.proxyPassword);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            webClient.setCredentialsProvider(credentialsProvider);
        }
        webClient.getOptions().setTimeout(60000);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        //Web client vomits messages. Lets stop it.
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("com.apache.commons.httpclient").setLevel(Level.OFF);
        webClient.setIncorrectnessListener((String arg0, Object arg1) -> {
        });
        //webClient.getOptions().setThrowExceptionOnScriptError(false);
        //webClient.getOptions().setThrowExceptionOnFailingStatusCode(false); //setExceptionOnFailingStatusCode(false);
        
        //NullJavaScriptErrorListener jsel = new NullJavaScriptErrorListener();
        //webClient.setJavaScriptErrorListener(jsel);
        webClient.setCssErrorHandler(new ErrorHandler() {
            @Override
            public void warning(CSSParseException csspe) throws CSSException {
            }

            @Override
            public void error(CSSParseException csspe) throws CSSException {
            }

            @Override
            public void fatalError(CSSParseException csspe) throws CSSException {
            }
        });
        
        
        sqicLogger.setLevel(SqicConfig.getLevel());
        sqicLogger.log(Level.INFO, "Info Test {0}","param0");
        sqicLogger.log(Level.WARNING, "Warning Test {0}","param0");
        sqicLogger.log(Level.SEVERE, "Severe Test {0}","param0");
        sqicLogger.log(Level.ALL, "All Test {0}","param0"); 
        
        if(true) {
            sqicLogger.log(Level.INFO, "Logging in to Sentral");
            boolean loggedInSentral = false;
            int logInTriesSentral = 0;
            while(!loggedInSentral && logInTriesSentral <3) {
                sqicLogger.log(Level.INFO, "Trying to log in with {0}", SqicConfig.sentralUsername);
                logInTriesSentral ++;
                loggedInSentral = logIntoSentral(webClient);
            }

            if(loggedInSentral) {
                sqicLogger.log(Level.INFO, "Logged in to Sentral");
                try {
                    HtmlPage page;
                    if(SqicConfig.editIncidents) {
                        sqicLogger.log(Level.INFO, "Editting Quick incidents");
                        page = webClient.getPage(SqicConfig.sentralAddress+SqicConfig.reportURL);
                        DomNodeList<DomElement> tables = page.getElementsByTagName("table");
                        for (DomElement table : tables) {
                            if(table.hasAttribute("class")&&table.getAttribute("class").contains("table-striped")){
                                HtmlTableBody tableBody = (HtmlTableBody) ((HtmlTable) table).getBodies().get(0);
                                HtmlTableRow row;
                                ArrayList<Integer> incidentIDs = new ArrayList<>();
                                for (int i = 1; i < tableBody.getRows().size(); i++) {
                                    row = tableBody.getRows().get(i);
                                    int incId = Integer.valueOf(row.getCells().get(2).asText().trim().split("#")[1].split("\r")[0].trim());
                                    incidentIDs.add(incId);
                                }    
                                Collections.sort(incidentIDs);
                                ExecutorService executor = Executors.newFixedThreadPool(1);
                                //For every incident in the pool, creater a worker thread
                                for (int incidentID : incidentIDs ) {
                                    if(incidentID >= SqicConfig.startFromIncident) {
                                        Runnable worker = new IncidentWorker(incidentID,SqicConfig.sentralAddress+SqicConfig.incientViewUrl+incidentID,webClient,sqicLogger);
                                        executor.execute(worker);
                                    }
                                  }
                                executor.shutdown();
                                try {
                                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                                } catch (InterruptedException ex) {
                                      sqicLogger.log(Level.SEVERE, "Task Interupted: {0}",ex);
                                }
                                sqicLogger.log(Level.INFO, "Finished all incidents");
                                SqicConfig.startFromIncident = incidentIDs.get(incidentIDs.size()-1)+1;

                                saveConfig();
                            }
                        }
                    }
                    B:if(SqicConfig.removeOverAwarded) {
                        //System.out.println("Here again!");
                        sqicLogger.log(Level.INFO, "removing over awarded");
                        page = webClient.getPage(SqicConfig.sentralAddress+SqicConfig.overAwardedURL);
                        
                        while(page.getElementsByTagName("table").size()>0){
                            //System.out.println("getting here");
                            DomNodeList<DomElement> overAwardedTables = page.getElementsByTagName("table");
                            A: for (DomElement table : overAwardedTables) {
                                if(table.hasAttribute("class")&&table.getAttribute("class").contains("table-striped")){
                                    HtmlTableBody tableBody = (HtmlTableBody) ((HtmlTable) table).getBodies().get(0);
                                    HtmlTableRow row;
                                    String student = "no name";
                                    int expected = 0;
                                    int current = 0;
                                    for (int i = 0; i < tableBody.getRows().size(); i++) {
                                        //System.out.println("looking at row "+ i +" of "+tableBody.getRows().size() );
                                        row = tableBody.getRows().get(i);
                                        //System.out.println("row "+i+": "+row.asText());
                                        if(row.getCells().size()>=3) {
                                            if(row.getCells().get(2).asText().contains("Nomination") && row.getCells().get(3).asText().contains("Nomination") ) {
                                                student = row.getCells().get(0).asText();
                                                expected = Integer.parseInt(row.getCells().get(2).asText().split(" Nomination")[0]);
                                                current = Integer.parseInt(row.getCells().get(3).asText().split(" Nomination")[0]);
                                            }
                                            /*
                                            if(row.getCells().get(2).getElementsByTagName("input").size() > 0) {
                                                DomNodeList<HtmlElement> inputs = row.getCells().get(2).getElementsByTagName("input");

                                                for (HtmlElement input : inputs) {

                                                    if(input.getAttribute("onclick").contains("removeStudentNomination")) {

                                                    }

                                                }
                                            }
                                            */

                                        } if(row.getCells().size() == 1 || i >= tableBody.getRows().size()-1 ) {
                                            //System.out.println("getting ready to remove");
                                            if(i >= tableBody.getRows().size()-1) {
                                                //System.out.println("we found the last row");
                                                i=i+1;
                                            }
                                            if(tableBody.getRows().get(i-1).getCells().get(0).asText().contains("No over awarded nominations found")) {
                                                break B;
                                            }
                                            if(i >0 && tableBody.getRows().get(i-1).getCells().get(2).getElementsByTagName("input").size() >= 2) {
                                                DomNodeList<HtmlElement> inputs = tableBody.getRows().get(i-1).getCells().get(2).getElementsByTagName("input");

                                                for (HtmlElement input : inputs) {
                                                    if(input.getAttribute("onclick").contains("removeStudentNomination")) {
                                                        sqicLogger.log(Level.INFO, student+ " expected " +expected+" but has "+ current);
                                                        page = input.click();
                                                        //msecWait(3000);
                                                        break A;
                                                    }

                                                }
                                            }
                                        }
                                    }    
                                }
                            }
                        }
                    }
                    //System.out.println("Why are we here already?");
                    //run the over awarded page to remove people with awards which hasve since been removed
                    //page = webClient.getPage(SqicConfig.sentralAddress+SqicConfig.reportURL);
                    //go to the awards page to approve all rewards
                    //page = webClient.getPage(SqicConfig.sentralAddress+SqicConfig.reportURL);
                } catch (IOException ex) {
                    sqicLogger.log(Level.SEVERE, "IO Exception:{0}",ex);
                }
            } else {
                sqicLogger.log(Level.SEVERE, "Unable to log in to Sentral");
            }
        }
        sqicLogger.log(Level.INFO, "Saving Data.");
        shutdown();
    }
    
    public static void msecWait(long msec) {
        try {
            Thread.sleep(msec);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void loadConfig() {
        try {
            FileReader fr = new FileReader(config);
            BufferedReader br = new BufferedReader(fr);
            SqicConfig = gson.fromJson(br, SqicConfig.class);
            if(SqicConfig.useLog) {
                fileLoggerHandler = new FileHandler(SqicConfig.logFile, 1000000, 3, true);
                fileLoggerHandler.setFormatter(new SimpleFormatter());
                fileLoggerHandler.setLevel(SqicConfig.getLevel());
                sqicLogger.addHandler(fileLoggerHandler);
                sqicLogger.log(Level.INFO, "Writing log to file {0}", SqicConfig.logFile);
            }
            sqicLogger.setLevel(Level.ALL);
        } catch(FileNotFoundException ex ) {
            sqicLogger.log(Level.INFO, "Config not found. Creating config.\n");
            saveConfig();
            shutdown();
        } catch(JsonIOException | JsonSyntaxException ex) {
            sqicLogger.log(Level.SEVERE, "Problem reading config file.\n{0}", ex.getCause());
            shutdown();
        } catch (IOException ex) {
            sqicLogger.log(Level.SEVERE, "Could not create log file.\n{0}", ex.getCause());
        } catch (SecurityException ex) {
            sqicLogger.log(Level.SEVERE, "Security issue with log file\n{0}", ex.getCause());
        }
    }
    
    public static void saveConfig() {
        FileWriter fw = null;
        BufferedWriter bw = null;
        
        File file = new File(config);
        if (file.exists()) {
            File file2 = new File(config+".old");
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
        }
        
        try {
            fw = new FileWriter(config);
            bw = new BufferedWriter(fw);
            bw.write(gson.toJson(SqicConfig));
        } catch (IOException ex) {
            sqicLogger.log(Level.WARNING, "Unable to saveConfig.", ex);
        } finally {
            try{
                if(bw != null) {
                    bw.close();
                }
                if(fw != null) {
                    fw.close();
                }
            } catch(Exception ex) {
                sqicLogger.severe("Writing file didn't complete correctly.");
            }
        }
    }
    
    public static boolean logIntoSentral(WebClient webClient) {
        sqicLogger.log(Level.INFO, "Trying to log in to Sentral server {0}", SqicConfig.sentralAddress+"dashboard/");
        final HtmlPage page;
        try {
            page = webClient.getPage(SqicConfig.sentralAddress+"dashboard/");
            if(page!=null && !page.getUrl().toString().equalsIgnoreCase(SqicConfig.sentralAddress+"dashboard/") ) {
                final HtmlForm form = page.getHtmlElementById("loginForm");       
                final HtmlTextInput textField = page.getHtmlElementById("loginUsername");
                final HtmlPasswordInput pwd = page.getHtmlElementById("loginPassword");   
                textField.click();
                textField.setValueAttribute(SqicConfig.sentralUsername);
                pwd.click();
                pwd.setValueAttribute(SqicConfig.sentralPassword);   
                ( form.getInputByValue("Login")).click();
            }
            msecWait(SqicConfig.waitTime);
        } catch(Exception ex) {
            sqicLogger.log(Level.SEVERE, "Unable to log in to Sentral Server.\n {0}", ex);
            return false;
        }
        if(page!=null && page.getUrl().toString().equalsIgnoreCase(SqicConfig.sentralAddress+"dashboard/")) {
            sqicLogger.log(Level.INFO, "Login to Sentral Successful");
            return true;
        }
        return false;
    }

    private static void shutdown() {
        sqicLogger.log(Level.INFO, "Shutting Down");
        if(fileLoggerHandler != null) {
            fileLoggerHandler.flush();
            fileLoggerHandler.close();
        }
        System.exit(0);
    }

}