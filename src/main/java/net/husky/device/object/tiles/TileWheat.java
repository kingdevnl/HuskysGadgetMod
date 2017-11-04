package net.husky.device.object.tiles;

import net.husky.device.api.utils.RenderUtil;
import net.husky.device.object.Game;
import net.husky.device.object.Game.Layer;

public class TileWheat extends Tile
{
	public TileWheat(int id, int x, int y)
	{
		super(id, x, y);
	}

	@Override
	public void render(Game game, int x, int y, Layer layer)
	{
		RenderUtil.drawRectWithTexture(game.xPosition + x * Tile.WIDTH, game.yPosition + y * Tile.HEIGHT - 6, this.x * 16, this.y * 16, WIDTH, HEIGHT + 1, 16, 16);
		RenderUtil.drawRectWithTexture(game.xPosition + x * Tile.WIDTH, game.yPosition + y * Tile.HEIGHT - 2, this.x * 16, this.y * 16, WIDTH, HEIGHT + 1, 16, 16);
	}
	
	@Override
	public boolean isFullTile()
	{
		return false;
	}
}
