package edu.smith.cs.csc212.p2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.jjfoley.gfx.IntPoint;

/**
 * A World is a 2d grid, represented as a width, a height, and a list of WorldObjects in that world.
 * @author jfoley
 *
 */
public class World {
	/**
	 * The size of the grid (x-tiles).
	 */
	private int width;
	/**
	 * The size of the grid (y-tiles).
	 */
	private int height;
	/**
	 * A list of objects in the world (Fish, Snail, Rock, etc.).
	 */
	private List<WorldObject> items;
	/**
	 * A reference to a random object, so we can randomize placement of objects in this world.
	 */
	private Random rand = ThreadLocalRandom.current();

	/**
	 * Create a new world of a given width and height.
	 * @param w - width of the world.
	 * @param h - height of the world.
	 */
	public World(int w, int h) {
		items = new ArrayList<>();
		width = w;
		height = h;
	}

	/**
	 * What is under this point?
	 * @param x - the tile-x.
	 * @param y - the tile-y.
	 * @return a list of objects!
	 */
	public List<WorldObject> find(int x, int y) {
		List<WorldObject> found = new ArrayList<>();
		
		// Check out every object in the world to find the ones at a particular point.
		for (WorldObject w : this.items) {
			// But only the ones that match are "found".
			if (x == w.getX() && y == w.getY()) {
				found.add(w);
			}
		}
		
		// Give back the list, even if empty.
		return found;
	}
	
	
	/**
	 * This is used by PlayGame to draw all our items!
	 * @return the list of items.
	 */
	public List<WorldObject> viewItems() {
		// Don't let anybody add to this list!
		// Make them use "register" and "remove".

		// This is kind of an advanced-Java trick to return a list where add/remove crash instead of working.
		return Collections.unmodifiableList(items);
	}

	/**
	 * Add an item to this World.
	 * @param item - the Fish, Rock, Snail, or other WorldObject.
	 */
	public void register(WorldObject item) {
		// Print out what we've added, for our sanity.
		System.out.println("register: "+item.getClass().getSimpleName());
		items.add(item);
	}
	
	/**
	 * This is the opposite of register. It removes an item (like a fish) from the World.
	 * @param item - the item to remove.
	 */
	public void remove(WorldObject item) {
		// Print out what we've removed, for our sanity.
		System.out.println("remove: "+item.getClass().getSimpleName());
		items.remove(item);
	}
	
	/**
	 * How big is the world we model?
	 * @return the width.
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * How big is the world we model?
	 * @return the height.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Try to find an unused part of the World for a new object!
	 * @return a point (x,y) that has nothing else in the grid.
	 */
	public IntPoint pickUnusedSpace() {
		int tries = width * height;
		for (int i=0; i<tries; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			if (this.find(x, y).size() != 0) {
				continue;
			}
			return new IntPoint(x,y);
		}
		// If we get here, we tried a lot of times and couldn't find a random point.
		// Let's crash our Java program!
		throw new IllegalStateException("Tried to pickUnusedSpace "+tries+" times and it failed! Maybe your grid is too small!");
	}
	
	/**
	 * Insert an item randomly into the grid.
	 * @param item - the rock, fish, snail or other WorldObject.
	 */
	public void insertRandomly(WorldObject item) {
		item.setPosition(pickUnusedSpace());
		this.register(item);
		item.checkFindMyself();
	}
	
	/**
	 * Insert a new Rock into the world at random.
	 * @return the Rock.
	 */
	public Rock insertRockRandomly() {
		Rock r = new Rock(this);
		insertRandomly(r);
		return r;
	}
	
	/**
	 * Insert a new Fish into the world at random of a specific color.
	 * @param color - the color of the fish.
	 * @return the new fish itself.
	 */
	public Fish insertFishRandomly(int color) {
		Fish f = new Fish(color, this);
		insertRandomly(f);
		return f;
	}
	
	public FishHome insertFishHome() {
		FishHome home = new FishHome(this);
		insertRandomly(home);
		return home;
	}
	
	/**
	 * Insert a new Snail at random into the world.
	 * @return the snail!
	 */
	public Snail insertSnailRandomly() {
		Snail snail = new Snail(this);
		insertRandomly(snail);
		return snail;
	}
	
	/** 
	 * Insert falling rocks at random into the world. 
	 * @return the falling rocks! 
	 */
	public FallingRock insertFallingRockRandomly() {
		FallingRock fallingRock = new FallingRock(this);
		insertRandomly(fallingRock);
		return fallingRock;
	}
	
	/**
	 * Insert food randomly into the world. 
	 * @return the food!
	 */
	public FishFood insertFishFoodRandomly() {
		FishFood food = new FishFood(this);
		insertRandomly(food);
		return food;
	}
	
	/**
	 * Insert bubble randomly into the world. 
	 * @return the bubble!
	 */
	public Bubble insertBubbleRandomly() {
		Bubble bubble = new Bubble(this);
		insertRandomly(bubble);
		return bubble;
	}
	
	
	/**
	 * Determine if a WorldObject can swim to a particular point.
	 * 
	 * @param whoIsAsking - the object (not just the player!)
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 * @return true if they can move there.
	 */
	public boolean canSwim(WorldObject whoIsAsking, int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		
		// This will be important.
		boolean isPlayer = whoIsAsking.isPlayer();
		
		// We will need to look at who all is in the spot to determine if we can move there.
		List<WorldObject> inSpot = this.find(x, y);
		
		for (WorldObject it : inSpot) {
			// TODO(P2): Don't let us move over rocks as a Fish. (Done).
			// The other fish shouldn't step "on" the player, the player should step on the other fish.
			if (it instanceof Snail) {
				// This if-statement doesn't let anyone step on the Snail.
				// The Snail(s) are not gonna take it.
				return false;
			}
			
			if (it instanceof Rock || it instanceof FallingRock) {
				return false; 
			}
			
			if (it instanceof Fish && !(isPlayer)) {
				return false; 
			}
		}
		
		// If we didn't see an obstacle, we can move there!
		return true;
	}
	
	/**
	 * This is how objects may move. Only Snails do right now.
	 */
	public void stepAll() {
		for (WorldObject it : this.items) {
			it.step();
		}
	}
	
	/**
	 * This signature is a little scary, but we need to support any subclass of WorldObject.
	 * We don't know followers is a {@code List<Fish>} but it should work no matter what!
	 * @param target the leader.
	 * @param followers a set of objects to follow the leader.
	 */
	public static void objectsFollow(WorldObject target, List<? extends WorldObject> followers) {
		// TODO(P2) Comment this method! (done)
		/* What is recentPositions?
		 *  
		 * Answer: recentPositions is the deque (list) of positions that the leader (target) 
		 * has been through. This gets updated mod 64 (size of the grid) and the "old" ones are excluded. By having 
		 * a deque, it becomes possible for the "followers" to be added to positions that the leader has just recently
		 * been moving through - creating a following effect.*/
		
		
		/* What is followers? 
		 * 
		 * 
		 * Answer: an extended WorldObjects list. The program goes into the step() function of the 
		 * FishGame (because the objectsFollow is passed in FishGame with the extended list specified to be found world objects),
		 *  and decides that if there's another WorldObject in the same spot as the "player", remove them, see if the world object
		 *  was in missing: if it was, add to found instead. Because we cast these world objects as Fish objects, they are always 
		 *  fish*/
		
		/* What is target?
		 * Answer: The only place the objects Follow is passed is in the FishGame, and the target is the player always. */
		
		
		/* Why is past = putWhere[i+1]? Why not putWhere[i]?
		 * Answer: Because the index of lists always starts with 0, and keeping in mind that the list recentPositions is a deque,
		 * the most recent positions of the target get added to the front. In fact the 0 position is target itself. We want that 
		 * for each world object in followers add them right next to the target rather on the target itself and forward.*/
		
		List<IntPoint> putWhere = new ArrayList<>(target.recentPositions);
		for (int i=0; i<followers.size(); i++) {
			IntPoint past = putWhere.get(i+1);
			followers.get(i).setPosition(past.x, past.y);
//			System.out.println(target.isPlayer());
		}
	}
}
