package com.alexei.spaceshooter;

import java.util.ArrayList;
import java.util.Arrays;

/***
 * Created by Alex on 18/06/2015.
 *
 * An abstract class that represents a weapon. Weapons have a damage associated with them and a fire rate. The most important
 * method is the 'fire' method. It works by returning Projectiles according to a fire rate. The weapon's timer is updated every render call.
 * When the timer expires, the fire method is automatically called which creates a projectile (or projectiles), and the timer resets
 * and starts counting again.
 */
public abstract class Weapon {
    private float damage;
    private int fireRate; // the number of milliseconds between firing of the weapon
    private static ArrayList<Projectile> projectiles = new ArrayList<Projectile>(); // a list of projectiles that all weapons have fired
    private Timer timer;
    private Unit unit; // the unit that this weapon belongs to
    private SoundName weaponSoundName = null;

    public Weapon() {
        timer = new Timer(0, 0); // create a timer that resets infinitely upon counting down
    }

    public void update(float deltaTime) {
        timer.update(deltaTime); // update the weapon timer

        if (timer.isTimerElapsed()) { // when the weapon timer expired, fire the weapon
            projectiles.addAll(Arrays.asList(fire()));
            // play a sound when the weapon is fired
            SpaceShooter.playSound(weaponSoundName);
        }
    }

    /***
     * The fire method produces a projectile according the the fire rate. The projectile is added to the list of
     * projectiles that the weapon keeps track of. When the game is updated from the main render method, a loop over
     * each weapon is performed and the list of projectiles is determined/updated.
     * @return Returns a list of projectile objects
     */
    public abstract Projectile[] fire();

    /***
     * Remove a given projectile from the list. This is usually called when a projectile goes outside the screen
     * and/or when the projectile hits a target.
     * @param projectile
     */
    public void removeProjectile(Projectile projectile) {
        projectiles.remove(projectile);
    }
    public void removeProjectile(int index) {
        if (index >= 0 && index < projectiles.size()) projectiles.remove(index);
    }

    public static ArrayList<Projectile> getProjectiles() { return projectiles; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public int getFireRate() { return fireRate; }
    public void setFireRate(int fireRate) { this.fireRate = fireRate; timer.setDuration(fireRate); timer.reset(); }
    public void setUnit(Unit unit) { this.unit = unit; }
    public Unit getUnit() { return unit; }
    public void setWeaponSound(SoundName weaponSoundName) { this.weaponSoundName = weaponSoundName; }
    public SoundName getWeaponSound() { return weaponSoundName; }

}