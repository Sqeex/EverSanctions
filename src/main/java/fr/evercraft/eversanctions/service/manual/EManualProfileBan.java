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
package fr.evercraft.eversanctions.service.manual;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.BanTypes;

import com.google.common.base.Preconditions;

import org.spongepowered.api.util.ban.Ban.Builder;
import org.spongepowered.api.util.ban.Ban.Profile;

import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.services.sanction.manual.SanctionManualProfile;

public class EManualProfileBan extends EManualProfile implements SanctionManualProfile.Ban {
	
	public EManualProfileBan(final UUID uuid, final long date_start, final Optional<Long> expiration, final Text reason, final String source) {
		this(uuid, date_start, expiration, reason, source, Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public EManualProfileBan(final UUID uuid, final long date_start, final Optional<Long> expiration, final Text reason, final String source, 
			final Optional<Long> pardon_date, final Optional<Text> pardon_reason, final Optional<String> pardon_source) {
		super(uuid, date_start, expiration, reason, source, pardon_date, pardon_reason, pardon_source);
	}
	
	public Profile getBan(GameProfile profile) {
		Preconditions.checkNotNull(profile, "profile");
		
		Builder builder = org.spongepowered.api.util.ban.Ban.builder()
				.type(BanTypes.PROFILE)
				.profile(profile)
				.reason(this.getReason())
				.startDate(Instant.ofEpochMilli(this.getCreationDate()))
				.source(EChat.of(this.getSource()));
		
		if(this.getExpirationDate().isPresent()) {
			builder = builder.expirationDate(Instant.ofEpochMilli(this.getExpirationDate().get()));
		}
		return (Profile) builder.build();
	}
}
