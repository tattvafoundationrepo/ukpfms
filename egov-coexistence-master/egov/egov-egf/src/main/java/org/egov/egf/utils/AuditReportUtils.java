package org.egov.egf.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditReportUtils {
	
	public static final String NO_VALUE = "____"; 

	public static String getFormattedDateTime(final String inputDateString) {

		if (inputDateString == null || inputDateString.length() == 0)
			return "";

		String formattedDate = "";
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		try {
			Date date = inputFormat.parse(inputDateString);
			formattedDate = outputFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return formattedDate;
	}

}
