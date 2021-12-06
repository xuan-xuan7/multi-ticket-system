// all tickets for one train

package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TrainTicket {
    public final int coachNum;
    public final int seatNum;
    public final int stationNum;
    // record seat used during which stations
    public AtomicLong[] seatState;

    public TrainTicket(int coachnum, int seatnum, int stationnum){
        seatNum = coachnum * seatnum;
        coachNum = coachnum;
        stationNum = stationnum;
        seatState = new AtomicLong[seatNum];
        for(int i = 0; i < seatNum; i ++){
            seatState[i] = new AtomicLong(0);
        }
    }

    // lock: only one thread can operate this trains[i] and give a seat number
    public int lockForSeat(final int departure, final int arrival){
        // use binary to record which stations are used
        long passStations = (1 << (arrival - departure)) - 1;
        passStations = passStations << departure;

        // search for empty seat
        for(int i = 0; i < seatNum; i ++){
            long temp = seatState[i].get();
            // add stations to empty seat
            // spin lock
            while((temp & passStations) == 0){
                if(seatState[i].compareAndSet(temp, (temp | passStations))){
                    return i + 1;
                }
                temp = seatState[i].get();
            }
        }

        // no more seat
        return -1;
    }

    public boolean unlockForSeat(final int seatnum, final int departure, final int arrival){

        long passStations = (1 << (arrival - departure)) - 1;
        passStations = passStations << departure;

        while(true){
            long temp = seatState[seatnum - 1].get();
            if(seatState[seatnum - 1].compareAndSet(temp, (temp & ~passStations))){
                return true;
            }
        }
    }

    public AtomicInteger searchForSeat(final int departure, final int arrival){
        AtomicInteger ticketNum = new AtomicInteger(0);
        long passStations = (1 << (arrival - departure)) - 1;
        passStations = passStations << departure;

        for(int i = 0; i < seatNum; i ++){
            long temp = seatState[i].get();
            if((passStations & temp) == 0){
                ticketNum.getAndIncrement();
            }
        }

        return ticketNum;
    }

}
