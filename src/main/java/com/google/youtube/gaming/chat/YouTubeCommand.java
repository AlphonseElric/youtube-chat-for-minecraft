/**
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.google.youtube.gaming.chat;

import com.google.api.services.youtube.model.*;
import com.google.youtube.gaming.chat.YouTubeChatService.YouTubeChatMessageListener;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An in-game command for managing the YouTube Chat service. Usage:
 *
 * /ytchat [start|stop|logout|echoStart|echoStop]
 */
public class YouTubeCommand extends CommandBase implements YouTubeChatMessageListener {
  private List<String> aliases = new ArrayList<>();
  private ChatService service;

  public YouTubeCommand(ChatService service) {
    this.service = service;
  }

  @Override
  public String getCommandName() {
    return "ytchat";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return getCommandName() + " [start|stop|logout|echoStart|echoStop|post";
  }

  @Override
  public List getCommandAliases() {
    aliases.add("ytchat");
    aliases.add("ytc");

    return aliases;
  }

  @Override
  public void processCommand(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      showUsage(sender);
      return;
    }

    if (args[0].equalsIgnoreCase("start")) {
      YouTubeConfiguration configuration = YouTubeConfiguration.getInstance();
      String clientSecret = configuration.getClientSecret();
      if (clientSecret == null || clientSecret.isEmpty()) {
        showMessage("No client secret configurated", sender);
        return;
      }
      service.start(configuration.getVideoId(), clientSecret, sender);
    } else if (args[0].equalsIgnoreCase("stop")) {
      service.stop(sender);
    } else if (args[0].equalsIgnoreCase("logout")) {
      service.stop(sender);
      try {
        Auth.clearCredentials();
      } catch (IOException e) {
        showMessage(e.getMessage(), sender);
      }
    } else if (args[0].equalsIgnoreCase("echoStart"))
    {
        if (!service.isInitialized()) {
          showMessage("Service is not initialized", sender);
          showUsage(sender);
        } else {
          service.subscribe(this);
        }
    } else if (args[0].equalsIgnoreCase("echoStop")) {
        service.unsubscribe(this);
    } else if(args[0].equalsIgnoreCase("post")) {
        StringBuilder message = new StringBuilder();
        for (String arg: args) {
            if(!arg.equalsIgnoreCase("post")) {
                message.append(arg).append(" ");
            }
        }
        Consumer<String> id = i -> showMessage(EnumChatFormatting.RED + "[YTChat] "
            + EnumChatFormatting.GREEN + "Message Posted.", sender);
        service.postMessage(message.toString(), id);
    } else {
        showUsage(sender);
    }
  }


  private void showUsage(ICommandSender sender) {
    showMessage("Usage: " + getCommandUsage(sender), sender);
  }

  private void showMessage(String message, ICommandSender sender) {
    sender.addChatMessage(new ChatComponentText(message));
  }

  private void showStreamMessage(LiveChatMessageAuthorDetails author, String message, String id, ICommandSender sender) {
      ChatStyle chatStyle = new ChatStyle();
      sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[YTChat] " + (author.getIsChatModerator() ? EnumChatFormatting.BLUE : EnumChatFormatting.WHITE) +
          (author.getIsChatOwner() ? EnumChatFormatting.GOLD : EnumChatFormatting.WHITE) + author.getDisplayName() + EnumChatFormatting.WHITE + ": " + message)
          .setChatStyle(chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "del " + id)))
          .setChatStyle(chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "Click to delete this message!"))))
      );
  }

  @Override
  public boolean canCommandSenderUseCommand(ICommandSender sender) {
    return true;
  }

  @Override
  public boolean isUsernameIndex(String[] args, int index) {
    return false;
  }



  @Override
  public void onMessageReceived(
      LiveChatMessageAuthorDetails author,
      LiveChatSuperChatDetails superChatDetails,
      String id,
      String message) {

    if(!YouTubeConfiguration.getInstance().getSuperOnly()) {
      showStreamMessage(author, message, id, Minecraft.getMinecraft().thePlayer);
    }

    if (superChatDetails != null
        && superChatDetails.getAmountMicros() != null
        && superChatDetails.getAmountMicros().longValue() > 0) {

      showMessage(EnumChatFormatting.RED + "[YTChat] "
          + EnumChatFormatting.GREEN + "Received "
          + EnumChatFormatting.GOLD
          + superChatDetails.getAmountDisplayString()
          + EnumChatFormatting.GREEN
          + " from "
          + (author.getIsChatModerator() ? EnumChatFormatting.BLUE : EnumChatFormatting.WHITE)
          + author.getDisplayName(), Minecraft.getMinecraft().thePlayer);
    }
  }
}
