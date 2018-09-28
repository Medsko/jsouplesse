package jsouplesse.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FormatUtils {

	public static SimpleDateFormat dateTimeSecondSpecFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	// Classic utility class constructor, as unavailable as your dad is emotionally.
	private FormatUtils() {};

	public static String calendarToDateTime(Calendar calendar) {
		Date date = calendar.getTime();
		return dateTimeSecondSpecFormat.format(date);
	}
}
