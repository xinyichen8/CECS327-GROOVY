package application;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.sound.sampled.AudioInputStream;

import api.*;
import api.audio.AudioPlayer;
import data.models.*;
import services.LibraryService;

public class Client {

	/**
	 * Created a client socket and streaming the playlist to play the playlist's songs 
	 * @param args - arguments
	 * @throws IOException if input or output is invalid.
	 */
	public static void main(String[] args) throws IOException {
		// get a datagram socket
		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(5000);
		socket.setReceiveBufferSize(60011 * 30 * 100);

		
//		PlayerController pc = new PlayerController(socket);
//		pc.playSong(1);
//		System.out.println("Done");

//		UserController uc = new UserController(socket);
//		User user = uc.getUser("user23", "cecs327");
//		if (user != null) {
//			System.out.println(user.getUserID());
//		}
//		else {
//			System.out.println("Not Found");
//		}
		SearchController sc = new SearchController(socket);
		List<Artist> artists = sc.SearchByArtist("A");
		for (Artist artist : artists) {
			System.out.println(artist.getName());
		}
		
		socket.close();
	}

}
