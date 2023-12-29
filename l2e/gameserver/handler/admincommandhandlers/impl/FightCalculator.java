package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.Rnd;
import l2e.commons.util.StringUtil;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FightCalculator implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_fight_calculator", "admin_fight_calculator_show", "admin_fcs"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      try {
         if (command.startsWith("admin_fight_calculator_show")) {
            this.handleShow(command.substring("admin_fight_calculator_show".length()), activeChar);
         } else if (command.startsWith("admin_fcs")) {
            this.handleShow(command.substring("admin_fcs".length()), activeChar);
         } else if (command.startsWith("admin_fight_calculator")) {
            this.handleStart(command.substring("admin_fight_calculator".length()), activeChar);
         }
      } catch (StringIndexOutOfBoundsException var4) {
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleStart(String params, Player activeChar) {
      StringTokenizer st = new StringTokenizer(params);
      int lvl1 = 0;
      int lvl2 = 0;
      int mid1 = 0;
      int mid2 = 0;

      while(st.hasMoreTokens()) {
         String s = st.nextToken();
         if (s.equals("lvl1")) {
            lvl1 = Integer.parseInt(st.nextToken());
         } else if (s.equals("lvl2")) {
            lvl2 = Integer.parseInt(st.nextToken());
         } else if (s.equals("mid1")) {
            mid1 = Integer.parseInt(st.nextToken());
         } else if (s.equals("mid2")) {
            mid2 = Integer.parseInt(st.nextToken());
         }
      }

      NpcTemplate npc1 = null;
      if (mid1 != 0) {
         npc1 = NpcsParser.getInstance().getTemplate(mid1);
      }

      NpcTemplate npc2 = null;
      if (mid2 != 0) {
         npc2 = NpcsParser.getInstance().getTemplate(mid2);
      }

      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      String replyMSG;
      if (npc1 != null && npc2 != null) {
         replyMSG = StringUtil.concat(
            "<html><title>Selected mobs to fight</title><body><table><tr><td>First</td><td>Second</td></tr><tr><td>level ",
            String.valueOf(lvl1),
            "</td><td>level ",
            String.valueOf(lvl2),
            "</td></tr><tr><td>id ",
            String.valueOf(npc1.getId()),
            "</td><td>id ",
            String.valueOf(npc2.getId()),
            "</td></tr><tr><td>",
            npc1.getName(),
            "</td><td>",
            npc2.getName(),
            "</td></tr></table><center><br><br><br><button value=\"OK\" action=\"bypass -h admin_fight_calculator_show ",
            String.valueOf(npc1.getId()),
            " ",
            String.valueOf(npc2.getId()),
            "\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
      } else if (lvl1 != 0 && npc1 == null) {
         List<NpcTemplate> npcs = NpcsParser.getInstance().getAllOfLevel(lvl1);
         StringBuilder sb = StringUtil.startAppend(50 + npcs.size() * 200, "<html><title>Select first mob to fight</title><body><table>");

         for(NpcTemplate n : npcs) {
            StringUtil.append(
               sb,
               "<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 ",
               String.valueOf(lvl1),
               " lvl2 ",
               String.valueOf(lvl2),
               " mid1 ",
               String.valueOf(n.getId()),
               " mid2 ",
               String.valueOf(mid2),
               "\">",
               n.getName(),
               "</a></td></tr>"
            );
         }

         sb.append("</table></body></html>");
         replyMSG = sb.toString();
      } else if (lvl2 != 0 && npc2 == null) {
         List<NpcTemplate> npcs = NpcsParser.getInstance().getAllOfLevel(lvl2);
         StringBuilder sb = StringUtil.startAppend(50 + npcs.size() * 200, "<html><title>Select second mob to fight</title><body><table>");

         for(NpcTemplate n : npcs) {
            StringUtil.append(
               sb,
               "<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 ",
               String.valueOf(lvl1),
               " lvl2 ",
               String.valueOf(lvl2),
               " mid1 ",
               String.valueOf(mid1),
               " mid2 ",
               String.valueOf(n.getId()),
               "\">",
               n.getName(),
               "</a></td></tr>"
            );
         }

         sb.append("</table></body></html>");
         replyMSG = sb.toString();
      } else {
         replyMSG = "<html><title>Select mobs to fight</title><body><table><tr><td>First</td><td>Second</td></tr><tr><td><edit var=\"lvl1\" width=80></td><td><edit var=\"lvl2\" width=80></td></tr></table><center><br><br><br><button value=\"OK\" action=\"bypass -h admin_fight_calculator lvl1 $lvl1 lvl2 $lvl2\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>";
      }

      adminReply.setHtml(activeChar, replyMSG);
      activeChar.sendPacket(adminReply);
   }

   private void handleShow(String params, Player activeChar) {
      params = params.trim();
      Creature npc1 = null;
      Creature npc2 = null;
      if (params.length() == 0) {
         npc1 = activeChar;
         npc2 = (Creature)activeChar.getTarget();
         if (npc2 == null) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
         }
      } else {
         int mid1 = 0;
         int mid2 = 0;
         StringTokenizer st = new StringTokenizer(params);
         mid1 = Integer.parseInt(st.nextToken());
         mid2 = Integer.parseInt(st.nextToken());
         npc1 = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(mid1));
         npc2 = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(mid2));
      }

      int miss1 = 0;
      int miss2 = 0;
      int shld1 = 0;
      int shld2 = 0;
      int crit1 = 0;
      int crit2 = 0;
      double patk1 = 0.0;
      double patk2 = 0.0;
      double pdef1 = 0.0;
      double pdef2 = 0.0;
      double dmg1 = 0.0;
      double dmg2 = 0.0;
      int sAtk1 = npc1.calculateTimeBetweenAttacks();
      int sAtk2 = npc2.calculateTimeBetweenAttacks();
      sAtk1 = 100000 / sAtk1;
      sAtk2 = 100000 / sAtk2;

      for(int i = 0; i < 10000; ++i) {
         boolean _miss1 = Formulas.calcHitMiss(npc1, npc2);
         if (_miss1) {
            ++miss1;
         }

         byte _shld1 = Formulas.calcShldUse(npc1, npc2, null, false);
         if (_shld1 > 0) {
            ++shld1;
         }

         boolean _crit1 = Rnd.chance(Formulas.calcCrit(npc1, npc2, null, false));
         if (_crit1) {
            ++crit1;
         }

         double _patk1 = npc1.getPAtk(npc2);
         _patk1 += npc1.getRandomDamageMultiplier();
         patk1 += _patk1;
         double _pdef1 = npc1.getPDef(npc2);
         pdef1 += _pdef1;
         if (!_miss1) {
            double _dmg1 = Formulas.calcPhysDam(npc1, npc2, null, _shld1, _crit1, false);
            dmg1 += _dmg1;
            npc1.abortAttack();
         }
      }

      for(int i = 0; i < 10000; ++i) {
         boolean _miss2 = Formulas.calcHitMiss(npc2, npc1);
         if (_miss2) {
            ++miss2;
         }

         byte _shld2 = Formulas.calcShldUse(npc2, npc1, null, false);
         if (_shld2 > 0) {
            ++shld2;
         }

         boolean _crit2 = Rnd.chance(Formulas.calcCrit(npc2, npc1, null, false));
         if (_crit2) {
            ++crit2;
         }

         double _patk2 = npc2.getPAtk(npc1);
         _patk2 *= npc2.getRandomDamageMultiplier();
         patk2 += _patk2;
         double _pdef2 = npc2.getPDef(npc1);
         pdef2 += _pdef2;
         if (!_miss2) {
            double _dmg2 = Formulas.calcPhysDam(npc2, npc1, null, _shld2, _crit2, false);
            dmg2 += _dmg2;
            npc2.abortAttack();
         }
      }

      miss1 /= 100;
      miss2 /= 100;
      shld1 /= 100;
      shld2 /= 100;
      crit1 /= 100;
      crit2 /= 100;
      patk1 /= 10000.0;
      patk2 /= 10000.0;
      pdef1 /= 10000.0;
      pdef2 /= 10000.0;
      dmg1 /= 10000.0;
      dmg2 /= 10000.0;
      int tdmg1 = (int)((double)sAtk1 * dmg1);
      int tdmg2 = (int)((double)sAtk2 * dmg2);
      double maxHp1 = npc1.getMaxHp();
      int hp1 = (int)(Formulas.calcHpRegen(npc1) * 100000.0 / (double)Formulas.getRegeneratePeriod(npc1));
      double maxHp2 = npc2.getMaxHp();
      int hp2 = (int)(Formulas.calcHpRegen(npc2) * 100000.0 / (double)Formulas.getRegeneratePeriod(npc2));
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      StringBuilder replyMSG = StringUtil.startAppend(1000, "<html><title>Selected mobs to fight</title><body><table>");
      if (params.length() == 0) {
         replyMSG.append("<tr><td width=140>Parameter</td><td width=70>me</td><td width=70>target</td></tr>");
      } else {
         StringUtil.append(
            replyMSG,
            "<tr><td width=140>Parameter</td><td width=70>",
            ((NpcTemplate)npc1.getTemplate()).getName(),
            "</td><td width=70>",
            ((NpcTemplate)npc2.getTemplate()).getName(),
            "</td></tr>"
         );
      }

      StringUtil.append(
         replyMSG,
         "<tr><td>miss</td><td>",
         String.valueOf(miss1),
         "%</td><td>",
         String.valueOf(miss2),
         "%</td></tr><tr><td>shld</td><td>",
         String.valueOf(shld2),
         "%</td><td>",
         String.valueOf(shld1),
         "%</td></tr><tr><td>crit</td><td>",
         String.valueOf(crit1),
         "%</td><td>",
         String.valueOf(crit2),
         "%</td></tr><tr><td>pAtk / pDef</td><td>",
         String.valueOf((int)patk1),
         " / ",
         String.valueOf((int)pdef1),
         "</td><td>",
         String.valueOf((int)patk2),
         " / ",
         String.valueOf((int)pdef2),
         "</td></tr><tr><td>made hits</td><td>",
         String.valueOf(sAtk1),
         "</td><td>",
         String.valueOf(sAtk2),
         "</td></tr><tr><td>dmg per hit</td><td>",
         String.valueOf((int)dmg1),
         "</td><td>",
         String.valueOf((int)dmg2),
         "</td></tr><tr><td>got dmg</td><td>",
         String.valueOf(tdmg2),
         "</td><td>",
         String.valueOf(tdmg1),
         "</td></tr><tr><td>got regen</td><td>",
         String.valueOf(hp1),
         "</td><td>",
         String.valueOf(hp2),
         "</td></tr><tr><td>had HP</td><td>",
         String.valueOf((int)maxHp1),
         "</td><td>",
         String.valueOf((int)maxHp2),
         "</td></tr><tr><td>die</td>"
      );
      if (tdmg2 - hp1 > 1) {
         StringUtil.append(replyMSG, "<td>", String.valueOf((int)(100.0 * maxHp1 / (double)(tdmg2 - hp1))), " sec</td>");
      } else {
         replyMSG.append("<td>never</td>");
      }

      if (tdmg1 - hp2 > 1) {
         StringUtil.append(replyMSG, "<td>", String.valueOf((int)(100.0 * maxHp2 / (double)(tdmg1 - hp2))), " sec</td>");
      } else {
         replyMSG.append("<td>never</td>");
      }

      replyMSG.append("</tr></table><center><br>");
      if (params.length() == 0) {
         replyMSG.append(
            "<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
         );
      } else {
         StringUtil.append(
            replyMSG,
            "<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show ",
            String.valueOf(((NpcTemplate)npc1.getTemplate()).getId()),
            " ",
            String.valueOf(((NpcTemplate)npc2.getTemplate()).getId()),
            "\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
         );
      }

      replyMSG.append("</center></body></html>");
      adminReply.setHtml(activeChar, replyMSG.toString());
      activeChar.sendPacket(adminReply);
      if (params.length() != 0) {
         ((MonsterInstance)npc1).deleteMe();
         ((MonsterInstance)npc2).deleteMe();
      }
   }
}
