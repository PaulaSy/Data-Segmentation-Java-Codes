package RealtimeProcessing;

/**
 * Created by ps413734 on 9.12.2014.
 */
public final class JourneyConstants {

    private JourneyConstants(){}

    public static final int recordedAtTimeIndex = 0;
    public static final int validUntilTimeIndex = 1;
    public static final int lineIndex = 2;
    public static final int directionIndex = 3;
    public static final int dateFrameIndex = 4;
    public static final int longitudeIndex = 5;
    public static final int latitudeIndex = 6;
    public static final int operatorIndex = 7;
    public static final int bearingIndex = 8;
    public static final int delayIndex = 9;
    public static final int vehicleIndex = 10;
    public static final int journeyPatternIndex = 11;
    public static final int originShortNameIndex = 12;
    public static final int destinationShortNameIndex = 13;
    public static final int originAimedDepartureTimeIndex = 14;
    public static final int onwardCallsIndex = 15;
    public static final int speedIndex = 16;

    public static final int MAX_JOURNEYS = 100;

    public static final int UNKNOWN = 0;
    public static final int AT_STOP = 1;
    public static final int BETWEEN_STOPS = 2;
    public static final int JOURNEY_NOT_STARTED = 3;
    public static final int JOURNEY_ENDED = 4;

    public static final String GTFSstopsFile = "C:/data/stops.txt";
    public static final int MAX_BUS_STOP_NUMBER  = 10000;

    public static final double AT_STOP_DISTANCE = 50.0;
    public static final double LOOSER_STOP_DISTANCE = 100.0;

}
