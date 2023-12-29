package l2e.gameserver.handler.voicedcommandhandlers.impl;

import gnu.trove.list.array.TIntArrayList;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.communityhandlers.impl.CommunityBuffer;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.service.buffer.SchemeBuff;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SchemeBuffs implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"buff"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String target) {
      if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
         CommunityBuffer.getInstance().sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
         return false;
      } else if (!checkCondition(player)) {
         return false;
      } else if (player.isBlocked()) {
         return false;
      } else if (target != null && !target.isEmpty()) {
         StringTokenizer st = new StringTokenizer(target);
         String name = null;
         if (st.hasMoreTokens()) {
            name = st.nextToken();
         }

         if (name == null) {
            CommunityBuffer.getInstance().sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.INVALID_SCHEME", player.getLang()).toString());
            return false;
         } else if (player.getBuffSchemeByName(name) != null && player.getBuffSchemeByName(name).getBuffs() != null) {
            TIntArrayList buffs = new TIntArrayList();
            TIntArrayList levels = new TIntArrayList();

            for(SchemeBuff buff : player.getBuffSchemeByName(name).getBuffs()) {
               int id = buff.getSkillId();
               int level = player.hasPremiumBonus() ? buff.getPremiumLevel() : buff.getLevel();
               if (!CommunityBuffer.isValidSkill(player, id, level)) {
                  Util.handleIllegalPlayerAction(player, "" + player.getName() + " try to cheat with Community Buffer!");
                  return false;
               }

               String ef = CommunityBuffer.getInstance().getBuffType(player, id);
               switch(ef) {
                  case "buff":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "resist":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "song":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "dance":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "chant":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "others":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "special":
                     buffs.add(id);
                     levels.add(level);
               }
            }

            if (buffs.size() == 0) {
               CommunityBuffer.getInstance().sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NO_BUFFS", player.getLang()).toString());
               return false;
            } else {
               if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
                  int price = buffs.size() * Config.BUFF_AMOUNT;
                  if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return false;
                  }

                  if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)price) {
                     ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                     message.add(price);
                     message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                     CommunityBuffer.getInstance().sendErrorMessageToPlayer(player, message.toString());
                     return false;
                  }

                  player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)price, player, true);
               }

               for(int i = 0; i < buffs.size(); ++i) {
                  if (!CommunityBuffer.isItemRemoveSkill(player, buffs.get(i)) || CommunityBuffer.checkItemsForSkill(player, buffs.get(i), false)) {
                     Skill skill = SkillsParser.getInstance().getInfo(buffs.get(i), levels.get(i));
                     if (skill != null) {
                        int buffTime = CommunityBuffer.getBuffTime(player, skill.getId());
                        if (buffTime > 0 && skill.hasEffects()) {
                           Env env = new Env();
                           env.setCharacter(player);
                           env.setTarget(player);
                           env.setSkill(skill);

                           for(EffectTemplate et : skill.getEffectTemplates()) {
                              Effect ef = et.getEffect(env);
                              if (ef != null) {
                                 ef.setAbnormalTime(buffTime * 60);
                                 ef.scheduleEffect(true);
                              }
                           }
                        } else {
                           skill.getEffects(player, player, false);
                        }
                     }
                  }
               }

               return true;
            }
         } else {
            player.sendMessage(new ServerMessage("CommunityBuffer.NEED_CREATE_SCHEME", player.getLang()).toString());
            return false;
         }
      } else {
         return true;
      }
   }

   private static boolean checkCondition(Player player) {
      if (player == null) {
         return false;
      } else if (player.getUCState() <= 0
         && !player.isBlocked()
         && !player.isCursedWeaponEquipped()
         && !player.isInDuel()
         && !player.isFlying()
         && !player.isJailed()
         && !player.isInOlympiadMode()
         && !player.inObserverMode()
         && !player.isAlikeDead()
         && !player.isDead()) {
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
            player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
            return false;
         } else {
            if (player.isInsideZone(ZoneId.PVP) && !player.isInFightEvent()) {
               if (player.isInsideZone(ZoneId.FUN_PVP)) {
                  FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
                  if (zone != null && zone.canUseCbBuffs()) {
                     return true;
                  }
               } else if (player.isInsideZone(ZoneId.SIEGE) && !Config.ALLOW_COMMUNITY_BUFF_IN_SIEGE) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                  return false;
               }
            }

            if (!player.isInCombat() && !player.isCastingNow() && !player.isAttackingNow()) {
               return true;
            } else {
               player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               return false;
            }
         }
      } else {
         player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
         return false;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
