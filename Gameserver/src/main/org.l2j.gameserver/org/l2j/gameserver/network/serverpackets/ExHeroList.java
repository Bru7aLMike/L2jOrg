/*
 * Copyright © 2019-2020 L2JOrg
 *
 * This file is part of the L2JOrg project.
 *
 * L2JOrg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2JOrg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.network.serverpackets;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.entity.Hero;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;

import java.util.Map;

/**
 * @author -Wooden-, KenM, godson
 */
public class ExHeroList extends ServerPacket {
    private final Map<Integer, StatsSet> _heroList;

    public ExHeroList() {
        _heroList = Hero.getInstance().getHeroes();
    }

    @Override
    public void writeImpl(GameClient client, WritableBuffer buffer) {
        writeId(ServerExPacketId.EX_HERO_LIST, buffer );

        buffer.writeInt(_heroList.size());
        for (Integer heroId : _heroList.keySet()) {
            final StatsSet hero = _heroList.get(heroId);
            buffer.writeString(hero.getString(Hero.CHAR_NAME));
            buffer.writeInt(hero.getInt(Hero.CLASS_ID));
            buffer.writeString(hero.getString(Hero.CLAN_NAME, ""));
            buffer.writeInt(hero.getInt(Hero.CLAN_CREST, 0));
            buffer.writeString(hero.getString(Hero.ALLY_NAME, ""));
            buffer.writeInt(hero.getInt(Hero.ALLY_CREST, 0));
            buffer.writeInt(hero.getInt(Hero.COUNT));
            buffer.writeInt(0x00);
        }
    }


}
