package com.edwardszczepanski.ninecircles.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.edwardszczepanski.ninecircles.NineCircles;
import com.edwardszczepanski.ninecircles.Tween.SpriteAccessor;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;


public class SplashScreen implements Screen{
    private NineCircles game;
    private Sprite splash;
    private TweenManager tweenManager;

    public SplashScreen(NineCircles game){
        this.game = game;
    }

    public void handleInput(float delta){
        if (Gdx.input.isTouched()){
            game.setScreen(new PlayScreen(game));

        }
    }

    public void update(float delta){
        handleInput(delta);
    }

    @Override
    public void show() {

        tweenManager = new TweenManager();
        Tween.registerAccessor(Sprite.class, new SpriteAccessor());

        Texture stamps = new Texture(Gdx.files.internal("stamps.png"));
        splash = new Sprite(stamps);
        splash.setCenter(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);

        Tween.set(splash, SpriteAccessor.ALPHA).target(0).start(tweenManager);
        Tween.to(splash, SpriteAccessor.ALPHA, 2).target(1).repeatYoyo(1, 2).start(tweenManager);
    }

    @Override
    public void render(float delta) {
        update(delta);
        tweenManager.update(delta);

        Gdx.gl.glClearColor(1, 1, 1, 1); // Color then opacity
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        splash.draw(game.batch);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        game.batch.dispose();
        splash.getTexture().dispose();
    }
}
