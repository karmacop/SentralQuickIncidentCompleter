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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Glenn Tester
 */
public class IncidentWorker implements Runnable {

    String incidentURL;
    WebClient webClient;
    WebWindow window;
    int id;
    Logger logger;

    public IncidentWorker(int id, String incidentURL, WebClient webClient,Logger logger) {
        this.incidentURL = incidentURL;
        this.webClient = webClient;
        this.id = id;
        this.logger = logger;
    }

    @Override
    public void run() {
        boolean updated = false;
        logger.log(Level.INFO, "Starting incident {0}",id);
        try {
            window = webClient.openWindow(new URL(incidentURL), incidentURL);
            HtmlPage page = (HtmlPage) window.getEnclosedPage();
            System.out.println("Create window");
            Sqic.msecWait(2000);
            System.out.println(page.asXml().charAt(0));
            for (DomElement element : page.getElementsByTagName("button")) {
                HtmlButton button = (HtmlButton) element;
                if (button.asText().trim().equalsIgnoreCase("edit")) {
                    System.out.println("Found edit");
                    try {
                        System.out.println("Click button");
                        HtmlPage page2 = button.click();
                        Sqic.msecWait(2000);
                        System.out.println(page2.asXml().charAt(0));
                        for (DomElement buttonElement : page2.getElementsByTagName("button")) {
                            HtmlButton htmlButton = (HtmlButton) buttonElement;
                            if (htmlButton.asText().trim().equalsIgnoreCase("save")) {
                                System.out.println("Found save");
                                HtmlPage page3 = htmlButton.click();
                                Sqic.msecWait(2000);
                                System.out.println(page3.asXml().charAt(0));
                                for (DomElement buttonSaveElement : page3.getElementsByTagName("input")) {
                                    HtmlInput htmlButtonSave = (HtmlInput) buttonSaveElement;
                                    System.out.println("input:"+htmlButtonSave.asText().trim());
                                    if (htmlButtonSave.asText().trim().equalsIgnoreCase("save")) {
                                        System.out.println("Found final save");
                                        HtmlPage page4 = htmlButtonSave.click();
                                        Sqic.msecWait(2000);
                                        System.out.println(page3.asXml().charAt(0));
                                        updated = true;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        System.err.println("Didn't click");
                        logger.log(Level.SEVERE, null, ex);
                    }

                }
            }
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if(updated) {
            logger.log(Level.INFO, "Incident {0} updated",id);
        } else {
            logger.log(Level.INFO, "Incident {0} failed",id);
        }
    }
}
