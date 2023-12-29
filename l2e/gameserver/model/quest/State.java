package l2e.gameserver.model.quest;

public class State {
   public static final byte CREATED = 0;
   public static final byte STARTED = 1;
   public static final byte COMPLETED = 2;

   public static String getStateName(byte state) {
      switch(state) {
         case 1:
            return "Started";
         case 2:
            return "Completed";
         default:
            return "Start";
      }
   }

   public static byte getStateId(String statename) {
      switch(statename) {
         case "Started":
            return 1;
         case "Completed":
            return 2;
         default:
            return 0;
      }
   }
}
