package l2e.gameserver.listener;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.listener.character.AttackListener;
import l2e.gameserver.listener.character.DeathListener;
import l2e.gameserver.listener.character.SkillUseListener;
import l2e.gameserver.listener.clan.ClanCreationListener;
import l2e.gameserver.listener.clan.ClanMembershipListener;
import l2e.gameserver.listener.clan.ClanWarListener;
import l2e.gameserver.listener.clan.ClanWarehouseListener;
import l2e.gameserver.listener.events.AddToInventoryEvent;
import l2e.gameserver.listener.events.AttackEvent;
import l2e.gameserver.listener.events.AugmentEvent;
import l2e.gameserver.listener.events.ChatEvent;
import l2e.gameserver.listener.events.ClanCreationEvent;
import l2e.gameserver.listener.events.ClanJoinEvent;
import l2e.gameserver.listener.events.ClanLeaderChangeEvent;
import l2e.gameserver.listener.events.ClanLeaveEvent;
import l2e.gameserver.listener.events.ClanLevelUpEvent;
import l2e.gameserver.listener.events.ClanWarEvent;
import l2e.gameserver.listener.events.ClanWarehouseAddItemEvent;
import l2e.gameserver.listener.events.ClanWarehouseDeleteItemEvent;
import l2e.gameserver.listener.events.ClanWarehouseTransferEvent;
import l2e.gameserver.listener.events.DeathEvent;
import l2e.gameserver.listener.events.DlgAnswerEvent;
import l2e.gameserver.listener.events.EquipmentEvent;
import l2e.gameserver.listener.events.FortSiegeEvent;
import l2e.gameserver.listener.events.FortSiegeListener;
import l2e.gameserver.listener.events.HennaEvent;
import l2e.gameserver.listener.events.ItemCreateEvent;
import l2e.gameserver.listener.events.ItemDestroyEvent;
import l2e.gameserver.listener.events.ItemDropEvent;
import l2e.gameserver.listener.events.ItemPickupEvent;
import l2e.gameserver.listener.events.ItemTransferEvent;
import l2e.gameserver.listener.events.PlayerEvent;
import l2e.gameserver.listener.events.PlayerLevelChangeEvent;
import l2e.gameserver.listener.events.ProfessionChangeEvent;
import l2e.gameserver.listener.events.RequestBypassToServerEvent;
import l2e.gameserver.listener.events.SiegeEvent;
import l2e.gameserver.listener.events.SiegeListener;
import l2e.gameserver.listener.events.SkillUseEvent;
import l2e.gameserver.listener.events.TransformEvent;
import l2e.gameserver.listener.player.AugmentListener;
import l2e.gameserver.listener.player.DropListener;
import l2e.gameserver.listener.player.EquipmentListener;
import l2e.gameserver.listener.player.HennaListener;
import l2e.gameserver.listener.player.ItemTracker;
import l2e.gameserver.listener.player.NewItemListener;
import l2e.gameserver.listener.player.PlayerDespawnListener;
import l2e.gameserver.listener.player.PlayerLevelListener;
import l2e.gameserver.listener.player.PlayerListener;
import l2e.gameserver.listener.player.PlayerSpawnListener;
import l2e.gameserver.listener.player.ProfessionChangeListener;
import l2e.gameserver.listener.player.TransformListener;
import l2e.gameserver.listener.talk.ChatFilterListener;
import l2e.gameserver.listener.talk.ChatListener;
import l2e.gameserver.listener.talk.DlgAnswerListener;
import l2e.gameserver.listener.talk.RequestBypassToServerListener;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;

public abstract class ScriptListener extends Quest {
   private final List<AbstractListener> _listeners = new ArrayList<>();

   public ScriptListener(String name, String descr) {
      super(-1, name, descr);
   }

   public ScriptListener(int questId, String name, String descr) {
      super(questId, name, descr);
   }

   @Override
   public boolean unload() {
      for(AbstractListener listener : this._listeners) {
         listener.unregister();
      }

      this._listeners.clear();
      return super.unload();
   }

   private void removeListeners(List<AbstractListener> removeList) {
      for(AbstractListener listener : removeList) {
         listener.unregister();
         this._listeners.remove(listener);
      }
   }

   public void addDeathNotify(Creature character) {
      DeathListener listener = new DeathListener(character) {
         @Override
         public boolean onDeath(Creature attacker, Creature target) {
            DeathEvent event = new DeathEvent();
            event.setKiller(attacker);
            event.setVictim(target);
            return ScriptListener.this.onDeath(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeDeathNotify(Creature character) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof DeathListener && ((DeathListener)listener).getCharacter() == character) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addLoginLogoutNotify() {
      PlayerSpawnListener spawn = new PlayerSpawnListener() {
         @Override
         public void onPlayerLogin(Player player) {
            ScriptListener.this.onPlayerLogin(player);
         }
      };
      PlayerDespawnListener despawn = new PlayerDespawnListener() {
         @Override
         public void onPlayerLogout(Player player) {
            ScriptListener.this.onPlayerLogout(player);
         }
      };
      this._listeners.add(spawn);
      this._listeners.add(despawn);
   }

   public void removeLoginLogoutNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof PlayerSpawnListener || listener instanceof PlayerDespawnListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addAttackNotify(Creature character) {
      AttackListener listener = new AttackListener(character) {
         @Override
         public boolean onAttack(Creature attacker, Creature target) {
            AttackEvent event = new AttackEvent();
            event.setAttacker(attacker);
            event.setTarget(target);
            return ScriptListener.this.onAttack(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeAttackNotify(Creature character) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof AttackListener && ((AttackListener)listener).getCharacter() == character) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addSkillUseNotify(Creature character) {
      SkillUseListener listener = new SkillUseListener(character) {
         @Override
         public boolean onSkillUse(Creature caster, Skill skill, boolean simultaneously, Creature target, GameObject[] targets) {
            SkillUseEvent event = new SkillUseEvent();
            event.setCaster(caster);
            event.setSkill(skill);
            event.setTarget(target);
            event.setTargets(targets);
            return ScriptListener.this.onSkillUse(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeSkillUseNotify(Creature character) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof SkillUseListener && ((SkillUseListener)listener).getCharacter() == character) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addClanCreationLevelUpNotify() {
      ClanCreationListener listener = new ClanCreationListener() {
         @Override
         public void onClanCreate(ClanCreationEvent event) {
            ScriptListener.this.onClanCreated(event);
         }

         @Override
         public boolean onClanLevelUp(ClanLevelUpEvent event) {
            return ScriptListener.this.onClanLeveledUp(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeClanCreationLevelUpNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ClanCreationListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addClanJoinLeaveNotify() {
      ClanMembershipListener listener = new ClanMembershipListener() {
         @Override
         public boolean onJoin(ClanJoinEvent event) {
            return ScriptListener.this.onClanJoin(event);
         }

         @Override
         public boolean onLeaderChange(ClanLeaderChangeEvent event) {
            return ScriptListener.this.onClanLeaderChange(event);
         }

         @Override
         public boolean onLeave(ClanLeaveEvent event) {
            return ScriptListener.this.onClanLeave(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeClanJoinLeaveNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ClanMembershipListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addClanWarehouseNotify(Clan clan) {
      if (clan != null) {
         ClanWarehouseListener listener = new ClanWarehouseListener(clan) {
            @Override
            public boolean onAddItem(ClanWarehouseAddItemEvent event) {
               return ScriptListener.this.onClanWarehouseAddItem(event);
            }

            @Override
            public boolean onDeleteItem(ClanWarehouseDeleteItemEvent event) {
               return ScriptListener.this.onClanWarehouseDeleteItem(event);
            }

            @Override
            public boolean onTransferItem(ClanWarehouseTransferEvent event) {
               return ScriptListener.this.onClanWarehouseTransferItem(event);
            }
         };
         this._listeners.add(listener);
      }
   }

   public void removeClanWarehouseNotify(Clan clan) {
      if (clan != null) {
         List<AbstractListener> removeList = new ArrayList<>();

         for(AbstractListener listener : this._listeners) {
            if (listener instanceof ClanWarehouseListener && ((ClanWarehouseListener)listener).getWarehouse() == clan.getWarehouse()) {
               removeList.add(listener);
            }
         }

         this.removeListeners(removeList);
      }
   }

   public void addClanWarNotify() {
      ClanWarListener listener = new ClanWarListener() {
         @Override
         public boolean onWarStart(ClanWarEvent event) {
            event.setStage(ScriptListener.EventStage.START);
            return ScriptListener.this.onClanWarEvent(event);
         }

         @Override
         public boolean onWarEnd(ClanWarEvent event) {
            event.setStage(ScriptListener.EventStage.END);
            return ScriptListener.this.onClanWarEvent(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeClanWarNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ClanWarListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addFortSiegeNotify() {
      FortSiegeListener listener = new FortSiegeListener() {
         @Override
         public boolean onStart(FortSiegeEvent event) {
            event.setStage(ScriptListener.EventStage.START);
            return ScriptListener.this.onFortSiegeEvent(event);
         }

         @Override
         public void onEnd(FortSiegeEvent event) {
            event.setStage(ScriptListener.EventStage.END);
            ScriptListener.this.onFortSiegeEvent(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeFortSiegeNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof FortSiegeListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addSiegeNotify() {
      SiegeListener listener = new SiegeListener() {
         @Override
         public boolean onStart(SiegeEvent event) {
            event.setStage(ScriptListener.EventStage.START);
            return ScriptListener.this.onSiegeEvent(event);
         }

         @Override
         public void onEnd(SiegeEvent event) {
            event.setStage(ScriptListener.EventStage.END);
            ScriptListener.this.onSiegeEvent(event);
         }

         @Override
         public void onControlChange(SiegeEvent event) {
            ScriptListener.this.onCastleControlChange(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeSiegeNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof SiegeListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addItemAugmentNotify() {
      AugmentListener listener = new AugmentListener() {
         @Override
         public boolean onAugment(AugmentEvent event) {
            return ScriptListener.this.onItemAugment(event);
         }

         @Override
         public boolean onRemoveAugment(AugmentEvent event) {
            return ScriptListener.this.onItemAugment(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeItemAugmentNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof AugmentListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addItemDropPickupNotify() {
      DropListener listener = new DropListener() {
         @Override
         public boolean onDrop(ItemDropEvent event) {
            return ScriptListener.this.onItemDrop(event);
         }

         @Override
         public boolean onPickup(ItemPickupEvent event) {
            return ScriptListener.this.onItemPickup(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeItemDropPickupNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof DropListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addPlayerLevelNotify(Player player) {
      PlayerLevelListener listener = new PlayerLevelListener(player) {
         @Override
         public boolean onLevelChange(Playable playable, byte levels) {
            PlayerLevelChangeEvent event = new PlayerLevelChangeEvent();
            event.setPlayer(playable.getActingPlayer());
            event.setOldLevel(playable.getLevel());
            event.setNewLevel(playable.getLevel() + levels);
            return ScriptListener.this.onLevelChange(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removePlayerLevelNotify(Player player) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof PlayerLevelListener && listener.getPlayer() == player) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addProfessionChangeNotify(Player player) {
      ProfessionChangeListener listener = new ProfessionChangeListener(player) {
         @Override
         public void professionChanged(ProfessionChangeEvent event) {
            ScriptListener.this.onProfessionChange(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeProfessionChangeNotify(Player player) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ProfessionChangeListener && listener.getPlayer() == player) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addEquipmentNotify(Player player) {
      EquipmentListener listener = new EquipmentListener(player) {
         @Override
         public boolean onEquip(EquipmentEvent event) {
            return ScriptListener.this.onItemEquip(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeEquipmentNotify(Player player) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof EquipmentListener && ((EquipmentListener)listener).getPlayer() == player) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addHennaNotify() {
      HennaListener listener = new HennaListener() {
         @Override
         public boolean onAddHenna(HennaEvent event) {
            return ScriptListener.this.onHennaModify(event);
         }

         @Override
         public boolean onRemoveHenna(HennaEvent event) {
            return ScriptListener.this.onHennaModify(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removeHennaNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof HennaListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addItemTracker(List<Integer> itemIds) {
      if (itemIds != null) {
         ItemTracker listener = new ItemTracker(itemIds) {
            @Override
            public void onDrop(ItemDropEvent event) {
               ScriptListener.this.onItemTrackerEvent(event);
            }

            @Override
            public void onAddToInventory(AddToInventoryEvent event) {
               ScriptListener.this.onItemTrackerEvent(event);
            }

            @Override
            public void onDestroy(ItemDestroyEvent event) {
               ScriptListener.this.onItemTrackerEvent(event);
            }

            @Override
            public void onTransfer(ItemTransferEvent event) {
               ScriptListener.this.onItemTrackerEvent(event);
            }
         };
         this._listeners.add(listener);
      }
   }

   public void removeItemTrackers() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ItemTracker) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addNewItemNotify(List<Integer> itemIds) {
      if (itemIds != null) {
         NewItemListener listener = new NewItemListener(itemIds) {
            @Override
            public boolean onCreate(ItemCreateEvent event) {
               return ScriptListener.this.onItemCreate(event);
            }
         };
         this._listeners.add(listener);
      }
   }

   public void removeNewItemNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof NewItemListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addTransformNotify(Player player) {
      if (player != null) {
         TransformListener listener = new TransformListener(player) {
            @Override
            public boolean onTransform(TransformEvent event) {
               event.setTransforming(true);
               return ScriptListener.this.onPlayerTransform(event);
            }

            @Override
            public boolean onUntransform(TransformEvent event) {
               return ScriptListener.this.onPlayerTransform(event);
            }
         };
         this._listeners.add(listener);
      }
   }

   public void removeTransformNotify(Player player) {
      if (player != null) {
         List<AbstractListener> removeList = new ArrayList<>();

         for(AbstractListener listener : this._listeners) {
            if (listener instanceof TransformListener && listener.getPlayer() == player) {
               removeList.add(listener);
            }
         }

         this.removeListeners(removeList);
      }
   }

   public void addPlayerChatFilter() {
      ChatFilterListener listener = new ChatFilterListener() {
         @Override
         public String onTalk(ChatEvent event) {
            return ScriptListener.this.filterChat(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removePlayerChatFilter() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ChatFilterListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addPlayerTalkNotify() {
      ChatListener listener = new ChatListener() {
         @Override
         public void onTalk(ChatEvent event) {
            ScriptListener.this.onPlayerTalk(event);
         }
      };
      this._listeners.add(listener);
   }

   public void removePlayerTalkNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof ChatListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addDlgAnswerNotify(Player player) {
      DlgAnswerListener dlgAnswer = new DlgAnswerListener(player) {
         @Override
         public boolean onDlgAnswer(Player player, int messageId, int answer, int requesterId) {
            DlgAnswerEvent event = new DlgAnswerEvent();
            event.setActiveChar(player);
            event.setMessageId(messageId);
            event.setAnswer(answer);
            event.setRequesterId(requesterId);
            return ScriptListener.this.onDlgAnswer(event);
         }
      };
      this._listeners.add(dlgAnswer);
   }

   public void removeDlgAnswerNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof DlgAnswerListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void removeDlgAnswerNotify(Player player) {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof DlgAnswerListener && ((DlgAnswerListener)listener).getPlayer() == player) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addRequestBypassToServerNotify() {
      RequestBypassToServerListener bypass = new RequestBypassToServerListener() {
         @Override
         public void onRequestBypassToServer(RequestBypassToServerEvent event) {
            ScriptListener.this.onRequestBypassToServer(event);
         }
      };
      this._listeners.add(bypass);
   }

   public void removeRequestBypassToServerNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof DlgAnswerListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void addPlayerNotify() {
      PlayerListener bypass = new PlayerListener() {
         @Override
         public void onCharCreate(PlayerEvent event) {
            ScriptListener.this.onCharCreate(event);
         }

         @Override
         public void onCharDelete(PlayerEvent event) {
            ScriptListener.this.onCharDelete(event);
         }

         @Override
         public void onCharRestore(PlayerEvent event) {
            ScriptListener.this.onCharRestore(event);
         }

         @Override
         public void onCharSelect(PlayerEvent event) {
            ScriptListener.this.onCharSelect(event);
         }
      };
      this._listeners.add(bypass);
   }

   public void removePlayerNotify() {
      List<AbstractListener> removeList = new ArrayList<>();

      for(AbstractListener listener : this._listeners) {
         if (listener instanceof PlayerListener) {
            removeList.add(listener);
         }
      }

      this.removeListeners(removeList);
   }

   public void onPlayerLogin(Player player) {
   }

   public void onPlayerLogout(Player player) {
   }

   public boolean onAttack(AttackEvent event) {
      return true;
   }

   public boolean onDeath(DeathEvent event) {
      return true;
   }

   public boolean onSkillUse(SkillUseEvent event) {
      return true;
   }

   public void onClanCreated(ClanCreationEvent event) {
   }

   public boolean onClanLeveledUp(ClanLevelUpEvent event) {
      return true;
   }

   public boolean onClanJoin(ClanJoinEvent event) {
      return true;
   }

   public boolean onClanLeave(ClanLeaveEvent event) {
      return true;
   }

   public boolean onClanLeaderChange(ClanLeaderChangeEvent event) {
      return true;
   }

   public boolean onClanWarehouseAddItem(ClanWarehouseAddItemEvent event) {
      return true;
   }

   public boolean onClanWarehouseDeleteItem(ClanWarehouseDeleteItemEvent event) {
      return true;
   }

   public boolean onClanWarehouseTransferItem(ClanWarehouseTransferEvent event) {
      return true;
   }

   public boolean onClanWarEvent(ClanWarEvent event) {
      return true;
   }

   public boolean onFortSiegeEvent(FortSiegeEvent event) {
      return true;
   }

   public boolean onSiegeEvent(SiegeEvent event) {
      return true;
   }

   public void onCastleControlChange(SiegeEvent event) {
   }

   public void onTvtEvent(ScriptListener.EventStage stage) {
   }

   public boolean onItemAugment(AugmentEvent event) {
      return true;
   }

   public boolean onItemDrop(ItemDropEvent event) {
      return true;
   }

   public boolean onItemPickup(ItemPickupEvent event) {
      return true;
   }

   public boolean onItemEquip(EquipmentEvent event) {
      return true;
   }

   public boolean onLevelChange(PlayerLevelChangeEvent event) {
      return true;
   }

   public void onProfessionChange(ProfessionChangeEvent event) {
   }

   public boolean onHennaModify(HennaEvent event) {
      return true;
   }

   public void onItemTrackerEvent(EventListener event) {
   }

   public boolean onItemCreate(ItemCreateEvent event) {
      return true;
   }

   public boolean onPlayerTransform(TransformEvent event) {
      return true;
   }

   public String filterChat(ChatEvent event) {
      return "";
   }

   public void onPlayerTalk(ChatEvent event) {
   }

   public boolean onDlgAnswer(DlgAnswerEvent event) {
      return true;
   }

   protected void onRequestBypassToServer(RequestBypassToServerEvent event) {
   }

   protected void onCharSelect(PlayerEvent event) {
   }

   protected void onCharCreate(PlayerEvent event) {
   }

   protected void onCharDelete(PlayerEvent event) {
   }

   protected void onCharRestore(PlayerEvent event) {
   }

   public static enum EventStage {
      START,
      END,
      EVENT_STOPPED,
      REGISTRATION_BEGIN,
      CONTROL_CHANGE;
   }

   public static enum ItemTrackerEvent {
      DROP,
      ADD_TO_INVENTORY,
      DESTROY,
      TRANSFER;
   }
}
