package gr.ilsp.fc.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class CalendarUtils {

	public static XMLGregorianCalendar getXMLGregorianCalendarNow() throws DatatypeConfigurationException {
		XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		GregorianCalendar now = new GregorianCalendar();
		xgc.setYear(now.get(Calendar.YEAR));
		xgc.setMonth(now.get(Calendar.MONTH) + 1);
		xgc.setDay(now.get(Calendar.DAY_OF_MONTH));
		return xgc;
	}
}