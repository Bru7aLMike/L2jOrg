/*
 * Copyright © 2019-2021 L2JOrg
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
package org.l2j.scripts.handlers.actionhandlers;

import org.l2j.gameserver.ai.CtrlIntention;
import org.l2j.gameserver.engine.geo.GeoEngine;
import org.l2j.gameserver.enums.InstanceType;
import org.l2j.gameserver.handler.IActionHandler;
import org.l2j.gameserver.model.WorldObject;
import org.l2j.gameserver.model.actor.Summon;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.events.EventDispatcher;
import org.l2j.gameserver.model.events.impl.character.player.OnPlayerSummonTalk;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.ActionFailed;
import org.l2j.gameserver.network.serverpackets.PetStatusShow;

import static org.l2j.gameserver.util.MathUtil.isInsideRadius2D;

public class SummonAction implements IActionHandler
{
	@Override
	public boolean action(Player player, WorldObject target, boolean interact)
	{
		// Aggression target lock effect
		if (player.isLockedTarget() && (player.getLockedTarget() != target))
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CHANGE_ENMITY);
			return false;
		}
		
		if ((player == ((Summon) target).getOwner()) && (player.getTarget() == target))
		{
			player.sendPacket(new PetStatusShow((Summon) target));
			player.updateNotMoveUntil();
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSummonTalk((Summon) target), target);
		}
		else if (player.getTarget() != target)
		{
			player.setTarget(target);
		}
		else if (interact)
		{
			if (target.isAutoAttackable(player))
			{
				if (GeoEngine.getInstance().canSeeTarget(player, target))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					player.onActionRequest();
				}
			}
			else
			{
				// This Action Failed packet avoids activeChar getting stuck when clicking three or more times
				player.sendPacket(ActionFailed.STATIC_PACKET);
				if (isInsideRadius2D(target, player, 150))
				{
					player.updateNotMoveUntil();
				}
				else if (GeoEngine.getInstance().canSeeTarget(player, target))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Summon;
	}
}
