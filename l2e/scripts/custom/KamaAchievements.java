package l2e.scripts.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class KamaAchievements extends Quest {
   private static final int Pathfinder = 32484;

   public KamaAchievements(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32484);
      this.addTalkId(32484);
      Calendar resetTime = Calendar.getInstance();
      resetTime.set(11, 6);
      resetTime.set(12, 30);
      long resetDelay = resetTime.getTimeInMillis();
      if (resetDelay < System.currentTimeMillis()) {
         resetDelay += 86400000L;
      }

      this.startQuestTimer("cleanKamalokaResults", resetDelay, null, null, true);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("cleanKamalokaResults")) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("DELETE FROM kamaloka_results");
            statement.execute();
         } catch (Exception var17) {
            _log.warning("KamaAchievments: Could not empty kamaloka_results table: " + var17);
         }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String SCRIPT_PATH = "data/scripts/custom/KamaAchievements/" + player.getLang() + "/";
      if (npc.getId() == 32484) {
         NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
         if (npc.isInsideRadius(18228, 146030, -3088, 500, true, false)) {
            html.setFile(player, SCRIPT_PATH + "dion-list.htm");
            html.replace("%REPLACE%", this.getRimKamalokaPlayerList(2030, 2535, 3040, 1));
         } else if (npc.isInsideRadius(-13948, 123819, -3112, 500, true, false)) {
            html.setFile(player, SCRIPT_PATH + "gludio-list.htm");
            html.replace("%REPLACE%", this.getRimKamalokaPlayerList(2030, 2535, 1, 1));
         } else if (npc.isInsideRadius(108384, 221614, -3592, 500, true, false)) {
            html.setFile(player, SCRIPT_PATH + "heine-list.htm");
            html.replace("%REPLACE%", this.getRimKamalokaPlayerList(3040, 3545, 4050, 1));
         } else if (npc.isInsideRadius(80960, 56455, -1552, 500, true, false)) {
            html.setFile(player, SCRIPT_PATH + "oren-list.htm");
            html.replace("%REPLACE%", this.getRimKamalokaPlayerList(3545, 4050, 4555, 5060));
         } else if (npc.isInsideRadius(42674, -47909, -797, 500, true, false)) {
            html.setFile(player, SCRIPT_PATH + "rune-list.htm");
            html.replace("%REPLACE%", this.getRimKamalokaPlayerList(5565, 6070, 6575, 7080));
         } else {
            if (!npc.isInsideRadius(85894, -142108, -1336, 500, true, false)) {
               return null;
            }

            html.setFile(player, SCRIPT_PATH + "schuttgart-list.htm");
            html.replace("%REPLACE%", this.getRimKamalokaPlayerList(4555, 5060, 5565, 6070));
         }

         player.sendPacket(html);
      }

      return null;
   }

   private String getRimKamalokaPlayerList(int a, int b, int c, int d) {
      String list = "";

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT char_name FROM kamaloka_results WHERE Level IN (?, ?, ?, ?) ORDER BY Grade DESC, Count DESC"
         );
         statement.setInt(1, a);
         statement.setInt(2, b);
         statement.setInt(3, c);
         statement.setInt(4, d);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            list = list + "---" + rset.getString("char_name") + "---<br>";
         }
      } catch (Exception var20) {
         _log.warning("KamaAchievments: Could not empty kamaloka_results table: " + var20);
      }

      return list;
   }

   public static void main(String[] args) {
      new KamaAchievements(-1, "KamaAchievements", "custom");
   }
}
