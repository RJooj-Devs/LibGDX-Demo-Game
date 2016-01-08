package com.edwardszczepanski.ninecircles.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.edwardszczepanski.ninecircles.NineCircles;
import com.edwardszczepanski.ninecircles.Scenes.Hud;
import com.edwardszczepanski.ninecircles.Sprites.Enemy;
import com.edwardszczepanski.ninecircles.Sprites.Hero;
import com.edwardszczepanski.ninecircles.Tools.*;

import java.util.ArrayList;

import box2dLight.RayHandler;


public class PlayScreen implements Screen {
    private NineCircles game;

    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private Hud hud;
    private TextureAtlas atlas;

    // Lighting
    public static RayHandler rayHandler;

    // Sprites
    private Hero hero;
    private ArrayList<Enemy> enemyList;

    // Tiled map variables
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    // Box2d Variables
    private World world;
    private Box2DDebugRenderer b2dr;


    public PlayScreen(NineCircles game){
        atlas = new TextureAtlas("packers.pack");
        this.game = game;
        gamecam = new OrthographicCamera();

        gamePort = new FitViewport(NineCircles.V_WIDTH / NineCircles.PPM, NineCircles.V_HEIGHT / NineCircles.PPM, gamecam);
        hud = new Hud(game.batch);

        maploader = new TmxMapLoader();
        map = maploader.load("desert2.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / NineCircles.PPM); // This can take in a second value which is scale

        world = new World(new Vector2(0, 0), true); // Here are the gravity values. I don't want gravity
        b2dr = new Box2DDebugRenderer();

        new B2WorldCreator(this);

        rayHandler = rayHandlerGenerator();

        hero = new Hero(world, this);

        enemyList = new ArrayList<Enemy>();
        for(int i = 0; i < 8; ++i){
            enemyList.add(new Enemy(world, this, (float) (Math.random()) * 1250 / NineCircles.PPM, (float) (Math.random()) * 1250 / NineCircles.PPM));
        }

        world.setContactListener(new WorldContactListener());

    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        // Render game map
        renderer.render();

        // Render Box2DDebugLines
        //b2dr.render(world, gamecam.combined);

        // Here is the code to display the sprite
        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();

        if(!enemyList.isEmpty()){
            for(int i = 0; i < enemyList.size(); ++i){
                if (!enemyList.get(i).isDestroyed()){
                    enemyList.get(i).draw(game.batch);
                }
            }
        }

        if (hero.getBulletList() != null){
            for(int i = 0; i < hero.getBulletList().size(); ++i){
                hero.getBulletList().get(i).draw(game.batch);
            }
        }

        hero.draw(game.batch); // Here is the actual Battle Cruiser being drawn

        game.batch.end();

        // Here is the lighting system. Anything before it will be affected by the lighting
        rayHandler.setCombinedMatrix(gamecam.combined.cpy().scl(1),
                gamecam.position.x, gamecam.position.y,
                gamecam.viewportWidth * gamecam.zoom,
                gamecam.viewportHeight * gamecam.zoom);

        rayHandler.render();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);

        hud.stage.draw();
    }


    public void update(float delta) {
        handleInput(delta);

        // This is how many times you want calculations done
        world.step(1 / 60f, 6, 2);

        // This updates the hero sprite
        hero.update(delta);
        hero.updateConeLight();

        if (!enemyList.isEmpty()) {
            for (int i = 0; i < enemyList.size(); ++i){
                if(!enemyList.get(i).isDestroyed()){
                    if(enemyList.get(i).getHealth() <= 0){
                        enemyList.get(i).deleteBody();
                        enemyList.get(i).setDestroyed(true);
                        hud.addScore(100);
                        enemyList.add(new Enemy(world, this, (float)(Math.random())*1250/NineCircles.PPM, (float)(Math.random())*1250/NineCircles.PPM));
                    }
                    else{
                        enemyList.get(i).update(delta, hero.getHeroBody().getPosition().x, hero.getHeroBody().getPosition().y);
                    }
                }
            }
        }

        if (hero.getBulletList() != null){
            for(int i = 0; i < hero.getBulletList().size(); ++i){
                hero.getBulletList().get(i).update(delta);
                // This will automatically delete a bullet after 2 seconds or if it has just been destroyed
                if (System.nanoTime() - hero.getBulletList().get(i).getCreationTime() > 1 * 1000000000.0f || hero.getBulletList().get(i).getDestroyed()) {
                    hero.getBulletList().get(i).bulletLight().remove(true);
                    hero.getBulletList().get(i).deleteBody();
                    hero.getBulletList().remove(i);
                }
            }
        }

        hud.update(delta);

        gamecam.position.x = hero.getHeroBody().getPosition().x;
        gamecam.position.y = hero.getHeroBody().getPosition().y;

        gamecam.update();
        renderer.setView(gamecam);
        rayHandler.update();
    }

    public TextureAtlas getAtlas(){
        return atlas;
    }

    public void handleInput(float delta){
        if(Gdx.input.isKeyPressed(Input.Keys.W) && hero.getHeroBody().getLinearVelocity().y <= 5){
            hero.getHeroBody().applyForce(new Vector2(0, 4f), hero.getHeroBody().getWorldCenter(), true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S) && hero.getHeroBody().getLinearVelocity().y >= -5){
            hero.getHeroBody().applyForce(new Vector2(0, -4f), hero.getHeroBody().getWorldCenter(), true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D) && hero.getHeroBody().getLinearVelocity().x <= 5){
            hero.getHeroBody().applyForce(new Vector2(4f, 0), hero.getHeroBody().getWorldCenter(), true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A) && hero.getHeroBody().getLinearVelocity().x >= -5){
            hero.getHeroBody().applyForce(new Vector2(-4f, 0), hero.getHeroBody().getWorldCenter(), true);
        }

        //if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
        if(Gdx.input.justTouched()){
            hero.heroBullet(world, this, hero.getHeroBody().getPosition().x, hero.getHeroBody().getPosition().y, hero.getRotation(), hero.getHeroRadius() / NineCircles.PPM);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MenuScreen(game));
        }
    }

    public RayHandler rayHandlerGenerator(){
        RayHandler localRay = new RayHandler(world);
        RayHandler.useDiffuseLight(true);
        localRay.setAmbientLight(0.1f, 0.1f, 0.1f, 0.2f);
        localRay.setShadows(true);
        return localRay;
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    public TiledMap getMap(){
        return map;
    }

    public World getWorld(){
        return world;
    }

    @Override
    public void dispose() {
        hud.dispose();
        map.dispose();
        renderer.dispose();
        b2dr.dispose();
        world.dispose();
        rayHandler.dispose();
        hero.getHeroCone().dispose();
        hero.getHeroPoint().dispose();
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
    public void show() {

    }
}
