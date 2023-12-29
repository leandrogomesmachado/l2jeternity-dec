package l2e.gameserver.model.olympiad;

import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;

public class OlympiadGameClassed extends OlympiadGameNormal {
   private OlympiadGameClassed(int id, Participant[] opponents) {
      super(id, opponents);
   }

   @Override
   public final CompetitionType getType() {
      return CompetitionType.CLASSED;
   }

   @Override
   protected final int getDivider() {
      return Config.ALT_OLY_DIVIDER_CLASSED;
   }

   @Override
   protected final int[][] getReward() {
      return Config.ALT_OLY_CLASSED_REWARD;
   }

   @Override
   protected final String getWeeklyMatchType() {
      return "competitions_done_week_classed";
   }

   protected static final OlympiadGameClassed createGame(int id, List<List<Integer>> classList) {
      if (classList != null && !classList.isEmpty()) {
         while(!classList.isEmpty()) {
            List<Integer> list = classList.get(Rnd.nextInt(classList.size()));
            if (list != null && list.size() >= 2) {
               Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
               if (opponents != null) {
                  return new OlympiadGameClassed(id, opponents);
               }

               classList.remove(list);
            } else {
               classList.remove(list);
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
