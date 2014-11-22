package com.mayhew3.drafttower.client;

/**
 * GWT test for end-to-end scenarios.
 */
public class EndToEndTest extends TestBase {

  public void testForcePickThroughEntireDraft() {
    login(1);
    click("-clock-playPause");
    while (!getInnerText("-teamOrder-round").equals("It's over!")) {
      click("-pickControls-force");
    }
  }
}