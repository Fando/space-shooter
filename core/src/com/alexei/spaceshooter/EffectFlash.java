package com.alexei.spaceshooter;

/**
 * Created by Alex on 25/06/2015.
 */
public class EffectFlash extends ParticleEmitter {

    public static final float PARTICLE_SIZE = 10;
    public static final float PARTICLE_SIZE_VARIATION = 5;

    public static final float PARTICLE_DIRECTION_ANGLE_FROM = 0;
    public static final float PARTICLE_DIRECTION_ANGLE_TO = 360;
    public static final float PARTICLE_DIRECTION_ANGLE_VARIATION = 0;

    public static final float PARTICLE_SPEED = 150;
    public static final float PARTICLE_SPEED_VARIATION = 50;

    public static final float PARTICLE_ROTATION_SPEED = 0;
    public static final float PARTICLE_ROTATION_SPEED_VARIATION = 0;

    public static final com.badlogic.gdx.graphics.Color PARTICLE_COLOR = new com.badlogic.gdx.graphics.Color(com.badlogic.gdx.graphics.Color.BLUE);
    public static final float PARTICLE_COLOR_VARIATION = 0; // TODO

    public static final float PARTICLE_ALPHA = 1;
    public static final float PARTICLE_ALPHA_VARIATION = 0;

    public static final int PARTICLE_LIFE_PERIOD = 400;
    public static final int PARTICLE_LIFE_PERIOD_VARIATION = 50;

    public static final ParticleEmitter.ParticleShape PARTICLE_SHAPE = ParticleShape.Rect;

    public static final int EMISSION_EVENTS_IN_CYCLE = 1;
    public static final int EMISSION_AMOUNT_PER_EVENT = 30;
    public static final int EMISSION_EVENT_DELAY = 0;
    public static final int EMISSION_CYCLES = 1;
    public static final int EMISSION_CYCLE_DELAY = 0;

    public EffectFlash(float x, float y, Visual visual) {
        super(x, y);

        // assume the passed visual's velocity
        if (visual != null) {
            this.setVelocity(visual.getVelocity());
        }

        setParticleSize(PARTICLE_SIZE);
        setParticleSizeVariation(PARTICLE_SIZE_VARIATION);

        setParticleDirectionAngleFrom(PARTICLE_DIRECTION_ANGLE_FROM);
        setParticleDirectionAngleTo(PARTICLE_DIRECTION_ANGLE_TO);
        setParticleDirectionAngleVariation(PARTICLE_DIRECTION_ANGLE_VARIATION);

        setParticleSpeed(PARTICLE_SPEED);
        setParticleSpeedVariation(PARTICLE_SPEED_VARIATION);

        setParticleRotationSpeed(PARTICLE_ROTATION_SPEED);
        setParticleRotationSpeedVariation(PARTICLE_ROTATION_SPEED_VARIATION);

        setColor(PARTICLE_COLOR);
        setParticleSize(PARTICLE_SIZE);

        getColor().a = PARTICLE_ALPHA;

        // particle life
        setParticleLifePeriod(PARTICLE_LIFE_PERIOD);
        setParticleLifePeriodVariation(PARTICLE_LIFE_PERIOD_VARIATION);

        // shape
        setParticleShape(PARTICLE_SHAPE);

        // emission timing variables
        setEmissionEventsInCycle(EMISSION_EVENTS_IN_CYCLE);
        setEmissionAmountPerEvent(EMISSION_AMOUNT_PER_EVENT);
        setEmissionEventDelay(EMISSION_EVENT_DELAY);
        setEmissionCycles(EMISSION_CYCLES);
        setEmissionCycleDelay(EMISSION_CYCLE_DELAY);
    }

    public EffectFlash(float x, float y) {
        this(x, y,null);
    }
}
