package com.alexei.spaceshooter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.HashMap;

import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SpaceShooter extends ApplicationAdapter {
	static float FPS = 60;
	ShapeRenderer shapeRenderer;

	public static ShapeDrawer drawer;
	private final int BUFFER_ZONE = 30; // additional area outside the screen perimeter which is part of the game.
	// It is needed for distinguishing between when an enemy is spawned and when the enemy leaves the screen area.

	// input processing
	private TouchData touch = new TouchData();
	private boolean screenTouched = false;
	private Vector2 touchDisplacement = new Vector2(0,0);
	private static final float SLOW_MO_GAME_SPEED_LIMIT = 0.2f;
	private static final int SLOW_DOWN_PERIOD = 300; // ms
	private static  float gameSpeedDelta = (1-SLOW_MO_GAME_SPEED_LIMIT) / (FPS * (SLOW_DOWN_PERIOD/1000f));
	private float gameSpeed = 1f;
	FPSLogger fpsLoggger = new FPSLogger();

	// ship and enemies
	Ship ship;
	private static ArrayList<Unit> enemies = new ArrayList<Unit>(); // an array of enemy objects that are checked for collision with bullets and ship

	// particle emitters
	ArrayList<ParticleEmitter> particleEmitters = new ArrayList<ParticleEmitter>(); // an array of effect objects

	// ground scroll speed
	public static final float GROUND_SCROLL_SPEED = 50;

	// star fields
	Starfield starfield;
	static int STAR_COUNT = 100;
	static float STAR_SCROLL_ANGLE = 270; // down
	static float STAR_SCROLL_SPEED = 50; // units are dpi per second
	static float MIN_STAR_SIZE = 2;
	static float MAX_STAR_SIZE = 3;

	Starfield starfield_2;
	static int STAR_COUNT_2 = 10;
	static float STAR_SCROLL_ANGLE_2 = 270; // down
	static float STAR_SCROLL_SPEED_2 = 100; // units are dpi per second
	static float MIN_STAR_SIZE_2 = 8;
	static float MAX_STAR_SIZE_2 = 13;

	// enemies
	Timer enemySpawnTimer;

	// sound
	private static HashMap<SoundName, Sound> sounds;
	private static HashMap<SoundType, ArrayList<SoundName>> soundTypes;
	private static Music music;
	private static HashMap<SoundName, Music> musicArray;

	// textures
	private static Texture texture_ship;
	private static Texture texture_enemy1;
	private static Texture texture_enemy2;

	// batch
	public static SpriteBatch batch;

	// fonts
	private BitmapFont font;

	// items
	public static ArrayList<Item> items;

	// ScoreTracker
	ScoreTracker scoreTracker;

	Texture textureSolid;

	public SpaceShooter() {
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void create () {
		// Creating a color filling (but textures would work the same way)
		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
		pix.fill();
		textureSolid = new Texture(pix);
		TextureRegion textureRegion = new TextureRegion(textureSolid);
//		PolygonRegion polyReg = new PolygonRegion(textureRegion,
//				new float[] {      // Four vertices
//						0, 0,            // Vertex 0         3--2
//						100, 0,          // Vertex 1         | /|
//						100, 100,        // Vertex 2         |/ |
//						0, 100           // Vertex 3         0--1
//				}, new short[] {
//				0, 1, 2,         // Two triangles using vertex indices.
//				0, 2, 3          // Take care of the counter-clockwise direction.
//		});
//		PolygonSprite poly = new PolygonSprite(polyReg);
//		poly.setOrigin(100, 100);

		// init rendering objects and important game elements
		touch.handle();
		batch = new SpriteBatch();

		shapeRenderer = new ShapeRenderer();
		drawer = new ShapeDrawer(batch, textureRegion);
		enemySpawnTimer = new Timer(800, 0);
		scoreTracker = new ScoreTracker();

		initializeVisualElements();

		// set initial game mode
		GameModeManager.setMode(GameModeManager.GameMode.Menu);

		// load sound
		loadSounds();

		// play menu music
		playMusic(SoundName.Ut);

		// set the input processor
		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean touchDown(int x, int y, int pointer, int button) {
				touch.set(x, y, false);
				screenTouched = true;
				// remember the distance between the touch location and the position of the ship
				touchDisplacement.set(x - ship.getX(), -y - ship.getY());
				return true; // return true to indicate the event was handled
			}

			@Override
			public boolean touchUp(int x, int y, int pointer, int button) {
				touch.set(x, y, true);
				screenTouched = false;
				return true; // return true to indicate the event was handled
			}

			@Override
			public boolean touchDragged(int x, int y, int pointer) {
				// if screenTouched, update the position of the ship to new position
				// if new position is out of bounds, do not update.
				// update x and y separately
				if (screenTouched) {
					if (x - touchDisplacement.x >= 0 && x - touchDisplacement.x <= Gdx.graphics.getWidth() - ship.getWidth())
						ship.setX(x - touchDisplacement.x);
					else touchDisplacement.set(x - ship.getX(), touchDisplacement.y);
					if (-y - touchDisplacement.y >= 0 && -y - touchDisplacement.y <= Gdx.graphics.getHeight() - ship.getHeight())
						ship.setY(-y - touchDisplacement.y);
					else touchDisplacement.set(touchDisplacement.x, -y - ship.getY());
				}
				return true; // return true to indicate the event was handled
			}
		});
	}

	/***
	 * Initialize all the visual game elements here and add them to the GameRenderer
	 */
	public void initializeVisualElements() {

		// load textures
		texture_ship = new Texture(Gdx.files.internal("ship.png"));
		texture_enemy1 = new Texture(Gdx.files.internal("enemy1.png"));
		texture_enemy2 = new Texture(Gdx.files.internal("enemy2.png"));

		starfield = new Starfield(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), STAR_SCROLL_ANGLE, STAR_SCROLL_SPEED, STAR_COUNT, MIN_STAR_SIZE, MAX_STAR_SIZE);
		starfield_2 = new Starfield(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), STAR_SCROLL_ANGLE_2, STAR_SCROLL_SPEED_2, STAR_COUNT_2, MIN_STAR_SIZE_2, MAX_STAR_SIZE_2);
		ship = new Ship();

		// load fonts
		BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();

		float menuFontScale = 2f;
		font = new BitmapFont();
		font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		font.getData().setScale(menuFontScale, menuFontScale);

		// items
		items = new ArrayList<Item>();
	}

	@Override
	public void render () {
		fpsLoggger.log();

		float deltaTime = Gdx.graphics.getDeltaTime() * 1000; // ms

		// set open gl renderer settings
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		switch (GameModeManager.getMode()) {
			case Menu: {
				// place ship in bottom center of screen
				ship.setX((screenWidth - ship.getWidth())/2f);
				ship.setY((ship.getHeight() * 2));

				// update star field
				starfield.update(deltaTime);
				starfield_2.update(deltaTime);

				// render star fields
				starfield.render(shapeRenderer, batch);
				starfield_2.render(shapeRenderer, batch);

				// TODO: if play button is pressed, start new game, set game mode to 'playing'
				// draw a big green circle in the middle of the screen
				shapeRenderer.setColor(Color.GREEN);
				shapeRenderer.circle(screenWidth / 2f, screenHeight / 2f, screenWidth / 4f);

				// handle touch event in Menu mode
				if (touch.isTouchUpRaised()) {
					touch.handleTouchUp();
					// TODO: load new game
					ship.setLife(ship.getMaxLife());
					ship.setX(screenWidth / 2f);
					ship.setY(screenWidth / 8f);
					Weapon.getProjectiles().clear();
					enemies.clear();

					GameModeManager.setMode(GameModeManager.GameMode.Playing);
					playSound(SoundName.Go);
					scoreTracker.reset();
					//stopAllMusic();
					//playMusic(SoundName.Ut);
				}

				break;
			}
			case Died: {

				// remove items
				items.clear();

				// update star field
				starfield.update(deltaTime);
				starfield_2.update(deltaTime);

				// render star fields
				starfield.render(shapeRenderer, batch);
				starfield_2.render(shapeRenderer, batch);

				// update Visual.visuals
				for (int i=Visual.getVisualEffects().size()-1;i>=0;i--) {
					Visual v = Visual.getVisualEffects().get(i);
					v.update(deltaTime);
					if (v.isDead()) Visual.getVisualEffects().remove(i);
				}

				// TODO: show points, kill count, stars collected, achievements, etc
				shapeRenderer.setColor(Color.RED);
				shapeRenderer.circle(screenWidth / 2f, screenHeight / 2f, screenWidth / 4f);

				// TODO: on back button press, go to 'menu' mode
				if (touch.isTouchUpRaised()) {
					touch.handleTouchUp();

					// TODO: load menu
					GameModeManager.setMode(GameModeManager.GameMode.Menu);

					stopAllMusic();
					playMusic(SoundName.Ut);
				}

				// render scores
				scoreTracker.render(shapeRenderer,batch,font);

				break;
			}
			case Playing: {

			// update delta time based on whether the player is touching the screen or not
			if (screenTouched) {
				// update game speed timer (speed up the game when finger is back in contact with screen)
				if (gameSpeed < 1) {
					gameSpeed += gameSpeedDelta;
					if (gameSpeed > 1) gameSpeed = 1;
				}
			}else {
				// update game speed timer (slow down the game when finger is released from screen)
				if (gameSpeed > SLOW_MO_GAME_SPEED_LIMIT) {
					gameSpeed -= gameSpeedDelta;
					if (gameSpeed < SLOW_MO_GAME_SPEED_LIMIT) gameSpeed = SLOW_MO_GAME_SPEED_LIMIT;
				}
			}

			deltaTime = deltaTime * gameSpeed;

			// UPDATE - Playing mode

			// check if ship is dead
			if (ship.isDead()) {
				// TODO: end current game
				touch.handle();
				GameModeManager.setMode(GameModeManager.GameMode.Died);

				stopSound(SoundName.Alarm);
				stopAllMusic();
				break;
			}

			// randomize enemy units by spawning them every so often
			enemySpawnTimer.update(deltaTime);
			if (enemySpawnTimer.isTimerElapsed()) {
				Unit enemy = new EnemyShipA();
				enemy.setX(MathUtils.random(0, screenWidth - enemy.getWidth()));
				enemy.setY(screenHeight + 1);
				enemies.add(enemy);
				//enemy.setTextureRegion(texture_enemy1);

				// spawn tank
				if (MathUtils.random() <= 0.2) { // spawn tank with probability
					enemy = new EnemyShipB();
					enemy.setX(MathUtils.random(0, screenWidth - enemy.getWidth()));
					enemy.setY(screenHeight + 1);
					enemies.add(enemy);
					//enemy.setTextureRegion(texture_enemy2);
				}
			}
			// update score tracker
			scoreTracker.update(deltaTime);

			// update star field
			starfield.update(deltaTime);
			starfield_2.update(deltaTime);

			// update Visual.visuals
			for (int i=Visual.getVisualEffects().size()-1;i>=0;i--) {
				Visual v = Visual.getVisualEffects().get(i);
				v.update(deltaTime);
				if (v.isDead()) Visual.getVisualEffects().remove(i);
			}

			// update projectiles
			for (Projectile p : Weapon.getProjectiles()) {
				p.update(deltaTime);
			}

			// update enemy units
			for(Unit e : enemies) {
				e.update(deltaTime);
			}

			// update ship
			ship.update(deltaTime);

			// update items
			for (int i = items.size()-1;i>=0;i--) {
				Item item = items.get(i);
				item.update(deltaTime);
				if (item.isDead()) { items.remove(item); continue; }

				if (item.isColliding(ship) && !item.isPickedUp()) {
					item.pickUp();
					scoreTracker.collectStar();
				}

				// TODO: make ship's magnet attract stars when ship is within range of a star
				if (item.squareDistanceToCenter(ship) <= 90000) {
					if (item.isMagnetizing()) {
						item.setDirection(ship);
					}else {
						item.magnetize(ship);
					}
				}
				else if (item.squareDistanceToCenter(ship) > 90000 && item.isMagnetizing()) {
					item.unmagnetize();
				}

			}

			doCollisionDetection();

			// RENDER - Playing mode

			// render star fields
			starfield.render(shapeRenderer, batch);
			starfield_2.render(shapeRenderer, batch);

			// render projectiles
			for (Projectile p : Weapon.getProjectiles()) {
				p.render(shapeRenderer, batch);
			}

			// render enemies
			for (Unit e : enemies) {
				e.render(shapeRenderer, batch);
			}

			// render items
			// items are currently only dropped stars which we are rendered
			// using the ShapeDrawer instead of ShapeRenderer
			shapeRenderer.end();
			batch.begin();
			Matrix4 curMatrix = batch.getTransformMatrix().cpy();
			for (Item i: items) {
				i.render(shapeRenderer, batch);
			}
			batch.end();
			batch.setTransformMatrix(curMatrix);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			// draw player's life bar
			float lifePercentage = ship.getLife() / ship.getMaxLife();
			shapeRenderer.setColor(Color.GRAY);
			shapeRenderer.line(ship.getCenterX(), ship.getCenterY(), ship.getRight() + 20, ship.getTop());
			shapeRenderer.setColor(Color.DARK_GRAY);
			shapeRenderer.rect(ship.getRight() + 20, ship.getTop(), 100, 8);
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.rect(ship.getRight() + 20, ship.getTop(), (lifePercentage * 100), 8);

			// render player ship
			ship.render(shapeRenderer, batch);

//			shapeRenderer.end();
//
//			// draw percentage life - since we're drawing using a batch, we need to end the shape renderer and begin a batch
//			batch.begin(); // begin batch
//			batch.getTransformMatrix().setToTranslationAndScaling(ship.getRight() + 20 + lifePercentage * 100 + 5, ship.getTop() + font.getLineHeight() + 10,0,1.5f,1.5f,1);
//			font.setColor(Color.LIGHT_GRAY);
//			//font.draw(batch, ((int) (lifePercentage * 100)) + "%", ship.getRight() + 20 + lifePercentage * 100 + 5, ship.getTop() + font.getLineHeight() + 10);
//			font.draw(batch, ((int) (lifePercentage * 100)) + "%", 0,0);
//			batch.end(); // end the batch
//
//			// set gl settings again
//			Gdx.gl.glEnable(GL20.GL_BLEND);
//			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//
//			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // begin shape renderer again

			// draw semi-opaque rectangle over the entire screen when player lifts finger off screen
//			if (!screenTouched) {
//				Color copy = Color.BLACK.cpy();
//				float alpha = 0.5f * (1-(gameSpeed-SLOW_MO_GAME_SPEED_LIMIT)/(1-SLOW_MO_GAME_SPEED_LIMIT));
//				copy.a = alpha;
//				shapeRenderer.setColor(copy);
//				shapeRenderer.rect(0,0,screenWidth,screenHeight);
//			}

			// render scores
			scoreTracker.render(shapeRenderer, batch, font);

			// Draw playing menu
			// Lifting the finger off the screen while in Playing mode shows a menu
			if (!screenTouched) {
				shapeRenderer.end();

				Color color = new Color(65/255f,1,0, 1);
				batch.getTransformMatrix().idt();
				batch.begin();
				float size = 100f;
				float margin = 100f;
				float buttonX = screenWidth - size - margin;
				float buttonY = margin;
				batch.setColor(color);
				drawer.rectangle(buttonX, buttonY, size, size, 5f, JoinType.SMOOTH);

				float barWidth = 10f;
				float barScale = 0.5f;
				float barHeight = size * barScale;
				float barYOffset = size * (1-barScale)/2;
				float barXOffset = barWidth / 2;
				drawer.filledRectangle(buttonX + size / 3 - barXOffset, buttonY + barYOffset, barWidth, barHeight, color);
				drawer.filledRectangle(buttonX + size * 2 / 3 - barXOffset, buttonY + barYOffset, barWidth, barHeight, color);

				float FONT_SCALE = 1f;
				float fontPad = 5f;

				// score
				float fontX = buttonX;
				float fontY = buttonY - 5;

				font.setColor(Color.GREEN);
				font.draw(batch, "Pause", fontX, fontY, size - 2 * fontPad, Align.left, false);

				batch.end();

				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			}

			break;
			}
		default: { System.out.println("error in switch statement, default mode selected"); }
		}

		// render Visual.visuals
		for (Visual v : Visual.getVisualEffects()) {
			v.render(shapeRenderer, batch);
		}

		shapeRenderer.end();
	}

	public void doCollisionDetection() {
		// remove projectiles that are out of bounds
		for (int i = Weapon.getProjectiles().size() - 1; i >= 0; i--) {
			Projectile p = Weapon.getProjectiles().get(i);
			if (p.getX() < -p.getWidth() - BUFFER_ZONE || p.getX() > Gdx.graphics.getWidth() + BUFFER_ZONE ||
					p.getY() < -p.getHeight() - BUFFER_ZONE || p.getY() > Gdx.graphics.getHeight() + BUFFER_ZONE) {
				// remove bullet
				Weapon.getProjectiles().remove(i);
			}
		}

		// remove enemies that are out of bounds
		for (int i = enemies.size() - 1; i >= 0; i--) {
			Unit e = enemies.get(i);
			if (e.getX() < -e.getWidth() - BUFFER_ZONE || e.getX() > Gdx.graphics.getWidth() + BUFFER_ZONE ||
					e.getY() < -e.getHeight() - BUFFER_ZONE || e.getY() > Gdx.graphics.getHeight() + BUFFER_ZONE) {
				// remove
				enemies.remove(i);
			}
		}

		// check for collisions between the player's projectiles and enemy units
		for (int i = enemies.size() - 1; i >= 0; i--) {
			Unit e = enemies.get(i);

			for (int j = Weapon.getProjectiles().size() - 1; j >= 0; j--) {
				Projectile p = Weapon.getProjectiles().get(j);
				if (p.isShipProjectile()) { // check collision between player's projectiles and enemy units
					if (p.getX() + p.getWidth() > e.getX() && p.getX() < e.getX() + e.getWidth() && p.getY() + p.getHeight() > e.getY() && p.getY() < e.getY() + e.getHeight()) {
						// a hit is detected:

						// projectile does damage to enemy
						p.doDamage(e);

						// remove projectile from array
						Weapon.getProjectiles().remove(j);

						// damage sound
						//playSound(e.getDamageSound());

						// check if enemy is dead
						if (e.isDead()) {
							// death sound
							//playSound(e.getDeathSound());
							scoreTracker.addEnemyKilled();
							// remove enemy unit from array
							enemies.remove(i);
							break;
						}
					}
				}
			}
		}

		// check collision between enemy's projectiles and player's ship
		for (int j = Weapon.getProjectiles().size() - 1; j >= 0; j--) {
			Projectile p = Weapon.getProjectiles().get(j);
			if (!p.isShipProjectile()) {
				if (p.isColliding(ship)) {
					p.doDamage(ship);
					Weapon.getProjectiles().remove(p);
					// TODO: check if player dead here

//					if (ship.isDead()) {
//						stopSound(SoundName.Alarm);
//					}
//
//					// play alarm when health is below critical
//					if (!ship.isCriticalLifeActivated() && ship.isCriticalHealth()) {
//						ship.setIsCriticalLifeActivated(true);
//						playSound(SoundName.Warning);
//						playSound(SoundName.Alarm, true);
//					}
//					else if (ship.isCriticalLifeActivated() && !ship.isCriticalHealth()) {
//						ship.setIsCriticalLifeActivated(false);
//						stopSound(SoundName.Alarm);
//					}
				}
			}
		}

		// check for collisions between the player's ship and enemy ships
		for (int i = enemies.size() - 1; i >= 0; i--) {
			Unit e = enemies.get(i);
			if (e.isColliding(ship)) {
				e.receiveDamage(ship);
				ship.receiveDamage(e);

				if (e.isDead())	{ // check whether the enemy has died from collision with player
					enemies.remove(e);
					scoreTracker.addEnemyKilled();
				}
			}
		}
	}

	/**
	 * A function for getting a target enemy unit that the player's rocket missile could shoot.
	 * This function is called by the player's rocket missile weapon each time it fires.
	 * @return
	 */
	public static Visual acquireTarget() {
		if (enemies.isEmpty()) return null;
		return enemies.get(MathUtils.random(0, enemies.size()-1)); // for now return a random enemy
	}

	public static boolean isTargetDead(Visual visual) {
		return enemies.indexOf(visual)==-1;
	}

	public void loadSounds() {
		
		String folder = "sounds";
				
		sounds = new HashMap<SoundName, Sound>(); // all sounds

		sounds.put(SoundName.Alarm, Gdx.audio.newSound(Gdx.files.internal(folder + "\\alarm.mp3")));
		sounds.put(SoundName.LaserShoot2, Gdx.audio.newSound(Gdx.files.internal(folder + "\\laser_shoot2.mp3")));
		sounds.put(SoundName.Hit7, Gdx.audio.newSound(Gdx.files.internal(folder + "\\hit7.mp3")));
		sounds.put(SoundName.Explode2, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode2_2.mp3")));
		sounds.put(SoundName.Explode3, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode3.mp3")));
		sounds.put(SoundName.Explode4, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode4_4.mp3")));
		sounds.put(SoundName.Explode5, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode5_5.mp3")));
		sounds.put(SoundName.Explode8, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode8.mp3")));

		sounds.put(SoundName.LaserShoot, Gdx.audio.newSound(Gdx.files.internal(folder + "\\laser_shoot.mp3")));
		sounds.put(SoundName.Rocket, Gdx.audio.newSound(Gdx.files.internal(folder + "\\rocket.mp3")));
		sounds.put(SoundName.EndGame, Gdx.audio.newSound(Gdx.files.internal(folder + "\\end_game.mp3")));
		sounds.put(SoundName.Laser, Gdx.audio.newSound(Gdx.files.internal(folder + "\\laser.mp3")));
		sounds.put(SoundName.Explode, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode.mp3")));
		sounds.put(SoundName.GetDamage, Gdx.audio.newSound(Gdx.files.internal(folder + "\\get_damage.mp3")));
		sounds.put(SoundName.Tick, Gdx.audio.newSound(Gdx.files.internal(folder + "\\tick.mp3")));
		sounds.put(SoundName.Ready, Gdx.audio.newSound(Gdx.files.internal(folder + "\\ready.mp3")));
		sounds.put(SoundName.Go, Gdx.audio.newSound(Gdx.files.internal(folder + "\\go.mp3")));
		sounds.put(SoundName.Warning, Gdx.audio.newSound(Gdx.files.internal(folder + "\\warning.mp3")));

	/*
		sounds.put(SoundName.Explode3, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode3.mp3")));
		sounds.put(SoundName.Explode4, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode4.mp3")));
		sounds.put(SoundName.Explode5, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode5.mp3")));
		sounds.put(SoundName.Explode6, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode6.mp3")));
		sounds.put(SoundName.Explode7, Gdx.audio.newSound(Gdx.files.internal(folder + "\\explode7.mp3")));
*/

		soundTypes = new HashMap<SoundType, ArrayList<SoundName>>(); // sounds by category

		ArrayList<SoundName> soundsHit = new ArrayList<SoundName>();
		ArrayList<SoundName> soundsExplode = new ArrayList<SoundName>();

		soundTypes.put(SoundType.Hit, soundsHit);
		soundTypes.put(SoundType.Explode, soundsExplode);

		// hits
		/*soundsHit.add(SoundName.Hit);
		soundsHit.add(SoundName.Hit2);
		soundsHit.add(SoundName.Hit3);
		soundsHit.add(SoundName.Hit4);*/
		soundsHit.add(SoundName.Hit7);

		// explosions
		//soundsExplode.add(SoundName.Explode);
		soundsExplode.add(SoundName.Explode2);
		/*soundsExplode.add(SoundName.Explode3);
		soundsExplode.add(SoundName.Explode4);
		soundsExplode.add(SoundName.Explode5);
		soundsExplode.add(SoundName.Explode6);
		soundsExplode.add(SoundName.Explode7);*/

		// music
		String musicFolder = "music";
		musicArray = new HashMap<SoundName, Music>();

		music = Gdx.audio.newMusic(Gdx.files.internal(musicFolder + "\\ut.mp3"));
		music.setLooping(true);
		music.setVolume(0.4f);
		musicArray.put(SoundName.Ut, music);

		music = Gdx.audio.newMusic(Gdx.files.internal(musicFolder + "\\action_music.mp3"));
		music.setLooping(true);
		music.setVolume(1f);
		musicArray.put(SoundName.ActionMusic, music);

	}

	/**
	 * Given a sound name, finds it in the general sounds array and plays it.
	 * @param soundName
	 */
	public static void playSound(SoundName soundName) {
		playSound(soundName, false);
	}

	public static void playSound(SoundName soundName, boolean loop) {
		if (soundName != null && sounds != null && sounds.containsKey(soundName)) {
			Sound s = sounds.get(soundName);
			if (loop) {
				s.loop(1f);
			} else {
				s.play(1f);
			}
		}
	}

	public static void stopSound(SoundName soundName) {
		if (soundName != null && sounds != null && sounds.containsKey(soundName)) {
			Sound s = sounds.get(soundName);
			s.stop();
		}
	}

	/**
	 * Plays a sound selected at random from a collection of sounds of the same type.
	 * For example explosions are sounds of the same type. Each time an enemy unit explodes
	 * we want the user to hear an explosion sound, but not the same sound over and over.
	 * This method allows to easily specify variety.
	 * @param soundType The SoundType to select a random sound from.
	 */
	public static void playSoundType(SoundType soundType) {
		SoundName soundName = getRandomSoundName(soundType);
		playSound(soundName);
	}

	/**
	 * In addition to residing in the general 'sounds' array, when appropriate, a sound is also added to a corresponding
	 * sound-type array. SoundType enum is used as a key to retrieve the corresponding array list of SoundNames from a the 'soundTypes' hash map.
	 * A random sound name is then selected from the sound type array list and returned.
	 * @param soundType The type of sound to selects a random sound name from.
	 * @return
	 */
	public static SoundName getRandomSoundName(SoundType soundType) {
		if (soundTypes != null && soundTypes.containsKey(soundType)) {
			ArrayList<SoundName> soundNames = soundTypes.get(soundType);
			if (soundNames != null && soundNames.size() > 0) return soundTypes.get(soundType).get(MathUtils.random(0, soundNames.size()-1));
		}

		return null;
	}

	public static void stopAllSounds() {
		if (sounds != null) {
			for(Sound s : sounds.values()) {
				s.stop();
			}
		}
	}

	public static void playMusic(SoundName soundName) {
		if (soundName != null && musicArray != null && musicArray.containsKey(soundName)) {
			Music m = musicArray.get(soundName);
			m.play();
		}

	}

	public static void stopMusic(SoundName soundName) {
		if (soundName != null && musicArray != null && musicArray.containsKey(soundName)) {
			Music m = musicArray.get(soundName);
			m.stop();
		}
	}

	public static void stopAllMusic() {
		if (musicArray != null) {
			for(Music m:musicArray.values()) {
				m.stop();
			}
		}
	}
}
