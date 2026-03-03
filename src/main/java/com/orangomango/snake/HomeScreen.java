package com.orangomango.snake;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.DropShadow;

import java.util.*;
import java.io.*;
import org.json.JSONObject;

import com.orangomango.snake.game.GameScreen;
import com.orangomango.snake.ui.*;
import com.orangomango.account.Account;

import static com.orangomango.snake.MainApplication.APP_HOST;
import static com.orangomango.snake.MainApplication.APP_UID;

public class HomeScreen{
	public static final int WIDTH = 900; // Ratio: 1.5
	public static final int HEIGHT = 600;
	private static final int FPS = 40;
	private static final Color COLOR_EASY = Color.web("#10b981");
	private static final Color COLOR_MEDIUM = Color.web("#3b82f6");
	private static final Color COLOR_HARD = Color.web("#ef4444");
	private static final Color COLOR_EXTREME = Color.web("#a855f7");
	
	private Stage stage;
	private ArrayList<UiElement> uielements = new ArrayList<>();
	private Rectangle2D headerRect = new Rectangle2D(0.05, 0.04, 0.90, 0.08);
	private Rectangle2D leaderboardRect = new Rectangle2D(0.31, 0.15, 0.38, 0.62);
	private Button leadEasy, leadMedium, leadHard, leadExtreme;
	private Button loginButton;
	private Account account = null;
	private String gameMode = null;
	private ArrayList<ArrayList<Pair<String, int[]>>> leaderboards = new ArrayList<>();
	private int currentLeadMode;
	private InputField usernameField, passwordField;
	
	public HomeScreen(Stage stage){
		this.stage = stage;
	}
	
	public Scene getScene(){
		Account.HOST = APP_HOST;
		Account.registerApplication(APP_UID);

		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));

		// Sliders
		Slider cellSlider = new Slider(0.73, 0.25, 0.20, 0.10, "Cell Size", "px");
		cellSlider.setInterval(5, 60, 30);
		Slider speedSlider = new Slider(0.73, 0.40, 0.20, 0.10, "Speed", "ms");
		speedSlider.setInterval(45, 500, 150);

		// Toggle buttons
		ToggleButton aiMode = new ToggleButton(0.73, 0.55, 0.20, 0.08, "Auto-Play");
		ToggleButton wrapping = new ToggleButton(0.73, 0.65, 0.20, 0.08, "Wrapping");

		// Login
		this.usernameField = new InputField(0.062, 0.231, 0.216, 0.05, "Username");
		this.passwordField = new InputField(0.062, 0.293, 0.216, 0.05, "Password");
		passwordField.setPasswordField(true);
		this.loginButton = new Button(Color.web("#10b981"), Color.web("#059669"), 0.062, 0.362, 0.216, 0.05, "LOGIN", UiElement.FONT_SMALLSMALL, Color.web("#ffffff"), () -> {
			if (this.account == null){
				Account account = new Account(this.usernameField.getText(), this.passwordField.getText());
				JSONObject data = account.login();
				if (data != null && data.getBoolean("success")){
					String uid = data.getJSONObject("data").getString("uid");
					System.out.println("Logged in as "+uid);
					login(account);
				} else {
					this.loginButton.bounce();
				}
			} else {
				logout();
			}
		});

		// Load login data
		try {
			File file = new File("login.data");
			if (file.exists()){
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String uid = reader.readLine();
				reader.close();

				Account account = new Account(uid);
				if (account.getUsername() == null){
					System.out.println("Cached authentication failed");
				} else {
					login(account);
				}
			}
		} catch (IOException ex){
			ex.printStackTrace();
		}

		Button playButton = new Button(Color.web("#10b981"), Color.web("#059669"), 0.25, 0.82, 0.50, 0.12, "PLAY", UiElement.FONT_MEDIUM, Color.web("#ffffff"), () -> {
			loop.stop();
			GameScreen gs = new GameScreen(this.stage, this.account, this.gameMode, (int)cellSlider.getValue(), (int)speedSlider.getValue(), aiMode.getSelected(), wrapping.getSelected());
			this.stage.setScene(gs.getScene());
		});

		MultistateButton difficultyButton = new MultistateButton(Color.web("#001d27"), 0.065, 0.55, 0.21, 0.18);
		difficultyButton.addState("EASY", COLOR_EASY, () -> {
			cellSlider.setValue(60);
			speedSlider.setValue(300);
			aiMode.setSelected(false);
			wrapping.setSelected(false);
			this.gameMode = "easy";
		});
		difficultyButton.addState("MEDIUM", COLOR_MEDIUM, () -> {
			cellSlider.setValue(30);
			speedSlider.setValue(150);
			aiMode.setSelected(false);
			wrapping.setSelected(false);
			this.gameMode = "medium";
		});
		difficultyButton.addState("HARD", COLOR_HARD, () -> {
			cellSlider.setValue(20);
			speedSlider.setValue(80);
			aiMode.setSelected(false);
			wrapping.setSelected(false);
			this.gameMode = "hard";
		});
		difficultyButton.addState("EXTREME", COLOR_EXTREME, () -> {
			cellSlider.setValue(50);
			speedSlider.setValue(60);
			aiMode.setSelected(false);
			wrapping.setSelected(false);
			this.gameMode = "extreme";
		});

		difficultyButton.setState(1);
		this.gameMode = "medium";

		// Runnables for setting different game mode
		Runnable rankedGame = () -> {
			playButton.setStyle(Color.web("#10b981"), null, "PLAY RANKED", null);
			difficultyButton.setDisabled(false);
		};

		Runnable casualGame = () -> {
			playButton.setStyle(Color.web("#3B82F6"), null, "PLAY CASUAL", null);
			difficultyButton.setDisabled(true);
			this.gameMode = null;
		};

		difficultyButton.setOnStateChanged(rankedGame);
		cellSlider.setOnStateChanged(casualGame);
		speedSlider.setOnStateChanged(casualGame);
		aiMode.setOnStateChanged(casualGame);
		wrapping.setOnStateChanged(casualGame);

		// Leaderboard
		this.leadEasy = new Button(Color.web("#001d27"), COLOR_EASY, this.leaderboardRect.getMinX()+0.05*this.leaderboardRect.getWidth(), this.leaderboardRect.getMinY()+0.12*this.leaderboardRect.getHeight(), 0.2175*this.leaderboardRect.getWidth(), 0.08*this.leaderboardRect.getHeight(), "EASY", UiElement.FONT_SMALLSMALL, COLOR_EASY, () -> selectLeadMode(0));
		this.leadMedium = new Button(Color.web("#001d27"), COLOR_MEDIUM, this.leaderboardRect.getMinX()+0.28*this.leaderboardRect.getWidth(), this.leaderboardRect.getMinY()+0.12*this.leaderboardRect.getHeight(), 0.2175*this.leaderboardRect.getWidth(), 0.08*this.leaderboardRect.getHeight(), "MED", UiElement.FONT_SMALLSMALL, COLOR_MEDIUM, () -> selectLeadMode(1));
		this.leadHard = new Button(Color.web("#001d27"), COLOR_HARD, this.leaderboardRect.getMinX()+0.51*this.leaderboardRect.getWidth(), this.leaderboardRect.getMinY()+0.12*this.leaderboardRect.getHeight(), 0.2175*this.leaderboardRect.getWidth(), 0.08*this.leaderboardRect.getHeight(), "HARD", UiElement.FONT_SMALLSMALL, COLOR_HARD, () -> selectLeadMode(2));
		this.leadExtreme = new Button(Color.web("#001d27"), COLOR_EXTREME, this.leaderboardRect.getMinX()+0.74*this.leaderboardRect.getWidth(), this.leaderboardRect.getMinY()+0.12*this.leaderboardRect.getHeight(), 0.2175*this.leaderboardRect.getWidth(), 0.08*this.leaderboardRect.getHeight(), "EXT", UiElement.FONT_SMALLSMALL, COLOR_EXTREME, () -> selectLeadMode(3));

		this.leadEasy.setBorderSize(0.002);
		this.leadMedium.setBorderSize(0.002);
		this.leadHard.setBorderSize(0.002);
		this.leadExtreme.setBorderSize(0.002);

		selectLeadMode(1);
		fetchLeaderboards();

		this.uielements.add(new Container(Color.web("#0f172a"), Color.web("#1e293b"), 0.05, 0.15, 0.24, 0.28, Color.web("#94f7d4"), UiElement.FONT_SMALL, "MangoGames ID")); // Authentication
		this.uielements.add(new Container(Color.web("#0f172a"), Color.web("#1e293b"), 0.05, 0.45, 0.24, 0.32, Color.web("#94f7d4"), UiElement.FONT_SMALL, "Game mode")); // Game mode selection
		this.uielements.add(new Container(Color.web("#0f172a"), Color.web("#1e293b"), this.leaderboardRect.getMinX(), this.leaderboardRect.getMinY(), this.leaderboardRect.getWidth(), this.leaderboardRect.getHeight(), Color.web("#94f7d4"), UiElement.FONT_SMALL, "Leaderboard")); // Leaderboard
		this.uielements.add(new Container(Color.web("#0f172a"), Color.web("#1e293b"), 0.71, 0.15, 0.24, 0.62, Color.web("#94f7d4"), UiElement.FONT_SMALL, "Custom game")); // Custom settings
		this.uielements.add(new Container(Color.web("#0f172a"), Color.web("#10b981"), this.headerRect.getMinX(), this.headerRect.getMinY(), this.headerRect.getWidth(), this.headerRect.getHeight(), null, null, null)); // Game logo
		
		this.uielements.add(playButton);
		this.uielements.add(cellSlider);
		this.uielements.add(speedSlider);
		this.uielements.add(aiMode);
		this.uielements.add(wrapping);
		this.uielements.add(this.usernameField);
		this.uielements.add(this.passwordField);
		this.uielements.add(loginButton);
		this.uielements.add(difficultyButton);
		this.uielements.add(this.leadEasy);
		this.uielements.add(this.leadMedium);
		this.uielements.add(this.leadHard);
		this.uielements.add(this.leadExtreme);

		canvas.setOnMouseMoved(e -> this.uielements.stream().filter(el -> el instanceof MouseSensible).forEach(el -> ((MouseSensible)el).onHover(e.getX(), e.getY())));
		canvas.setOnMousePressed(e -> this.uielements.stream().filter(el -> el instanceof MouseSensible).forEach(el -> ((MouseSensible)el).onClick(e.getX(), e.getY())));
		canvas.setOnMouseReleased(e -> this.uielements.stream().filter(el -> el instanceof MouseSensible).forEach(el -> ((MouseSensible)el).onRelease(e.getX(), e.getY())));
		canvas.setOnMouseDragged(e -> this.uielements.stream().filter(el -> el instanceof MouseSensible).forEach(el -> ((MouseSensible)el).onDrag(e.getX(), e.getY())));

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.uielements.stream().filter(el -> el instanceof InputField).forEach(el -> ((InputField)el).keyPress(e)));
		canvas.setOnKeyTyped(e -> this.uielements.stream().filter(el -> el instanceof InputField).forEach(el -> ((InputField)el).keyType(e)));

		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		return new Scene(pane, WIDTH, HEIGHT);
	}

	private void login(Account account){
		this.account = account;
		fetchLeaderboards();

		this.usernameField.setText("");
		this.passwordField.setText("");
		this.loginButton.setStyle(Color.web("#ef4444"), Color.web("#7f1d1d"), "LOGOUT", null);

		try {
			File file = new File("login.data");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(this.account.getUID());
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	private void logout(){
		this.account.logout();
		this.account = null;
		this.loginButton.setStyle(Color.web("#10b981"), Color.web("#059669"), "LOGIN", null);

		File file = new File("login.data");
		if (file.exists()) file.delete();
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.web("#020617"));
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		for (UiElement element : this.uielements){
			if (this.account != null && (element == this.usernameField || element == this.passwordField)) continue;
			element.render(gc);
		}

		// Render login text
		if (this.account != null){
			gc.save();
			gc.setFill(Color.web("#94f7d4"));
			gc.setFont(UiElement.FONT_SMALLSMALL);
			gc.fillText("LOGGED IN AS", UiElement.rw(0.17), UiElement.rh(0.275));
			
			gc.setFill(Color.WHITE);
			gc.setFont(UiElement.FONT_SMALL);
			gc.fillText(account.getUsername(), UiElement.rw(0.17), UiElement.rh(0.330));
			gc.restore();
		}

		// Render leaderboard
		if (this.leaderboards.size() > 0){
			ArrayList<Pair<String, int[]>> data = this.leaderboards.get(this.currentLeadMode);
			renderLeaderboard(gc, data);
		}

		// Render game name and version
		gc.save();
		gc.setFont(UiElement.FONT_SMALL);
		gc.setFill(Color.web("#10b981"));
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setEffect(new DropShadow(20, Color.web("#10b981")));
		gc.fillText("Snake v3.0 - orangomango.org", UiElement.rw(this.headerRect.getMinX()+this.headerRect.getWidth()*0.5), UiElement.rh(this.headerRect.getMinY()+this.headerRect.getHeight()*0.65));
		gc.restore();
	}

	private void renderLeaderboard(GraphicsContext gc, ArrayList<Pair<String, int[]>> data) {
		final double bx = this.leaderboardRect.getMinX();
		final double by = this.leaderboardRect.getMinY();
		final double bw = this.leaderboardRect.getWidth();
		final double bh = this.leaderboardRect.getHeight();
		final double headerY = UiElement.rh(by + 0.26 * bh);
		final double userRowY = UiElement.rh(by + 0.93 * bh);

		gc.save();
		gc.setFont(UiElement.FONT_SMALLSMALL);
		gc.setFill(Color.web("#94f7d4"));
		
		gc.setTextAlign(TextAlignment.LEFT);
		gc.fillText("#", UiElement.rw(bx+0.08*bw), headerY);
		gc.fillText("PLAYER", UiElement.rw(bx+0.22*bw), headerY);
		
		gc.setTextAlign(TextAlignment.RIGHT);
		gc.fillText("SCORE", UiElement.rw(bx+0.92*bw), headerY);
		gc.restore();

		for (int i = 0; i < Math.min(data.size(), 6); i++) {
			double rowY = by + (0.33 + (i * 0.075)) * bh;
			Pair<String, int[]> entry = data.get(i);
			
			gc.setFill(i < 3 ? Color.web("#10b981") : Color.web("#cbd5e1"));
			gc.setFont(UiElement.FONT_SMALL);

			gc.setTextAlign(TextAlignment.LEFT);
			gc.fillText(String.valueOf(i+1), UiElement.rw(bx+0.08*bw), UiElement.rh(rowY));
			gc.fillText(entry.getKey(), UiElement.rw(bx+0.22*bw), UiElement.rh(rowY));

			gc.setTextAlign(TextAlignment.RIGHT);
			gc.fillText(String.valueOf(entry.getValue()[0]), UiElement.rw(bx+0.92*bw), UiElement.rh(rowY));
		}

		if (this.account != null){
			String currentUsername = this.account.getUsername();
			int userRank = -1, userScore = 0;
			int count = 1;

			for (Pair<String, int[]> p : data){
				if (p.getKey().equals(currentUsername)){
					userScore = p.getValue()[0];
					userRank = count;
					break;
				}
				count++;
			}

			if (userRank != -1){
				gc.setStroke(Color.web("#10b981"));
				gc.setLineDashes(4, 4);
				gc.strokeRoundRect(UiElement.rw(bx + 0.05 * bw), UiElement.rh(by + 0.82 * bh), UiElement.rw(0.90 * bw), UiElement.rh(0.14 * bh), 10, 10);
				gc.setLineDashes(null);

				gc.setFill(Color.web("#94f7d4"));
				gc.setFont(UiElement.FONT_SMALLSMALL);
				gc.setTextAlign(TextAlignment.LEFT);
				gc.fillText("PERSONAL STATUS", UiElement.rw(bx + 0.08 * bw), UiElement.rh(by + 0.87 * bh));

				gc.setFill(Color.web("#10b981"));
				gc.setFont(UiElement.FONT_SMALL);
				
				gc.fillText("#" + userRank, UiElement.rw(bx + 0.08 * bw), userRowY);
				gc.fillText(currentUsername, UiElement.rw(bx + 0.22 * bw), userRowY);
				gc.setTextAlign(TextAlignment.RIGHT);
				gc.fillText(String.valueOf(userScore), UiElement.rw(bx + 0.92 * bw), userRowY);
			}
		}
	}


	private void selectLeadMode(int index){
		this.currentLeadMode = index;

		switch (index){
			case 0:
				this.leadEasy.setStyle(null, COLOR_EASY, null, COLOR_EASY);
				this.leadMedium.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadHard.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadExtreme.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				break;
			case 1:
				this.leadEasy.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadMedium.setStyle(null, COLOR_MEDIUM, null, COLOR_MEDIUM);
				this.leadHard.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadExtreme.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				break;
			case 2:
				this.leadEasy.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadMedium.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadHard.setStyle(null, COLOR_HARD, null, COLOR_HARD);
				this.leadExtreme.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				break;
			case 3:
				this.leadEasy.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadMedium.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadHard.setStyle(null, Color.web("#1e293b"), null, Color.web("#64748b"));
				this.leadExtreme.setStyle(null, COLOR_EXTREME, null, COLOR_EXTREME);
				break;
		}
	}

	private void fetchLeaderboards(){
		Account temp = this.account == null ? new Account(null, null) : this.account;
		ArrayList<JSONObject> objects = new ArrayList<>();
		objects.add(temp.getLeaderboard("easy"));
		objects.add(temp.getLeaderboard("medium"));
		objects.add(temp.getLeaderboard("hard"));
		objects.add(temp.getLeaderboard("extreme"));

		for (JSONObject ob : objects){
			if (ob == null) continue;
			ArrayList<Pair<String, int[]>> scores = new ArrayList<>();
			for (Object o : ob.getJSONArray("data")){
				JSONObject pl = (JSONObject)o;
				int[] arr = new int[pl.getJSONArray("score").length()];
				for (int i = 0; i < arr.length; i++){
					arr[i] = pl.getJSONArray("score").getInt(i);
				}

				scores.add(new Pair<String, int[]>(pl.getString("name"), arr));
			}
			this.leaderboards.add(scores);
		}

		this.leaderboards.stream().forEach(l -> l.sort((p1, p2) -> {
			int[] l1 = p1.getValue();
			int[] l2 = p2.getValue();
			int len = Math.max(l1.length, l2.length);
			for (int i = 0; i < len; i++){
				int a = i < l1.length ? l1[i] : 0;
				int b = i < l2.length ? l2[i] : 0;
				if (a != b) return Integer.compare(b, a);
			}
			return 0;
		}));
	}
}
