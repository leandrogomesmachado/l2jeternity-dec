package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public class RequestHardWareInfo extends GameClientPacket {
   private String mac;
   private String cpu;
   private String vgaName;
   private String driverVersion;
   private int windowsPlatformId;
   private int windowsMajorVersion;
   private int windowsMinorVersion;
   private int windowsBuildNumber;
   private int DXVersion;
   private int DXRevision;
   private int cpuSpeed;
   private int cpuCoreCount;
   private int unk8;
   private int unk9;
   private int PhysMemory1;
   private int PhysMemory2;
   private int unk12;
   private int videoMemory;
   private int unk14;
   private int vgaVersion;

   @Override
   protected void readImpl() {
      this.mac = this.readS();
      this.windowsPlatformId = this.readD();
      this.windowsMajorVersion = this.readD();
      this.windowsMinorVersion = this.readD();
      this.windowsBuildNumber = this.readD();
      this.DXVersion = this.readD();
      this.DXRevision = this.readD();
      this.cpu = this.readS();
      this.cpuSpeed = this.readD();
      this.cpuCoreCount = this.readD();
      this.unk8 = this.readD();
      this.unk9 = this.readD();
      this.PhysMemory1 = this.readD();
      this.PhysMemory2 = this.readD();
      this.unk12 = this.readD();
      this.videoMemory = this.readD();
      this.unk14 = this.readD();
      this.vgaVersion = this.readD();
      this.vgaName = this.readS();
      this.driverVersion = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            if (activeChar.isGM()) {
               _log.info(
                  "Mac: {"
                     + this.mac
                     + "} WPI: {"
                     + this.windowsPlatformId
                     + "} WMV1: {"
                     + this.windowsMajorVersion
                     + "} WMV2: {"
                     + this.windowsMinorVersion
                     + "} WBN: {"
                     + this.windowsBuildNumber
                     + "} DXV: {"
                     + this.DXVersion
                     + "} DXR: {"
                     + this.DXRevision
                     + "} CPU: {"
                     + this.cpu
                     + "} CPUS: {"
                     + this.cpuSpeed
                     + "} CPUCC: {"
                     + this.cpuCoreCount
                     + "} PM1: {"
                     + this.PhysMemory1
                     + "} PM2: {"
                     + this.PhysMemory2
                     + "} VM: {"
                     + this.videoMemory
                     + "} VGAV: {"
                     + this.vgaVersion
                     + "} VGAN: {"
                     + this.vgaName
                     + "} DV: {"
                     + this.driverVersion
                     + "}"
               );
               _log.info("UNK8: {" + this.unk8 + "} UNK9: {" + this.unk9 + "} UNK12: {" + this.unk12 + "} UNK14: {" + this.unk14 + "}");
            }
         }
      }
   }
}
