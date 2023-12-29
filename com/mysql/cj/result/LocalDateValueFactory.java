package com.mysql.cj.result;

import com.mysql.cj.Messages;
import com.mysql.cj.WarningListener;
import com.mysql.cj.exceptions.DataReadException;
import java.time.LocalDate;

public class LocalDateValueFactory extends DefaultValueFactory<LocalDate> {
   private WarningListener warningListener;

   public LocalDateValueFactory() {
   }

   public LocalDateValueFactory(WarningListener warningListener) {
      this();
      this.warningListener = warningListener;
   }

   public LocalDate createFromDate(int year, int month, int day) {
      if (year == 0 && month == 0 && day == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         return LocalDate.of(year, month, day);
      }
   }

   public LocalDate createFromTimestamp(int year, int month, int day, int hours, int minutes, int seconds, int nanos) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromDate(year, month, day);
   }

   @Override
   public String getTargetTypeName() {
      return LocalDate.class.getName();
   }
}
