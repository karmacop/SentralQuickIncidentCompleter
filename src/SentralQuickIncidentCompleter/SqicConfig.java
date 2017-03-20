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

import java.util.logging.Level;
import lombok.Data;

/**
 *
 * @author Glenn Tester
 */
@Data
public class SqicConfig {
    
    //public String nesaUsername;
    //public String nesaPassword;
    public boolean useProxy;
    public String proxyUsername;
    public String proxyPassword;
    public String proxyHost;
    public int proxyPort;
    public boolean useLog;
    public String logFile;
    //public int timeout;
    //final int calendarYear;
    final int waitTime;
    public String logLevel;
    //public boolean uploadToSentral;
    public String sentralUsername;
    public String sentralPassword;
    public String sentralAddress;
    //public boolean getFromNesa;
    //public boolean saveCSV;
    //public String saveCsvPath;
    public String reportURL;
    public String overAwardedURL;
    public int startFromIncident;
    public String incientViewUrl;
    public int connections;
    public boolean editIncidents;
    public boolean removeOverAwarded;
    
    public SqicConfig() {
        //nesaUsername = "username";
        //nesaPassword = "password";
        useProxy = false;
        proxyUsername = "proxyuser";
        proxyPassword = "proxypass";
        proxyHost = "http://www.site.com";
        proxyPort = 8080;
        useLog = true;
        editIncidents = true;
        removeOverAwarded = true;
        logFile = "SqicSync%g.log";
        //timeout = 10000;
        waitTime = 3000;
        //calendarYear = 2017;
        logLevel = "ALL";
        sentralUsername = "first.last";
        sentralPassword = "password";
        //uploadToSentral = false;
        sentralAddress = "http://web1.school-h.schools.nsw.edu.au/";
        //getFromNesa = true;
        reportURL = "wellbeing/reports/incidents?report_id=849&orderby=date&sort=desc&page=all";
        overAwardedURL = "wellbeing/awards/over-awarded-nominations";
        incientViewUrl ="wellbeing/incidents/view?id=";
        startFromIncident = 0;
        connections = 1;
    }
    
    //enum Levels {"ALL","INFO","WARNING","SEVERE"};
    
    public Level getLevel() {
        return levels2Level(this.logLevel);
    }
    
    public Level levels2Level(String level) {
        switch(level){
            case "SEVERE":
                return Level.SEVERE;
            case "WARNING":
                return Level.WARNING;
            case "INFO":
                return Level.INFO;
            default:
                return Level.ALL;
        }
    }
    
    /*public HashMap<String, Object> toMap() throws IllegalArgumentException, IllegalAccessException{
        Class<?> objClass = this.getClass();
        HashMap<String, Object> outMap = new HashMap<>();

        Field[] fields = objClass.getFields();
        for(Field field : fields) {
            outMap.put(field.getName(), field.get(this));
        }
        return outMap;
    }
    
    public void fromMap(Map<String, Object> map) {
        Class<?> objClass = this.getClass();

        Field[] fields = objClass.getFields();
        for(Field field : fields) {
            System.out.println("before:"+field.getName()+"="+map.get(field.getName()));
            if(map.containsKey(field.getName())){
                try {
                    field.set(this, map.get(field.getName()));
                    System.out.println("field value:"+map.get(field.getName()));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(ERNLissSyncConfig.class.getName()).log(Level.WARNING, field.getName()+" must be a " + field.getType().getName(), ex);
                    System.err.println("Error");
                }
            } else {
                Logger.getLogger(ERNLissSyncConfig.class.getName()).log(Level.INFO, "Can not store field \"{0}\"", field.getName());
                System.err.println("Error");
            }
            System.out.println("after:"+field.getName()+"="+map.get(field.getName()));
        }
    }*/
}
