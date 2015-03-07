package RealtimeProcessing;

import static RealtimeProcessing.JourneyConstants.*;


/**
 * Created by ps413734 on 12.12.2014.
 */
public class JourneySummary {


    private int state;
    private String prevStop;
    private String nextStop;
    private int index;
    private travelTimesTable TT;

    public JourneySummary(String rawDataLine[]) {
        // set state between stops: it will be checked at next instant
        state = BETWEEN_STOPS;

        String onWardCalls = rawDataLine[15];
        TT = new travelTimesTable(onWardCalls);
        prevStop = "0";
        nextStop = getNextOnwardCall(onWardCalls);
        index = 0;


    }

    // onwardCalls in some format?? is this needed if there is the TT with stop sequence?
    public class travelTimesTable {
        // contains stop sequence, scheduled arrival and leave times,
        // observed arrival and leave times, previous stop code,
        // maximum speed between stops, delay at arrival, delay at leaving
        private String stopsequence[];
        private long expectedArrival[];
        private long expectedDeparture[];
        private long arrivalTime[];
        private long departureTime[];
        private double maxSpeed[];
        private double delayAtArrival[];
        private double delayAtDeparture[];
        private String previousStop[];
        private int ttSize;

        public int getTTsize() {
            return (ttSize);
        }

        // constructor
        public travelTimesTable(String onWardCalls) {

            String nextStops[] = onWardCalls.split(";");
            int n = nextStops.length;

            ttSize = n;
            stopsequence = new String[n];
            expectedArrival = new long[n];
            expectedDeparture = new long[n];
            arrivalTime = new long[n];
            departureTime = new long[n];
            maxSpeed = new double[n];
            delayAtArrival = new double[n];
            delayAtDeparture = new double[n];
            previousStop = new String[n];

            for (int i = 0; i < n; i = i + 1) {
                String nextOne[] = nextStops[i].split(":");
                stopsequence[i] = nextOne[3];
                // initialize all the "previous stops" to "0", meaning unknown here
                previousStop[i] = "0";
                if (i == 0) {
                    expectedArrival[i] = Long.parseLong(nextOne[1]);
                    expectedDeparture[i] = Long.parseLong(nextOne[2]);
                } else {
                    expectedArrival[i] = expectedArrival[i] + Long.parseLong(nextOne[1]);
                    expectedDeparture[i] = expectedDeparture[i] + Long.parseLong(nextOne[2]);
                }

            }

        }

        public void setPreviousStop(String pstop, int index) {
            previousStop[index] = pstop;
        }

        public String getNextStop(int index) {
            String nStop;
            if (index >= (ttSize-1)) {
                nStop = "0";
            } else {
                nStop = stopsequence[index + 1];
            }
            return (nStop);
        }

        public void setDepartureTimeAndDelay(long newTime, String strNewDelay, int index) {
            departureTime[index] = newTime;
            delayAtDeparture[index] = parseDelay(strNewDelay);
        }

        public void setArrivalTimeAndDelay(long newTime, String strNewDelay, int index) {
            arrivalTime[index] = newTime;
            delayAtArrival[index] = parseDelay(strNewDelay);
        }

        public void setMaxSpeed(double newSpeed, int index) {
            if (newSpeed > maxSpeed[index]) {
                maxSpeed[index] = newSpeed;
            }
        }


        public String getStopsequence(int i){
            return (stopsequence[i]);
        }

        public String getPreviousStop(int i){
            return (previousStop[i]);
        }

        public String getArrivalTime(int i){
            return (Long.toString(arrivalTime[i]));
        }

        public String getDepartureTime(int i){
            return (Long.toString(departureTime[i]));
        }

        public String getDelayAtDeparture(int i){
            return (Double.toString(delayAtDeparture[i]));
        }

        public String getDelayAtArrival(int i){
            return (Double.toString(delayAtArrival[i]));
        }

        public String getMaxSpeed(int i){
            return (Double.toString(maxSpeed[i]));
        }

    }



    public int getState(){
        return (state);
    }

    public int getTTSize(){
        return(TT.getTTsize());
    }


    public String getTTPreviousStop(int i){
        return (TT.getPreviousStop(i));
    }

    public String getTTArrivalTime(int i){
        return (TT.getArrivalTime(i));
    }

    public String getTTDepartureTime(int i){
        return (TT.getDepartureTime(i));
    }

    public String getTTDelayAtDeparture(int i){
        return (TT.getDelayAtDeparture(i));
    }

    public String getTTDelayAtArrival(int i){
        return (TT.getDelayAtArrival(i));
    }

    public String getTTMaxSpeed(int i){
        return (TT.getMaxSpeed(i));
    }

    public boolean thisTTLineNotFilled(int i){
        if(Long.parseLong(TT.getArrivalTime(i))==0.0){
            return (true);
        }else{
            return (false);
        }
    }


    public String getTTstopsequence(int i){
        return(TT.getStopsequence(i));
    }

    public void setMaxSpeed(double newSpeed) {
        TT.setMaxSpeed(newSpeed,index);
    }

    public boolean increaseTTindex() {
        if (index >= (TT.getTTsize()-1)) {
            return (false);
        } else {
            index = index + 1;
            return (true);
        }
    }


    private double parseDelay(String strDelay) {
        double sign = 1.0;
        if (strDelay.startsWith("-")) {
            sign = -1.0;
        }

        String delims = "[THMS]";
        String[] el = strDelay.split(delims);

        double delay = sign * (Double.parseDouble(el[2]) * 3600.0 // hours
                + Double.parseDouble(el[3]) * 60.0                // minutes
                + Double.parseDouble(el[4]));                  // seconds

        return (delay);
    }


    public String getNextOnwardCall(String owc) {
        if (owc.isEmpty()) {
            return ("0");
        }
        String[] onwardcalls = owc.split(";");
        String nextOne = onwardcalls[0];
        String[] nextOneSplit = nextOne.split(":");
        return (nextOneSplit[3]);
    }


    public void processObservation(String[] csvLine,BusStops busStops) {

        if(state==AT_STOP){
            processAtStop(csvLine,busStops);
        } else if(state==BETWEEN_STOPS){
            processBetweenStops(csvLine,busStops);
        }
    }

    // NOT USED
    private void moveToNextStop(String[] csvline, BusStops busStops) {

        String owc = csvline[onwardCallsIndex];
        //System.out.println(owc);
        if (owc.isEmpty()) {
            // at the end of the journey


            // assume that the bus arrived at the last stop
            int laststop=TT.getTTsize()-1;
            TT.setArrivalTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]),csvline[delayIndex],laststop);
            TT.setMaxSpeed(Double.parseDouble(csvline[speedIndex]),laststop);
            TT.setPreviousStop(prevStop,laststop);

            nextStop = "0";
            state = JOURNEY_ENDED;
        } else {
            String[] owcsplit = owc.split(";");
            //System.out.println(owcsplit[0]);
            String[] owcsplit1 = owcsplit[0].split(":");
            String nextowc = owcsplit1[3];

            Double lat = Double.parseDouble(csvline[latitudeIndex]);
            Double lon = Double.parseDouble(csvline[longitudeIndex]);

            if (nextowc.compareTo(nextStop) != 0) {

                // check if the next stop (according to onwardcalls) is in the TT
                for (int k = index; k < TT.getTTsize(); k = k + 1) {

                    // if the bus is close enough to one of the stops on the way, map this moment to that stop
                    if(busStops.BusStopMap.containsKey(TT.getStopsequence(k))) {
                        Double dist = busStops.BusStopMap.get(TT.getStopsequence(k)).distanceTo(lat, lon);
                        if (dist < 100) {

                            if (Long.parseLong(TT.getArrivalTime(k)) == 0.0) {
                                TT.setPreviousStop(prevStop, k);
                                TT.setArrivalTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]), csvline[delayIndex], k);
                                prevStop = nextStop;
                                nextStop = TT.getStopsequence(k);
                            }
                            TT.setMaxSpeed(Double.parseDouble(csvline[speedIndex]), k);
                            TT.setDepartureTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]), csvline[delayIndex], k);
                        }
                    }

                    if (nextowc.compareTo(TT.stopsequence[k]) == 0) {
                        index = k;

                        nextStop = nextowc;
                        break;
                    }
                }

            }
        }

    }

    public void moveOn(String csvline[],BusStops busStops){
        Double lat = Double.parseDouble(csvline[latitudeIndex]);
        Double lon = Double.parseDouble(csvline[longitudeIndex]);

            // check if the bus seems to be further than thought (maybe there was a gap in the data)
            for (int k = (index+1); k < TT.getTTsize(); k = k + 1) {

                // if the bus is close enough to one of the stops on the way, map this moment to that stop
                // however, leave the state as "between stops", the "at stop" can possibly be set on the next round
                // also, don't consider the journey ended here, but at the next processing round
                if(busStops.BusStopMap.containsKey(TT.getStopsequence(k))) {
                    Double dist = busStops.BusStopMap.get(TT.getStopsequence(k)).distanceTo(lat, lon);
                    if (dist < LOOSER_STOP_DISTANCE) {

                        if (Long.parseLong(TT.getArrivalTime(k)) == 0.0) {
                            TT.setPreviousStop(prevStop, k);
                            TT.setArrivalTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]), csvline[delayIndex], k);
                            prevStop = nextStop;
                            nextStop = TT.getStopsequence(k);
                            index=k;
                        }
                        TT.setMaxSpeed(Double.parseDouble(csvline[speedIndex]), k);
                        TT.setDepartureTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]), csvline[delayIndex], k);
                        break;
                    }
                }

            }
    }

    private void processAtStop(String[] csvline, BusStops busStops) {

        // is the bus still at this stop?
        String stopcodekey = nextStop;

//        if(nextStop.compareTo("0001")==0){
//            System.out.println("next stop");
//        }
        boolean isAtThisStop;
        if (busStops.BusStopMap.containsKey(stopcodekey)) {
            isAtThisStop = busStops.BusStopMap.get(stopcodekey).isAtBusStop
                    (Double.parseDouble(csvline[latitudeIndex])
                            , Double.parseDouble(csvline[longitudeIndex]));

        }else {
          //  System.out.println("false");
            isAtThisStop = false;
        }
        if (isAtThisStop) {

           // System.out.println("still at stop");
            // set new departure time
            TT.setDepartureTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex])
                    , csvline[delayIndex], index);
        } else {
            // the bus has left the stop

            boolean notLast = increaseTTindex();
            if (notLast) {
                TT.setMaxSpeed(Double.parseDouble(csvline[speedIndex]), index);
                prevStop = nextStop;
                nextStop = TT.getStopsequence(index);
                state = BETWEEN_STOPS;
            }else {
                // it should not arrive here??
                state = JOURNEY_ENDED;
            }
        }
    }

    private void processBetweenStops(String[] csvline,BusStops busStops) {
        // check for possible gaps in the data, i.e. is the next stop according to the
        // TT the same as according to
        //checkAndHandleContradiction(csvline);
        String stopcodekey = nextStop;

        // has the bus arrived to the next stop?
        if(busStops.BusStopMap.containsKey(stopcodekey)) {

            boolean isAtThisStop = busStops.BusStopMap.get(stopcodekey).isAtBusStop
                    (Double.parseDouble(csvline[latitudeIndex]), Double.parseDouble(csvline[longitudeIndex]));
            if (!isAtThisStop) {
                // maybe it has passed the stop already?

                    // check if the bus has arrived to some further stop
                    moveOn(csvline,busStops);
                    TT.setMaxSpeed(Double.parseDouble(csvline[speedIndex]), index);

            } else {
                // has arrived
               // System.out.println("arrived");

                TT.setArrivalTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]),
                        csvline[delayIndex], index);
//                TT.setDepartureTimeAndDelay(Long.parseLong(csvline[recordedAtTimeIndex]),
//                        csvline[delayIndex], index);
                TT.setPreviousStop(prevStop, index);
                if(index==(TT.getTTsize()-1)){
                    // last stop
                    state = JOURNEY_ENDED;
                }else {
                    state = AT_STOP;
                }
            }
        }else {
         //   System.out.println("stop is missing");
        }

    }



}
