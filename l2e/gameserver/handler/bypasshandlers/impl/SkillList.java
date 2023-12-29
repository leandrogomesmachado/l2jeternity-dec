package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class SkillList implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"SkillList"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof NpcInstance)) {
         return false;
      } else if (activeChar.getWeightPenalty() < 3 && activeChar.isInventoryUnder90(true)) {
         if (Config.ALT_GAME_SKILL_LEARN) {
            try {
               String id = command.substring(9).trim();
               if (id.length() != 0) {
                  NpcInstance.showSkillList(activeChar, (Npc)target, ClassId.getClassId(Integer.parseInt(id)));
               } else {
                  List<ClassId> classesToTeach = ((NpcInstance)target).getClassesToTeach();

                  for(ClassId cid : classesToTeach) {
                     if (cid.equalsOrChildOf(activeChar.getClassId())) {
                        break;
                     }
                  }

                  NpcHtmlMessage html = new NpcHtmlMessage(((Npc)target).getObjectId());
                  html.setFile(activeChar, activeChar.getLang(), "data/html/trainer/multiclass-skills.htm");
                  String text = "";
                  if (classesToTeach.isEmpty()) {
                     text = text + "No Skills.<br>";
                  } else {
                     List<ClassId> addClasses = new ArrayList<>();

                     for(ClassId cid : classesToTeach) {
                        if (cid.level() == 2) {
                           for(ClassId cd : ClassId.values()) {
                              if (cd != ClassId.inspector && cd.childOf(cid) && cd.level() == cid.level() + 1) {
                                 addClasses.add(cd);
                              }
                           }
                        }
                     }

                     addClasses.addAll(classesToTeach);
                     int count = 0;

                     for(ClassId classCheck = activeChar.getClassId(); count == 0 && classCheck != null; classCheck = classCheck.getParent()) {
                        for(ClassId cid : addClasses) {
                           if (cid.level() <= classCheck.level()
                              && cid.level() >= classCheck.level()
                              && !SkillTreesParser.getInstance().getAvailableSkills(activeChar, cid, false, false).isEmpty()) {
                              text = text
                                 + "<a action=\"bypass -h npc_%objectId%_SkillList "
                                 + cid.getId()
                                 + "\">"
                                 + Util.className(activeChar, cid.getId())
                                 + "</a><br>\n";
                              ++count;
                           }
                        }
                     }

                     ClassId var20 = null;
                     addClasses.clear();
                  }

                  text = text + "</body></html>";
                  html.replace("%classes%", text);
                  html.replace("%objectId%", String.valueOf(((Npc)target).getObjectId()));
                  activeChar.sendPacket(html);
                  activeChar.sendActionFailed();
               }
            } catch (Exception var15) {
               _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var15);
            }
         } else {
            NpcInstance.showSkillList(activeChar, (Npc)target, activeChar.getClassId());
         }

         return true;
      } else {
         activeChar.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
         return false;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
