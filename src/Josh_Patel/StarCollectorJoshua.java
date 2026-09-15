package Josh_Patel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.game.AbstractGameAgent;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Star;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

public class StarCollectorJoshua extends TeamClient {

	HashMap<Star, Ship> beaconToShipMap;
	HashMap<Ship, Star> shipToStarMap;

	/**
	 * Send each ship to a beacon
	 */
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

		// loop through each ship
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;
				AbstractAction current = ship.getCurrentAction();

				// does the ship have a beacon it is aiming for?
				if (current == null || current.isMovementFinished(space) || !shipToStarMap.containsKey(ship)) {
					Position currentPosition = ship.getPosition();
					Star beacon = pickNearestFreeStar(space, ship);

					AbstractAction newAction = null;

					if (beacon == null) {
						// there is no beacon available so do nothing
						newAction = new DoNothingAction();
					} else {
						beaconToShipMap.put(beacon, ship);
						shipToStarMap.put(ship, beacon);
						Position newGoal = beacon.getPosition();
						newAction = new MoveToObjectAction(space, currentPosition, beacon);
					}
					actions.put(ship.getId(), newAction);
				} else {
					actions.put(ship.getId(), ship.getCurrentAction());
				}
			} else {
				// it is a base and Star collector doesn't do anything to bases
				actions.put(actionable.getId(), new DoNothingAction());
			}
		}

		return actions;
	}


	/**
	 * Find the nearest free beacon to this ship
	 * @param space
	 * @param ship
	 * @return
	 */
	private Star pickNearestFreeStar(Toroidal2DPhysics space, Ship ship) {
		// get the current beacons
		Set<Star> beacons = space.getStars();

		Star closestStar = null;
		double bestDistance = Double.POSITIVE_INFINITY;

		for (Star beacon : beacons) {
			if (beaconToShipMap.containsKey(beacon)) {
				continue;
			}

			double dist = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
			if (dist < bestDistance) {
				bestDistance = dist;
				closestStar = beacon;
			}
		}

		return closestStar;
	}




	/**
	 * Clean up data structure including beacon maps
	 */
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {

		// once a beacon has been picked up, remove it from the list 
		// of beacons being pursued (so it can be picked up at its
		// new location)
		for (Star beacon : space.getStars()) {
			if (!beacon.isAlive()) {
				shipToStarMap.remove(beaconToShipMap.get(beacon));
				beaconToShipMap.remove(beacon);
			}
		}

	}

	@Override
	public void initialize(Toroidal2DPhysics space) {
		beaconToShipMap = new HashMap<Star, Ship>();
		shipToStarMap = new HashMap<Ship, Star>();
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * Star collector never purchases
	 */
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, 
			ResourcePile resourcesAvailable, 
			PurchaseCosts purchaseCosts) {
		return new HashMap<UUID,PurchaseTypes>();
	}


	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<UUID, AbstractGameAgent> getGameSearch(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		// TODO Auto-generated method stub
		return null;
	}

}