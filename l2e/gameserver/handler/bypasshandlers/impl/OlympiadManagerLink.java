package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.NpcBufferHolder;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.OlympiadManagerInstance;
import l2e.gameserver.model.actor.templates.player.OlympiadTemplate;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.olympiad.CompetitionType;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExHeroList;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class OlympiadManagerLink implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"olympiaddesc", "olympiadnoble", "olybuff", "olympiad"};
   private static final String FEWER_THAN = "Fewer than " + String.valueOf(Config.ALT_OLY_REG_DISPLAY);
   private static final String MORE_THAN = "More than " + String.valueOf(Config.ALT_OLY_REG_DISPLAY);
   private static final int GATE_PASS = Config.ALT_OLY_COMP_RITEM;

   @Override
   public final boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof OlympiadManagerInstance)) {
         return false;
      } else {
         try {
            if (command.toLowerCase().startsWith("olympiaddesc")) {
               int val = Integer.parseInt(command.substring(13, 14));
               String suffix = command.substring(14);
               ((OlympiadManagerInstance)target).showChatWindow(activeChar, val, suffix);
            } else if (command.toLowerCase().startsWith("olympiadnoble")) {
               NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
               if (activeChar.isCursedWeaponEquipped()) {
                  html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_cursed_weapon.htm");
                  activeChar.sendPacket(html);
                  return false;
               }

               if (activeChar.getClassIndex() != 0) {
                  html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_sub.htm");
                  html.replace("%objectId%", String.valueOf(target.getObjectId()));
                  activeChar.sendPacket(html);
                  return false;
               }

               if (!activeChar.isNoble() || activeChar.getClassId().level() < 3) {
                  html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_thirdclass.htm");
                  html.replace("%objectId%", String.valueOf(target.getObjectId()));
                  activeChar.sendPacket(html);
                  return false;
               }

               int val = Integer.parseInt(command.substring(14));
               switch(val) {
                  case 0:
                     if (!OlympiadManager.getInstance().isRegistered(activeChar)) {
                        html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_desc2a.htm");
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        html.replace("%olympiad_round%", String.valueOf(Olympiad.getInstance().getPeriod()));
                        html.replace("%olympiad_week%", String.valueOf(Olympiad.getInstance().getCurrentCycle()));
                        html.replace("%olympiad_participant%", String.valueOf(OlympiadManager.getInstance().getCountOpponents()));
                        activeChar.sendPacket(html);
                     } else {
                        html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_unregister.htm");
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(html);
                     }
                     break;
                  case 1:
                     OlympiadManager.getInstance().unRegisterNoble(activeChar);
                     break;
                  case 2:
                     int nonClassed = OlympiadManager.getInstance().getRegisteredNonClassBased().size();
                     int teams = OlympiadManager.getInstance().getRegisteredTeamsBased().size();
                     Collection<List<Integer>> allClassed = OlympiadManager.getInstance().getRegisteredClassBased().values();
                     int classed = 0;
                     if (!allClassed.isEmpty()) {
                        for(List<Integer> cls : allClassed) {
                           if (cls != null) {
                              classed += cls.size();
                           }
                        }
                     }

                     html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_registered.htm");
                     if (Config.ALT_OLY_REG_DISPLAY > 0) {
                        html.replace("%listClassed%", classed < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
                        html.replace("%listNonClassedTeam%", teams < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
                        html.replace("%listNonClassed%", nonClassed < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
                     } else {
                        html.replace("%listClassed%", String.valueOf(classed));
                        html.replace("%listNonClassedTeam%", String.valueOf(teams));
                        html.replace("%listNonClassed%", String.valueOf(nonClassed));
                     }

                     html.replace("%objectId%", String.valueOf(target.getObjectId()));
                     activeChar.sendPacket(html);
                     break;
                  case 3:
                     int points = Olympiad.getInstance().getNoblePoints(activeChar.getObjectId());
                     html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_points1.htm");
                     html.replace("%points%", String.valueOf(points));
                     html.replace("%objectId%", String.valueOf(target.getObjectId()));
                     activeChar.sendPacket(html);
                     break;
                  case 4:
                     OlympiadManager.getInstance().registerNoble(activeChar, CompetitionType.NON_CLASSED);
                     break;
                  case 5:
                     OlympiadManager.getInstance().registerNoble(activeChar, CompetitionType.CLASSED);
                     break;
                  case 6:
                     int passes = Olympiad.getInstance().getNoblessePasses(activeChar, false);
                     if (passes > 0) {
                        html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_settle.htm");
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(html);
                     } else {
                        html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_nopoints2.htm");
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(html);
                     }
                     break;
                  case 7:
                     MultiSellParser.getInstance().separateAndSend(102, activeChar, (Npc)target, false);
                     break;
                  case 8:
                     MultiSellParser.getInstance().separateAndSend(103, activeChar, (Npc)target, false);
                     break;
                  case 9:
                     int point = Olympiad.getInstance().getLastNobleOlympiadPoints(activeChar.getObjectId());
                     html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/noble_points2.htm");
                     html.replace("%points%", String.valueOf(point));
                     html.replace("%objectId%", String.valueOf(target.getObjectId()));
                     activeChar.sendPacket(html);
                     break;
                  case 10:
                     int passes = Olympiad.getInstance().getNoblessePasses(activeChar, true);
                     if (passes > 0) {
                        ItemInstance item = activeChar.getInventory().addItem("Olympiad", GATE_PASS, (long)passes, activeChar, target);
                        InventoryUpdate iu = new InventoryUpdate();
                        iu.addModifiedItem(item);
                        activeChar.sendPacket(iu);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                        sm.addItemNumber((long)passes);
                        sm.addItemName(item);
                        activeChar.sendPacket(sm);
                     }
                     break;
                  case 11:
                     OlympiadManager.getInstance().registerNoble(activeChar, CompetitionType.TEAMS);
                     break;
                  default:
                     _log.warning("Olympiad System: Couldnt send packet for request " + val);
               }
            } else if (command.toLowerCase().startsWith("olybuff")) {
               if (activeChar.olyBuff <= 0) {
                  return false;
               }

               NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
               String[] params = command.split(" ");
               if (params[1] == null) {
                  _log.warning("Olympiad Buffer Warning: npcId = " + ((Npc)target).getId() + " has no buffGroup set in the bypass for the buff selected.");
                  return false;
               }

               int buffGroup = Integer.parseInt(params[1]);
               NpcBufferHolder.NpcBufferData npcBuffGroupInfo = NpcBufferHolder.getInstance().getSkillInfo(((Npc)target).getId(), buffGroup);
               if (npcBuffGroupInfo == null) {
                  _log.warning(
                     "Olympiad Buffer Warning: npcId = "
                        + ((Npc)target).getId()
                        + " Location: "
                        + target.getX()
                        + ", "
                        + target.getY()
                        + ", "
                        + target.getZ()
                        + " Player: "
                        + activeChar.getName()
                        + " has tried to use skill group ("
                        + buffGroup
                        + ") not assigned to the NPC Buffer!"
                  );
                  return false;
               }

               Skill skill = npcBuffGroupInfo.getSkill().getSkill();
               target.setTarget(activeChar);
               if (activeChar.olyBuff > 0 && skill != null) {
                  --activeChar.olyBuff;
                  target.broadcastPacket(new MagicSkillUse(target, activeChar, skill.getId(), skill.getLevel(), 0, 0));
                  skill.getEffects(activeChar, activeChar, false);
                  Summon summon = activeChar.getSummon();
                  if (summon != null) {
                     target.broadcastPacket(new MagicSkillUse(target, summon, skill.getId(), skill.getLevel(), 0, 0));
                     skill.getEffects(summon, summon, false);
                  }
               }

               if (activeChar.olyBuff > 0) {
                  html.setFile(
                     activeChar,
                     activeChar.getLang(),
                     activeChar.olyBuff == 5 ? "data/html/olympiad/olympiad_buffs.htm" : "data/html/olympiad/olympiad_5buffs.htm"
                  );
                  html.replace("%objectId%", String.valueOf(target.getObjectId()));
                  activeChar.sendPacket(html);
               } else {
                  html.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/olympiad_nobuffs.htm");
                  html.replace("%objectId%", String.valueOf(target.getObjectId()));
                  activeChar.sendPacket(html);
                  target.decayMe();
               }
            } else if (command.toLowerCase().startsWith("olympiad")) {
               int val = Integer.parseInt(command.substring(9, 10));
               NpcHtmlMessage reply = new NpcHtmlMessage(target.getObjectId());
               switch(val) {
                  case 2:
                     int classId = Integer.parseInt(command.substring(11));
                     if (classId >= 88 && classId <= 118 || classId >= 131 && classId <= 134 || classId == 136) {
                        List<OlympiadTemplate> names = Olympiad.getInstance().getClassLeaderBoard(classId);
                        reply.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/olympiad_ranking.htm");
                        int index = 1;

                        for(OlympiadTemplate tpl : names) {
                           if (index == tpl.getRank()) {
                              reply.replace("%place" + index + "%", String.valueOf(tpl.getRank()));
                              reply.replace("%rank" + index + "%", tpl.getName());
                              reply.replace("%point" + index + "%", String.valueOf(tpl.getPoints()));
                              reply.replace("%won" + index + "%", String.valueOf(tpl.getWin()));
                              reply.replace("%multi" + index + "%", "" + tpl.getWin() + " / " + tpl.getLose() + "");
                              reply.replace("%wr" + index + "%", "" + tpl.getWr() + "%");
                              if (++index > 10) {
                                 break;
                              }
                           }
                        }

                        while(index <= 10) {
                           reply.replace("%place" + index + "%", "");
                           reply.replace("%rank" + index + "%", "");
                           reply.replace("%point" + index + "%", "");
                           reply.replace("%won" + index + "%", "");
                           reply.replace("%multi" + index + "%", "");
                           reply.replace("%wr" + index + "%", "");
                           ++index;
                        }

                        reply.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(reply);
                     }
                     break;
                  case 3:
                  default:
                     _log.warning("Olympiad System: Couldnt send packet for request " + val);
                     break;
                  case 4:
                     activeChar.sendPacket(new ExHeroList());
                     break;
                  case 5:
                     if (Hero.getInstance().isInactiveHero(activeChar.getObjectId())) {
                        Hero.getInstance().activateHero(activeChar);
                        reply.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/monument_give_hero.htm");
                     } else {
                        reply.setFile(activeChar, activeChar.getLang(), "data/html/olympiad/monument_dont_hero.htm");
                     }

                     reply.replace("%objectId%", String.valueOf(target.getObjectId()));
                     activeChar.sendPacket(reply);
               }
            }
         } catch (Exception var16) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var16);
         }

         return true;
      }
   }

   @Override
   public final String[] getBypassList() {
      return COMMANDS;
   }
}
