/*
 * This file is part of EverSanctions.
 *
 * EverSanctions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverSanctions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverSanctions.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.eversanctions.command.ban;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ECommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.sanction.SanctionService;
import fr.evercraft.everapi.services.sanction.manual.SanctionManualProfile;
import fr.evercraft.everapi.sponge.UtilsDate;
import fr.evercraft.eversanctions.ESMessage.ESMessages;
import fr.evercraft.eversanctions.ESPermissions;
import fr.evercraft.eversanctions.EverSanctions;

public class ESBan extends ECommand<EverSanctions> {
	
	public ESBan(final EverSanctions plugin) {
        super(plugin, "ban", "tempban");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ESPermissions.BAN.get());
	} 

	@Override
	public Text description(final CommandSource source) {
		return ESMessages.BAN_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_USER.getString() + "> <" + EAMessages.ARGS_TIME.getString() + "> <" + EAMessages.ARGS_REASON.getString() + ">")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1){
			return this.getAllUsers(args.get(0));
		} else if (args.size() == 2) {
			return Arrays.asList(SanctionService.UNLIMITED, "\"1mo 7d 12h\"", "1h");
		} else if (args.size() == 3) {
			return Arrays.asList("reason...");
		}
		return Arrays.asList();
	}
	
	@Override
	protected List<String> getArg(final String arg) {
		List<String> args = super.getArg(arg);
		// Le message est transformer en un seul argument
		if (args.size() > 3) {
			List<String> args_send = new ArrayList<String>();
			args_send.add(args.get(0));
			args_send.add(args.get(1));
			args_send.add(Pattern.compile("^[ \"]*" + args.get(0) + "[ \"]*" + args.get(1) + "[ \"][ ]*").matcher(arg).replaceAll(""));
			return args_send;
		}
		return args;
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		// Nombre d'argument correct
		if (args.size() == 3) {
			
			Optional<EUser> user = this.plugin.getEServer().getOrCreateEUser(args.get(0));
			// Le joueur existe
			if (user.isPresent()){
				return this.commandBan(source, user.get(), args.get(1), args.get(2));
			// Le joueur est introuvable
			} else {
				source.sendMessage(ESMessages.PREFIX.getText().concat(EAMessages.PLAYER_NOT_FOUND.getText()));
			}
			
		// Nombre d'argument incorrect
		} else {
			source.sendMessage(this.help(source));
		}
		return CompletableFuture.completedFuture(false);
	}
	
	private CompletableFuture<Boolean> commandBan(final CommandSource staff, EUser user, final String time_string, final String reason) {
		// Le staff et le joueur sont identique
		if (staff.getIdentifier().equals(user.getIdentifier())) {
			ESMessages.BAN_ERROR_EQUALS.sender()
				.replace("<player>", user.getName())
				.sendTo(staff);
			return CompletableFuture.completedFuture(false);
		}
		
		// Le joueur a déjà un ban en cours
		if (user.getManual(SanctionManualProfile.Type.BAN_PROFILE).isPresent()) {
			ESMessages.BAN_ERROR_NOEMPTY.sender()
				.replace("<player>", user.getName())
				.sendTo(staff);
			return CompletableFuture.completedFuture(false);
		}
		
		// Aucune raison
		if (reason.isEmpty()) {
			ESMessages.BAN_ERROR_REASON.sender()
				.replace("<player>", user.getName())
				.sendTo(staff);
			return CompletableFuture.completedFuture(false);
		}
		
		long creation = System.currentTimeMillis();
			
		// Ban définitif
		if (time_string.equalsIgnoreCase(SanctionService.UNLIMITED)) {
			return this.commandUnlimitedBan(staff, user, creation, reason);
		}
		
		Optional<Long> time = UtilsDate.parseDuration(creation, time_string, true);
		
		// Temps incorrect
		if (!time.isPresent()) {
			EAMessages.IS_NOT_TIME.sender()
				.prefix(ESMessages.PREFIX)
				.replace("<time>", time_string)
				.sendTo(staff);
			return CompletableFuture.completedFuture(false);
		}
		
		// Ban tempotaire
		return this.commandTempBan(staff, user, creation, time.get(), reason);
	}
	
	private CompletableFuture<Boolean> commandUnlimitedBan(final CommandSource staff, final EUser user, final long creation, final String reason) {
		// Ban annulé
		if (!user.ban(creation, Optional.empty(), EChat.of(reason), staff)) {
			ESMessages.BAN_ERROR_CANCEL.sender()
				.replace("<player>", user.getName())
				.sendTo(staff);
			return CompletableFuture.completedFuture(false);
		}
		
		ESMessages.BAN_UNLIMITED_STAFF.sender()
			.replace("<reason>", reason)
			.replace("<player>", user.getName())
			.sendTo(staff);
		
		if(user instanceof EPlayer) {
			EPlayer player = (EPlayer) user;
			player.kick(ESMessages.BAN_UNLIMITED_PLAYER.getFormat().toText(
				"<staff>", staff.getName(),
				"<reason>", reason));
		}
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandTempBan(final CommandSource staff, final EUser user, final long creation, final long expiration, final String reason) {
		if (!user.ban(creation, Optional.of(expiration), EChat.of(reason), staff)) {
			ESMessages.BAN_ERROR_CANCEL.sender()
				.replace("<player>", user.getName())
				.sendTo(staff);
			return CompletableFuture.completedFuture(false);
		}
		
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<player>", EReplace.of(user.getName()));
		replaces.put("<staff>", EReplace.of(staff.getName()));
		replaces.put("<reason>", EReplace.of(reason));
		replaces.put("<duration>", EReplace.of(() -> this.plugin.getEverAPI().getManagerUtils().getDate().formatDateDiff(creation, expiration)));
		replaces.put("<time>", EReplace.of(() -> this.plugin.getEverAPI().getManagerUtils().getDate().parseTime(expiration)));
		replaces.put("<date>", EReplace.of(() -> this.plugin.getEverAPI().getManagerUtils().getDate().parseDate(expiration)));
		replaces.put("<datetime>", EReplace.of(() -> this.plugin.getEverAPI().getManagerUtils().getDate().parseDateTime(expiration)));
		
		
		ESMessages.BAN_TEMP_STAFF.sender()
			.replaceString(replaces)
			.sendTo(staff);
		
		if(user instanceof EPlayer) {
			((EPlayer) user).kick(ESMessages.BAN_TEMP_PLAYER.getFormat().toText2(replaces));
		}
		return CompletableFuture.completedFuture(true);
	}
}
