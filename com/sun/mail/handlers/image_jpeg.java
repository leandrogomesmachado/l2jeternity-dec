package com.sun.mail.handlers;

import java.awt.Image;
import javax.activation.ActivationDataFlavor;

public class image_jpeg extends image_gif {
   private static ActivationDataFlavor myDF = new ActivationDataFlavor(Image.class, "image/jpeg", "JPEG Image");

   @Override
   protected ActivationDataFlavor getDF() {
      return myDF;
   }
}
