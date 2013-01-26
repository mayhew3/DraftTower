package com.mayhew3.drafttower.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite {

  private final BeanFactory beanFactory;

  @Inject
  public MainPageWidget(DraftClock clock,
      final DraftSocketHandler socketHandler,
      final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    // TODO: use UiBinder to lay out real UI.
    FlowPanel container = new FlowPanel();
    container.add(clock);

    container.add(new Button("Start", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage(getDraftCommand(DraftCommand.Command.START_DRAFT));
      }
    }));

    container.add(new Button("Pause", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage(getDraftCommand(DraftCommand.Command.PAUSE));
      }
    }));

    container.add(new Button("Resume", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage(getDraftCommand(DraftCommand.Command.RESUME));
      }
    }));

    container.add(new Button("Simulate pick", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage(getDraftCommand(DraftCommand.Command.DO_PICK));
      }
    }));

    initWidget(container);
  }

  private String getDraftCommand(DraftCommand.Command commandType) {
    AutoBean<DraftCommand> commandBean = beanFactory.createDraftCommand();
    DraftCommand command = commandBean.as();
    command.setCommandType(commandType);
    return AutoBeanCodex.encode(commandBean).getPayload();
  }
}