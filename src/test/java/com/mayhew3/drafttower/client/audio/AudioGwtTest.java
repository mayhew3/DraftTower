package com.mayhew3.drafttower.client.audio;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.safehtml.shared.UriUtils;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;

/**
 * Tests audio widget.
 */
public class AudioGwtTest extends TestBase {

  public void testAudioPlaysOnDraftStatusChange() {
    login(2);
    simulateDraftStart();
    simulateDraftStatus(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(
                DraftStatusTestUtil.createDraftPick(1, "", false, testComponent.beanFactory())),
            testComponent.beanFactory()));
    IFrameElement audioFrame = IFrameElement.as(
        ensureDebugIdAndGetElement("-audio-frame", true));
    assertContains(UriUtils.encode("selects"), audioFrame.getSrc());
    assertContains(UriUtils.encode("you're on the clock"), audioFrame.getSrc());
  }

  public void testPreviousAudioCancelled() {
    login(3);
    simulateDraftStart();
    simulateDraftStatus(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, testComponent.beanFactory())),
            testComponent.beanFactory()));
    IFrameElement audioFrame = IFrameElement.as(
        ensureDebugIdAndGetElement("-audio-frame", true));
    assertContains(UriUtils.encode("you're on deck"), audioFrame.getSrc());
    simulateDraftStatus(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(
                DraftStatusTestUtil.createDraftPick(1, "", false, testComponent.beanFactory()),
                DraftStatusTestUtil.createDraftPick(2, "", false, testComponent.beanFactory())),
            testComponent.beanFactory()));
    assertDoesNotContain(UriUtils.encode("you're on deck"), audioFrame.getSrc());
    assertContains(UriUtils.encode("you're on the clock"), audioFrame.getSrc());
  }

  public void testAudioVolumeLow() {
    login(2);
    click("-speech");
    simulateDraftStart();
    simulateDraftStatus(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(
                DraftStatusTestUtil.createDraftPick(1, "", false, testComponent.beanFactory())),
            testComponent.beanFactory()));
    IFrameElement audioFrame = IFrameElement.as(
        ensureDebugIdAndGetElement("-audio-frame", true));
    assertDoesNotContain(UriUtils.encode("selects"), audioFrame.getSrc());
    assertContains(UriUtils.encode("you're on the clock"), audioFrame.getSrc());
  }

  public void testAudioVolumeOff() {
    login(2);
    click("-speech");
    click("-speech");
    simulateDraftStart();
    simulateDraftStatus(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(
                DraftStatusTestUtil.createDraftPick(1, "", false, testComponent.beanFactory())),
            testComponent.beanFactory()));
    IFrameElement audioFrame = IFrameElement.as(
        ensureDebugIdAndGetElement("-audio-frame", true));
    assertDoesNotContain(UriUtils.encode("selects"), audioFrame.getSrc());
    assertDoesNotContain(UriUtils.encode("you're on the clock"), audioFrame.getSrc());
  }
}