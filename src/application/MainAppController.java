package application;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javax.sound.sampled.AudioInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


import api.AudioPlayer;
import api.PlayerController;
import api.Searcher;
import api.SongController;
import api.UserProfileController;
import data.models.Album;
import data.models.Artist;
import data.models.Playlist;
import data.models.Song;
import data.models.SongInfo;
import data.models.User;
import data.models.UserProfile;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import com.sun.prism.impl.Disposer;

public class MainAppController implements Initializable {

	//global variables
	@FXML
	private ListView<?> playlistScreen;
	@FXML
	private ImageView addPlaylist;
	@FXML
	private ImageView playMusic;
	@FXML
	private ImageView previousMusic;
	@FXML
	private ImageView nextMusic;
	@FXML
	private ImageView stopMusic;
	@FXML
	private ImageView Mute;
	@FXML
	private ImageView queue;
	@FXML
	private Text songName;
	@FXML
	private Text artistName;
	@FXML
	private Label userNameText;
	@FXML
	private Slider sldVolume;
	@FXML
	private TableView<Object> Result;
	@FXML
	private TableColumn<Object,String> col1;
	@FXML
	private TableColumn<Object,String> col2;
	@FXML
	private TableColumn<Object,String> col3;
	@FXML
	private TableColumn<Object, String> col4;

	@FXML
	private TableView<Playlist> playlistTable;
	@FXML
	private TableColumn<Disposer.Record,Boolean> delete;
	@FXML
	private TableColumn<Playlist,String> playlistName;
	@FXML
	private ImageView btnAlbum;
	@FXML
	private ImageView btnSong;
	@FXML
	private ImageView btnArtist;
	@FXML
	private TextField txtSearch;
	@FXML
	private ImageView btnPlayList;
	@FXML
	private Text txtResult;
	

	private boolean isSearch = false;
	private int table = 0; //0 for playlist table, 1 for search table 
	private boolean isPlaying = false;
	private Playlist currentPlaylist = null;
	private static User currentUser = MainController.getUser();
	private DatagramSocket socket;
	private UserProfileController upc ;
	private UserProfile user;
	private List<Playlist> playlist;
	private ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private ObservableList<Object> userSong = FXCollections.observableArrayList();
	private ObservableList<Object> songs = FXCollections.observableArrayList();
	private ObservableList<Object> artistSong = FXCollections.observableArrayList();
	private ObservableList<Object> albumSong = FXCollections.observableArrayList();

	private SongController sc = new SongController();

	// Audio player
	private PlayerController player;
	api.audio.AudioPlayer ap = new api.audio.AudioPlayer();
	private Searcher search = new Searcher();

	/**
	 * Initializing server/client sockets
	 * Initializing the display for the user based on the username
	 * Displaying the user's playlist profile
	 * Inserting buttons
	 * @param arg0 - {URL} the first argument
	 * @param arg1 - {ResourceBundle} the second argument
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(5000);
			socket.setReceiveBufferSize(60011 * 30 * 100);
			player= new PlayerController(socket);
			upc = new UserProfileController(socket);
			user = upc.GetUserProfile(currentUser.getUserID());
			playlist = user.getPlaylists();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Change the label to the username
		userNameText.setText(currentUser.getUsername());
		setTabletoPlaylist();
		for (int i = 0; i< playlist.size();i++)
		{
			playlists.add(playlist.get(i));
		}

		//display playlist name on sidebar
		playlistName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
		playlistTable.setItems(playlists);
		
		
		//disable the playlist button 
		btnPlayList.setVisible(false);
		
	}

	

	
	/**
	 * When user double click the playlist, it will display the songs in the playlist on the right
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void clickItem(MouseEvent event)
	{
		table=0;
		Playlist userChoose = playlistTable.getSelectionModel().getSelectedItem();
		
		//when user right click and try to delete a playlist
		if (event.getButton()==MouseButton.SECONDARY)
		{
			MenuItem delete = new MenuItem("Delete");
			ContextMenu contextMenu = new ContextMenu();
	        contextMenu.getItems().addAll(delete);
	        contextMenu.show(playlistTable,event.getScreenX(),event.getScreenY());

			delete.setOnAction(new EventHandler<ActionEvent>() {

	            @Override
	            public void handle(ActionEvent event) {
	            	try {
						upc.DeletePlaylist(userChoose.getPlaylistID());
					} catch (IOException e) {
						e.printStackTrace();
					}
	            	playlists.removeAll(playlists);
	    			playlist=upc.GetPlaylists();
	    			for(int i = 0; i<playlist.size();i++)
	    			{
	    				playlists.add(playlist.get(i));
	    			}
                	playlistName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
            		playlistTable.setItems(playlists);
                	playlistTable.refresh();
	            }
	        });
		}

		isSearch=false;
	    if (event.getClickCount() == 2) //Checking double click
	    {
	    	// set the playlist btn to be visible
	    	btnPlayList.setVisible(true);
	    	setTabletoPlaylist();
	    	currentPlaylist = userChoose;
	    	for(int i = 0; i<Result.getItems().size();i++)
			{
				Result.getItems().clear();
			}
	    	
	    	playlists.removeAll(playlists);
			playlist=upc.GetPlaylists();
			
			for(int i = 0; i<playlist.size();i++)
			{
				playlists.add(playlist.get(i));
			}
			for(int j = 0; j<playlists.size(); j++)
			{
				if (playlists.get(j).getPlaylistID()==currentPlaylist.getPlaylistID())
				{
					updatePlayTable(playlists.get(j));
				}
			}
	    }
	}

	/**
	 * update the playlist table
	 * @param userChoose the playlist user selected
	 */
	public void updatePlayTable(Playlist userChoose)
	{
		List<Song> songPlay = new ArrayList<Song>();
		
        ArrayList<SongInfo> songin = (ArrayList<SongInfo>) userChoose.getSongInfos();
        userSong.removeAll(userSong);
        
        if (songin!=null && !songin.isEmpty())
        {
        	for (int i = 0; i<userChoose.getSongInfos().size();i++)
	        {
	        	songPlay.add(userChoose.getSongInfos().get(i).getSong());
	        	userSong.add(userChoose.getSongInfos().get(i));

	        }
        	// _________________** this should work but its not **__________________

        	txtResult.setText(userChoose.getName());

	        //display object to the table
			col1.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(((SongInfo) cellData.getValue()).getSong().getTitle()));
//			col2.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(atc.GetArtistBySongTitle(((SongInfo) cellData.getValue()).getSong().getTitle()).getName()));
//			col3.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(amc.GetAlbumBySongTitle(((SongInfo) cellData.getValue()).getSong().getTitle()).getName()));
			col4.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(((SongInfo) cellData.getValue()).getAddedDate().toString()));

			Result.setItems(userSong);
			Result.refresh();

        }
	}
	/**
	 * Search if song button is clicked then
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void btnSongClick(MouseEvent event)
	{
		table=1;
		search(txtSearch.getText(), "song");

	}

	/**
	 * Search if album button is clicked then
	 * @param event - {MouseEvent} the action
	 * @throws IOException if input or output is invalid.
	 */
	@FXML
	public void btnAlbumClick(MouseEvent event) throws IOException
	{
		table=1;
		search(txtSearch.getText(), "album");
	}

	/**
	 * Search if artist button is clicked then
	 * @param event - {MouseEvent} the action
	 * @throws IOException if input or output is invalid.
	 */
	@FXML
	public void btnArtistClick(MouseEvent event) throws IOException
	{
		table=1;
		search(txtSearch.getText(), "artist");
	}
	
	
	// ____________________________________________________________ HERE ______________________________________________________________
	


	// when btn is clicked > image
	Image pic1 = new Image("file:resources/if-close.png");
	
	// when btn is unclicked > img
	Image pic2 = new Image("file:/resources/if-play.png");

	boolean isPlayListClicked = false;
	
	@FXML
	public void btnPlayListClicked(MouseEvent event)
	{			
		
		if(isPlayListClicked == false) {
			btnPlayList.setImage(pic2);
			
			// Should be List<Song>
			Playlist playlist = upc.GetPlaylists().get(0);
			List<Song> songs = new ArrayList<Song>();
			
			for (SongInfo info : playlist.getSongInfos()) {
				songs.add(info.getSong());
			}
			player.loadSongs(songs);
			player.playQueue();
			
			
		}else if(isPlayListClicked == true) {
			btnPlayList.setImage(pic1);
			

		}
		
	}
	
	
	
	

	/**
	 * When row in table isClicked
	 * @param event - {MouseEvent} the action
	 * @throws IOException if input or output is invalid.
	 */
	@FXML
	public void playTable(MouseEvent event) throws IOException
	{
		if (event.getButton()==MouseButton.SECONDARY)
		{
			if (table==1)
			{
				Song userChoose = (Song) Result.getSelectionModel().getSelectedItem();
				MenuItem add = new MenuItem("Add");
				ContextMenu contextMenu = new ContextMenu();
		        contextMenu.getItems().addAll(add);
		        contextMenu.show(playlistTable,event.getScreenX(),event.getScreenY());

				add.setOnAction(new EventHandler<ActionEvent>() {

		            @Override
		            public void handle(ActionEvent event) {
		            	ArrayList<String> dropDown = new ArrayList<String>();
	                	if (playlist!=null && !playlist.isEmpty())
	                	{
	                		for(int i = 0; i<playlist.size();i++)
	            			{
	                    		dropDown.add(playlist.get(i).getName());
	            			}
	                		ChoiceDialog<String> dialog = new ChoiceDialog<>(dropDown.get(0), dropDown);
	                    	dialog.setTitle("Playlist");
	                    	dialog.setHeaderText("Select Playlist");
	                    	dialog.setContentText("Please select the playlist you wish to add to");
	                    	Optional<String> result = dialog.showAndWait();
	                    	if (result.isPresent())
	                    	{
	                    		Date date = new Date();
	        					SongInfo newSong = new SongInfo(userChoose, date);
	        					for (int i = 0; i < playlist.size(); i++)
	        					{
	        						if (playlist.get(i).getName().equals(result.get()))
	        						{
	        							try {
	        								upc.AddSongToPlaylist(playlist.get(i).getPlaylistID(), userChoose.getSongID());
	        							} catch(IOException e) {
	        								e.printStackTrace();
	        							}
	        						}
	        					}
	                    	}
	
	                	}
	                	else
	                	{
	                		Alert error = new Alert(Alert.AlertType.ERROR);
	            			error.setTitle("No playlist");
	            			error.setHeaderText("There is no playlist");
	                        error.setContentText("Please create a new playlist before you add a song");
	                        Stage errorStage = (Stage) error.getDialogPane().getScene().getWindow();
	                        error.showAndWait();
	                	}
		            }
		        });
			}
			else if(table==0) //playlist display in the center if clicked happens
			{
				SongInfo userChoose = (SongInfo) Result.getSelectionModel().getSelectedItem();
				MenuItem delete = new MenuItem("Delete");
				ContextMenu contextMenu = new ContextMenu();
		        contextMenu.getItems().addAll(delete);
		        contextMenu.show(playlistTable,event.getScreenX(),event.getScreenY());

				delete.setOnAction(new EventHandler<ActionEvent>() {

		            @Override
		            public void handle(ActionEvent event) 
		            {
		            	try {
							upc.DeleteSongFromPlaylist(currentPlaylist.getPlaylistID(), userChoose.getSong().getSongID());
						} catch (IOException e) {
							e.printStackTrace();
						}
		            	playlists.removeAll(playlists);
		    			playlist=upc.GetPlaylists();
		    			for(int i = 0; i<playlist.size();i++)
		    			{
		    				playlists.add(playlist.get(i));
		    			}
		    			for(int j = 0; j<playlists.size(); j++)
		    			{
		    				if (playlists.get(j).getPlaylistID()==currentPlaylist.getPlaylistID())
		    				{
		    					updatePlayTable(playlists.get(j));
		    				}
		    				
		    			}
		            	
		            }
		            
		            });
			}
		}
		
		if (event.getClickCount() == 2) //Checking double click
	    {
			if(isSearch== true)
			{
				Song song = (Song) Result.getSelectionModel().getSelectedItem();
				List<Song> songs = new ArrayList <Song>();
				songs.add(song);
				player.loadSongs(songs);
				player.playQueue();
				
//				ap.stop();
//				ap.play(songId, false);
//				songName.setText(userSong.getTitle());
//				String artist = atc.GetArtistBySongTitle(userSong.getTitle()).getName();
//				artistName.setText(artist);
			}
			else
			{
				SongInfo song = (SongInfo) Result.getSelectionModel().getSelectedItem();
				List<Song> songs = new ArrayList <Song>();
				songs.add(song.getSong());
				player.loadSongs(songs);
				player.playQueue();
				//				ap.stop();
//				ap.play(songId, false);
//				songName.setText(userSong.getSong().getTitle());
//				String artist = atc.GetArtistBySongTitle(userSong.getSong().getTitle()).getName();
//				artistName.setText(artist);
			}

	    }
	}

	/**
	 * Searching for the user's input in the list of of all songs
	 * @param text - {String} the name of the object being searched
	 * @param type - {String} the type of object that is searched
	 */
	private void search(String text, String type) {
		isSearch=true;
		if(type == "song") {
			//update result page to search for that song
			List<Song> song=search.findFromSongs(text);
			setSearchSong(song);
		} else if(type == "album") {
			//update result page to search for that album
			List<Album> album=search.findFromAlbums(text);;
			setSearchAlbum(album);
		} else if(type == "artist") {
			//update result page to search for that artist
			List<Artist> artist=search.findFromArtists(text);
			setSearchArtist(artist);
		}
	}

	/**
	 * Search song according to song name
	 * Displaying the song results based on the user's search
	 * @param song - {List} list of song objects
	 */
	public void setSearchSong(List<Song> song)
	{
		for(int i = 0; i<Result.getItems().size();i++)
		{
			Result.getItems().clear();
		}
		Result.refresh();
		for (int i = 0; i< song.size();i++)
		{
			songs.add(song.get(i));
		}
		col1.setText("Song");
		col2.setText("Artist");
		col3.setText("Album");
		col4.setText("Duration");
		
		//display object to the table
		col1.setCellValueFactory(cellData ->  new ReadOnlyStringWrapper(((Song) cellData.getValue()).getTitle()));
//		col2.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(atc.GetArtistBySongTitle(((Song) cellData.getValue()).getTitle()).getName()));
//		col3.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(amc.GetAlbumBySongTitle(((Song) cellData.getValue()).getTitle()).getName()));
		col4.setCellValueFactory(cellData ->  new ReadOnlyStringWrapper(sc.FormatDuration(((Song) cellData.getValue()).getDuration())));


		Result.setItems(songs);
		Result.refresh();
	}

	/**
	 * Search album according to album name
	 * Displaying the album results based on the user's search
	 * @param album - {List} list of album objects
	 */
	public void setSearchAlbum(List<Album> album)
	{
		for(int i = 0; i<Result.getItems().size();i++)
		{
			Result.getItems().clear();
		}
		Result.refresh();
		for (int i = 0; i< album.size();i++)
		{
			for (int j = 0; j<album.get(i).getSongs().size();j++)
			{
				albumSong.add(album.get(i).getSongs().get(j));
			}
		}
		col1.setText("Song");
		col2.setText("Artist");
		col3.setText("Album");
		
		col4.setText("Duration");

		//display object to the table

		col1.setCellValueFactory(cellData -> new ReadOnlyStringWrapper (((Song) cellData.getValue()).getTitle()));
//		col2.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(atc.GetArtistBySongTitle(((Song) cellData.getValue()).getTitle()).getName()));
//		col3.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(amc.GetAlbumBySongTitle(((Song) cellData.getValue()).getTitle()).getName()));

		col4.setCellValueFactory(cellData ->  new ReadOnlyStringWrapper(sc.FormatDuration(((Song) cellData.getValue()).getDuration())));


		Result.setItems(albumSong);
		Result.refresh();
	}

	/**
	 * Search artist according to artist name
	 * Displaying the artist results based on the user's search
	 * @param artist - {List} list of artist objects
	 */
	public void setSearchArtist(List<Artist> artist)
	{
		for(int i = 0; i<Result.getItems().size();i++)
		{
			Result.getItems().clear();
		}
		Result.refresh();
		for (int i = 0; i< artist.size();i++)
		{
			for(int j = 0; j < artist.get(i).getAlbums().size();j++)
			{
				for(int k = 0; k< artist.get(i).getAlbums().get(j).getSongs().size();k++)
				{
					artistSong.add(artist.get(i).getAlbums().get(j).getSongs().get(k));
				}
			}
		}
		col2.setText("Song");
		col1.setText("Artist");
		col3.setText("Album");
		col4.setText("Duration");

		//display object to the table
		col1.setCellValueFactory(cellData -> new ReadOnlyStringWrapper (((Song) cellData.getValue()).getTitle()));
//		col2.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(atc.GetArtistBySongTitle(((Song) cellData.getValue()).getTitle()).getName()));
//		col3.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(amc.GetAlbumBySongTitle(((Song) cellData.getValue()).getTitle()).getName()));
//		col4.setCellValueFactory(cellData ->  new ReadOnlyStringWrapper(sc.FormatDuration(((Song) cellData.getValue()).getDuration())));
		
		System.out.print(sc.FormatDuration(artist.get(0).getAlbums().get(0).getSongs().get(0).getDuration()));

		Result.setItems(artistSong);
		Result.refresh();
	}

	/**
	 * Set center panel to be playlist panel
	 */
	public void setTabletoPlaylist()
	{
		col1.setText("Name");
		col2.setText("Artist");
		col3.setText("Album");
		col4.setText("DateAdded");
	}

	/**
	 * Event Listener on ImageView[#addPlaylist].onMouseClicked to add the song to the playlist
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void addPlaylistClicked(MouseEvent event)
	{
		showInputBox();
	}
	
	@FXML
	public void selectQueue(MouseEvent event)
	{
		
	}

	/**
	 * Prompt user to input new playlist name
	 */
	public void showInputBox()
	{
		TextInputDialog dialog = new TextInputDialog("");

		dialog.setTitle("Enter Playlist Name");
		dialog.setHeaderText("Enter the playlist name:");
		dialog.setContentText("Name:");
		Optional<String> result = dialog.showAndWait();
		//create new playlist
		if(result.isPresent())
		{
			String playlistName = result.get();
			
			try {
				upc.CreatePlaylist(playlistName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			playlists.removeAll(playlists);
			playlist=upc.GetPlaylists();
			for(int i = 0; i<playlist.size();i++)
			{
				playlists.add(playlist.get(i));
			}
			playlistTable.setItems(playlists);
			playlistTable.refresh();
		}
		playlistTable.refresh();
	}

	/**
	 * Event Listener on ImageView[#playMusic].onMouseClicked to play the song at specific time
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void playMusicClicked(MouseEvent event)
	{

		player.resume();
	}

	
	private EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
	    @Override
	    public void handle(KeyEvent event) {
	        if(event.getCode() == KeyCode.LEFT) 
	        {
	            player.previous();
	        } 
	        else if(event.getCode() == KeyCode.RIGHT)
	        {
	        	player.next();
	        }
	        else if(event.getCode() == KeyCode.SPACE) {
	            player.pause();
	        }
	        event.consume();
	    }
	};
	
	
	
	@FXML
	public void spacePress(KeyEvent e)
	{
		keyListener.handle(e);
	}
	
	@FXML
	public void rightPress(KeyEvent e)
	{
		keyListener.handle(e);
	}
	
	@FXML
	public void leftPress(KeyEvent e)
	{
		keyListener.handle(e);
	}
	
	/**
	 * Event Listener on ImageView[#previousMusic].onMouseClicked to play previous song
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void previousIsClicked(MouseEvent event)
	{
		player.previous();
	}

	/**
	 * Event Listener on ImageView[#nextMusic].onMouseClicked to play the next song
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void nextIsClicked(MouseEvent event)
	{
		player.next();
	}

	/**
	 * Event Listener on ImageView[#stopMusic].onMouseClicked to stop the song at the current time
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void musicStopClicked(MouseEvent event)
	{
		player.pause();
	}

	/**
	 * Event Listener on ImageView[#Mute].onMouseClicked to mute the song
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void muteIsClicked(MouseEvent event)
	{
		player.setVolume(0);
	}
	// Event Listener on ImageView[#exit].onMouseClicked


	@FXML
	public void shuffleClicked(MouseEvent event)
	{
		//call the shuffle function
	}
	@FXML
	public void repeatClicked(MouseEvent event)
	{
		//call the repeat function
	}

	
	
	/**
	 * The volume slider is not working yet
	 * @param event - {MouseEvent} the action
	 */
	@FXML
	public void onSliderChanged(MouseEvent event) {
	    sldVolume.valueProperty().addListener(new ChangeListener<Object>() {

            @Override
            public void changed(ObservableValue<?> arg0, Object arg1, Object arg2) {
            	float sliderValue = (float) sldVolume.getValue();
            	player.setVolume(sliderValue);
            }
        });


	}
}
