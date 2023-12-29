package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class SevenRaidBosses implements IVoicedCommandHandler {
   private static final String[] commands = new String[]{"7rb"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!Config.ALLOW_SEVENBOSSES_COMMAND) {
         return false;
      } else {
         if (command.equalsIgnoreCase("7rb")) {
            QuestState st = activeChar.getQuestState("_254_LegendaryTales");
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setHtml(activeChar, buildHtml(st));
            activeChar.sendPacket(html);
         }

         return true;
      }
   }

   private static final String buildHtml(QuestState st) {
      StringBuilder sb = new StringBuilder();
      sb.append("<html><head>");
      sb.append("<title>7 RaidBosses Status</title>");
      sb.append("</head>");
      sb.append("<body><br>");
      sb.append("<br>Your Quest (Legendary Tales) status:<br>");
      if (st == null) {
         sb.append("Quest is not started yet. Please visit Glimore in dragon valley in order to start it.");
         sb.append("<br>");
      } else if (st.isCond(1)) {
         for(SevenRaidBosses.Bosses boss : SevenRaidBosses.Bosses.class.getEnumConstants()) {
            sb.append(boss.getName() + ": ");
            sb.append(checkMask(st, boss) ? "<font color=\"00FF00\">Killed.</font>" : "<font color=\"FF0000\">Not killed.</font>");
            sb.append("<br>");
         }
      } else {
         sb.append("Legendary Tales quest is completed.");
         sb.append("<br>");
      }

      sb.append("</body></html>");
      return sb.toString();
   }

   private static boolean checkMask(QuestState qs, SevenRaidBosses.Bosses boss) {
      int pos = boss.getMask();
      return (qs.getInt("raids") & pos) == pos;
   }

   @Override
   public String[] getVoicedCommandList() {
      return commands;
   }

   public static enum Bosses {
      EMERALD_HORN("Emerald Horn"),
      DUST_RIDER("Dust Rider"),
      BLEEDING_FLY("Bleeding Fly"),
      BLACK_DAGGER("Blackdagger Wing"),
      SHADOW_SUMMONER("Shadow Summoner"),
      SPIKE_SLASHER("Spike Slasher"),
      MUSCLE_BOMBER("Muscle Bomber");

      private final String name;
      private final int _mask;

      private Bosses(String name) {
         this.name = name;
         this._mask = 1 << this.ordinal();
      }

      public int getMask() {
         return this._mask;
      }

      public String getName() {
         return this.name;
      }
   }
}
