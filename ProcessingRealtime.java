package RealtimeProcessing;

//import java.io.BufferedReader;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import RealtimeProcessing.JourneysClient.VehicleActivityList;
import RealtimeProcessing.JourneysClient.VehicleActivityList.VehicleActivity;
import java.text.SimpleDateFormat;


public class ProcessingRealtime {

    public static void main(String[] args) {

        System.out.println("here");

        try {
            String outputfilename = "C:/Users/ps413734/Journeys/RealtimeTest/test_traveltimes.csv";


            File outputfile = new File(outputfilename);
            FileWriter fwriter;
            if (!outputfile.exists()) {
                outputfile.createNewFile();
            }
            fwriter = new FileWriter(outputfile);

            TravelTimes JourneyData = new TravelTimes(fwriter);

            int counter=0;

            while (counter < 600) {

                try {

                    VehicleActivityList vehicleActivityList = JourneysClient.getVehicleActivity();
                    for(VehicleActivity vehicleActivity : vehicleActivityList.vehicleActivities) {

                        String owc;
                        // check if
                        if(vehicleActivity.monitoredVehicleJourney.onwardCalls != null &&
                                !vehicleActivity.monitoredVehicleJourney.onwardCalls.isEmpty()){
                            owc = ConvertOnwardCallsToString( vehicleActivity.monitoredVehicleJourney.onwardCalls );
                        }else{
                            owc="";
                        }

                        // convert vehicleActivity into csv-like string
                        String rawDataline = ConvertDate( vehicleActivity.recordedAtTime ) + "," +
                                ConvertDate(vehicleActivity.validUntilTime ) + "," +
                                vehicleActivity.monitoredVehicleJourney.lineRef + "," +
                                vehicleActivity.monitoredVehicleJourney.directionRef + "," +
                                vehicleActivity.monitoredVehicleJourney.framedVehicleJourneyRef.dateFrameRef + "," +
                                vehicleActivity.monitoredVehicleJourney.vehicleLocation.longitude + "," +
                                vehicleActivity.monitoredVehicleJourney.vehicleLocation.latitude + "," +
                                vehicleActivity.monitoredVehicleJourney.operatorRef + "," +
                                vehicleActivity.monitoredVehicleJourney.bearing + "," +
                                vehicleActivity.monitoredVehicleJourney.delay + "," +
                                vehicleActivity.monitoredVehicleJourney.vehicleRef + "," +
                                vehicleActivity.monitoredVehicleJourney.journeyPatternRef + "," +
                                vehicleActivity.monitoredVehicleJourney.originShortName + "," +
                                vehicleActivity.monitoredVehicleJourney.destinationShortName + "," +
                                vehicleActivity.monitoredVehicleJourney.originAimedDepartureTime + "," +
                                owc + "," +
                                vehicleActivity.monitoredVehicleJourney.speed;

                       // System.out.println(rawDataline);
                        JourneyData.readOneLine(rawDataline);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                counter = counter +1;
                System.out.println(counter);
                try {
                    Thread.sleep(900);
                }catch (InterruptedException ie){
                    System.out.println("sleep error");
                }
            }

            // write the rest of the journeys in the file (the ones that have not been observed to have ended)
            for (Map.Entry<String, JourneySummary> entry : JourneyData.JourneyMap.entrySet()) {
                // write all those TT:s into file that were for some reason not considered ended
                JourneyData.writeTTinFile(entry.getKey());
            }

                    JourneyData.JourneyMap.clear();

                    fwriter.flush();
                    fwriter.close();




        }catch (IOException e){
            System.out.println("error");
        }

    }

    static String ConvertOnwardCallsToString(List<VehicleActivity.MonitoredVehicleJourney.OnwardCall> owcs){

        String owcstring = "";

       // System.out.println(owcs.size());

        for(int i=0; i<owcs.size(); i=i+1){
        //for(VehicleActivity.MonitoredVehicleJourney.OnwardCall owc:owcs){

            VehicleActivity.MonitoredVehicleJourney.OnwardCall owc=owcs.get(i);

            String[] splits = owc.stopPointRef.split("/");
            String nextStopPointShortName = splits[splits.length - 1];

            String thisowc = owc.order + ":" + ConvertDateOwc(owc.expectedArrivalTime) + ":" +
                    ConvertDateOwc(owc.expectedDepartureTime) + ":" + nextStopPointShortName;
            owcstring = owcstring + thisowc;

            if(i<(owcs.size()-1)){
                owcstring = owcstring + ";";
            }

        }
        return (owcstring);
    }


    static String ConvertDate(String datestr){

        Long ts = 0L;
        try{
            //System.out.println(datestr);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Date parsedDate = dateFormat.parse(datestr);
            ts=parsedDate.getTime();
        }catch(Exception e){//this generic but you csan control another types of exception
            System.out.println("time error");
        }
        return (ts.toString());
    }

    static String ConvertDateOwc(String datestr){

        Long ts = 0L;
        try{
            //System.out.println(datestr);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            Date parsedDate = dateFormat.parse(datestr);
            ts=parsedDate.getTime();
        }catch(Exception e){//this generic but you csan control another types of exception
            System.out.println("time error");
        }
        return (ts.toString());
    }


}
