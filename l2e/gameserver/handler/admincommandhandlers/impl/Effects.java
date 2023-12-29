package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ChestInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExRedSky;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SSQInfo;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SunRise;
import l2e.gameserver.network.serverpackets.SunSet;

public class Effects implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_invis",
      "admin_invisible",
      "admin_setinvis",
      "admin_vis",
      "admin_visible",
      "admin_invis_menu",
      "admin_earthquake",
      "admin_earthquake_menu",
      "admin_bighead",
      "admin_shrinkhead",
      "admin_gmspeed",
      "admin_gmspeed_menu",
      "admin_unpara_all",
      "admin_para_all",
      "admin_unpara",
      "admin_para",
      "admin_unpara_all_menu",
      "admin_para_all_menu",
      "admin_unpara_menu",
      "admin_para_menu",
      "admin_polyself",
      "admin_unpolyself",
      "admin_polyself_menu",
      "admin_unpolyself_menu",
      "admin_clearteams",
      "admin_setteam_close",
      "admin_setteam",
      "admin_social",
      "admin_effect",
      "admin_social_menu",
      "admin_special",
      "admin_special_menu",
      "admin_effect_menu",
      "admin_abnormal",
      "admin_abnormal_menu",
      "admin_play_sounds",
      "admin_play_sound",
      "admin_atmosphere",
      "admin_atmosphere_menu",
      "admin_set_displayeffect",
      "admin_set_displayeffect_menu"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command);
      st.nextToken();
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_invis_menu")) {
         if (!activeChar.isInvisible()) {
            activeChar.setInvisible(true);
            activeChar.broadcastUserInfo(true);
         } else {
            activeChar.setInvisible(false);
            activeChar.broadcastUserInfo(true);
         }

         command = "";
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_invis")) {
         if (!activeChar.isInvisible()) {
            activeChar.setInvisible(true);
            activeChar.broadcastUserInfo(true);
         } else {
            activeChar.setInvisible(false);
            activeChar.broadcastUserInfo(true);
         }
      } else if (command.startsWith("admin_setinvis")) {
         if (activeChar.getTarget() == null || !activeChar.getTarget().isCreature()) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
         }

         Creature target = (Creature)activeChar.getTarget();
         target.setInvisible(!target.isInvisible());
         activeChar.sendMessage("You've made " + target.getName() + " " + (target.isInvisible() ? "invisible" : "visible") + ".");
      } else if (command.startsWith("admin_vis")) {
         activeChar.setInvisible(false);
         activeChar.broadcastUserInfo(true);
      } else if (command.startsWith("admin_earthquake")) {
         try {
            String val1 = st.nextToken();
            int intensity = Integer.parseInt(val1);
            String val2 = st.nextToken();
            int duration = Integer.parseInt(val2);
            EarthQuake eq = new EarthQuake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
            activeChar.broadcastPacket(eq);
         } catch (Exception var27) {
            activeChar.sendMessage("Usage: //earthquake <intensity> <duration>");
         }
      } else if (command.startsWith("admin_atmosphere")) {
         try {
            String type = st.nextToken();
            String state = st.nextToken();
            int duration = Integer.parseInt(st.nextToken());
            this.adminAtmosphere(type, state, duration, activeChar);
         } catch (Exception var26) {
            activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red> <duration>");
         }
      } else if (command.equals("admin_play_sounds")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/songs/songs.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_play_sounds")) {
         try {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/songs/songs" + command.substring(18) + ".htm");
            activeChar.sendPacket(adminhtm);
         } catch (StringIndexOutOfBoundsException var25) {
            activeChar.sendMessage("Usage: //play_sounds <pagenumber>");
         }
      } else if (command.startsWith("admin_play_sound")) {
         try {
            this.playAdminSound(activeChar, command.substring(17));
         } catch (StringIndexOutOfBoundsException var24) {
            activeChar.sendMessage("Usage: //play_sound <soundname>");
         }
      } else if (command.equals("admin_para_all")) {
         try {
            for(Player player : World.getInstance().getAroundPlayers(activeChar)) {
               if (!player.isGM()) {
                  player.startAbnormalEffect(AbnormalEffect.HOLD_1);
                  player.setIsParalyzed(true);
                  player.startParalyze();
               }
            }
         } catch (Exception var37) {
         }
      } else if (command.equals("admin_unpara_all")) {
         try {
            for(Player player : World.getInstance().getAroundPlayers(activeChar)) {
               player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
               player.setIsParalyzed(false);
            }
         } catch (Exception var36) {
         }
      } else if (command.startsWith("admin_para")) {
         String type = "1";

         try {
            type = st.nextToken();
         } catch (Exception var23) {
         }

         try {
            GameObject target = activeChar.getTarget();
            Creature player = null;
            if (target instanceof Creature) {
               player = (Creature)target;
               if (type.equals("1")) {
                  player.startAbnormalEffect(AbnormalEffect.HOLD_1);
               } else {
                  player.startAbnormalEffect(AbnormalEffect.HOLD_2);
               }

               player.setIsParalyzed(true);
               player.startParalyze();
            }
         } catch (Exception var22) {
         }
      } else if (command.startsWith("admin_unpara")) {
         String type = "1";

         try {
            type = st.nextToken();
         } catch (Exception var21) {
         }

         try {
            GameObject target = activeChar.getTarget();
            Creature player = null;
            if (target instanceof Creature) {
               player = (Creature)target;
               if (type.equals("1")) {
                  player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
               } else {
                  player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
               }

               player.setIsParalyzed(false);
            }
         } catch (Exception var20) {
         }
      } else if (command.startsWith("admin_bighead")) {
         try {
            GameObject target = activeChar.getTarget();
            Creature player = null;
            if (target instanceof Creature) {
               player = (Creature)target;
               player.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
            }
         } catch (Exception var19) {
         }
      } else if (command.startsWith("admin_shrinkhead")) {
         try {
            GameObject target = activeChar.getTarget();
            Creature player = null;
            if (target instanceof Creature) {
               player = (Creature)target;
               player.stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
            }
         } catch (Exception var18) {
         }
      } else if (command.startsWith("admin_gmspeed")) {
         try {
            int val = Integer.parseInt(st.nextToken());
            activeChar.stopSkillEffects(7029);
            if (val >= 1 && val <= 4) {
               Skill gmSpeedSkill = SkillsParser.getInstance().getInfo(7029, val);
               activeChar.doSimultaneousCast(gmSpeedSkill);
            }
         } catch (Exception var17) {
            activeChar.sendMessage("Usage: //gmspeed <value> (0=off...4=max)");
         }

         if (command.contains("_menu")) {
            command = "";
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
            activeChar.sendPacket(adminhtm);
         }
      } else if (command.startsWith("admin_polyself")) {
         try {
            String id = st.nextToken();
            activeChar.getPoly().setPolyInfo("npc", id);
            activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
            activeChar.broadcastCharInfo();
         } catch (Exception var16) {
            activeChar.sendMessage("Usage: //polyself <npcId>");
         }
      } else if (command.startsWith("admin_unpolyself")) {
         activeChar.getPoly().setPolyInfo(null, "1");
         activeChar.decayMe();
         activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
         activeChar.broadcastCharInfo();
      } else if (command.equals("admin_clearteams")) {
         try {
            for(Player player : World.getInstance().getAroundPlayers(activeChar)) {
               player.setTeam(0);
               player.broadcastUserInfo(true);
            }
         } catch (Exception var35) {
         }
      } else if (command.startsWith("admin_setteam_close")) {
         try {
            String val = st.nextToken();
            int radius = 400;
            if (st.hasMoreTokens()) {
               radius = Integer.parseInt(st.nextToken());
            }

            int teamVal = Integer.parseInt(val);

            for(Creature player : World.getInstance().getAroundCharacters(activeChar, radius, 200)) {
               player.setTeam(teamVal);
            }
         } catch (Exception var34) {
            activeChar.sendMessage("Usage: //setteam_close <teamId>");
         }
      } else if (command.startsWith("admin_setteam")) {
         try {
            String val = st.nextToken();
            int teamVal = Integer.parseInt(val);
            Creature target = null;
            if (!(activeChar.getTarget() instanceof Creature)) {
               return false;
            }

            target = (Creature)activeChar.getTarget();
            target.setTeam(teamVal);
         } catch (Exception var15) {
            activeChar.sendMessage("Usage: //setteam <teamId>");
         }
      } else if (command.startsWith("admin_social")) {
         try {
            String target = null;
            GameObject obj = activeChar.getTarget();
            if (st.countTokens() == 2) {
               int social = Integer.parseInt(st.nextToken());
               target = st.nextToken();
               if (target != null) {
                  Player player = World.getInstance().getPlayer(target);
                  if (player != null) {
                     if (this.performSocial(social, player, activeChar)) {
                        activeChar.sendMessage(player.getName() + " was affected by your request.");
                     }
                  } else {
                     try {
                        int radius = Integer.parseInt(target);

                        for(GameObject object : World.getInstance().getAroundObjects(activeChar)) {
                           if (activeChar.isInsideRadius(object, radius, false, false)) {
                              this.performSocial(social, object, activeChar);
                           }
                        }

                        activeChar.sendMessage(radius + " units radius affected by your request.");
                     } catch (NumberFormatException var32) {
                        activeChar.sendMessage("Incorrect parameter");
                     }
                  }
               }
            } else if (st.countTokens() == 1) {
               int social = Integer.parseInt(st.nextToken());
               if (obj == null) {
                  obj = activeChar;
               }

               if (this.performSocial(social, obj, activeChar)) {
                  activeChar.sendMessage(obj.getName() + " was affected by your request.");
               } else {
                  activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
               }
            } else if (!command.contains("menu")) {
               activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
            }
         } catch (Exception var33) {
            if (Config.DEBUG) {
               var33.printStackTrace();
            }
         }
      } else if (command.startsWith("admin_abnormal")) {
         try {
            String target = null;
            GameObject obj = activeChar.getTarget();
            if (st.countTokens() == 2) {
               String parm = st.nextToken();
               int abnormal = Integer.decode("0x" + parm);
               target = st.nextToken();
               if (target != null) {
                  Player player = World.getInstance().getPlayer(target);
                  if (player != null) {
                     if (this.performAbnormal(abnormal, player)) {
                        activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
                     } else {
                        activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
                     }
                  } else {
                     try {
                        int radius = Integer.parseInt(target);

                        for(GameObject object : World.getInstance().getAroundObjects(activeChar)) {
                           if (activeChar.isInsideRadius(object, radius, false, false)) {
                              this.performAbnormal(abnormal, object);
                           }
                        }

                        activeChar.sendMessage(radius + " units radius affected by your request.");
                     } catch (NumberFormatException var30) {
                        activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
                     }
                  }
               }
            } else if (st.countTokens() == 1) {
               int abnormal = Integer.decode("0x" + st.nextToken());
               if (obj == null) {
                  obj = activeChar;
               }

               if (this.performAbnormal(abnormal, obj)) {
                  activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
               } else {
                  activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
               }
            } else if (!command.contains("menu")) {
               activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
            }
         } catch (Exception var31) {
            if (Config.DEBUG) {
               var31.printStackTrace();
            }
         }
      } else if (command.startsWith("admin_special")) {
         try {
            String target = null;
            GameObject obj = activeChar.getTarget();
            if (st.countTokens() == 2) {
               String parm = st.nextToken();
               int special = Integer.decode("0x" + parm);
               target = st.nextToken();
               if (target != null) {
                  Player player = World.getInstance().getPlayer(target);
                  if (player != null) {
                     if (this.performSpecial(special, player)) {
                        activeChar.sendMessage(player.getName() + "'s special status was affected by your request.");
                     } else {
                        activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
                     }
                  } else {
                     try {
                        int radius = Integer.parseInt(target);

                        for(GameObject object : World.getInstance().getAroundObjects(activeChar)) {
                           if (activeChar.isInsideRadius(object, radius, false, false)) {
                              this.performSpecial(special, object);
                           }
                        }

                        activeChar.sendMessage(radius + " units radius affected by your request.");
                     } catch (NumberFormatException var28) {
                        activeChar.sendMessage("Usage: //special <hex_special_mask> [player|radius]");
                     }
                  }
               }
            } else if (st.countTokens() == 1) {
               int special = Integer.decode("0x" + st.nextToken());
               if (obj == null) {
                  obj = activeChar;
               }

               if (this.performSpecial(special, obj)) {
                  activeChar.sendMessage(obj.getName() + "'s special status was affected by your request.");
               } else {
                  activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
               }
            } else if (!command.contains("menu")) {
               activeChar.sendMessage("Usage: //special <special_mask> [player_name|radius]");
            }
         } catch (Exception var29) {
            if (Config.DEBUG) {
               var29.printStackTrace();
            }
         }
      } else if (command.startsWith("admin_effect")) {
         try {
            GameObject obj = activeChar.getTarget();
            int level = 1;
            int hittime = 1;
            int skill = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
               level = Integer.parseInt(st.nextToken());
            }

            if (st.hasMoreTokens()) {
               hittime = Integer.parseInt(st.nextToken());
            }

            if (obj == null) {
               obj = activeChar;
            }

            if (!(obj instanceof Creature)) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            } else {
               Creature target = (Creature)obj;
               target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
               activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
            }
         } catch (Exception var14) {
            activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
         }
      } else if (command.startsWith("admin_set_displayeffect")) {
         GameObject target = activeChar.getTarget();
         if (!(target instanceof Npc)) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
         }

         Npc npc = (Npc)target;

         try {
            String type = st.nextToken();
            int diplayeffect = Integer.parseInt(type);
            npc.setDisplayEffect(diplayeffect);
         } catch (Exception var13) {
            activeChar.sendMessage("Usage: //set_displayeffect <id>");
         }
      }

      if (command.contains("menu")) {
         this.showMainPage(activeChar, command);
      }

      return true;
   }

   private boolean performAbnormal(int action, GameObject target) {
      if (target instanceof Creature) {
         Creature character = (Creature)target;
         AbnormalEffect eff = AbnormalEffect.getById(action);
         if (eff != null) {
            for(AbnormalEffect ef : character.getAbnormalEffects()) {
               if (ef != null && ef.getId() == eff.getId()) {
                  character.stopAbnormalEffect(ef);
                  return true;
               }
            }

            character.startAbnormalEffect(eff);
            return true;
         }
      }

      return false;
   }

   private boolean performSpecial(int action, GameObject target) {
      if (target instanceof Player) {
         Creature character = (Creature)target;
         AbnormalEffect eff = AbnormalEffect.getById(action);
         if (eff != null) {
            for(AbnormalEffect ef : character.getAbnormalEffects()) {
               if (ef != null && ef.getId() == eff.getId()) {
                  character.stopAbnormalEffect(ef);
                  return true;
               }
            }

            character.startAbnormalEffect(eff);
            return true;
         }
      }

      return false;
   }

   private boolean performSocial(int action, GameObject target, Player activeChar) {
      try {
         if (!(target instanceof Creature)) {
            return false;
         }

         if (target instanceof ChestInstance) {
            activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
            return false;
         }

         if (target instanceof Npc && (action < 1 || action > 3)) {
            activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
            return false;
         }

         if (target instanceof Player && (action < 2 || action > 18 && action != 2122)) {
            activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
            return false;
         }

         Creature character = (Creature)target;
         character.broadcastPacket(new SocialAction(character.getObjectId(), action));
      } catch (Exception var5) {
      }

      return true;
   }

   private void adminAtmosphere(String type, String state, int duration, Player activeChar) {
      GameServerPacket packet = null;
      if (type.equals("signsky")) {
         if (state.equals("dawn")) {
            packet = new SSQInfo(2);
         } else if (state.equals("dusk")) {
            packet = new SSQInfo(1);
         }
      } else if (type.equals("sky")) {
         if (state.equals("night")) {
            packet = SunSet.STATIC_PACKET;
         } else if (state.equals("day")) {
            packet = SunRise.STATIC_PACKET;
         } else if (state.equals("red")) {
            if (duration != 0) {
               packet = new ExRedSky(duration);
            } else {
               packet = new ExRedSky(10);
            }
         }
      } else {
         activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red> <duration>");
      }

      if (packet != null) {
         Broadcast.toAllOnlinePlayers(packet);
      }
   }

   private void playAdminSound(Player activeChar, String sound) {
      PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
      activeChar.sendPacket(_snd);
      activeChar.broadcastPacket(_snd);
      activeChar.sendMessage("Playing " + sound + ".");
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void showMainPage(Player activeChar, String command) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/effects_menu.htm");
      activeChar.sendPacket(adminhtm);
      if (command.contains("abnormal")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/abnormal.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.contains("special")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/special.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.contains("social")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/social.htm");
         activeChar.sendPacket(adminhtm);
      }
   }
}
