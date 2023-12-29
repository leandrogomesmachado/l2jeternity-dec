package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SoulCrystalParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.SoulCrystalTemplate;
import l2e.gameserver.model.actor.templates.npc.AbsorbInfo;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class _350_EnhanceYourWeapon extends Quest {
   private static final int[] STARTING_NPCS = new int[]{30115, 30856, 30194};
   private static final int RED_SOUL_CRYSTAL0_ID = 4629;
   private static final int GREEN_SOUL_CRYSTAL0_ID = 4640;
   private static final int BLUE_SOUL_CRYSTAL0_ID = 4651;

   private boolean check(QuestState st) {
      for(int i = 4629; i < 4665; ++i) {
         if (st.hasQuestItems(i)) {
            return true;
         }
      }

      return false;
   }

   public _350_EnhanceYourWeapon(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int npcId : STARTING_NPCS) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
         if (template != null && !template.getAbsorbInfo().isEmpty()) {
            this.addSkillSeeId(new int[]{template.getId()});
            this.addKillId(template.getId());
         }
      }
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      super.onSkillSee(npc, caster, skill, targets, isSummon);
      if (skill == null || skill.getId() != 2096) {
         return null;
      } else if (caster == null || caster.isDead()) {
         return null;
      } else if (npc instanceof Attackable && !npc.isDead() && !npc.getTemplate().getAbsorbInfo().isEmpty()) {
         try {
            ((Attackable)npc).addAbsorber(caster);
         } catch (Exception var7) {
            _log.log(Level.SEVERE, "", (Throwable)var7);
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         Party party = player.getParty();
         List<_350_EnhanceYourWeapon.PlayerResult> list;
         if (party == null) {
            list = new ArrayList<>(1);
            list.add(new _350_EnhanceYourWeapon.PlayerResult(player));
         } else {
            list = new ArrayList<>(party.getMembers().size());

            for(Player m : party.getMembers()) {
               if (m.isInRange(npc.getLocation(), (long)Config.ALT_PARTY_RANGE2)) {
                  list.add(new _350_EnhanceYourWeapon.PlayerResult(m));
               }
            }
         }

         if (list != null && !list.isEmpty()) {
            for(AbsorbInfo info : npc.getTemplate().getAbsorbInfo()) {
               this.calcAbsorb(list, (MonsterInstance)npc, info);
            }

            for(_350_EnhanceYourWeapon.PlayerResult r : list) {
               r.send();
            }
         }

         return null;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (event.endsWith("-04.htm")) {
         st.startQuest();
      } else if (event.endsWith("-09.htm")) {
         st.giveItems(4629, 1L);
      } else if (event.endsWith("-10.htm")) {
         st.giveItems(4640, 1L);
      } else if (event.endsWith("-11.htm")) {
         st.giveItems(4651, 1L);
      } else if (event.equalsIgnoreCase("exit.htm")) {
         st.exitQuest(true);
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (st.getState() == 0) {
            st.set("cond", "0");
         }

         if (st.getInt("cond") == 0) {
            htmltext = npc.getId() + "-01.htm";
         } else if (this.check(st)) {
            htmltext = npc.getId() + "-03.htm";
         } else if (!st.hasQuestItems(4629) && !st.hasQuestItems(4640) && !st.hasQuestItems(4651)) {
            htmltext = npc.getId() + "-21.htm";
         }

         return htmltext;
      }
   }

   private void calcAbsorb(List<_350_EnhanceYourWeapon.PlayerResult> players, MonsterInstance npc, AbsorbInfo info) {
      int memberSize = 0;
      List<_350_EnhanceYourWeapon.PlayerResult> targets;
      switch(info.getAbsorbType()) {
         case LAST_HIT:
            targets = Collections.singletonList(players.get(0));
            break;
         case PARTY_ALL:
            targets = players;
            break;
         case PARTY_RANDOM:
            memberSize = players.size();
            if (memberSize == 1) {
               targets = Collections.singletonList(players.get(0));
            } else {
               int size = Rnd.get(memberSize);
               targets = new ArrayList<>(size);
               List<_350_EnhanceYourWeapon.PlayerResult> temp = new ArrayList<>(players);
               Collections.shuffle(temp);

               for(int i = 0; i < size; ++i) {
                  targets.add(temp.get(i));
               }
            }
            break;
         case PARTY_ONE:
            memberSize = players.size();
            if (memberSize == 1) {
               targets = Collections.singletonList(players.get(0));
            } else {
               int rnd = Rnd.get(memberSize);
               targets = Collections.singletonList(players.get(rnd));
            }
            break;
         default:
            return;
      }

      for(_350_EnhanceYourWeapon.PlayerResult target : targets) {
         if (target != null && (target.getMessage() == null || target.getMessage() == SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED)) {
            Player targetPlayer = target.getPlayer();
            if ((!info.isSkill() || npc.isAbsorbed(targetPlayer)) && targetPlayer.getQuestState(this.getName()) != null) {
               boolean resonation = false;
               SoulCrystalTemplate soulCrystal = null;
               ItemInstance[] items = targetPlayer.getInventory().getItems();

               for(ItemInstance item : items) {
                  SoulCrystalTemplate crystal = SoulCrystalParser.getInstance().getCrystal(item.getId());
                  if (crystal != null) {
                     if (soulCrystal != null) {
                        target.setMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
                        resonation = true;
                        break;
                     }

                     soulCrystal = crystal;
                  }
               }

               if (!resonation && soulCrystal != null) {
                  if (!info.canAbsorb(soulCrystal.getLvl() + 1)) {
                     target.setMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
                  } else {
                     int nextItemId = 0;
                     if (info.getCursedChance() > 0 && soulCrystal.getCursedNextId() > 0) {
                        nextItemId = Rnd.chance(info.getCursedChance()) ? soulCrystal.getCursedNextId() : 0;
                     }

                     if (nextItemId == 0) {
                        nextItemId = Rnd.chance(info.getChance()) ? soulCrystal.getNextId() : 0;
                     }

                     if (nextItemId == 0) {
                        target.setMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
                     } else {
                        ItemInstance Item = targetPlayer.getInventory().destroyItemByItemId("SoulCrystal", soulCrystal.getId(), 1L, targetPlayer, npc);
                        if (Item != null) {
                           InventoryUpdate playerIU = new InventoryUpdate();
                           playerIU.addRemovedItem(Item);
                           Item = targetPlayer.getInventory().addItem("SoulCrystal", nextItemId, 1L, targetPlayer, npc);
                           playerIU.addItem(Item);
                           SystemMessage sms = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                           sms.addItemName(nextItemId);
                           targetPlayer.sendPacket(sms);
                           targetPlayer.sendPacket(playerIU);
                           targetPlayer.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
                        } else {
                           target.setMessage(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void main(String[] args) {
      new _350_EnhanceYourWeapon(350, _350_EnhanceYourWeapon.class.getSimpleName(), "");
   }

   protected static class PlayerResult {
      private final Player _player;
      private SystemMessageId _message;

      public PlayerResult(Player player) {
         this._player = player;
      }

      public Player getPlayer() {
         return this._player;
      }

      public SystemMessageId getMessage() {
         return this._message;
      }

      public void setMessage(SystemMessageId message) {
         this._message = message;
      }

      public void send() {
         if (this._message != null) {
            this._player.sendPacket(this._message);
            this._message = null;
         }
      }
   }
}
