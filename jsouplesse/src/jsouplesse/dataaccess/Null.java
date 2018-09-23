package jsouplesse.dataaccess;

/**
 * The inner classes of this singleton provide a way to indicate that a
 * certain instance of a primitive wrapper is null. This comes in handy
 * when using 'instance of' checks on these - wait - could this also be achieved with Optional<Integer> (for instance)?  
 */
public class Null {
	
	public static class Integer {}

	public static class String {}
	
	public static class Double {}
	
	public static class Timestamp {}
	
	public static class Clob {}
	
	public static class Blob {}
			
}
