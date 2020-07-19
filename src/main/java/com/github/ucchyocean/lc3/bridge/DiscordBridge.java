package com.github.ucchyocean.lc3.bridge;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.channel.ChatSource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DiscordBridge extends ListenerAdapter {
	private JDA jda;
	private static final Pattern stripPattern = Pattern.compile("(?<!@)[&§](?i)[0-9a-fklmnorx]");
	private static final String SOURCE_NAME = "Discord";

	private DiscordBridge(){}

	public static DiscordBridge load(String token) {
		try {
			JDABuilder builder = JDABuilder.createDefault(token);
			DiscordBridge discordBridge = new DiscordBridge();
			discordBridge.jda = builder.build();
			discordBridge.jda.addEventListener(discordBridge);
			return discordBridge;
		} catch (LoginException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

		if (event.getMember() == null || jda == null || event.getAuthor().equals(jda.getSelfUser()) || event.getAuthor().isBot()) {
			return;
		}

		LunaChatAPI api = LunaChat.getAPI();
		LunaChatConfig config = LunaChat.getConfig();
		Collection<Channel> channels = api.getChannels();
		StringBuilder message = new StringBuilder(event.getMessage().getContentStripped());

		if (StringUtils.isBlank(message.toString()) && event.getMessage().getAttachments().size() == 0) return;

		if (!event.getMessage().getAttachments().isEmpty()) {
			for (Message.Attachment attachment : event.getMessage().getAttachments().subList(0, Math.min(event.getMessage().getAttachments().size(), 3))) {
				message.append(attachment.getUrl());
			}
		}

		boolean isRelayDefaultChannel = channels.stream()
				.anyMatch(channel -> channel.getDiscordChannelId().equals(event.getChannel().getId()) && channel.isDiscordRelayForcedChannel());

		if (isRelayDefaultChannel){
			Channel discordDefaultChannel = api.getChannel(config.getDiscordForcedSendChannel());
			if (discordDefaultChannel != null){
				discordDefaultChannel.chatFromOtherSource(event.getMember().getEffectiveName(), ChatSource.DISCORD.getSource(), message.toString());
			}
			return;
		}

		channels.stream()
				.filter(channel -> channel.getDiscordChannelId().equals(event.getChannel().getId()))
				.forEach(channel -> channel.chatFromOtherSource(event.getMember().getEffectiveName(), ChatSource.DISCORD.getSource(), message.toString()));
	}

	public void chat(String msg, String channelId){
		if (StringUtils.isBlank(msg) || StringUtils.isBlank(channelId) || jda == null) {
			return;
		}
		TextChannel channel = jda.getTextChannelById(channelId);
		if (channel != null) {
			msg = stripPattern.matcher(msg).replaceAll("");
			msg = translateEmotes(msg, channel.getGuild().getEmotes());

			int maxLength = Message.MAX_CONTENT_LENGTH;
			if (msg.length() > maxLength){
				return;
			}
			try {
				channel.sendMessage(new MessageBuilder().append(msg).build()).queue();
			}catch (PermissionException e){
				if (e.getPermission() != Permission.UNKNOWN) {
					LunaChat.getPlugin().log(Level.WARNING, "\"" + e.getPermission().getName() + "\"権限がないため \"" + channel + "\"にチャットを送信できませんでした");
				} else {
					LunaChat.getPlugin().log(Level.WARNING, "\"" + e.getMessage() + "\"" +"のため \"" + channel + "\"にチャットを送信できませんでした");
				}
			}catch (IllegalStateException e) {
				LunaChat.getPlugin().log(Level.WARNING, "チャットを送信できませんでした " + channel + ": " + e.getMessage());
			}
		}
	}

	public boolean isExistTextChannel(String channelId){
		 TextChannel channel = jda.getTextChannelById(channelId);
		return channel != null;
	}

	public String translateEmotes(String messageToTranslate, List<Emote> emotes) {
		for (Emote emote : emotes)
			messageToTranslate = messageToTranslate.replace(":" + emote.getName() + ":", emote.getAsMention());
		return messageToTranslate;
	}
}
