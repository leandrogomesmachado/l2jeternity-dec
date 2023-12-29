package l2e.gameserver.model.holders;

import java.time.DayOfWeek;

public final class ReflectionReenterTimeHolder {
   private final DayOfWeek _day;
   private final int _hour;
   private final int _minute;
   private final long _time;

   public ReflectionReenterTimeHolder(long time) {
      this._time = time;
      this._day = null;
      this._hour = -1;
      this._minute = -1;
   }

   public ReflectionReenterTimeHolder(DayOfWeek day, int hour, int minute) {
      this._time = -1L;
      this._day = day;
      this._hour = hour;
      this._minute = minute;
   }

   public final Long getTime() {
      return this._time;
   }

   public final DayOfWeek getDay() {
      return this._day;
   }

   public final int getHour() {
      return this._hour;
   }

   public final int getMinute() {
      return this._minute;
   }
}
