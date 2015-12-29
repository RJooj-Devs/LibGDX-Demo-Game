package com.edwardszczepanski.ninecircles;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.edwardszczepanski.ninecircles.Screens.MenuScreen;
import com.edwardszczepanski.ninecircles.Screens.PlayScreen;

public class NineCircles extends Game {
    public static final int V_WIDTH = 640;
    public static final int V_HEIGHT = 480;
	public static final float PPM = 50;

	public SpriteBatch batch;
	private NineCircles game;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		game = this;
		setScreen(new MenuScreen(game));
	}

	@Override
	public void render () {
		super.render();
	}
}