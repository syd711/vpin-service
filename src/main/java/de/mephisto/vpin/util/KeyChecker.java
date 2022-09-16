package de.mephisto.vpin.util;

import org.apache.commons.lang3.StringUtils;
import org.jnativehook.keyboard.NativeKeyEvent;

/**
 * Checks the native key event, e.g.
 * NATIVE_KEY_PRESSED,keyCode=46,keyText=C,keyChar=Undefiniert,modifiers=Strg,keyLocation=KEY_LOCATION_STANDARD,rawCode=67
 * for Ctrl+C
 */
public class KeyChecker {

  private int modifier;
  private String letter = null;

  public KeyChecker(String hotkey) {
    if (hotkey.contains("+")) {
      this.modifier = Integer.parseInt(hotkey.split("\\+")[0]);
      this.letter = hotkey.split("\\+")[1];
    }
    else {
      this.letter = hotkey;
    }
  }

  public boolean matches(NativeKeyEvent event) {
    String keyText = NativeKeyEvent.getKeyText(event.getKeyCode());
    return (keyText.equalsIgnoreCase(this.letter) && this.modifier == event.getModifiers()) ||
        (StringUtils.isEmpty(letter) && this.modifier == event.getModifiers());
  }
}
