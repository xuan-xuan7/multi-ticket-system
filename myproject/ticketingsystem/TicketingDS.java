package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class TicketingDS implements TicketingSystem {

    private AtomicInteger ticketId;
		
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum){

    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival){

    }

    @Override
    public int inquiry(int route, int departure, int arrival){

    }

    @Override
    public boolean refundTicket(Ticket ticket){

    }

}
