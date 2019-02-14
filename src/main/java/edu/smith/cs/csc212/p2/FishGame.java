package edu.smith.cs.csc212.p2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.sun.tools.javac.jvm.Items;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	/** 
	 * These are the fish that are now at home. 
	 */
	List<Fish> fishAtHome;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	
	FishFood food;
	Bubble bubble;
	
	
	
	/** 
	 * Making variable for number of rocks (and falling rocks). 
	 */
	public static final int numRocks = 10;
	public static final int numFallingRocks = 10; 
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		fishAtHome = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		// TODO(lab) Generate some more rocks! (done)
		// TODO(lab) Make 5 into a constant, so it's easier to find & change. (done) 
		for (int i=0; i<numRocks; i++) {
			world.insertRockRandomly();
		}
		
		// insert falling rocks 
		for (int i = 0; i<numFallingRocks; i++) {
			world.insertFallingRockRandomly();
		}
		
		// TODO(lab) Make the snail! (Done: Inserting 2 snails.)
		world.insertSnailRandomly();
		world.insertSnailRandomly();
		
		//insert food in the world (position changed in step().)
		food = world.insertFishFoodRandomly();
		
		//insert a bubble in the world randomly.
		bubble = world.insertBubbleRandomly();
		
	
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the PlayFish app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		// TODO(P2) We want to bring the fish home before we win!
		return missing.isEmpty() && fishAtHome.size() >= 7;
	}
	
	public int scoreDecider(Fish wo) {
		if (wo.getColor() == Color.green) {
			return 20; 
		}
		
		else if (wo.getColor() == Color.yellow) {
			return 30; 
		}
		
		else if (wo.getColor() == Color.cyan) {
			return 40;
		}
		
		else if (wo.getColor() == Color.magenta) {
			return 50;
		}
		
		else if (wo.getColor() == Color.orange) {
			return 60;
		}
		
		else if (wo.getColor() == Color.pink) {
			return 70;
		}
		
		else if (wo.getColor() == Color.white) {
			return 80; 
		}
		
		else {
			return 10;
		}
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 * @throws InterruptedException 
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1; //below increment steps the fish has taken too.
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				
				// Remove from world.
				// TODO(lab): add to found instead! (So we see objectsFollow work!) (done)
				
				//world.remove(wo);
				found.add((Fish) wo);
				
			//if the wo is another fish and they're in a bubble state, make the bubble pop!
			if (wo.isFish()) {
				Fish otherFish = (Fish) wo;
				if (otherFish.bubbleState) {
					otherFish.bubbleState = false;
				}
			}
		
				
				// Increase score when you find a fish! (instead: increase score when you guide them home)
				//score += 10; 
				//score += scoreDecider((Fish) wo);	
			}
			//making fish go home. remove from the world.
			if (wo.isFishHome() && found.size() > 0) {
				for (int i = found.size() - 1; i >= 0; i--) {
					fishAtHome.add(found.get(i));
					score += scoreDecider(found.get(i));
					found.remove(i);
				}
				for (Fish item : fishAtHome) {
					world.remove(item);
				}	
			}
		}
		//if too many steps taken and there are still fish following the player, make them missing again (do we decrease score?)
		for (int i = found.size() -1; i >= 0; i--) {
			found.get(i).stepCounter += 1;
			if (found.get(i).stepCounter > 20) {
				Fish fishToWander = found.get(i);
				missing.add(fishToWander);
				fishToWander.stepCounter = 0;
				found.remove(i);
				
			}
		}	
			
		//make fish go home if they randomly wander on top of home
		List<WorldObject> woCopy = world.viewItems(); 
		for (int i = woCopy.size() - 1; i >= 0; i--) {
			WorldObject checkWO = woCopy.get(i);
			if (checkWO instanceof Fish && !(checkWO.isPlayer())) {
				if (checkWO.inSameSpot(this.home)) {
					fishAtHome.add((Fish) checkWO);
					if (missing.contains(checkWO)) {
						missing.remove(checkWO);
					}
					world.remove(checkWO);
				}
			}
			
			// fish food eaten by player or other fish 
			//(no waiting involved because it keeps giving me an "illegalmonitorstateexception")
			if (checkWO instanceof Fish) {
				if (checkWO.inSameSpot(this.food) && checkWO.isPlayer()) {
					this.score += 10;
					this.food.setPosition(world.pickUnusedSpace());
 
				}
				if (checkWO.inSameSpot(this.food)) {
					this.food.setPosition(world.pickUnusedSpace());
					
				}
				//check if other fish are in place of the bubble. remove bubble and then make fish in bubbleState. 
				if(checkWO.inSameSpot(this.bubble) && !(checkWO.isPlayer()) && this.bubble.bubbleExists) {
					((Fish) checkWO).bubbleState = true;
					this.bubble.bubbleExists = false;
					world.remove(this.bubble);
					
				}
			}
			
		}
		// if all bubbles in world popped and there are no bubbles, create bubbles:
		
		// NOTE for JJ: for some reason the code below doesn't make a new instance of the bubble
		// instead it makes the fish never enter the bubble state. i am not quite sure what's going on 
		
//		if (this.bubble.bubbleExists = false) {
//			Bubble newBubble = world.insertBubbleRandomly();
//			this.bubble = newBubble;
//			this.bubble.bubbleExists = true;
//				}
		
			
			
		
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {

			if (rand.nextDouble() < 0.8 && lost.fastScared) {
				lost.moveRandomly();
			}
			// 30% of the time, lost fish move randomly.
			if (rand.nextDouble() < 0.3) {
				// TODO(lab): What goes here? lost fish should move randomly. (done)
				lost.moveRandomly(); // here, below add .fastScared() 
			}
		}
	}
	

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// TODO(P2) use this print to debug your World.canSwim changes! (done?)
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		// TODO(P2) allow the user to click and remove rocks/and bubbles. (done)
		for (WorldObject aRockOrBubble : atPoint) {
			if (!aRockOrBubble.isFish() && !aRockOrBubble.isPlayer() && 
				!aRockOrBubble.isSnail() && !aRockOrBubble.isFishHome() && 
				!aRockOrBubble.isFishFood()) {
				//System.out.println("this is a rock");
				world.remove(aRockOrBubble);
			}
		}

	}
	
}
