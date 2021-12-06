package ticketingsystem;

public class Test {

	final static int[] threadnums = {1, 2, 4, 8, 16, 32, 64};
	final static int routenum = 5;
	final static int coachnum = 8;
	final static int seatnum = 100;
	final static int stationnum = 10;

	final static int testNum = 10000;
	final static int buyTicket = 30;
	final static int refundTicket = 10;
	final static int searchTicket = 60;

	public static void main(String[] args) throws InterruptedException {
        
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		//ToDo
	    
	}
}
