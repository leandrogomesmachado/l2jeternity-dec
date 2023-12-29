package l2e.gameserver.model.olympiad;

import java.util.List;
import l2e.gameserver.Config;

public class OlympiadGameNonClassed extends OlympiadGameNormal {
   private OlympiadGameNonClassed(int id, Participant[] opponents) {
      super(id, opponents);
   }

   @Override
   public final CompetitionType getType() {
      return CompetitionType.NON_CLASSED;
   }

   @Override
   protected final int getDivider() {
      return Config.ALT_OLY_DIVIDER_NON_CLASSED;
   }

   @Override
   protected final int[][] getReward() {
      return Config.ALT_OLY_NONCLASSED_REWARD;
   }

   @Override
   protected final String getWeeklyMatchType() {
      return "competitions_done_week_non_classed";
   }

   protected static final OlympiadGameNonClassed createGame(int id, List<Integer> list) {
      Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
      return opponents == null ? null : new OlympiadGameNonClassed(id, opponents);
   }
}
