package com.example.demo;

import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class UserPlane extends FighterPlane {

	private static final String IMAGE_NAME = "userplane.png";
	private static final double Y_UPPER_BOUND = -40;
	private static final double Y_LOWER_BOUND = 600.0;
	private static final double INITIAL_X_POSITION = 5.0;
	private static final double INITIAL_Y_POSITION = 300.0;
	private static final int IMAGE_HEIGHT = 150;
	private static final int VERTICAL_VELOCITY = 8;
	private static final int PROJECTILE_X_POSITION = 110;
	private static final int PROJECTILE_Y_POSITION_OFFSET = 20;

	private int velocityMultiplier;
	private int numberOfKills;
	private final HeartDisplay heartDisplay;

	public UserPlane(int initialHealth, HeartDisplay heartDisplay) {
		super(IMAGE_NAME, IMAGE_HEIGHT, INITIAL_X_POSITION, INITIAL_Y_POSITION, initialHealth);
		velocityMultiplier = 0;
		this.heartDisplay = heartDisplay;
	}

	@Override
	public void updatePosition() {
		if (!isMoving()) {
			return;
		}
		double initialTranslateY = getTranslateY();
		moveVertically(VERTICAL_VELOCITY * velocityMultiplier);
		if (isOutOfBounds()) {
			setTranslateY(initialTranslateY);
		}
	}

	private boolean isOutOfBounds() {
		double newPosition = getLayoutY() + getTranslateY();
		return newPosition < Y_UPPER_BOUND || newPosition > Y_LOWER_BOUND;
	}

	@Override
	public void updateActor() {
		updatePosition();
	}

	@Override
	public ActiveActorDestructible fireProjectile() {
			playSound("userplaneshoot.wav");
			double[] projectilePosition = getProjectilePosition(PROJECTILE_X_POSITION, PROJECTILE_Y_POSITION_OFFSET);
			return new UserProjectile(projectilePosition[0], projectilePosition[1]);
	}

	private void playSound(String soundName) {
		try {
			String soundPath = "com/example/demo/sounds/" + soundName;
			URL soundURL = getClass().getClassLoader().getResource(soundPath);

			if (soundURL == null) {
				throw new IllegalArgumentException("Sound file not found: " + soundPath);
			}

			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();

			clip.addLineListener(event -> {
				if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
					clip.close();
				}
			});
		} catch (Exception e) {
			System.out.println("Error with playing sound: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean isMoving() {
		return velocityMultiplier != 0;
	}

	public void moveUp() {
		velocityMultiplier = -1;
	}

	public void moveDown() {
		velocityMultiplier = 1;
	}

	public void stop() {
		velocityMultiplier = 0;
	}

	public int getNumberOfKills() {
		return numberOfKills;
	}

	public void incrementKillCount() {
		numberOfKills++;
	}

	public boolean collidesWith(ActiveActorDestructible otherActor) {
		return this.getBoundsInParent().intersects(otherActor.getBoundsInParent());
	}

	private void handleCollisions() {
		heartDisplay.removeHeart();
	}

	public void checkCollisions(List<? extends ActiveActorDestructible> enemyPlanes, List<? extends ActiveActorDestructible> enemyProjectiles) {
		for (ActiveActorDestructible enemy : enemyPlanes) {
			if (collidesWith(enemy)) {
				handleCollisions();
				break;
			}
		}

		for (ActiveActorDestructible projectile : enemyProjectiles) {
			if (collidesWith(projectile)) {
				handleCollisions();
				break;
			}
		}
	}

	public void updateGame(List<? extends ActiveActorDestructible> enemyPlanes, List<? extends ActiveActorDestructible> enemyProjectiles) {
		updatePosition();
		checkCollisions(enemyPlanes, enemyProjectiles);
	}

	public HeartDisplay getHeartDisplay() {
		return heartDisplay;
	}

}
