package com.orangomango.snake.game.cycle;

import java.util.*;

import com.orangomango.snake.game.GameWorld;

public class Cycle{
	private Tile startTile;

	public Cycle(GameWorld world){
		Random random = new Random();
		final int[][] directions = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
		Tile[][] map = new Tile[world.getWidth()][world.getHeight()];
		for (int x = 0; x < map.length; x++){
			for (int y = 0; y < map[x].length; y++){
				map[x][y] = new Tile(x, y);
			}
		}
		this.startTile = map[0][0];

		List<List<Tile>> paths = findPath(map, this.startTile, this.startTile);
		System.out.println(paths.size());
		paths.stream().forEach(System.out::println);
		System.out.println();

		/*for (int i = 0; i < paths.size(); i++){
			List<Tile> path = paths.get(i);
			System.out.format("Path %d: %s", i, path);
			List<Tile> visited = new ArrayList<>();
			for (int j = 0; j < path.size(); j++){
				Tile tile = path.get(j);
				visited.add(tile);
				for (int y = 0; y < map[0].length; y++){
					for (int x = 0; x < map.length; x++){
						System.out.print(visited.contains(map[x][y]) ? "#" : ".");
					}
					System.out.println();
				}
				System.out.println();
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		}*/
	}

	private List<List<Tile>> findPath(Tile[][] map, Tile tile, Tile endTile){
		tile.visited = true;
		List<List<Tile>> output = new ArrayList<>();

		// Debug
		try { Thread.sleep(150); } catch (InterruptedException ex){}
		for (int y = 0; y < map[0].length; y++){
			for (int x = 0; x < map.length; x++){
				Tile t = map[x][y];
				System.out.print(t == tile ? "x" : (t.visited ? "o" : "."));
			}
			System.out.println();
		}
		System.out.println();

		Tile n = getTileAt(map, tile.getX(), tile.getY()-1);
		Tile e = getTileAt(map, tile.getX()+1, tile.getY());
		Tile s = getTileAt(map, tile.getX(), tile.getY()+1);
		Tile w = getTileAt(map, tile.getX()-1, tile.getY());

		Tile[] selection = new Tile[]{n, e, s, w};
		int[] selectedTiles = new int[4];
		Random random = new Random();
		do {
			int pos = random.nextInt(4);
			if (selectedTiles[pos] == 0){
				selectedTiles[pos] = 1;
				Tile selected = selection[pos];
				if (selected != null && ((!selected.visited && isAcceptable(selected, map, endTile)) || selected == endTile)){
					if (selected == endTile){
						if (allVisited(map)){
							List<Tile> temp = new ArrayList<>();
							temp.add(tile);
							temp.add(selected);
							output.add(temp);
							break; // Only one solution is needed
						}
					} else {
						List<List<Tile>> result = findPath(map, selected, endTile);
						for (List<Tile> list : result){
							List<Tile> temp = new ArrayList<>();
							temp.add(tile);
							temp.addAll(list);
							output.add(temp);
						}

						// Only one solution is needed
						if (result.stream().flatMap(l -> l.stream()).filter(t -> t == endTile).findAny().isPresent()){
							break;
						}
					}
				}
			}
		} while (selectedTiles[0]*selectedTiles[1]*selectedTiles[2]*selectedTiles[3] == 0); // The product is 0 if there is a 0 in the array

		tile.visited = false;
		return output;
	}

	private static boolean isAcceptable(Tile tile, Tile[][] map, Tile ref){
		// All tiles that have not been visited that are "connected" to the reference tile
		/*List<Tile> available = new ArrayList<>();
		List<Tile> visited = new ArrayList<>();
		available.add(ref);
		tile.visited = true; // Visit this tile temporary
		while (available.size() != 0){
			Tile t = available.remove(0);
			if (visited.contains(t)) continue;
			visited.add(t);

			Tile n = getTileAt(map, t.getX(), t.getY()-1);
			Tile e = getTileAt(map, t.getX()+1, t.getY());
			Tile s = getTileAt(map, t.getX(), t.getY()+1);
			Tile w = getTileAt(map, t.getX()-1, t.getY());
			if (n != null && !n.visited && !visited.contains(n)) available.add(n);
			if (e != null && !e.visited && !visited.contains(e)) available.add(e);
			if (s != null && !s.visited && !visited.contains(s)) available.add(s);
			if (w != null && !w.visited && !visited.contains(w)) available.add(w);
		}

		List<Tile> emptyTiles = new ArrayList<>();
		for (int x = 0; x < map.length; x++){
			for (int y = 0; y < map[x].length; y++){
				if (!map[x][y].visited){
					emptyTiles.add(map[x][y]);
				}
			}
		}

		//System.out.format("Visited size: %d\tEmpty tiles: %d\n", visited.size(), emptyTiles.size());

		tile.visited = false;
		return emptyTiles.size() == visited.size()-1;*/

		return true;
	}

	private static boolean allVisited(Tile[][] map){
		for (int x = 0; x < map.length; x++){
			for (int y = 0; y < map[x].length; y++){
				if (!map[x][y].visited) return false;
			}
		}

		return true;
	}

	private static Tile getTileAt(Tile[][] map, int x, int y){
		if (x >= 0 && y >= 0 && x < map.length && y < map[0].length){
			return map[x][y];
		} else return null;
	}

	public Tile getStartTile(){
		return this.startTile; // Temp
	}

	// DEBUG
	/*public void print(GameWorld world){
		String[][] map = new String[world.getWidth()][world.getHeight()];
		Tile currentTile = this.startTile;
		while (currentTile != null){
			Tile parent = this.startTile.parent;
			if (parent.getY()-currentTile.getY() == -1){
				map[currentTile.getX()][currentTile.getY()] = "^";
			} else if (parent.getX()-currentTile.getX() == 1){
				map[currentTile.getX()][currentTile.getY()] = ">";
			} else if (parent.getY()-currentTile.getY() == 1){
				map[currentTile.getX()][currentTile.getY()] = "v";
			} else if (parent.getX()-currentTile.getX() == -1){
				map[currentTile.getX()][currentTile.getY()] = "<";
			}
			currentTile = parent;
		}

		for (int y = 0; y < map[0].length; y++){
			for (int x = 0; x < map.length; x++){
				System.out.print(map[x][y]);
			}
			System.out.println();
		}
	}*/
}