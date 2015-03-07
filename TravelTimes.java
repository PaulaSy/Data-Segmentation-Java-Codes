package RealtimeProcessing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.lang.Integer;
import static RealtimeProcessing.JourneyConstants.*;
/**
 * Created by ps413734 on 8.12.2014.
 */
public class TravelTimes {

    // the table of Journey id:s
    // the data structure for TT:s
    // public HashMap<JourneyID, JourneySummary> JourneyMap;
    public HashMap<String, JourneySummary> JourneyMap;
    public BusStops busStops;
    public FileWriter writer;

    public TravelTimes(FileWriter fwriter) {
        //JourneyMap = new HashMap<JourneyID, JourneySummary>(MAX_JOURNEYS);
        JourneyMap = new HashMap<String, JourneySummary>(MAX_JOURNEYS);
        busStops = new BusStops(GTFSstopsFile);
        writer = fwriter;

    }
        // which level does this function belong to?
        public void writeTTinFile(String key) {

            JourneySummary js = JourneyMap.get(key);

            String[] splitkey=key.split(",");
            String keyLine = splitkey[0];
            String keyDirection = splitkey[1];
            String keyJourneyPattern = splitkey[2];
            String keyOriginShortName = splitkey[3];
            String keyDestinationShortName = splitkey[4];
            String keyVehicleId = splitkey[5];
            String keyOriginAimedDepartureTime = splitkey[6];


            try {

                for (int i = 0; i < js.getTTSize(); i = i + 1) {

                  if (js.thisTTLineNotFilled(i)) {
                        continue;
                    }

                    writer.append(keyLine);
                    writer.append(",");
                    writer.append(keyDirection);
                    writer.append(",");
                    writer.append(keyOriginAimedDepartureTime);
                    writer.append(",");
                    writer.append(keyJourneyPattern);
                    writer.append(",");
                    writer.append(keyOriginShortName);
                    writer.append(",");
                    writer.append(keyDestinationShortName);
                    writer.append(",");
                    writer.append(keyVehicleId);
                    writer.append(",");
                    writer.append(js.getTTstopsequence(i));
                    writer.append(",");
                    writer.append(js.getTTPreviousStop(i));
                    writer.append(",");
                    writer.append(js.getTTArrivalTime(i));
                    writer.append(",");
                    writer.append(js.getTTDelayAtArrival(i));
                    writer.append(",");
                    writer.append(js.getTTDepartureTime(i));
                    writer.append(",");
                    writer.append(js.getTTDelayAtDeparture(i));
                    writer.append(",");
                    writer.append(js.getTTMaxSpeed(i));
                    writer.append("\n");

                    writer.flush();
                }
            } catch (IOException e) {
                System.out.println("error in writing TT file");
            }

        }

    public String toKey(String[] csvline) {
        String key = csvline[lineIndex]+"," + csvline[directionIndex] + "," + csvline[journeyPatternIndex] + ","
                + csvline[originShortNameIndex] + "," + csvline[destinationShortNameIndex] + ","
                + csvline[vehicleIndex] + "," + csvline[originAimedDepartureTimeIndex];

        return (key);
    }

        public void readOneLine(String rawDataline) {
         //   System.out.println(rawDataline);
            // split the line by commas
            String csvline[] = rawDataline.split(",");

            if(csvline[vehicleIndex].isEmpty()){
                return;
            }

            // check the id-match
           // JourneyID key = new JourneyID(csvline);
            String key = toKey(csvline);
            boolean inJourneyMap = JourneyMap.containsKey(key);

            // if the journey is not yet in the Id-table add it
            // also, update the status of the Journey
            if (!inJourneyMap) {
                // if onwardCalls is empty, this journey has ended and there is no point in adding it to the table
                if (csvline[onwardCallsIndex].isEmpty()) {
                    return;
                } else {

                    // also, if there is just one (the last) stop in onwardCalls, don't add this journey
                    String[] owcs = csvline[onwardCallsIndex].split(";");
                    if(owcs.length==1){
                        return;
                    }

                    JourneySummary js = new JourneySummary(csvline);
                    JourneyMap.put(key, js);
                }
            }

            JourneySummary JS = JourneyMap.get(key);
            // process the observation and add information to journey summary
            JS.processObservation(csvline,busStops);
            if (JS.getState() == JOURNEY_ENDED) {
                writeTTinFile(key);
                JourneyMap.remove(key);
            }
        }

    }
