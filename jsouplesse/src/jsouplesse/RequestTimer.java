package jsouplesse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.stream.LongStream;

import jsouplesse.dataaccess.dao.WebSite;

/**
 * This helper class keeps track of the time passed since the last request was made.
 * For this mechanism to work, all scanners that are working on the same web site
 * should share one and the same {@link RequestTimer} - meaning that in a multi-threaded
 * implementation, both essential methods should be synchronized.
 * 
 * Each time a scanner is initialized for a new web site, a new {@link RequestTimer}
 * is also initialized.
 */
public class RequestTimer {

	protected Instant tsNextRequest;
	
	public RequestTimer() {}
	
	// TODO: instead of waiting one second when isRightTime() returns false, the Thread could also sleep for randomInterval milliseconds....
	
	/**
	 * Determines whether a next request can be sent, without instantly coming across as
	 * non-/in-/super-human. Checks the current time against {@link WebSite#getTsLastRequest()}.
	 * 
	 * @param Calendar tsLastRequest - the time stamp of when the last request was made.
	 * @return {@code true} if the last request was made more than 10 seconds ago, {@code false} otherwise.
	 */
	public boolean isRightTime() {
		// Check whether the first request to this web site has been made. 
		if (tsNextRequest == null) {
			updateTsNextRequest();
			return true;
		}
		// Get the current time.
		Instant now = Instant.now();
		// If now is equal to or later than tsNextRequest, enough time has passed.
		return now.compareTo(tsNextRequest) >= 0;
	}
	
	/**
	 * Updates the field tsNextRequest, by getting the current time and adding a
	 * random number of milliseconds to it, making the interval between requests
	 * vary between 3 and 11 seconds. 
	 */
	public void updateTsNextRequest() {
		// Create a random long stream.
		LongStream randomLongs = new Random().longs(3000L, 11000L);
		// Get the next random long from the stream.
		long millis = randomLongs.findFirst().getAsLong();
		// Log the result.
		System.out.println("Random number of milliseconds: " + millis);
		// Add it to the current time stamp as milliseconds and set as tsNextRequest.
		tsNextRequest = Instant.now().plus(millis, ChronoUnit.MILLIS);
	}
}
