package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final Drop game;
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 480;
    private final int BUCKET_WIDTH = 64;
    private final int BUCKET_HEIGHT = 64;

    private final Vector3 touchPos = new Vector3();

    Texture dropImage;
    Texture bucketImage;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    long lastDropTime;
    State state = State.RUN;

    public GameScreen(final Drop game) {
        this.game = game;

        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        //dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        bucket = new Rectangle();
        bucket.x = (int)(SCREEN_WIDTH / 2) - (int) (BUCKET_WIDTH / 2);
        bucket.y = SCREEN_HEIGHT - 460;
        bucket.width = BUCKET_WIDTH;
        bucket.height = BUCKET_HEIGHT;

        raindrops = new Array<>();
        spawnRainDrop();
    }

    @Override
    public void show() {
        // start the playback of the background music when the screen is shown
        rainMusic.play();
    }


    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        //begin a new batch and draw bucket and all drops
        game.batch.begin();
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops)
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        game.batch.end();

        //user input
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - (int)(BUCKET_WIDTH / 2);
        }
        if (Gdx.input.isKeyPressed((Input.Keys.LEFT)) || Gdx.input.isKeyPressed((Input.Keys.A)))
            bucket.x -= 500 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed((Input.Keys.RIGHT)) || Gdx.input.isKeyPressed((Input.Keys.D)))
            bucket.x += 500 * Gdx.graphics.getDeltaTime();
        //keep bucket within screen bounds
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > SCREEN_WIDTH - BUCKET_WIDTH) bucket.x = SCREEN_WIDTH - BUCKET_WIDTH;

        //spawn a new rain drop every so often
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRainDrop();

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we increase the
        // value our drops counter and add a sound effect.
        for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + BUCKET_WIDTH < 0) iter.remove();
            if (raindrop.overlaps(bucket)) {
                //dropSound.play();
                iter.remove();
            }
        }
    }

    private void spawnRainDrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, SCREEN_WIDTH - BUCKET_WIDTH);
        raindrop.y = SCREEN_HEIGHT;
        raindrop.width = BUCKET_WIDTH;
        raindrop.height = BUCKET_HEIGHT;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        //dropSound.dispose();
        rainMusic.dispose();
    }

    public void pause() {
        this.state = State.PAUSE;
    }

    public void resume() {
        this.state = State.RESUME;
    }

    @Override
    public void hide() {

    }

}
