package l2e.gameserver.handler.actionshifthandlers.impl;

import java.util.Arrays;
import l2e.gameserver.Config;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class NpcActionShift implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (!activeChar.getAccessLevel().isGm() && Arrays.binarySearch(Config.NPC_BLOCK_SHIFT_LIST, ((Npc)target).getId()) > 0) {
         return false;
      } else {
         if (activeChar.getAccessLevel().isGm()) {
            activeChar.setTarget(target);
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(activeChar, activeChar.getLang(), "data/html/admin/npcinfo.htm");
            html.replace("%class%", target.getClass().getSimpleName());
            html.replace("%id%", String.valueOf(((Npc)target).getTemplate().getId()));
            html.replace("%lvl%", String.valueOf(((Npc)target).getTemplate().getLevel()));
            html.replace("%name%", String.valueOf(((Npc)target).getTemplate().getName()));
            html.replace("%tmplid%", String.valueOf(((Npc)target).getTemplate().getId()));
            html.replace("%aggro%", String.valueOf(target instanceof Attackable ? ((Attackable)target).getAggroRange() : 0));
            html.replace("%hp%", String.valueOf((int)((Creature)target).getCurrentHp()));
            html.replace("%hpmax%", String.valueOf((int)((Creature)target).getMaxHp()));
            html.replace("%mp%", String.valueOf((int)((Creature)target).getCurrentMp()));
            html.replace("%mpmax%", String.valueOf((int)((Creature)target).getMaxMp()));
            html.replace("%refId%", String.valueOf(target.getReflectionId()));
            html.replace("%loc%", String.valueOf(target.getX() + " " + target.getY() + " " + target.getZ()));
            html.replace("%heading%", String.valueOf(((Creature)target).getHeading()));
            html.replace("%collision_radius%", String.valueOf(((Creature)target).getTemplate().getfCollisionRadius()));
            html.replace("%collision_height%", String.valueOf(((Creature)target).getTemplate().getfCollisionHeight()));
            html.replace("%dist%", String.valueOf((int)Math.sqrt(activeChar.getDistanceSq(target))));
            html.replace("%region%", target.getWorldRegion() != null ? target.getWorldRegion().getName() : "<font color=FF0000>null</font>");
            if (target.getSpawnedLoc() != null) {
               html.replace("%spawn%", target.getSpawnedLoc().getX() + " " + target.getSpawnedLoc().getY() + " " + target.getSpawnedLoc().getZ());
               html.replace(
                  "%loc2d%",
                  String.valueOf((int)Math.sqrt(((Creature)target).getPlanDistanceSq(target.getSpawnedLoc().getX(), target.getSpawnedLoc().getY())))
               );
               html.replace(
                  "%loc3d%",
                  String.valueOf(
                     (int)Math.sqrt(
                        ((Creature)target).getDistanceSq(target.getSpawnedLoc().getX(), target.getSpawnedLoc().getY(), target.getSpawnedLoc().getZ())
                     )
                  )
               );
            } else {
               html.replace("%spawn%", "<font color=FF0000>null</font>");
               html.replace("%loc2d%", "<font color=FF0000>--</font>");
               html.replace("%loc3d%", "<font color=FF0000>--</font>");
            }

            if (((Npc)target).getSpawn() != null) {
               if (((Npc)target).getSpawn().getRespawnMinDelay() == 0) {
                  html.replace("%resp%", "None");
               } else if (((Npc)target).getSpawn().hasRespawnRandom()) {
                  html.replace(
                     "%resp%", ((Npc)target).getSpawn().getRespawnMinDelay() / 1000 + "-" + ((Npc)target).getSpawn().getRespawnMaxDelay() / 1000 + " sec"
                  );
               } else {
                  html.replace("%resp%", ((Npc)target).getSpawn().getRespawnMinDelay() / 1000 + " sec");
               }

               int spawnIndex = ((Npc)target).getSpawn().getSpawnTemplate() != null
                  ? (((Npc)target).getSpawn().getSpawnTemplate().getSpawnRangeSize() > 1 ? ((Npc)target).getSpawn().getSpawnIndex() : 0)
                  : 0;
               html.replace(
                  "%territory%",
                  ((Npc)target).getSpawn().getTerritoryName() != null && !((Npc)target).getSpawn().getTerritoryName().isEmpty()
                     ? "" + ((Npc)target).getSpawn().getTerritoryName() + "(" + spawnIndex + ")"
                     : "<font color=FF0000>--</font>"
               );
            } else {
               html.replace("%resp%", "<font color=FF0000>--</font>");
               html.replace("%territory%", "<font color=FF0000>--</font>");
            }

            if (((Npc)target).hasAI()) {
               html.replace(
                  "%ai_intention%",
                  "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Intention:</font></td><td align=right width=170>"
                     + ((Npc)target).getAI().getIntention().name()
                     + "</td></tr></table></td></tr>"
               );
               html.replace(
                  "%ai_type%",
                  "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>AIType</font></td><td align=right width=170>"
                     + ((Npc)target).getAiType()
                     + "</td></tr></table></td></tr>"
               );
               html.replace(
                  "%ai_clan%",
                  "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Clan & Range:</font></td><td align=right width=170>"
                     + ((Npc)target).getFaction().getName()
                     + " "
                     + ((Npc)target).getFaction().getRange()
                     + "</td></tr></table></td></tr>"
               );
            } else {
               html.replace("%ai_intention%", "");
               html.replace("%ai_type%", "");
               html.replace("%ai_clan%", "");
            }

            String routeName = WalkingManager.getInstance().getRouteName((Npc)target);
            if (!routeName.isEmpty()) {
               html.replace(
                  "%route%",
                  "<tr><td><table width=270 border=0><tr><td width=100><font color=LEVEL>Route:</font></td><td align=right width=170>"
                     + routeName
                     + "</td></tr></table></td></tr>"
               );
            } else {
               html.replace("%route%", "");
            }

            activeChar.sendPacket(html);
         } else if (Config.ALT_GAME_VIEWNPC) {
            activeChar.setTarget(target);
            if (target.isAutoAttackable(activeChar)) {
               StatusUpdate su = new StatusUpdate(target);
               su.addAttribute(9, (int)((Creature)target).getCurrentHp());
               su.addAttribute(10, ((Creature)target).getMaxHp());
               activeChar.sendPacket(su);
            }

            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(activeChar, activeChar.getLang(), "data/html/mobinfo.htm");
            html.replace("%objid%", String.valueOf(target.getObjectId()));
            html.replace("%class%", target.getClass().getSimpleName());
            html.replace("%id%", String.valueOf(((Npc)target).getTemplate().getId()));
            html.replace("%lvl%", String.valueOf(((Npc)target).getTemplate().getLevel()));
            html.replace("%name%", String.valueOf(((Npc)target).getTemplate().getName()));
            html.replace("%tmplid%", String.valueOf(((Npc)target).getTemplate().getIdTemplate()));
            html.replace("%aggro%", String.valueOf(target instanceof Attackable ? ((Attackable)target).getAggroRange() : 0));
            html.replace("%hp%", String.valueOf((int)((Creature)target).getCurrentHp()));
            html.replace("%hpmax%", String.valueOf((int)((Creature)target).getMaxHp()));
            html.replace("%mp%", String.valueOf((int)((Creature)target).getCurrentMp()));
            html.replace("%mpmax%", String.valueOf((int)((Creature)target).getMaxMp()));
            html.replace("%patk%", String.valueOf((int)((Creature)target).getPAtk(null)));
            html.replace("%matk%", String.valueOf((int)((Creature)target).getMAtk(null, null)));
            html.replace("%pdef%", String.valueOf((int)((Creature)target).getPDef(null)));
            html.replace("%mdef%", String.valueOf((int)((Creature)target).getMDef(null, null)));
            html.replace("%accu%", String.valueOf(((Creature)target).getAccuracy()));
            html.replace("%evas%", String.valueOf(((Creature)target).getEvasionRate(null)));
            html.replace("%crit%", String.valueOf((int)((Creature)target).getCriticalHit(null, null)));
            html.replace("%rspd%", String.valueOf((int)((Creature)target).getRunSpeed()));
            html.replace("%aspd%", String.valueOf((int)((Creature)target).getPAtkSpd()));
            html.replace("%cspd%", String.valueOf((int)((Creature)target).getMAtkSpd()));
            html.replace("%str%", String.valueOf(((Creature)target).getSTR()));
            html.replace("%dex%", String.valueOf(((Creature)target).getDEX()));
            html.replace("%con%", String.valueOf(((Creature)target).getCON()));
            html.replace("%int%", String.valueOf(((Creature)target).getINT()));
            html.replace("%wit%", String.valueOf(((Creature)target).getWIT()));
            html.replace("%men%", String.valueOf(((Creature)target).getMEN()));
            html.replace("%loc%", String.valueOf(target.getX() + " " + target.getY() + " " + target.getZ()));
            html.replace("%dist%", String.valueOf((int)Math.sqrt(activeChar.getDistanceSq(target))));
            byte attackAttribute = ((Creature)target).getAttackElement();
            html.replace("%ele_atk%", Elementals.getElementName(attackAttribute));
            html.replace("%ele_atk_value%", String.valueOf(((Creature)target).getAttackElementValue(attackAttribute)));
            html.replace("%ele_dfire%", String.valueOf(((Creature)target).getDefenseElementValue((byte)0)));
            html.replace("%ele_dwater%", String.valueOf(((Creature)target).getDefenseElementValue((byte)1)));
            html.replace("%ele_dwind%", String.valueOf(((Creature)target).getDefenseElementValue((byte)2)));
            html.replace("%ele_dearth%", String.valueOf(((Creature)target).getDefenseElementValue((byte)3)));
            html.replace("%ele_dholy%", String.valueOf(((Creature)target).getDefenseElementValue((byte)4)));
            html.replace("%ele_ddark%", String.valueOf(((Creature)target).getDefenseElementValue((byte)5)));
            if (((Npc)target).getSpawn() != null) {
               html.replace("%spawn%", ((Npc)target).getSpawn().getX() + " " + ((Npc)target).getSpawn().getY() + " " + ((Npc)target).getSpawn().getZ());
               html.replace(
                  "%loc2d%",
                  String.valueOf((int)Math.sqrt(((Creature)target).getPlanDistanceSq(((Npc)target).getSpawn().getX(), ((Npc)target).getSpawn().getY())))
               );
               html.replace(
                  "%loc3d%",
                  String.valueOf(
                     (int)Math.sqrt(
                        ((Creature)target).getDistanceSq(((Npc)target).getSpawn().getX(), ((Npc)target).getSpawn().getY(), ((Npc)target).getSpawn().getZ())
                     )
                  )
               );
               html.replace("%resp%", String.valueOf(((Npc)target).getSpawn().getRespawnDelay() / 1000));
            } else {
               html.replace("%spawn%", "<font color=FF0000>null</font>");
               html.replace("%loc2d%", "<font color=FF0000>--</font>");
               html.replace("%loc3d%", "<font color=FF0000>--</font>");
               html.replace("%resp%", "<font color=FF0000>--</font>");
            }

            if (((Npc)target).hasAI()) {
               html.replace(
                  "%ai_intention%",
                  "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Intention:</font></td><td align=right width=170>"
                     + ((Npc)target).getAI().getIntention().name()
                     + "</td></tr></table></td></tr>"
               );
               html.replace(
                  "%ai%",
                  "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>AI</font></td><td align=right width=170>"
                     + ((Npc)target).getAI().getClass().getSimpleName()
                     + "</td></tr></table></td></tr>"
               );
               html.replace(
                  "%ai_type%",
                  "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>AIType</font></td><td align=right width=170>"
                     + ((Npc)target).getAiType()
                     + "</td></tr></table></td></tr>"
               );
               html.replace(
                  "%ai_clan%",
                  "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>Clan & Range:</font></td><td align=right width=170>"
                     + ((Npc)target).getFaction().getName()
                     + " "
                     + ((Npc)target).getFaction().getRange()
                     + "</td></tr></table></td></tr>"
               );
            } else {
               html.replace("%ai_intention%", "");
               html.replace("%ai%", "");
               html.replace("%ai_type%", "");
               html.replace("%ai_clan%", "");
               html.replace("%ai_enemy_clan%", "");
            }

            activeChar.sendPacket(html);
         }

         return true;
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.Npc;
   }
}
