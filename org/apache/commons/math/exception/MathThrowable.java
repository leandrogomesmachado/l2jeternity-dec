package org.apache.commons.math.exception;

import java.util.Locale;
import org.apache.commons.math.exception.util.Localizable;

public interface MathThrowable {
   Localizable getSpecificPattern();

   Localizable getGeneralPattern();

   Object[] getArguments();

   String getMessage(Locale var1);

   String getMessage();

   String getLocalizedMessage();
}
