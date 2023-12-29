package l2e.commons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import l2e.gameserver.model.actor.Player;

public class TimeUtils {
   public static final long SECOND_IN_MILLIS = 1000L;
   public static final long MINUTE_IN_MILLIS = 60000L;
   public static final long HOUR_IN_MILLIS = 3600000L;
   public static final long DAY_IN_MILLIS = 86400000L;
   private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

   public static long parse(String time) throws ParseException {
      return SIMPLE_FORMAT.parse(time).getTime();
   }

   public static String toSimpleFormat(Calendar cal) {
      return SIMPLE_FORMAT.format(cal.getTime());
   }

   public static String toSimpleFormat(long cal) {
      return SIMPLE_FORMAT.format(Long.valueOf(cal));
   }

   public static String convertDateToString(long time) {
      Date dt = new Date(time);
      return SIMPLE_FORMAT.format(dt);
   }

   public static String minutesToFullString(int period) {
      StringBuilder sb = new StringBuilder();
      if (period > 1440) {
         sb.append((period - period % 1440) / 1440).append(" days.");
         period %= 1440;
      }

      if (period > 60) {
         if (sb.length() > 0) {
            sb.append(", ");
         }

         sb.append((period - period % 60) / 60).append(" hours.");
         period %= 60;
      }

      if (period > 0) {
         if (sb.length() > 0) {
            sb.append(", ");
         }

         sb.append(period).append(" min.");
      }

      if (sb.length() < 1) {
         sb.append("less than 1 minute.");
      }

      return sb.toString();
   }

   public static long getMilisecondsToNextDay(List<Integer> days, int hourOfTheEvent) {
      return getMilisecondsToNextDay(days, hourOfTheEvent, 5);
   }

   public static long getMilisecondsToNextDay(List<Integer> days, int hourOfTheEvent, int minuteOfTheEvent) {
      int[] hours = new int[days.size()];

      for(int i = 0; i < hours.length; ++i) {
         hours[i] = days.get(i);
      }

      return getMilisecondsToNextDay(hours, hourOfTheEvent, minuteOfTheEvent);
   }

   public static long getMilisecondsToNextDay(int[] days, int hourOfTheEvent, int minuteOfTheEvent) {
      Calendar tempCalendar = Calendar.getInstance();
      tempCalendar.set(13, 0);
      tempCalendar.set(14, 0);
      tempCalendar.set(11, hourOfTheEvent);
      tempCalendar.set(12, minuteOfTheEvent);
      long currentTime = System.currentTimeMillis();
      Calendar eventCalendar = Calendar.getInstance();
      boolean found = false;
      long smallest = Long.MAX_VALUE;

      for(int day : days) {
         tempCalendar.set(5, day);
         long timeInMillis = tempCalendar.getTimeInMillis();
         if (timeInMillis <= currentTime) {
            if (timeInMillis < smallest) {
               smallest = timeInMillis;
            }
         } else if (!found || timeInMillis < eventCalendar.getTimeInMillis()) {
            found = true;
            eventCalendar.setTimeInMillis(timeInMillis);
         }
      }

      if (!found) {
         eventCalendar.setTimeInMillis(smallest);
         eventCalendar.add(2, 1);
      }

      return eventCalendar.getTimeInMillis() - currentTime;
   }

   public static long addDay(int count) {
      return (long)(count * 60 * 60 * 24) * 1000L;
   }

   public static long addHours(int count) {
      return (long)(count * 60 * 60) * 1000L;
   }

   public static long addMinutes(int count) {
      return (long)(count * 60) * 1000L;
   }

   public static long addSecond(int count) {
      return (long)count * 1000L;
   }

   public static String formatTime(Player player, int time) {
      return formatTime(player, time, true);
   }

   public static String formatTime(Player player, int time, boolean cut) {
      int days = 0;
      int hours = 0;
      int minutes = 0;
      days = time / 86400;
      hours = (time - days * 24 * 3600) / 3600;
      minutes = (time - days * 24 * 3600 - hours * 3600) / 60;
      String result;
      if (days >= 1) {
         if (hours >= 1 && !cut) {
            result = days
               + " "
               + Util.declension(player, (long)days, DeclensionKey.DAYS)
               + " "
               + hours
               + " "
               + Util.declension(player, (long)hours, DeclensionKey.HOUR);
         } else {
            result = days + " " + Util.declension(player, (long)days, DeclensionKey.DAYS);
         }
      } else if (hours >= 1) {
         if (minutes >= 1 && !cut) {
            result = hours
               + " "
               + Util.declension(player, (long)hours, DeclensionKey.HOUR)
               + " "
               + minutes
               + " "
               + Util.declension(player, (long)minutes, DeclensionKey.MINUTES);
         } else {
            result = hours + " " + Util.declension(player, (long)hours, DeclensionKey.HOUR);
         }
      } else {
         result = minutes + " " + Util.declension(player, (long)minutes, DeclensionKey.MINUTES);
      }

      return result;
   }
}
