package l2e.gameserver.model.petition;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.PetitionVote;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class Petition {
   private final long _submitTime = System.currentTimeMillis();
   private final int _id;
   private final PetitionType _type;
   private PetitionState _state = PetitionState.PENDING;
   private final String _content;
   private final List<CreatureSay> _messageLog = new CopyOnWriteArrayList<>();
   private final Player _petitioner;
   private Player _responder;

   public Petition(Player petitioner, String petitionText, int petitionType) {
      this._id = IdFactory.getInstance().getNextId();
      this._type = PetitionType.values()[--petitionType];
      this._content = petitionText;
      this._petitioner = petitioner;
   }

   public boolean addLogMessage(CreatureSay cs) {
      return this._messageLog.add(cs);
   }

   public List<CreatureSay> getLogMessages() {
      return this._messageLog;
   }

   public boolean endPetitionConsultation(PetitionState endState) {
      this.setState(endState);
      if (this.getResponder() != null && this.getResponder().isOnline()) {
         if (endState == PetitionState.RESPONDER_REJECT) {
            this.getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ENDED_WITH_C1);
            sm.addString(this.getPetitioner().getName());
            this.getResponder().sendPacket(sm);
            if (endState == PetitionState.PETITIONER_CANCEL) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED);
               sm.addNumber(this.getId());
               this.getResponder().sendPacket(sm);
            }
         }
      }

      if (this.getPetitioner() != null && this.getPetitioner().isOnline()) {
         this.getPetitioner().sendPacket(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK);
         this.getPetitioner().sendPacket(PetitionVote.STATIC_PACKET);
      }

      PetitionManager.getInstance().getCompletedPetitions().put(this.getId(), this);
      return PetitionManager.getInstance().getPendingPetitions().remove(this.getId()) != null;
   }

   public String getContent() {
      return this._content;
   }

   public int getId() {
      return this._id;
   }

   public Player getPetitioner() {
      return this._petitioner;
   }

   public Player getResponder() {
      return this._responder;
   }

   public long getSubmitTime() {
      return this._submitTime;
   }

   public PetitionState getState() {
      return this._state;
   }

   public String getTypeAsString() {
      return this._type.toString().replace("_", " ");
   }

   public void sendPetitionerPacket(GameServerPacket responsePacket) {
      if (this.getPetitioner() != null && this.getPetitioner().isOnline()) {
         this.getPetitioner().sendPacket(responsePacket);
      }
   }

   public void sendResponderPacket(GameServerPacket responsePacket) {
      if (this.getResponder() != null && this.getResponder().isOnline()) {
         this.getResponder().sendPacket(responsePacket);
      } else {
         this.endPetitionConsultation(PetitionState.RESPONDER_MISSING);
      }
   }

   public void setState(PetitionState state) {
      this._state = state;
   }

   public void setResponder(Player respondingAdmin) {
      if (this.getResponder() == null) {
         this._responder = respondingAdmin;
      }
   }
}
