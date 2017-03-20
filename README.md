# SentralQuickIncidentCompleter

SentralQuickIncidentCompleter is a stop gap measure as Sentral doesn't automatically give awards from in class incidents. It will open, edit, confirm the awards, and close the incident. It can also remove over-awarded awards. Doesn't currently accept award nominations.

## Sqic.json
This file stores all of the settings for Sqic. If it doesn't exist when Sqic is run, it will be created with default settings. This is an overview of the settings:

property | default value | description
--- | --- | ---
"useProxy" | false | Possibly the proxy works. I forget. Anyway, this is how you turn it on or off.
"proxyUsername" | "proxyuser" | The proxy username.
"proxyPassword" | "proxypass" | The proxy password.
"proxyHost" | "http://www.site.com" | The host for the proxy.
"proxyPort" | 8080 | The port for the proxy.
"useLog" | true | This turn the log on or off.
"editIncidents" | true | This enables/disables the editing of incident which will give awards for in class incidents.
"removeOverAwarded" | true | Enables/disables the removal of over-awarded awards.
"logFile" | "SqicSync%g.log" | The name of the log file. %g numbers the logs in case the log gets too large.
"waitTime" | 3000 | Sometimes the program will need to wait. It waits for this many milliseconds.
"logLevel" | "ALL" | How much to log. The options are INFO, WARNING, SEVERE, ALL.
"sentralUsername" | "first.last" | Username to log in to Sentral
"sentralPassword" | "password" | Password to log in to sentral
"sentralAddress" | "http://web1.school-h.schools.nsw.edu.au/" | The base URL for Sentral. I think the trailing / is important - haven't tested.
"reportURL" | "wellbeing/reports/incidents?report_id=849&orderby=date&sort=desc&page=all" | The URL of the report which lists all of the incidents which should be opened and edited (as long as the Is is after the startFromIncident value).
"overAwardedURL" | "wellbeing/awards/over-awarded-nominations" | The over awarded URL. Made this a variable in case things change in the future.
"incientViewUrl" | "wellbeing/incidents/view?id=" | Incident URL (incident id gets stuck on the end). Made this a variable in case things change in the future.
"startFromIncident" | 0 | This value stores the last edited id so that you don't need to check every incident - only the new ones from the last time the program ran.
"connections" | 1 | Sqic can edit multiple incidents at once by using multiple connections but testing failed. Maybe Sentral limits the number of connections to stop floods, maybe I'm doing something wrong. Left if here anyway.
