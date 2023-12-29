package l2e.commons.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import l2e.commons.annotations.Nullable;
import l2e.commons.util.file.filter.ExtFilter;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShowBoard;

public class Util {
   private static final Logger _log = Logger.getLogger(Util.class.getName());
   private static final NumberFormat ADENA_FORMATTER = NumberFormat.getIntegerInstance(Locale.ENGLISH);
   private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm (E)";
   private static Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", 32);
   private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
   private static final char[] ILLEGAL_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
   private static final char[] ALLOWED_CHARS = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
   private static final NumberFormat PERCENT_FORMAT_5 = NumberFormat.getPercentInstance(Locale.ENGLISH);
   private static final NumberFormat PERCENT_FORMAT_10 = NumberFormat.getPercentInstance(Locale.ENGLISH);
   private static final NumberFormat PERCENT_FORMAT_15 = NumberFormat.getPercentInstance(Locale.ENGLISH);

   public static boolean isInteger(char c) {
      for(char possibility : ALLOWED_CHARS) {
         if (possibility == c) {
            return true;
         }
      }

      return false;
   }

   public static boolean isInternalHostname(String host) {
      try {
         InetAddress addr = InetAddress.getByName(host);
         return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
      } catch (UnknownHostException var2) {
         _log.warning("Util: " + var2.getMessage());
         return false;
      }
   }

   public static String printData(byte[] data, int len) {
      return new String(HexUtils.bArr2HexEdChars(data, len));
   }

   public static String printData(byte[] data) {
      return printData(data, data.length);
   }

   public static String printData(ByteBuffer buf) {
      byte[] data = new byte[buf.remaining()];
      buf.get(data);
      String hex = printData(data, data.length);
      ((Buffer)buf).position(buf.position() - data.length);
      return hex;
   }

   public static byte[] generateHex(int size) {
      byte[] array = new byte[size];
      Rnd.nextBytes(array);
      return array;
   }

   public static String getStackTrace(Throwable t) {
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      return sw.toString();
   }

   public static Map<Integer, Integer> sortMap(Map<Integer, Integer> map, boolean asc) {
      ValueSortMap vsm = new ValueSortMap();
      return vsm.sortThis(map, asc);
   }

   public static boolean ArrayContains(int[] paramArrayOfInt, int paramInt) {
      for(int k : paramArrayOfInt) {
         if (k == paramInt) {
            return true;
         }
      }

      return false;
   }

   public static String replaceIllegalCharacters(String str) {
      String valid = str;

      for(char c : ILLEGAL_CHARACTERS) {
         valid = valid.replace(c, '_');
      }

      return valid;
   }

   public static boolean isValidFileName(String name) {
      File f = new File(name);

      try {
         f.getCanonicalPath();
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public static void handleIllegalPlayerAction(Player actor, String message) {
      SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
      String date = _formatter.format(new Date());
      actor.addBannedAction("[" + date + "] " + message);
      if (actor.getBannedActions().size() >= Config.PUNISH_VALID_ATTEMPTS) {
         IllegalPlayerAction.IllegalAction(actor, actor.getBannedActions(), Config.DEFAULT_PUNISH);
      }
   }

   public static void addServiceLog(String message) {
      if (Config.SERVICE_LOGS) {
         SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
         String date = _formatter.format(new Date());
         message = "[" + date + "] " + message;
         ServiceLogs.addServiceLogs(message);
      }
   }

   public static String getRelativePath(File base, File file) {
      return file.toURI().getPath().substring(base.toURI().getPath().length());
   }

   public static final boolean isOnAngle(GameObject actor, GameObject target, int direction, int maxAngle) {
      boolean value = false;
      double angleToTarget = calculateAngleFrom(actor, target);
      double calcangle = convertHeadingToDegree(actor.getHeading()) + (double)direction;
      double angleDiff = calcangle - angleToTarget;
      double maxAngleDiff = (double)maxAngle / 2.0;
      if (angleDiff <= -360.0 + maxAngleDiff) {
         angleDiff += 360.0;
      }

      if (angleDiff >= 360.0 - maxAngleDiff) {
         angleDiff -= 360.0;
      }

      if (Math.abs(angleDiff) <= maxAngleDiff) {
         value = true;
      }

      return value;
   }

   public static double calculateAngleFrom(GameObject obj1, GameObject obj2) {
      return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
   }

   public static final double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
      double angleTarget = Math.toDegrees(Math.atan2((double)(obj2Y - obj1Y), (double)(obj2X - obj1X)));
      if (angleTarget < 0.0) {
         angleTarget += 360.0;
      }

      return angleTarget;
   }

   public static final double convertHeadingToDegree(int clientHeading) {
      return (double)clientHeading / 182.044444444;
   }

   public static final int convertDegreeToClientHeading(double degree) {
      if (degree < 0.0) {
         degree += 360.0;
      }

      return (int)(degree * 182.044444444);
   }

   public static final int calculateHeadingFrom(GameObject obj1, GameObject obj2) {
      return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
   }

   public static final int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
      double angleTarget = Math.toDegrees(Math.atan2((double)(obj2Y - obj1Y), (double)(obj2X - obj1X)));
      if (angleTarget < 0.0) {
         angleTarget += 360.0;
      }

      return (int)(angleTarget * 182.044444444);
   }

   public static final int calculateHeadingFrom(double dx, double dy) {
      double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
      if (angleTarget < 0.0) {
         angleTarget += 360.0;
      }

      return (int)(angleTarget * 182.044444444);
   }

   public static double calculateDistance(int x1, int y1, int x2, int y2) {
      return calculateDistance(x1, y1, 0, x2, y2, 0, false);
   }

   public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
      double dx = (double)x1 - (double)x2;
      double dy = (double)y1 - (double)y2;
      if (includeZAxis) {
         double dz = (double)(z1 - z2);
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
      } else {
         return Math.sqrt(dx * dx + dy * dy);
      }
   }

   public static double calculateDistance(GameObject obj1, GameObject obj2, boolean includeZAxis) {
      return obj1 != null && obj2 != null
         ? calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis)
         : 1000000.0;
   }

   public static String capitalizeFirst(String str) {
      if (str != null && !str.isEmpty()) {
         char[] arr = str.toCharArray();
         char c = arr[0];
         if (Character.isLetter(c)) {
            arr[0] = Character.toUpperCase(c);
         }

         return new String(arr);
      } else {
         return str;
      }
   }

   @Deprecated
   public static String capitalizeWords(String str) {
      if (str != null && !str.isEmpty()) {
         char[] charArray = str.toCharArray();
         StringBuilder result = new StringBuilder();
         charArray[0] = Character.toUpperCase(charArray[0]);

         for(int i = 0; i < charArray.length; ++i) {
            if (Character.isWhitespace(charArray[i])) {
               charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
            }

            result.append(charArray[i]);
         }

         return result.toString();
      } else {
         return str;
      }
   }

   public static boolean checkIfInRange(int range, int x, int y, int z, GameObject obj2, boolean includeZAxis) {
      if (obj2 == null) {
         return false;
      } else if (range == -1) {
         return true;
      } else {
         int rad = 0;
         if (obj2 instanceof Creature) {
            rad += ((Creature)obj2).getTemplate().getCollisionRadius();
         }

         double dx = (double)(x - obj2.getX());
         double dy = (double)(y - obj2.getY());
         if (includeZAxis) {
            double dz = (double)(z - obj2.getZ());
            double d = dx * dx + dy * dy + dz * dz;
            return d <= (double)(range * range + 2 * range * rad + rad * rad);
         } else {
            double d = dx * dx + dy * dy;
            return d <= (double)(range * range + 2 * range * rad + rad * rad);
         }
      }
   }

   public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis) {
      if (obj1 != null && obj2 != null) {
         if (obj1.getReflectionId() != obj2.getReflectionId()) {
            return false;
         } else if (range == -1) {
            return true;
         } else {
            int rad = 0;
            if (obj1 instanceof Creature) {
               rad += ((Creature)obj1).getTemplate().getCollisionRadius();
            }

            if (obj2 instanceof Creature) {
               rad += ((Creature)obj2).getTemplate().getCollisionRadius();
            }

            double dx = (double)(obj1.getX() - obj2.getX());
            double dy = (double)(obj1.getY() - obj2.getY());
            double d = dx * dx + dy * dy;
            if (includeZAxis) {
               double dz = (double)(obj1.getZ() - obj2.getZ());
               d += dz * dz;
            }

            return d <= (double)(range * range + 2 * range * rad + rad * rad);
         }
      } else {
         return false;
      }
   }

   public static boolean checkIfInShortRadius(int radius, GameObject obj1, GameObject obj2, boolean includeZAxis) {
      if (obj1 == null || obj2 == null) {
         return false;
      } else if (radius == -1) {
         return true;
      } else {
         int dx = obj1.getX() - obj2.getX();
         int dy = obj1.getY() - obj2.getY();
         if (includeZAxis) {
            int dz = obj1.getZ() - obj2.getZ();
            return dx * dx + dy * dy + dz * dz <= radius * radius;
         } else {
            return dx * dx + dy * dy <= radius * radius;
         }
      }
   }

   public static int countWords(String str) {
      return str.trim().split("\\s+").length;
   }

   public static String implodeString(Iterable<String> strings, String delimiter) {
      StringJoiner sj = new StringJoiner(delimiter);
      strings.forEach(sj::add);
      return sj.toString();
   }

   public static <T> String implode(T[] array, String delim) {
      String result = "";

      for(T val : array) {
         result = result + val.toString() + delim;
      }

      if (!result.isEmpty()) {
         result = result.substring(0, result.length() - 1);
      }

      return result;
   }

   public static float roundTo(float number, int numPlaces) {
      if (numPlaces <= 1) {
         return (float)Math.round(number);
      } else {
         float exponent = (float)Math.pow(10.0, (double)numPlaces);
         return (float)Math.round(number * exponent) / exponent;
      }
   }

   public static boolean isDigit(String text) {
      if (text != null && !text.isEmpty()) {
         for(char c : text.toCharArray()) {
            if (!Character.isDigit(c)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean isAlphaNumeric(String text) {
      if (text != null && !text.isEmpty()) {
         for(char c : text.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static String formatAdena(long amount) {
      synchronized(ADENA_FORMATTER) {
         return ADENA_FORMATTER.format(amount);
      }
   }

   public static String formatDouble(double val, String format) {
      DecimalFormat formatter = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
      return formatter.format(val);
   }

   public static String formatDate(Date date, String format) {
      if (date == null) {
         return null;
      } else {
         DateFormat dateFormat = new SimpleDateFormat(format);
         return dateFormat.format(date);
      }
   }

   public static <T> boolean contains(T[] array, T obj) {
      for(T element : array) {
         if (element == obj) {
            return true;
         }
      }

      return false;
   }

   public static boolean contains(int[] array, int obj) {
      for(int element : array) {
         if (element == obj) {
            return true;
         }
      }

      return false;
   }

   public static File[] getDatapackFiles(String dirname, String extention) {
      File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
      return !dir.exists() ? null : dir.listFiles(new ExtFilter(extention));
   }

   public static String getDateString(Date date) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return dateFormat.format(Long.valueOf(date.getTime()));
   }

   public static String reverseColor(String color) {
      if (color.length() != 6) {
         return "000000";
      } else {
         char[] ch1 = color.toCharArray();
         char[] ch2 = new char[]{ch1[4], ch1[5], ch1[2], ch1[3], ch1[0], ch1[1]};
         return new String(ch2);
      }
   }

   public static int decodeColor(String color) {
      return Integer.decode("0x" + reverseColor(color));
   }

   public static void sendHtml(Player activeChar, String html) {
      NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
      npcHtml.setHtml(activeChar, html);
      activeChar.sendPacket(npcHtml);
   }

   public static void fillMultiEditContent(Player activeChar, String text) {
      activeChar.sendPacket(
         new ShowBoard(
            Arrays.asList(
               "0",
               "0",
               "0",
               "0",
               "0",
               "0",
               activeChar.getName(),
               Integer.toString(activeChar.getObjectId()),
               activeChar.getAccountName(),
               "9",
               " ",
               " ",
               text.replaceAll("<br>", Config.EOL),
               "0",
               "0",
               "0",
               "0"
            )
         )
      );
   }

   public static int getPlayersCountInRadius(int range, GameObject npc, boolean playable, boolean invisible) {
      int count = 0;

      for(Creature cha : World.getInstance().getAroundCharacters(npc)) {
         if (cha != null
            && playable
            && (cha.isPlayable() || cha.isPet())
            && (invisible || !cha.isInvisible())
            && (cha.getZ() >= npc.getZ() - 100 || cha.getZ() <= npc.getZ() + 100)
            && GeoEngine.canSeeTarget(cha, npc, false)
            && checkIfInRange(range, npc, cha, true)
            && !cha.isDead()) {
            ++count;
         }
      }

      return count;
   }

   public static String getTimeFromMilliseconds(long millisec) {
      long seconds = millisec / 1000L;
      long minutes = seconds / 60L;
      long hours = minutes / 60L;
      long days = hours / 24L;
      seconds -= minutes * 60L;
      minutes -= hours * 60L;
      hours -= days * 24L;
      String result = "";
      if (days > 0L) {
         result = result + days;
      }

      if (hours > 0L) {
         result = result + hours;
      }

      if (minutes > 0L) {
         result = result + minutes;
      }

      if (seconds > 0L && hours < 1L) {
         result = result + seconds;
      }

      return result;
   }

   public static String formatTime(int time) {
      if (time == 0) {
         return "now";
      } else {
         time = Math.abs(time);
         String ret = "";
         long numDays = (long)(time / 86400);
         time = (int)((long)time - numDays * 86400L);
         long numHours = (long)(time / 3600);
         time = (int)((long)time - numHours * 3600L);
         long numMins = (long)(time / 60);
         time = (int)((long)time - numMins * 60L);
         long numSeconds = (long)time;
         if (numDays > 0L) {
            ret = ret + numDays + "d ";
         }

         if (numHours > 0L) {
            ret = ret + numHours + "h ";
         }

         if (numMins > 0L) {
            ret = ret + numMins + "m ";
         }

         if (numSeconds > 0L) {
            ret = ret + numSeconds + "s";
         }

         return ret.trim();
      }
   }

   public static int[] getRange(int start, int end) {
      if (start > end) {
         return null;
      } else {
         int[] range = new int[end - start + 1];

         for(int l = 0; start < end + 1; ++l) {
            range[l] = start++;
         }

         return range;
      }
   }

   public static long[] getRange(long start, long end) {
      if (start > end) {
         return null;
      } else {
         long[] range = new long[(int)(end - start + 1L)];

         for(int l = 0; start < end + 1L; ++l) {
            range[l] = start++;
         }

         return range;
      }
   }

   public static int[] unpackInt(int a, int bits) {
      int m = 32 / bits;
      int mval = (int)Math.pow(2.0, (double)bits);
      int[] result = new int[m];

      for(int i = m; i > 0; --i) {
         int next = a;
         a >>= bits;
         result[i - 1] = next - a * mval;
      }

      return result;
   }

   public static int[] unpackLong(long a, int bits) {
      int m = 64 / bits;
      int mval = (int)Math.pow(2.0, (double)bits);
      int[] result = new int[m];

      for(int i = m; i > 0; --i) {
         long next = a;
         a >>= bits;
         result[i - 1] = (int)(next - a * (long)mval);
      }

      return result;
   }

   public static int packInt(int[] a, int bits) throws Exception {
      int m = 32 / bits;
      if (a.length > m) {
         throw new Exception("Overflow");
      } else {
         int result = 0;
         int mval = (int)Math.pow(2.0, (double)bits);

         for(int i = 0; i < m; ++i) {
            result <<= bits;
            int next;
            if (a.length > i) {
               next = a[i];
               if (next >= mval || next < 0) {
                  throw new Exception("Overload, value is out of range");
               }
            } else {
               next = 0;
            }

            result += next;
         }

         return result;
      }
   }

   public static boolean isMatchingRegexp(String text, String template) {
      Pattern pattern = null;

      try {
         pattern = Pattern.compile(template);
      } catch (PatternSyntaxException var4) {
         var4.printStackTrace();
      }

      if (pattern == null) {
         return false;
      } else {
         Matcher regexp = pattern.matcher(text);
         return regexp.matches();
      }
   }

   public static String replaceRegexp(String source, String template, String replacement) {
      Pattern pattern = null;

      try {
         pattern = Pattern.compile(template);
      } catch (PatternSyntaxException var5) {
         var5.printStackTrace();
      }

      if (pattern != null) {
         Matcher regexp = pattern.matcher(source);
         source = regexp.replaceAll(replacement);
      }

      return source;
   }

   public static double calcDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
      return calcDistance(x1 - x2, y1 - y2, z1 - z2);
   }

   public static double calcDistance(int dx, int dy, int dz) {
      double dist = Math.sqrt((double)(dx * dx + dy * dy));
      return Math.sqrt(dist * dist + (double)(dz * dz));
   }

   public static double calcDistance(int dx, int dy) {
      return Math.sqrt((double)(dx * dx + dy * dy));
   }

   public static boolean contains(byte[] array, byte obj) {
      Arrays.sort(array);
      int index = Arrays.binarySearch(array, obj);
      return index >= 0;
   }

   public static boolean isNumber(String s) {
      try {
         Double.parseDouble(s);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static String formatPay(Player player, long count, int item) {
      return count > 0L
         ? formatAdena(count) + " " + getItemName(player, item)
         : "" + ServerStorage.getInstance().getString(player.getLang(), "Util.FREE") + "";
   }

   public static String getItemName(Player player, int itemId) {
      switch(itemId) {
         case -300:
            return ServerStorage.getInstance().getString(player.getLang(), "Util.FAME");
         case -200:
            return ServerStorage.getInstance().getString(player.getLang(), "Util.REPUTATION");
         case -100:
            return ServerStorage.getInstance().getString(player.getLang(), "Util.PC_BANG");
         case -1:
            return ServerStorage.getInstance().getString(player.getLang(), "Util.PRIME_POINT");
         default:
            Item item = ItemsParser.getInstance().getTemplate(itemId);
            if (item == null) {
               return "No Name";
            } else {
               return player.getLang() != null && !player.getLang().equalsIgnoreCase("en") ? item.getNameRu() : item.getNameEn();
            }
      }
   }

   public static String getItemIcon(int itemId) {
      switch(itemId) {
         case -300:
            return "icon.pvp_point_i00";
         case -200:
            return "icon.skill0390";
         case -100:
            return "icon.etc_pccafe_point_i00";
         case -1:
            return "icon.etc_royal_membership_i00";
         default:
            Item item = ItemsParser.getInstance().getTemplate(itemId);
            return item != null ? item.getIcon() : "icon.etc_question_mark_i00";
      }
   }

   public static String getNumberWithCommas(long number) {
      String text = String.valueOf(number);
      int size = text.length();

      for(int i = size; i > 0; --i) {
         if ((size - i) % 3 == 0 && i < size) {
            text = text.substring(0, i) + ',' + text.substring(i);
         }
      }

      return text;
   }

   public static String dateFormat() {
      return new SimpleDateFormat("yyyy/MM/dd HH:mm (E)").format(new Date());
   }

   public static String dateFormat(long d) {
      return new SimpleDateFormat("yyyy/MM/dd HH:mm (E)").format(new Date(d));
   }

   public static String dateFormat(Date d) {
      return new SimpleDateFormat("yyyy/MM/dd HH:mm (E)").format(d);
   }

   public static String dateFormat(Calendar d) {
      return new SimpleDateFormat("yyyy/MM/dd HH:mm (E)").format(d.getTime());
   }

   public static int min(int value1, int value2, int... values) {
      int min = Math.min(value1, value2);

      for(int value : values) {
         if (min > value) {
            min = value;
         }
      }

      return min;
   }

   public static int max(int value1, int value2, int... values) {
      int max = Math.max(value1, value2);

      for(int value : values) {
         if (max < value) {
            max = value;
         }
      }

      return max;
   }

   public static long min(long value1, long value2, long... values) {
      long min = Math.min(value1, value2);

      for(long value : values) {
         if (min > value) {
            min = value;
         }
      }

      return min;
   }

   public static long max(long value1, long value2, long... values) {
      long max = Math.max(value1, value2);

      for(long value : values) {
         if (max < value) {
            max = value;
         }
      }

      return max;
   }

   public static float min(float value1, float value2, float... values) {
      float min = Math.min(value1, value2);

      for(float value : values) {
         if (min > value) {
            min = value;
         }
      }

      return min;
   }

   public static float max(float value1, float value2, float... values) {
      float max = Math.max(value1, value2);

      for(float value : values) {
         if (max < value) {
            max = value;
         }
      }

      return max;
   }

   public static double min(double value1, double value2, double... values) {
      double min = Math.min(value1, value2);

      for(double value : values) {
         if (min > value) {
            min = value;
         }
      }

      return min;
   }

   public static double max(double value1, double value2, double... values) {
      double max = Math.max(value1, value2);

      for(double value : values) {
         if (max < value) {
            max = value;
         }
      }

      return max;
   }

   public static int getIndexOfMaxValue(int... array) {
      int index = 0;

      for(int i = 1; i < array.length; ++i) {
         if (array[i] > array[index]) {
            index = i;
         }
      }

      return index;
   }

   public static int getIndexOfMinValue(int... array) {
      int index = 0;

      for(int i = 1; i < array.length; ++i) {
         if (array[i] < array[index]) {
            index = i;
         }
      }

      return index;
   }

   public static String getFormatedChance(double chance) {
      NumberFormat pf;
      if (chance < 1.0E-12) {
         pf = PERCENT_FORMAT_15;
      } else if (chance < 1.0E-7) {
         pf = PERCENT_FORMAT_10;
      } else {
         pf = PERCENT_FORMAT_5;
      }

      return pf.format(chance);
   }

   public static String formatDropChance(String chance) {
      String realChance = chance;
      if (chance.length() - chance.indexOf(46) > 6) {
         realChance = chance.substring(0, chance.indexOf(46) + 7);
      }

      if (realChance.endsWith(".0")) {
         realChance = realChance.substring(0, realChance.length() - 2);
      }

      return realChance + '%';
   }

   public static String declension(Player player, long count, DeclensionKey word) {
      String one = "";
      String two = "";
      String five = "";
      switch(word) {
         case DAYS:
            one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.DAYS_ONE") + "");
            two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.DAYS_TWO") + "");
            five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.DAYS_FIVE") + "");
            break;
         case HOUR:
            one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.HOURS_ONE") + "");
            two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.HOURS_TWO") + "");
            five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.HOURS_FIVE") + "");
            break;
         case MINUTES:
            one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.MIN_ONE") + "");
            two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.MIN_TWO") + "");
            five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.MIN_FIVE") + "");
            break;
         case PIECE:
            one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.PIECES_ONE") + "");
            two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.PIECES_TWO") + "");
            five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.PIECES_FIVE") + "");
            break;
         case POINT:
            one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.POINTS_ONE") + "");
            two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.POINTS_TWO") + "");
            five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.POINTS_FIVE") + "");
      }

      if (count > 100L) {
         count %= 100L;
      }

      if (count > 20L) {
         count %= 10L;
      }

      if (count == 1L) {
         return one.toString();
      } else {
         return count != 2L && count != 3L && count != 4L ? five.toString() : two.toString();
      }
   }

   public static String boolToString(Player player, boolean b) {
      return b
         ? "" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + ""
         : "" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "";
   }

   public static final String className(Player player, int classId) {
      return className(player.getLang(), classId);
   }

   public static final String className(String lang, int classId) {
      Map<Integer, String> classList = new HashMap<>();
      classList.put(0, "" + ServerStorage.getInstance().getString(lang, "ClassName.0") + "");
      classList.put(1, "" + ServerStorage.getInstance().getString(lang, "ClassName.1") + "");
      classList.put(2, "" + ServerStorage.getInstance().getString(lang, "ClassName.2") + "");
      classList.put(3, "" + ServerStorage.getInstance().getString(lang, "ClassName.3") + "");
      classList.put(4, "" + ServerStorage.getInstance().getString(lang, "ClassName.4") + "");
      classList.put(5, "" + ServerStorage.getInstance().getString(lang, "ClassName.5") + "");
      classList.put(6, "" + ServerStorage.getInstance().getString(lang, "ClassName.6") + "");
      classList.put(7, "" + ServerStorage.getInstance().getString(lang, "ClassName.7") + "");
      classList.put(8, "" + ServerStorage.getInstance().getString(lang, "ClassName.8") + "");
      classList.put(9, "" + ServerStorage.getInstance().getString(lang, "ClassName.9") + "");
      classList.put(10, "" + ServerStorage.getInstance().getString(lang, "ClassName.10") + "");
      classList.put(11, "" + ServerStorage.getInstance().getString(lang, "ClassName.11") + "");
      classList.put(12, "" + ServerStorage.getInstance().getString(lang, "ClassName.12") + "");
      classList.put(13, "" + ServerStorage.getInstance().getString(lang, "ClassName.13") + "");
      classList.put(14, "" + ServerStorage.getInstance().getString(lang, "ClassName.14") + "");
      classList.put(15, "" + ServerStorage.getInstance().getString(lang, "ClassName.15") + "");
      classList.put(16, "" + ServerStorage.getInstance().getString(lang, "ClassName.16") + "");
      classList.put(17, "" + ServerStorage.getInstance().getString(lang, "ClassName.17") + "");
      classList.put(18, "" + ServerStorage.getInstance().getString(lang, "ClassName.18") + "");
      classList.put(19, "" + ServerStorage.getInstance().getString(lang, "ClassName.19") + "");
      classList.put(20, "" + ServerStorage.getInstance().getString(lang, "ClassName.20") + "");
      classList.put(21, "" + ServerStorage.getInstance().getString(lang, "ClassName.21") + "");
      classList.put(22, "" + ServerStorage.getInstance().getString(lang, "ClassName.22") + "");
      classList.put(23, "" + ServerStorage.getInstance().getString(lang, "ClassName.23") + "");
      classList.put(24, "" + ServerStorage.getInstance().getString(lang, "ClassName.24") + "");
      classList.put(25, "" + ServerStorage.getInstance().getString(lang, "ClassName.25") + "");
      classList.put(26, "" + ServerStorage.getInstance().getString(lang, "ClassName.26") + "");
      classList.put(27, "" + ServerStorage.getInstance().getString(lang, "ClassName.27") + "");
      classList.put(28, "" + ServerStorage.getInstance().getString(lang, "ClassName.28") + "");
      classList.put(29, "" + ServerStorage.getInstance().getString(lang, "ClassName.29") + "");
      classList.put(30, "" + ServerStorage.getInstance().getString(lang, "ClassName.30") + "");
      classList.put(31, "" + ServerStorage.getInstance().getString(lang, "ClassName.31") + "");
      classList.put(32, "" + ServerStorage.getInstance().getString(lang, "ClassName.32") + "");
      classList.put(33, "" + ServerStorage.getInstance().getString(lang, "ClassName.33") + "");
      classList.put(34, "" + ServerStorage.getInstance().getString(lang, "ClassName.34") + "");
      classList.put(35, "" + ServerStorage.getInstance().getString(lang, "ClassName.35") + "");
      classList.put(36, "" + ServerStorage.getInstance().getString(lang, "ClassName.36") + "");
      classList.put(37, "" + ServerStorage.getInstance().getString(lang, "ClassName.37") + "");
      classList.put(38, "" + ServerStorage.getInstance().getString(lang, "ClassName.38") + "");
      classList.put(39, "" + ServerStorage.getInstance().getString(lang, "ClassName.39") + "");
      classList.put(40, "" + ServerStorage.getInstance().getString(lang, "ClassName.40") + "");
      classList.put(41, "" + ServerStorage.getInstance().getString(lang, "ClassName.41") + "");
      classList.put(42, "" + ServerStorage.getInstance().getString(lang, "ClassName.42") + "");
      classList.put(43, "" + ServerStorage.getInstance().getString(lang, "ClassName.43") + "");
      classList.put(44, "" + ServerStorage.getInstance().getString(lang, "ClassName.44") + "");
      classList.put(45, "" + ServerStorage.getInstance().getString(lang, "ClassName.45") + "");
      classList.put(46, "" + ServerStorage.getInstance().getString(lang, "ClassName.46") + "");
      classList.put(47, "" + ServerStorage.getInstance().getString(lang, "ClassName.47") + "");
      classList.put(48, "" + ServerStorage.getInstance().getString(lang, "ClassName.48") + "");
      classList.put(49, "" + ServerStorage.getInstance().getString(lang, "ClassName.49") + "");
      classList.put(50, "" + ServerStorage.getInstance().getString(lang, "ClassName.50") + "");
      classList.put(51, "" + ServerStorage.getInstance().getString(lang, "ClassName.51") + "");
      classList.put(52, "" + ServerStorage.getInstance().getString(lang, "ClassName.52") + "");
      classList.put(53, "" + ServerStorage.getInstance().getString(lang, "ClassName.53") + "");
      classList.put(54, "" + ServerStorage.getInstance().getString(lang, "ClassName.54") + "");
      classList.put(55, "" + ServerStorage.getInstance().getString(lang, "ClassName.55") + "");
      classList.put(56, "" + ServerStorage.getInstance().getString(lang, "ClassName.56") + "");
      classList.put(57, "" + ServerStorage.getInstance().getString(lang, "ClassName.57") + "");
      classList.put(88, "" + ServerStorage.getInstance().getString(lang, "ClassName.88") + "");
      classList.put(89, "" + ServerStorage.getInstance().getString(lang, "ClassName.89") + "");
      classList.put(90, "" + ServerStorage.getInstance().getString(lang, "ClassName.90") + "");
      classList.put(91, "" + ServerStorage.getInstance().getString(lang, "ClassName.91") + "");
      classList.put(92, "" + ServerStorage.getInstance().getString(lang, "ClassName.92") + "");
      classList.put(93, "" + ServerStorage.getInstance().getString(lang, "ClassName.93") + "");
      classList.put(94, "" + ServerStorage.getInstance().getString(lang, "ClassName.94") + "");
      classList.put(95, "" + ServerStorage.getInstance().getString(lang, "ClassName.95") + "");
      classList.put(96, "" + ServerStorage.getInstance().getString(lang, "ClassName.96") + "");
      classList.put(97, "" + ServerStorage.getInstance().getString(lang, "ClassName.97") + "");
      classList.put(98, "" + ServerStorage.getInstance().getString(lang, "ClassName.98") + "");
      classList.put(99, "" + ServerStorage.getInstance().getString(lang, "ClassName.99") + "");
      classList.put(100, "" + ServerStorage.getInstance().getString(lang, "ClassName.100") + "");
      classList.put(101, "" + ServerStorage.getInstance().getString(lang, "ClassName.101") + "");
      classList.put(102, "" + ServerStorage.getInstance().getString(lang, "ClassName.102") + "");
      classList.put(103, "" + ServerStorage.getInstance().getString(lang, "ClassName.103") + "");
      classList.put(104, "" + ServerStorage.getInstance().getString(lang, "ClassName.104") + "");
      classList.put(105, "" + ServerStorage.getInstance().getString(lang, "ClassName.105") + "");
      classList.put(106, "" + ServerStorage.getInstance().getString(lang, "ClassName.106") + "");
      classList.put(107, "" + ServerStorage.getInstance().getString(lang, "ClassName.107") + "");
      classList.put(108, "" + ServerStorage.getInstance().getString(lang, "ClassName.108") + "");
      classList.put(109, "" + ServerStorage.getInstance().getString(lang, "ClassName.109") + "");
      classList.put(110, "" + ServerStorage.getInstance().getString(lang, "ClassName.110") + "");
      classList.put(111, "" + ServerStorage.getInstance().getString(lang, "ClassName.111") + "");
      classList.put(112, "" + ServerStorage.getInstance().getString(lang, "ClassName.112") + "");
      classList.put(113, "" + ServerStorage.getInstance().getString(lang, "ClassName.113") + "");
      classList.put(114, "" + ServerStorage.getInstance().getString(lang, "ClassName.114") + "");
      classList.put(115, "" + ServerStorage.getInstance().getString(lang, "ClassName.115") + "");
      classList.put(116, "" + ServerStorage.getInstance().getString(lang, "ClassName.116") + "");
      classList.put(117, "" + ServerStorage.getInstance().getString(lang, "ClassName.117") + "");
      classList.put(118, "" + ServerStorage.getInstance().getString(lang, "ClassName.118") + "");
      classList.put(123, "" + ServerStorage.getInstance().getString(lang, "ClassName.123") + "");
      classList.put(124, "" + ServerStorage.getInstance().getString(lang, "ClassName.124") + "");
      classList.put(125, "" + ServerStorage.getInstance().getString(lang, "ClassName.125") + "");
      classList.put(126, "" + ServerStorage.getInstance().getString(lang, "ClassName.126") + "");
      classList.put(127, "" + ServerStorage.getInstance().getString(lang, "ClassName.127") + "");
      classList.put(128, "" + ServerStorage.getInstance().getString(lang, "ClassName.128") + "");
      classList.put(129, "" + ServerStorage.getInstance().getString(lang, "ClassName.129") + "");
      classList.put(130, "" + ServerStorage.getInstance().getString(lang, "ClassName.130") + "");
      classList.put(131, "" + ServerStorage.getInstance().getString(lang, "ClassName.131") + "");
      classList.put(132, "" + ServerStorage.getInstance().getString(lang, "ClassName.132") + "");
      classList.put(133, "" + ServerStorage.getInstance().getString(lang, "ClassName.133") + "");
      classList.put(134, "" + ServerStorage.getInstance().getString(lang, "ClassName.134") + "");
      classList.put(135, "" + ServerStorage.getInstance().getString(lang, "ClassName.135") + "");
      classList.put(136, "" + ServerStorage.getInstance().getString(lang, "ClassName.136") + "");
      return classList.get(classId);
   }

   public static final String className(Player player, String name) {
      String lang = player.getLang();
      Map<String, String> classList = new HashMap<>();
      classList.put("HumanFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.0") + "");
      classList.put("Warrior", "" + ServerStorage.getInstance().getString(lang, "ClassName.1") + "");
      classList.put("Gladiator", "" + ServerStorage.getInstance().getString(lang, "ClassName.2") + "");
      classList.put("Warlord", "" + ServerStorage.getInstance().getString(lang, "ClassName.3") + "");
      classList.put("HumanKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.4") + "");
      classList.put("Paladin", "" + ServerStorage.getInstance().getString(lang, "ClassName.5") + "");
      classList.put("DarkAvenger", "" + ServerStorage.getInstance().getString(lang, "ClassName.6") + "");
      classList.put("Rogue", "" + ServerStorage.getInstance().getString(lang, "ClassName.7") + "");
      classList.put("TreasureHunter", "" + ServerStorage.getInstance().getString(lang, "ClassName.8") + "");
      classList.put("Hawkeye", "" + ServerStorage.getInstance().getString(lang, "ClassName.9") + "");
      classList.put("HumanMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.10") + "");
      classList.put("HumanWizard", "" + ServerStorage.getInstance().getString(lang, "ClassName.11") + "");
      classList.put("Sorceror", "" + ServerStorage.getInstance().getString(lang, "ClassName.12") + "");
      classList.put("Necromancer", "" + ServerStorage.getInstance().getString(lang, "ClassName.13") + "");
      classList.put("Warlock", "" + ServerStorage.getInstance().getString(lang, "ClassName.14") + "");
      classList.put("Cleric", "" + ServerStorage.getInstance().getString(lang, "ClassName.15") + "");
      classList.put("Bishop", "" + ServerStorage.getInstance().getString(lang, "ClassName.16") + "");
      classList.put("Prophet", "" + ServerStorage.getInstance().getString(lang, "ClassName.17") + "");
      classList.put("ElvenFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.18") + "");
      classList.put("ElvenKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.19") + "");
      classList.put("TempleKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.20") + "");
      classList.put("Swordsinger", "" + ServerStorage.getInstance().getString(lang, "ClassName.21") + "");
      classList.put("ElvenScout", "" + ServerStorage.getInstance().getString(lang, "ClassName.22") + "");
      classList.put("Plainswalker", "" + ServerStorage.getInstance().getString(lang, "ClassName.23") + "");
      classList.put("SilverRanger", "" + ServerStorage.getInstance().getString(lang, "ClassName.24") + "");
      classList.put("ElvenMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.25") + "");
      classList.put("ElvenWizard", "" + ServerStorage.getInstance().getString(lang, "ClassName.26") + "");
      classList.put("Spellsinger", "" + ServerStorage.getInstance().getString(lang, "ClassName.27") + "");
      classList.put("ElementalSummoner", "" + ServerStorage.getInstance().getString(lang, "ClassName.28") + "");
      classList.put("ElvenOracle", "" + ServerStorage.getInstance().getString(lang, "ClassName.29") + "");
      classList.put("ElvenElder", "" + ServerStorage.getInstance().getString(lang, "ClassName.30") + "");
      classList.put("DarkElvenFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.31") + "");
      classList.put("PalusKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.32") + "");
      classList.put("ShillienKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.33") + "");
      classList.put("Bladedancer", "" + ServerStorage.getInstance().getString(lang, "ClassName.34") + "");
      classList.put("Assassin", "" + ServerStorage.getInstance().getString(lang, "ClassName.35") + "");
      classList.put("AbyssWalker", "" + ServerStorage.getInstance().getString(lang, "ClassName.36") + "");
      classList.put("PhantomRanger", "" + ServerStorage.getInstance().getString(lang, "ClassName.37") + "");
      classList.put("DarkElvenMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.38") + "");
      classList.put("DarkElvenWizard", "" + ServerStorage.getInstance().getString(lang, "ClassName.39") + "");
      classList.put("Spellhowler", "" + ServerStorage.getInstance().getString(lang, "ClassName.40") + "");
      classList.put("PhantomSummoner", "" + ServerStorage.getInstance().getString(lang, "ClassName.41") + "");
      classList.put("ShillienOracle", "" + ServerStorage.getInstance().getString(lang, "ClassName.42") + "");
      classList.put("ShillienElder", "" + ServerStorage.getInstance().getString(lang, "ClassName.43") + "");
      classList.put("OrcFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.44") + "");
      classList.put("OrcRaider", "" + ServerStorage.getInstance().getString(lang, "ClassName.45") + "");
      classList.put("Destroyer", "" + ServerStorage.getInstance().getString(lang, "ClassName.46") + "");
      classList.put("OrcMonk", "" + ServerStorage.getInstance().getString(lang, "ClassName.47") + "");
      classList.put("Tyrant", "" + ServerStorage.getInstance().getString(lang, "ClassName.48") + "");
      classList.put("OrcMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.49") + "");
      classList.put("OrcShaman", "" + ServerStorage.getInstance().getString(lang, "ClassName.50") + "");
      classList.put("Overlord", "" + ServerStorage.getInstance().getString(lang, "ClassName.51") + "");
      classList.put("Warcryer", "" + ServerStorage.getInstance().getString(lang, "ClassName.52") + "");
      classList.put("DwarvenFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.53") + "");
      classList.put("DwarvenScavenger", "" + ServerStorage.getInstance().getString(lang, "ClassName.54") + "");
      classList.put("BountyHunter", "" + ServerStorage.getInstance().getString(lang, "ClassName.55") + "");
      classList.put("DwarvenArtisan", "" + ServerStorage.getInstance().getString(lang, "ClassName.56") + "");
      classList.put("Warsmith", "" + ServerStorage.getInstance().getString(lang, "ClassName.57") + "");
      classList.put("duelist", "" + ServerStorage.getInstance().getString(lang, "ClassName.88") + "");
      classList.put("dreadnought", "" + ServerStorage.getInstance().getString(lang, "ClassName.89") + "");
      classList.put("phoenixKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.90") + "");
      classList.put("hellKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.91") + "");
      classList.put("sagittarius", "" + ServerStorage.getInstance().getString(lang, "ClassName.92") + "");
      classList.put("adventurer", "" + ServerStorage.getInstance().getString(lang, "ClassName.93") + "");
      classList.put("archmage", "" + ServerStorage.getInstance().getString(lang, "ClassName.94") + "");
      classList.put("soultaker", "" + ServerStorage.getInstance().getString(lang, "ClassName.95") + "");
      classList.put("arcanaLord", "" + ServerStorage.getInstance().getString(lang, "ClassName.96") + "");
      classList.put("cardinal", "" + ServerStorage.getInstance().getString(lang, "ClassName.97") + "");
      classList.put("hierophant", "" + ServerStorage.getInstance().getString(lang, "ClassName.98") + "");
      classList.put("evaTemplar", "" + ServerStorage.getInstance().getString(lang, "ClassName.99") + "");
      classList.put("swordMuse", "" + ServerStorage.getInstance().getString(lang, "ClassName.100") + "");
      classList.put("windRider", "" + ServerStorage.getInstance().getString(lang, "ClassName.101") + "");
      classList.put("moonlightSentinel", "" + ServerStorage.getInstance().getString(lang, "ClassName.102") + "");
      classList.put("mysticMuse", "" + ServerStorage.getInstance().getString(lang, "ClassName.103") + "");
      classList.put("elementalMaster", "" + ServerStorage.getInstance().getString(lang, "ClassName.104") + "");
      classList.put("evaSaint", "" + ServerStorage.getInstance().getString(lang, "ClassName.105") + "");
      classList.put("shillienTemplar", "" + ServerStorage.getInstance().getString(lang, "ClassName.106") + "");
      classList.put("spectralDancer", "" + ServerStorage.getInstance().getString(lang, "ClassName.107") + "");
      classList.put("ghostHunter", "" + ServerStorage.getInstance().getString(lang, "ClassName.108") + "");
      classList.put("ghostSentinel", "" + ServerStorage.getInstance().getString(lang, "ClassName.109") + "");
      classList.put("stormScreamer", "" + ServerStorage.getInstance().getString(lang, "ClassName.110") + "");
      classList.put("spectralMaster", "" + ServerStorage.getInstance().getString(lang, "ClassName.111") + "");
      classList.put("shillienSaint", "" + ServerStorage.getInstance().getString(lang, "ClassName.112") + "");
      classList.put("titan", "" + ServerStorage.getInstance().getString(lang, "ClassName.113") + "");
      classList.put("grandKhavatari", "" + ServerStorage.getInstance().getString(lang, "ClassName.114") + "");
      classList.put("dominator", "" + ServerStorage.getInstance().getString(lang, "ClassName.115") + "");
      classList.put("doomcryer", "" + ServerStorage.getInstance().getString(lang, "ClassName.116") + "");
      classList.put("fortuneSeeker", "" + ServerStorage.getInstance().getString(lang, "ClassName.117") + "");
      classList.put("maestro", "" + ServerStorage.getInstance().getString(lang, "ClassName.118") + "");
      classList.put("maleSoldier", "" + ServerStorage.getInstance().getString(lang, "ClassName.123") + "");
      classList.put("femaleSoldier", "" + ServerStorage.getInstance().getString(lang, "ClassName.124") + "");
      classList.put("trooper", "" + ServerStorage.getInstance().getString(lang, "ClassName.125") + "");
      classList.put("warder", "" + ServerStorage.getInstance().getString(lang, "ClassName.126") + "");
      classList.put("berserker", "" + ServerStorage.getInstance().getString(lang, "ClassName.127") + "");
      classList.put("maleSoulbreaker", "" + ServerStorage.getInstance().getString(lang, "ClassName.128") + "");
      classList.put("femaleSoulbreaker", "" + ServerStorage.getInstance().getString(lang, "ClassName.129") + "");
      classList.put("arbalester", "" + ServerStorage.getInstance().getString(lang, "ClassName.130") + "");
      classList.put("doombringer", "" + ServerStorage.getInstance().getString(lang, "ClassName.131") + "");
      classList.put("maleSoulhound", "" + ServerStorage.getInstance().getString(lang, "ClassName.132") + "");
      classList.put("femaleSoulhound", "" + ServerStorage.getInstance().getString(lang, "ClassName.133") + "");
      classList.put("trickster", "" + ServerStorage.getInstance().getString(lang, "ClassName.134") + "");
      classList.put("inspector", "" + ServerStorage.getInstance().getString(lang, "ClassName.135") + "");
      classList.put("judicator", "" + ServerStorage.getInstance().getString(lang, "ClassName.136") + "");
      return classList.get(name);
   }

   public static HashMap<Integer, String> parseTemplate(String html) {
      Matcher m = _pattern.matcher(html);

      HashMap<Integer, String> tpls;
      for(tpls = new HashMap<>(); m.find(); html = html.replace(m.group(0), "")) {
         tpls.put(Integer.parseInt(m.group(1)), m.group(2));
      }

      tpls.put(0, html);
      return tpls;
   }

   public static final String clanHallName(Player player, int hallId) {
      String lang = player != null ? player.getLang() : "en";
      return ServerStorage.getInstance().getString(lang, "ClanHall.NAME_" + hallId + "");
   }

   public static final String clanHallDescription(Player player, int hallId) {
      String lang = player != null ? player.getLang() : "en";
      return ServerStorage.getInstance().getString(lang, "ClanHall.DESCR_" + hallId + "");
   }

   public static final String clanHallLocation(Player player, int hallId) {
      String lang = player != null ? player.getLang() : "en";
      return ServerStorage.getInstance().getString(lang, "ClanHall.LOC_" + hallId + "");
   }

   public static boolean arrayContains(@Nullable Object[] array, @Nullable Object objectToLookFor) {
      if (array != null && objectToLookFor != null) {
         for(Object objectInArray : array) {
            if (objectInArray != null && objectInArray.equals(objectToLookFor)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void setHtml(String text, Player self, Object... arg) {
      if (text != null && self != null) {
         NpcHtmlMessage msg = new NpcHtmlMessage(0);
         if (!text.endsWith(".html") && !text.endsWith(".htm")) {
            msg.setHtml(self, Strings.bbParse(text));
         } else {
            msg.setFile(self, text);
         }

         if (arg != null && arg.length % 2 == 0) {
            for(int i = 0; i < arg.length; i = 2) {
               msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
            }
         }

         self.sendPacket(msg);
      }
   }

   public static String joinArrayWithCharacter(Collection<?> list, String joiner) {
      if (list != null && !list.isEmpty()) {
         String result = "";

         for(Object val : list) {
            result = result + val.toString() + joiner;
         }

         return result.substring(0, result.length() - joiner.length());
      } else {
         return "";
      }
   }

   public static String joinArrayWithCharacter(Object[] list, String joiner) {
      if (list != null && list.length >= 1) {
         String result = "";

         for(Object val : list) {
            result = result + val + joiner;
         }

         return result.substring(0, result.length() - joiner.length());
      } else {
         return "";
      }
   }

   public static String joinArrayWithCharacters(String[] list, String start, String end) {
      if (list != null && list.length >= 1) {
         String result = "";

         for(String val : list) {
            result = result + start + val + end;
         }

         return result;
      } else {
         return "";
      }
   }

   public static String getAllTokens(StringTokenizer st) {
      if (!st.hasMoreTokens()) {
         return "";
      } else {
         String text = st.nextToken();

         while(st.hasMoreTokens()) {
            text = text + " " + st.nextToken();
         }

         return text;
      }
   }

   public static String getNavigationBlock(int count, int page, int totalSize, int perPage, boolean isThereNextPage, String bypass) {
      String navigation = "";
      boolean prePage = false;
      boolean curPage = false;
      boolean nextPage = false;
      String bypassPrev = bypass;
      String bypassNext = bypass;

      for(int i = 1; i <= count; ++i) {
         if (!prePage) {
            if (page == 1) {
               navigation = navigation + "<td width=80 align=left valign=top>&nbsp;</td>";
            } else {
               bypassPrev = String.format("" + bypassPrev + "", page - 1);
               navigation = navigation
                  + "<td width=80 align=left valign=top><button action=\"bypass -h "
                  + bypassPrev
                  + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\"></td>";
            }

            prePage = true;
         }

         if (!curPage && i == page) {
            if (totalSize <= perPage) {
               navigation = navigation + "<td width=50 align=center valign=top>&nbsp;</td>";
            } else {
               navigation = navigation + "<td width=50 align=center valign=top>[ " + i + " ]</td>";
            }

            curPage = true;
         }

         if (!nextPage && i == page) {
            if (isThereNextPage && count >= page + 1) {
               bypassNext = String.format("" + bypassNext + "", page + 1);
               navigation = navigation
                  + "<td width=80 align=right valign=top><button action=\"bypass -h "
                  + bypassNext
                  + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\"></td>";
            } else {
               navigation = navigation + "<td width=80 align=right valign=top>&nbsp;</td>";
            }

            nextPage = true;
         }
      }

      if (navigation.equals("")) {
         navigation = "<td width=30 align=center valign=top>&nbsp;</td>";
      }

      return navigation;
   }

   public static int limit(int numToTest, int min, int max) {
      return numToTest > max ? max : (numToTest < min ? min : numToTest);
   }

   public static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];

      for(int i = 0; i < bytes.length; ++i) {
         int v = bytes[i] & 255;
         hexChars[i * 2] = HEX_ARRAY[v >>> 4];
         hexChars[i * 2 + 1] = HEX_ARRAY[v & 15];
      }

      return new String(hexChars);
   }

   static {
      PERCENT_FORMAT_5.setMaximumFractionDigits(5);
      PERCENT_FORMAT_5.setMinimumFractionDigits(0);
      PERCENT_FORMAT_10.setMaximumFractionDigits(10);
      PERCENT_FORMAT_10.setMinimumFractionDigits(0);
      PERCENT_FORMAT_15.setMaximumFractionDigits(15);
      PERCENT_FORMAT_15.setMinimumFractionDigits(0);
   }
}
