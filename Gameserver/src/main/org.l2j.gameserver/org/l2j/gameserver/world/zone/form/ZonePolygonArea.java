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
package org.l2j.gameserver.world.zone.form;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.engine.geo.GeoEngine;
import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.serverpackets.ExServerPrimitive;
import org.l2j.gameserver.world.zone.ZoneArea;

import java.awt.*;

/**
 * A not so primitive npoly zone
 *
 * @author durgus
 */
public class ZonePolygonArea extends ZoneArea {
    private final Polygon polygon;
    private final int minZ;
    private final int maxZ;

    public ZonePolygonArea(int[] x, int[] y, int minZ, int maxZ) {
        polygon = new Polygon(x, y, x.length);

        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);
    }

    @Override
    public boolean isInside(int x, int y, int z) {
        return polygon.contains(x, y) &&  (z >= minZ) && (z <= maxZ);
    }

    @Override
    public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
        return polygon.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.abs(ax2 - ax1), Math.abs(ay2 - ay1));
    }

    @Override
    public double distanceFrom(int x, int y) {
        final int[] _x = polygon.xpoints;
        final int[] _y = polygon.ypoints;
        double test;
        double shortestDist = Math.pow(_x[0] - x, 2) + Math.pow(_y[0] - y, 2);

        for (int i = 1; i < polygon.npoints; i++) {
            test = Math.pow(_x[i] - x, 2) + Math.pow(_y[i] - y, 2);
            if (test < shortestDist) {
                shortestDist = test;
            }
        }

        return Math.sqrt(shortestDist);
    }

    @Override
    public int getLowZ() {
        return minZ;
    }

    @Override
    public int getHighZ() {
        return maxZ;
    }

    @Override
    public void visualize(Player player, String zoneName) {
        var primitive = new ExServerPrimitive(zoneName, polygon.xpoints[0], polygon.ypoints[0], minZ);
        for(var i = 0; i < polygon.npoints; i++) {
            primitive.addLine(zoneName + i, Color.GREEN, false, polygon.xpoints[i], polygon.ypoints[i], minZ, polygon.xpoints[i], polygon.ypoints[i], maxZ);
            primitive.addLine(Color.GREEN, polygon.xpoints[i], polygon.ypoints[i], minZ, polygon.xpoints[(i+1) % polygon.npoints], polygon.ypoints[(i+1) % polygon.npoints], minZ);
            primitive.addLine(Color.GREEN, polygon.xpoints[i], polygon.ypoints[i], maxZ, polygon.xpoints[(i+1) % polygon.npoints], polygon.ypoints[(i+1) % polygon.npoints], maxZ);
        }
        player.sendPacket(primitive);
    }

    @Override
    public Location getRandomPoint() {
        final int _minX = polygon.getBounds().x;
        final int _maxX = polygon.getBounds().x + polygon.getBounds().width;
        final int _minY = polygon.getBounds().y;
        final int _maxY = polygon.getBounds().y + polygon.getBounds().height;

        int x = Rnd.get(_minX, _maxX);
        int y = Rnd.get(_minY, _maxY);

        int antiBlocker = 0;
        while (!polygon.contains(x, y) && (antiBlocker++ < 1000)) {
            x = Rnd.get(_minX, _maxX);
            y = Rnd.get(_minY, _maxY);
        }

        return new Location(x, y, GeoEngine.getInstance().getHeight(x, y, minZ));
    }

    public int[] getX() {
        return polygon.xpoints;
    }

    public int[] getY() {
        return polygon.ypoints;
    }
}
