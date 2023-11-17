package com.alexei.spaceshooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Alex on 17/06/2015.
 *
 * Represents the spaceship in the game
 */
public class Ship extends Unit {
    private static final float UNIT_POSITION_X = 0;
    private static final float UNIT_POSITION_Y = 0;
    private static final float UNIT_WIDTH = 80;
    private static final float UNIT_HEIGHT = 80;
    private static final float MAX_LIFE = 5f;
    private static final Color COLOR = Color.MAROON;
    private static final SoundName DEATH_SOUND = SoundName.EndGame;
    private static final SoundName DAMAGE_SOUND = SoundName.GetDamage;

    private boolean isCriticalLifeActivated = false;

    public static Vector2 position = new Vector2();
    public static Vector2 size= new Vector2();
    public static Vector2 center = new Vector2();

    public static Visual ship; // sloppy hack. assigning the only player ship object instance to it's own class variable.

    // private SpaceShooter game;

    public Ship() {
        super(UNIT_POSITION_X, UNIT_POSITION_Y, UNIT_WIDTH, UNIT_HEIGHT);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.setColor(COLOR);

        super.clearSounds();
        super.addDeathSound(DEATH_SOUND);
        super.addDamageSound(DAMAGE_SOUND);

        // update static vars
        position.set(UNIT_POSITION_X, UNIT_POSITION_Y);
        size.set(UNIT_WIDTH, UNIT_HEIGHT);
        center.set(UNIT_POSITION_X + UNIT_WIDTH / 2, UNIT_POSITION_Y + UNIT_HEIGHT/2);

        // add weapons
        super.addWeapon(new WeaponShipLaser(this));
        super.addWeapon(new WeaponShipRocket(this));

        // set ship reference
        ship = this; // sloppy hack
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        super.render(sr, batch);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // update this unit's static position variables with parent's values
        position.set(getX(),getY());
        size.set(getWidth(),getHeight());
        center.set(getCenterX(),getCenterY());
    }

    @Override
    public void receiveDamage(Projectile projectile) {
        receiveDamage(projectile.getDamage(), projectile);
        Gdx.input.vibrate(300);
    }

    @Override
    public void receiveDamage(Unit unit) {
        receiveDamage(1f, unit); // TODO: get damage amount from unit (bigger units do more damage when colliding with)
        Gdx.input.vibrate(300);
    }

    @Override
    public void receiveDamage(float damageAmount, Visual visual) {
        super.receiveDamage(damageAmount, visual);

        // play alarm when health is below critical
        if (!this.isCriticalLifeActivated() && this.isCriticalHealth()) {
            this.setIsCriticalLifeActivated(true);
            SpaceShooter.playSound(SoundName.Warning);
            SpaceShooter.playSound(SoundName.Alarm, true);
        }
        else if (this.isCriticalLifeActivated() && !this.isCriticalHealth()) {
            this.setIsCriticalLifeActivated(false);
            SpaceShooter.stopSound(SoundName.Alarm);
        }
    }

    @Override
    public void generateDamagePoints(Visual visual) {
        // empty on purpose because we don't want to show damage points on ship // TODO: do this later
    }

    public boolean isCriticalLifeActivated() {
        return isCriticalLifeActivated;
    }

    public void setIsCriticalLifeActivated(boolean isCriticalLifeActivated) {
        this.isCriticalLifeActivated = isCriticalLifeActivated;
    }
}
