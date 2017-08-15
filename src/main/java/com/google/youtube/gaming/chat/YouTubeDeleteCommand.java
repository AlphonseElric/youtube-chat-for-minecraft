package com.google.youtube.gaming.chat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class YouTubeDeleteCommand extends CommandBase {
    private ChatService service;

    public YouTubeDeleteCommand(ChatService service) {
        this.service = service;
    }

    @Override
    public String getCommandName() {
        return "del";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return getCommandName() + " id";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            showUsage(sender);
            return;
        }

        String messageId = args[0];
        System.out.print(messageId);
        Runnable response = () -> showMessage(EnumChatFormatting.RED + "[YTChat] "
                + EnumChatFormatting.GREEN + "Message deleted.", sender);
        service.deleteMessage(messageId, response);
    }

    private void showUsage(ICommandSender sender) {
        showMessage("Usage: " + getCommandUsage(sender), sender);
    }

    private void showMessage(String message, ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(message));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}
