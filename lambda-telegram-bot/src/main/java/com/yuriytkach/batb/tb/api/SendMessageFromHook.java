package com.yuriytkach.batb.tb.api;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SendMessageFromHook extends SendMessage {

  private final String method = "sendMessage";
}
