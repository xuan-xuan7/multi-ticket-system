package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Test {

	final static int[] threadnums = {1, 2, 4, 8, 16, 32, 64};
	final static int routenum = 5;
	final static int coachnum = 8;
	final static int seatnum = 100;
	final static int stationnum = 10;

	final static int testnum = 10000;
	final static int retpc = 10; // return ticket operation is 10% percent
	final static int buypc = 40; // buy ticket operation is 30% percent
	final static int inqpc = 100; //inquiry ticket operation is 60% percent

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(testnum);
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException {

		for (int threadnum : threadnums) {
			Test(threadnum);
		}
	}

	public static void Test(int threadnum) throws InterruptedException {

		final AtomicLong totalRefundNum = new AtomicLong(0);
		final AtomicLong totalBuyNum = new AtomicLong(0);
		final AtomicLong totalSearchNum = new AtomicLong(0);
		final AtomicLong totalRefundTime = new AtomicLong(0);
		final AtomicLong totalBuyTime = new AtomicLong(0);
		final AtomicLong totalSearchTime = new AtomicLong(0);

		Thread[] threads = new Thread[threadnum];
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		for (int i = 0; i< threadnum; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					Random rand = new Random();
					Ticket ticket;
					ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
					long refundTime = 0;
					long buyTime = 0;
					long searchTime = 0;
					long refundNum = 0;
					long buyNum = 0;
					long searchNum = 0;
					long begin;
					for (int i = 0; i < testnum; i++) {
						int sel = rand.nextInt(inqpc);
						if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // refund ticket
							int select = rand.nextInt(soldTicket.size());
							if ((ticket = soldTicket.remove(select)) != null) {
								begin = System.nanoTime();
								boolean result = tds.refundTicket(ticket);
								long end = System.nanoTime();
								refundTime += end - begin;
								refundNum ++;
								if(result){
									System.out.println(begin+ " " + end + " " + ThreadId.get() + " " + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach  + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
									System.out.flush();
								} else {
									System.out.println(ThreadId.get() + " " + "ErrOfRefund");
									System.out.flush();
								}
							} else {
								System.out.println(ThreadId.get() + " " + "ErrOfRefund");
								System.out.flush();
							}
						} else if (retpc <= sel && sel < buypc) { // buy ticket
							String passenger = passengerName();
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							begin = System.nanoTime();
							if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
								long end = System.nanoTime();
								buyTime += end - begin;
								buyNum ++;
								System.out.println(begin+ " " + end + " "  + ThreadId.get() + " " + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat);
								soldTicket.add(ticket);
								System.out.flush();
							} else {
								System.out.println(ThreadId.get() + " " + "TicketSoldOut" + " " + route + " " + departure+ " " + arrival);
								System.out.flush();
							}
						} else if (buypc <= sel && sel < inqpc) { // inquiry ticket
							int route = rand.nextInt(routenum) + 1;
							int departure = rand.nextInt(stationnum - 1) + 1;
							int arrival = departure + rand.nextInt(stationnum - departure) + 1; // arrival is always greater than departure
							begin = System.nanoTime();
							int restTicket = tds.inquiry(route, departure, arrival);
							long end = System.nanoTime();
							searchTime += end - begin;
							searchNum ++;
							System.out.println(begin + " " + end + " " + ThreadId.get() + " " + "RemainTicket" + " " + restTicket + " " + route+ " " + departure+ " " + arrival);
							System.out.flush();
						}
					}
					totalRefundNum.addAndGet(refundNum);
					totalBuyNum.addAndGet(buyNum);
					totalSearchNum.addAndGet(searchNum);
					totalRefundTime.addAndGet(refundTime);
					totalBuyTime.addAndGet(buyTime);
					totalSearchTime.addAndGet(searchTime);
				}
			});
		}

		long beginTime = System.currentTimeMillis();
		for(int i = 0; i < threadnum; i++){
			threads[i].start();
		}
		for (int i = 0; i< threadnum; i++) {
			threads[i].join();
		}
		long executionTime = System.currentTimeMillis() - beginTime;
		System.out.println("==== ThreadNum: " + threadnum + ", " + testnum + " op per thread ====");
		System.out.println("Total op num: " + testnum * threadnum);
		System.out.println("RouteNum: " + routenum + ", CoachNum: " + coachnum +
				", SeatNum: " + seatnum + ", StationNum: " + stationnum);
		System.out.println("Total execution times: " + executionTime);
		System.out.println("RetNum: " + totalRefundNum.get() + "\tBuyNum: " +
				totalBuyNum.get() + "\tQueryNum:" +
				totalSearchNum.get());
		System.out.println("Ret: "+ (totalRefundTime.get() / totalRefundNum.get()) + " ns/op" +
				"\tBuy: " + (totalBuyTime.get() / totalBuyNum.get()) + " ns/op" +
				"\tQuery: " + (totalSearchTime.get() / totalSearchNum.get()) + " ns/op");
		System.out.println("Throughput: " + (double)(totalRefundNum.get() + totalBuyNum.get() + totalSearchNum.get()) /
				(totalRefundTime.get() + totalBuyTime.get() + totalSearchTime.get()) * threadnum * 10e6 + " kop/s");
		System.out.println("Throughput: " + (double)(testnum * threadnum) / executionTime + " kop/s");
		System.out.println("\n\n");
	}
}
