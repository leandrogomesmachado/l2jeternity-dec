package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowDominionRegistry;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.ai.AbstractNpcAI;

public class MercenaryCaptain extends AbstractNpcAI {
   private static final Map<Integer, Integer> NPCS = new HashMap<>();
   private static final int STRIDER_WIND = 4422;
   private static final int STRIDER_STAR = 4423;
   private static final int STRIDER_TWILIGHT = 4424;
   private static final int GUARDIAN_STRIDER = 14819;
   private static final int ELITE_MERCENARY_CERTIFICATE = 13767;
   private static final int TOP_ELITE_MERCENARY_CERTIFICATE = 13768;
   private static final int DELAY = 3600000;
   private static final int MIN_LEVEL = 40;
   private static final int CLASS_LEVEL = 2;

   private MercenaryCaptain(String name, String descr) {
      super(name, descr);

      for(int id : NPCS.keySet()) {
         this.addStartNpc(id);
         this.addFirstTalkId(id);
         this.addTalkId(id);
      }

      for(TerritoryWarManager.Territory terr : TerritoryWarManager.getInstance().getAllTerritories()) {
         for(TerritoryWarManager.TerritoryNPCSpawn spawn : terr.getSpawnList()) {
            if (NPCS.keySet().contains(spawn.getId())) {
               this.startQuestTimer("say", 3600000L, spawn.getNpc(), null, true);
            }
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      if (player != null) {
         StringTokenizer st = new StringTokenizer(event, " ");
         String var6 = st.nextToken();
         switch(var6) {
            case "36481-02.htm":
               htmltext = event;
               break;
            case "36481-03.htm":
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               html.setFile(player, player.getLang(), "data/scripts/custom/MercenaryCaptain/" + player.getLang() + "/36481-03.htm");
               html.replace("%strider%", String.valueOf(TerritoryWarManager.MINTWBADGEFORSTRIDERS));
               html.replace("%gstrider%", String.valueOf(TerritoryWarManager.MINTWBADGEFORBIGSTRIDER));
               player.sendPacket(html);
               break;
            case "territory":
               player.sendPacket(new ExShowDominionRegistry(npc.getCastle().getId(), player));
               break;
            case "strider":
               String type = st.nextToken();
               int price = type.equals("3") ? TerritoryWarManager.MINTWBADGEFORBIGSTRIDER : TerritoryWarManager.MINTWBADGEFORSTRIDERS;
               int badgeId = NPCS.get(npc.getId());
               if (getQuestItemsCount(player, badgeId) < (long)price) {
                  return "36481-07.htm";
               }

               int striderId;
               switch(type) {
                  case "0":
                     striderId = 4422;
                     break;
                  case "1":
                     striderId = 4423;
                     break;
                  case "2":
                     striderId = 4424;
                     break;
                  case "3":
                     striderId = 14819;
                     break;
                  default:
                     this._log.warning(MercenaryCaptain.class.getSimpleName() + ": Unknown strider type: " + type);
                     return null;
               }

               takeItems(player, badgeId, (long)price);
               giveItems(player, striderId, 1L);
               htmltext = "36481-09.htm";
               break;
            case "elite":
               if (!hasQuestItems(player, 13767)) {
                  htmltext = "36481-10.htm";
               } else {
                  int listId = 676 + npc.getCastle().getId();
                  MultiSellParser.getInstance().separateAndSend(listId, player, npc, false);
               }
               break;
            case "top-elite":
               if (!hasQuestItems(player, 13768)) {
                  htmltext = "36481-10.htm";
               } else {
                  int listId = 685 + npc.getCastle().getId();
                  MultiSellParser.getInstance().separateAndSend(listId, player, npc, false);
               }
         }
      } else if (event.equalsIgnoreCase("say") && npc != null) {
         if (TerritoryWarManager.getInstance().isTWInProgress()) {
            this.broadcastNpcSay(npc, 23, NpcStringId.CHARGE_CHARGE_CHARGE);
         } else if (getRandom(2) == 0) {
            this.broadcastNpcSay(
               npc,
               23,
               NpcStringId.COURAGE_AMBITION_PASSION_MERCENARIES_WHO_WANT_TO_REALIZE_THEIR_DREAM_OF_FIGHTING_IN_THE_TERRITORY_WAR_COME_TO_ME_FORTUNE_AND_GLORY_ARE_WAITING_FOR_YOU
            );
         } else {
            this.broadcastNpcSay(
               npc,
               23,
               NpcStringId.DO_YOU_WISH_TO_FIGHT_ARE_YOU_AFRAID_NO_MATTER_HOW_HARD_YOU_TRY_YOU_HAVE_NOWHERE_TO_RUN_BUT_IF_YOU_FACE_IT_HEAD_ON_OUR_MERCENARY_TROOP_WILL_HELP_YOU_OUT
            );
         }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext;
      if (player.getLevel() < 40 || player.getClassId().level() < 2) {
         htmltext = "36481-08.htm";
      } else if (npc.isMyLord(player)) {
         htmltext = !npc.getCastle().getSiege().getIsInProgress() && !TerritoryWarManager.getInstance().isTWInProgress() ? "36481-04.htm" : "36481-05.htm";
      } else {
         htmltext = !npc.getCastle().getSiege().getIsInProgress() && !TerritoryWarManager.getInstance().isTWInProgress()
            ? npc.getId() + "-01.htm"
            : "36481-06.htm";
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new MercenaryCaptain(MercenaryCaptain.class.getSimpleName(), "custom");
   }

   static {
      NPCS.put(36481, 13757);
      NPCS.put(36482, 13758);
      NPCS.put(36483, 13759);
      NPCS.put(36484, 13760);
      NPCS.put(36485, 13761);
      NPCS.put(36486, 13762);
      NPCS.put(36487, 13763);
      NPCS.put(36488, 13764);
      NPCS.put(36489, 13765);
   }
}
