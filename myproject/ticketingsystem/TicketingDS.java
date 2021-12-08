package ticketingsystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketingDS implements TicketingSystem {

    private AtomicInteger ticketId;
    private TrainTicket[] trains;
    private Map<Long, Ticket> soldTicket;
		
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){
        trains = new TrainTicket[routenum];
        ticketId = new AtomicInteger(1);
        for(int i = 0; i < routenum; i ++){
            trains[i] = new TrainTicket(coachnum, seatnum, stationnum);
        }
        // ConcurrentHashMap for multiple situation
        soldTicket = new ConcurrentHashMap<Long, Ticket>();
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival){
        // buy ticket
        int seat = trains[route - 1].lockForSeat(departure - 1, arrival - 1);
        if(seat < 0){
            return null;
        }
        // create ticket info
        Ticket ticket  = new Ticket();
        ticket.tid = ticketId.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = route;
        ticket.departure = departure;
        ticket.arrival = arrival;
        // calculate coach and seat
        ticket.coach = ((seat - 1) / (trains[route - 1].seatNum / trains[route - 1].coachNum)) + 1;
        ticket.seat = ((seat - 1) % (trains[route - 1].seatNum / trains[route - 1].coachNum)) + 1;
        soldTicket.put(ticket.tid, ticket);
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival){
        int restSeat = trains[route - 1].searchForSeat(departure - 1, arrival - 1).get();
        return restSeat;
    }

    @Override
    public boolean refundTicket(Ticket ticket){
        // info error
        if(!soldTicket.containsKey(ticket.tid) || !(ticket == soldTicket.get(ticket.tid))){
            return false;
        }

        // calculate seat
        int seat = (ticket.coach - 1) * (trains[ticket.route - 1].seatNum / trains[ticket.route - 1].coachNum) + ticket.seat;

        // refund ticket
        if(trains[ticket.route - 1].unlockForSeat(seat, ticket.departure - 1, ticket.arrival - 1)){
            return soldTicket.remove(ticket.tid, ticket);
        }

        return false;
    }

    @Override
    public boolean buyTicketReplay(Ticket ticket){
        return true;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket){
        return true;
    }

}
