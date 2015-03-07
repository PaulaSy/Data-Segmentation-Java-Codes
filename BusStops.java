package RealtimeProcessing;

import java.io.BufferedReader;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/**
 * Created by ps413734 on 9.12.2014.
 */
public class BusStops {

    public HashMap<String,BusStopLocation> BusStopMap;

    public BusStops(String filename){
        BusStopMap = new HashMap<String, BusStopLocation>(JourneyConstants.MAX_BUS_STOP_NUMBER);
        BufferedReader br = null;
        String fileLine = "";

        try{
            br = new BufferedReader(new FileReader(filename));
            // read the header line
            String header = br.readLine();
            String[] splitheader = header.split(",");
            int stopcodeindex = 0;
            int latindex = 0;
            int lonindex = 0;
            for( int i=0; i<splitheader.length; i=i+1){
                if(splitheader[i].compareTo("stop_code")==0) stopcodeindex = i;
                if(splitheader[i].compareTo("stop_lat")==0) latindex = i;
                if(splitheader[i].compareTo("stop_lon")==0) lonindex = i;
            }

            while ((fileLine=br.readLine()) != null ){
                String[] bstop = fileLine.split(",");

                BusStopLocation bstoploc = new BusStopLocation(bstop[latindex],bstop[lonindex]);
                BusStopMap.put(bstop[stopcodeindex],bstoploc);
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public class BusStopLocation{
        private double latitude;
        private double longitude;

        public BusStopLocation(String latString,String lonString){
            latitude = Double.parseDouble(latString);
            longitude = Double.parseDouble(lonString);
        }

        public double getLatitude(){
            return(latitude);
        }

        public double getLongitude(){
            return(longitude);
        }

        public double distanceTo(double lat,double lon){

            double radLat = Math.toRadians(lat);
            double radLon = Math.toRadians(lon);
            double radLatitude = Math.toRadians(latitude);
            double radLongitude = Math.toRadians(longitude);

            double R1 = 6370000; // earth radius, meters
            double R2 = R1*Math.cos(radLat);

            double d_lat_m = R1*(radLat-radLatitude);
            double d_lon_m = R2*(radLon-radLongitude);

            double d = Math.sqrt((d_lat_m*d_lat_m+d_lon_m*d_lon_m));
            return(d);

        }

        public boolean isAtBusStop(double lat, double lon){
            double distance = distanceTo(lat,lon);
            if(distance<=JourneyConstants.AT_STOP_DISTANCE){
                return(true);
            }else {
                return (false);
            }
        }

    }


}

