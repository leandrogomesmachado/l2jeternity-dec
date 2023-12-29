package com.mysql.cj.result;

import com.mysql.cj.Messages;
import com.mysql.cj.WarningListener;
import com.mysql.cj.exceptions.DataReadException;
import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SqlDateValueFactory extends DefaultValueFactory<Date> {
   private TimeZone tz;
   private WarningListener warningListener;
   private Calendar cal;

   public SqlDateValueFactory(TimeZone tz) {
      this.tz = tz;
      this.cal = Calendar.getInstance(this.tz, Locale.US);
      this.cal.set(14, 0);
      this.cal.setLenient(false);
   }

   public SqlDateValueFactory(TimeZone tz, WarningListener warningListener) {
      this(tz);
      this.warningListener = warningListener;
   }

   public Date createFromDate(int year, int month, int day) {
      synchronized(this.cal) {
         if (year == 0 && month == 0 && day == 0) {
            throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
         } else {
            this.cal.clear();
            this.cal.set(year, month - 1, day);
            long ms = this.cal.getTimeInMillis();
            return new Date(ms);
         }
      }
   }

   public Date createFromTime(int hours, int minutes, int seconds, int nanos) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.ImplicitDatePartWarning", new Object[]{"java.sql.Date"}));
      }

      synchronized(this.cal) {
         Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
         c1.set(1970, 0, 1, hours, minutes, seconds);
         c1.set(14, 0);
         long ms = (long)(nanos / 1000000) + c1.getTimeInMillis();
         return new Date(ms);
      }
   }

   public Date createFromTimestamp(int year, int month, int day, int hours, int minutes, int seconds, int nanos) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{"java.sql.Date"}));
      }

      return this.createFromDate(year, month, day);
   }

   @Override
   public String getTargetTypeName() {
      return Date.class.getName();
   }
}
