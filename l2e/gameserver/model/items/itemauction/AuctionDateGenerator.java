package l2e.gameserver.model.items.itemauction;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import l2e.gameserver.model.stats.StatsSet;

public final class AuctionDateGenerator {
   public static final String FIELD_INTERVAL = "interval";
   public static final String FIELD_DAY_OF_WEEK = "day_of_week";
   public static final String FIELD_HOUR_OF_DAY = "hour_of_day";
   public static final String FIELD_MINUTE_OF_HOUR = "minute_of_hour";
   private static final long MILLIS_IN_WEEK = TimeUnit.MILLISECONDS.convert(7L, TimeUnit.DAYS);
   private final Calendar _calendar = Calendar.getInstance();
   private final int _interval;
   private int _day_of_week;
   private int _hour_of_day;
   private int _minute_of_hour;

   public AuctionDateGenerator(StatsSet config) throws IllegalArgumentException {
      this._interval = config.getInteger("interval", -1);
      int fixedDayWeek = config.getInteger("day_of_week", -1) + 1;
      this._day_of_week = fixedDayWeek > 7 ? 1 : fixedDayWeek;
      this._hour_of_day = config.getInteger("hour_of_day", -1);
      this._minute_of_hour = config.getInteger("minute_of_hour", -1);
      this.checkDayOfWeek(-1);
      this.checkHourOfDay(-1);
      this.checkMinuteOfHour(0);
   }

   public final synchronized long nextDate(long date) {
      this._calendar.setTimeInMillis(date);
      this._calendar.set(14, 0);
      this._calendar.set(13, 0);
      this._calendar.set(12, this._minute_of_hour);
      this._calendar.set(11, this._hour_of_day);
      if (this._day_of_week > 0) {
         this._calendar.set(7, this._day_of_week);
         return this.calcDestTime(this._calendar.getTimeInMillis(), date, MILLIS_IN_WEEK);
      } else {
         return this.calcDestTime(this._calendar.getTimeInMillis(), date, TimeUnit.MILLISECONDS.convert((long)this._interval, TimeUnit.DAYS));
      }
   }

   private final long calcDestTime(long time, long date, long add) {
      if (time < date) {
         time += (date - time) / add * add;
         if (time < date) {
            time += add;
         }
      }

      return time;
   }

   private final void checkDayOfWeek(int defaultValue) {
      if (this._day_of_week >= 1 && this._day_of_week <= 7) {
         if (this._interval > 1) {
            throw new IllegalArgumentException("Illegal params for 'interval' and 'day_of_week': you can use only one, not both");
         }
      } else {
         if (defaultValue == -1 && this._interval < 1) {
            throw new IllegalArgumentException("Illegal params for 'day_of_week': " + (this._day_of_week == -1 ? "not found" : this._day_of_week));
         }

         this._day_of_week = defaultValue;
      }
   }

   private final void checkHourOfDay(int defaultValue) {
      if (this._hour_of_day < 0 || this._hour_of_day > 23) {
         if (defaultValue == -1) {
            throw new IllegalArgumentException("Illegal params for 'hour_of_day': " + (this._hour_of_day == -1 ? "not found" : this._hour_of_day));
         }

         this._hour_of_day = defaultValue;
      }
   }

   private final void checkMinuteOfHour(int defaultValue) {
      if (this._minute_of_hour < 0 || this._minute_of_hour > 59) {
         if (defaultValue == -1) {
            throw new IllegalArgumentException("Illegal params for 'minute_of_hour': " + (this._minute_of_hour == -1 ? "not found" : this._minute_of_hour));
         }

         this._minute_of_hour = defaultValue;
      }
   }
}
