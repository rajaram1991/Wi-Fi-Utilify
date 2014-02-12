package com.example.onlinestreamer;


import android.app.Activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.wifitether.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


public class PickAStream extends Activity {

		private Button streamButton;
		
		private ImageButton playButton;
		
		private EditText textStreamed;
		
		private boolean isPlaying;
		
		private StreamingMediaPlayer audioStreamer;
		
	    public void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        setContentView(R.layout.pick_a_stream);
	        initControls();
	    } 
	    
	    
	    private void initControls() {
	    	textStreamed = (EditText) findViewById(R.id.text_kb_streamed);
			streamButton = (Button) findViewById(R.id.button_stream);
			streamButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					startStreamingAudio();
	        }});

			playButton = (ImageButton) findViewById(R.id.button_play);
			playButton.setEnabled(false);
			playButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (audioStreamer.getMediaPlayer().isPlaying()) {
						audioStreamer.getMediaPlayer().pause();
						playButton.setImageResource(R.drawable.button_play);
					} else {
						audioStreamer.getMediaPlayer().start();
						audioStreamer.startPlayProgressUpdater();
						playButton.setImageResource(R.drawable.button_pause);
					}
					isPlaying = !isPlaying;
	        }});
	    }
	    
	    private void startStreamingAudio() {
	    	try { 
	    		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
	    		if ( audioStreamer != null) {
	    			audioStreamer.interrupt();
	    		}
	    		audioStreamer = new StreamingMediaPlayer(this,textStreamed, playButton, streamButton,progressBar);
	    		audioStreamer.startStreaming(textStreamed.getText().toString(),2340, 149);
	    		//audioStreamer.startStreaming("http://195.122.253.112/public/mp3/Queen/Queen%20'Bohemian%20Rhapsody'.mp3",2340, 149);
	    	} catch (IOException e) {
		    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
	    	}
	    	    	
	    }
	


	/**
	 * MediaPlayer does not yet support streaming from external URLs so this class provides a pseudo-streaming function
	 * by downloading the content incrementally & playing as soon as we get enough audio in our temporary storage.
	 */
	private class StreamingMediaPlayer {

	    private static final int INTIAL_KB_BUFFER =  128*10/8;//assume 96kbps*10secs/8bits per byte
		//private static final int INTIAL_KB_BUFFER =  256*10/8;//assume 96kbps*10secs/8bits per byte
		
		private EditText textStreamed;
		
		private ImageButton playButton;
		
		private ProgressBar	progressBar;
		
		//  Track for display by progressBar
		private long mediaLengthInKb, mediaLengthInSeconds;
		private int totalKbRead = 0;
		
		// Create Handler to call View updates on the main UI thread.
		private final Handler handler = new Handler();

		private MediaPlayer 	mediaPlayer;
		
		private File downloadingMediaFile; 
		
		private boolean isInterrupted;
		
		private Context context;
		
		private int counter = 0;
		
	 	public StreamingMediaPlayer(Context  context,EditText textStreamed, ImageButton	playButton, Button	streamButton,ProgressBar	progressBar) 
	 	{
	 		this.context = context;
			this.textStreamed = textStreamed;
			this.playButton = playButton;
			this.progressBar = progressBar;
		}
		
	    /**  
	     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
	     */  
	    public void startStreaming(final String mediaUrl, long	mediaLengthInKb, long	mediaLengthInSeconds) throws IOException {    	

	    	this.mediaLengthInKb = mediaLengthInKb;
	    	this.mediaLengthInSeconds = mediaLengthInSeconds;
	    	
			Runnable r = new Runnable() {   
		        public void run() {   
		            try {   
		        		downloadAudioIncrement(mediaUrl);
		            } catch (IOException e) {
		            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
		            	return;
		            }   
		        }   
		    };   
		    new Thread(r).start();
	    }
	    
	    /**  
	     * Download the url stream to a temporary location and then call the setDataSource  
	     * for that local file
	     */  
	    public void downloadAudioIncrement(String mediaUrl) throws IOException {

	    	/*URLConnection cn = new URL(mediaUrl).openConnection(Proxy.NO_PROXY);  

	        cn.connect();  

	        InputStream stream = cn.getInputStream();*/
	    	URL url = new URL(mediaUrl);
	    	
	    	InputStream stream = url.openStream();
	    	//Toast.makeText(this.context, "here3", Toast.LENGTH_LONG);
	    	
	        if (stream == null) {
	        	Log.e(getClass().getName(), "Unable to create InputStream for mediaUrl:" + mediaUrl);
	        }

			//downloadingMediaFile = new File(context.getCacheDir(),"downloadingMedia_" + (counter++) + ".dat");
	        downloadingMediaFile = new File(context.getCacheDir(),"downloadingMedia_.dat");
	        FileOutputStream out = new FileOutputStream(downloadingMediaFile);   
	        byte buf[] = new byte[16384];

	        int totalBytesRead = 0, incrementalBytesRead = 0;
	        do {
	        	int numread = stream.read(buf);   
	            if (numread <= 0)
	                break;   

	            out.write(buf, 0, numread);
	            totalBytesRead += numread;
	            incrementalBytesRead += numread;
	            totalKbRead = totalBytesRead/1000;
	            
	            testMediaBuffer();
	           	fireDataLoadUpdate();
	        } while (validateNotInterrupted());   

	        if (validateNotInterrupted()) {
		       	fireDataFullyLoaded();
	            //testMediaBuffer();
	           	//fireDataLoadUpdate();
	        }
	       	stream.close();
	    }  

	    private boolean validateNotInterrupted() {
			if (isInterrupted) {
				if (mediaPlayer != null) {
					Log.e("StreamingMediaPlayer", "oh nos pausing!");
					mediaPlayer.pause();
					//mediaPlayer.release();
				}
				return false;
			} else {
				return true;
			}
	    }

	    
	    /**
	     * Test whether we need to transfer buffered data to the MediaPlayer.
	     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
	     */  
	    private void  testMediaBuffer() {
	        //Log.e("StreamingMediaPlayer", "in testmediabuffer");
		    Runnable updater = new Runnable() {
		        public void run() {
		            if (mediaPlayer == null) {
		            	//  Only create the MediaPlayer once we have the minimum buffered data
		            	if ( totalKbRead >= INTIAL_KB_BUFFER) {
		            		try {
			            		startMediaPlayer();
		            		} catch (Exception e) {
		            			Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
		            		}
		            	}
		            } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
		            	//  NOTE:  The media player has stopped at the end so transfer any existing buffered data
		            	//  We test for < 1second of data because the media player can stop when there is still
		            	//  a few milliseconds of data left to play
		            	transferBufferToMediaPlayer();
		            } else {
//		                Log.e("StreamingMediaPlayer", "duration: " + mediaPlayer.getDuration() + 
//		                		" current pos: " + mediaPlayer.getCurrentPosition());
		            }
		        }
		    };
		    handler.post(updater);
	    }
	    
	    private void startMediaPlayer() {
	        try {   
	        	
	        	
		    	//File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");
	        	File bufferedFile = new File(context.getCacheDir(),"playingMedia.dat");
	        	moveFile(downloadingMediaFile,bufferedFile);
	    		
	        	Log.e("Player",bufferedFile.length()+"");
	        	Log.e("Player",bufferedFile.getAbsolutePath());
	        	
	    		mediaPlayer = new MediaPlayer();
	    		FileInputStream fis = new FileInputStream(bufferedFile); 
	    		FileDescriptor fd = fis.getFD(); 
	    		mediaPlayer.setDataSource(fd);
	        	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	    		mediaPlayer.prepare();
	        	fireDataPreloadComplete();
	        	
	        } catch (IOException e) {
	        	Log.e(getClass().getName(), "Error initializing the MediaPlaer.", e);
	        	return;
	        }   
	    }
	    
	    /**
	     * Transfer buffered data to the MediaPlayer.
	     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
	     */  
	    private void transferBufferToMediaPlayer() {
		    try {

	            Log.e("StreamingMediaPlayer", "before duration: " + mediaPlayer.getDuration() + 
	            		" current pos: " + mediaPlayer.getCurrentPosition());
		    	// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
		    	boolean wasPlaying = mediaPlayer.isPlaying();
		    	int curPosition = mediaPlayer.getCurrentPosition();
		    	mediaPlayer.reset();

		    	//File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");
		    	File bufferedFile = new File(context.getCacheDir(),"playingMedia.dat");
		    	moveFile(downloadingMediaFile,bufferedFile);
	        	
				//mediaPlayer = new MediaPlayer();
	    		FileInputStream fis = new FileInputStream(bufferedFile);
	    		//BufferedInputStream bis = new BufferedInputStream(fis);
	    		FileDescriptor fd = fis.getFD(); 
	    		mediaPlayer.setDataSource(fd);
	    		fis.close();
	    		//mediaPlayer.setDataSource(bufferedFile.getAbsolutePath());
	    		//mediaPlayer.setAudioStreamType(AudioSystem.STREAM_MUSIC);
	    		mediaPlayer.prepare();
	    		mediaPlayer.seekTo(curPosition);
	    		

	            Log.e("StreamingMediaPlayer", "after duration: " + mediaPlayer.getDuration() + 
	            		" current pos: " + mediaPlayer.getCurrentPosition());
	    		
	    		//  Restart if at end of prior beuffered content or mediaPlayer was previously playing.  
	    		//	NOTE:  We test for < 1second of data because the media player can stop when there is still
	        	//  a few milliseconds of data left to play
	    		boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
	        	if (wasPlaying || atEndOfFile){
	        		Log.e("StreamingMediaPlayer", "Restarting media player");
	        		mediaPlayer.start();
	        	}
	        	else
	        	{
	        		Log.e("StreamingMediaPlayer", "Not restarting media player");
	        	}
	        	
			}catch (Exception e) {
		    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
			}
	    }
	    
	    private void fireDataLoadUpdate() {
			Runnable updater = new Runnable() {
		        public void run() {
		        	textStreamed.setText((CharSequence) (totalKbRead + " Kb read"));
		    		float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);
		    		progressBar.setSecondaryProgress((int)(loadProgress*100));
		        }
		    };
		    handler.post(updater);
	    }
	    
	    /**
	     * We have preloaded enough content and started the MediaPlayer so update the buttons & progress meters.
	     */
	    private void fireDataPreloadComplete() {
	    	Runnable updater = new Runnable() {
		        public void run() {
		    		mediaPlayer.start();
		    		startPlayProgressUpdater();
		        	playButton.setEnabled(true);
		        	//streamButton.setEnabled(false);
		        }
		    };
		    handler.post(updater);
	    }

	    private void fireDataFullyLoaded() {
			Runnable updater = new Runnable() { 
				public void run() {
	   	        	transferBufferToMediaPlayer();
		        	textStreamed.setText((CharSequence) ("Audio full loaded: " + totalKbRead + " Kb read"));
		        }
		    };
		    handler.post(updater);
	    }
	    
	    public MediaPlayer getMediaPlayer() {
	    	return mediaPlayer;
		}
		
	    public void startPlayProgressUpdater() {
	    	float progress = (((float)mediaPlayer.getCurrentPosition()/1000)/(float)mediaLengthInSeconds);
	    	progressBar.setProgress((int)(progress*100));
	    	
			if (mediaPlayer.isPlaying()) {
				Runnable notification = new Runnable() {
			        public void run() {
			        	startPlayProgressUpdater();
					}
			    };
			    handler.postDelayed(notification,1000);
	    	}
			else
			{
				Log.e("StreamingMediaPlayer", "Why is the mediaplayer not playing?");
			}
	    }    
	    
	    public void interrupt() {
	    	playButton.setEnabled(false);
	    	isInterrupted = true;
	    	validateNotInterrupted();
	    }
	    
		public void moveFile(File	oldLocation, File	newLocation)
		throws IOException {

			if ( oldLocation.exists( )) {
				BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
				BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
	            try {
			        byte[]  buff = new byte[8192];
			        int numChars;
			        while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
			        	writer.write( buff, 0, numChars );
	      		    }
	            } catch( IOException ex ) {
					throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
	            } finally {
	                try {
	                    if ( reader != null ){
	                    	writer.close();
	                        reader.close();
	                    }
	                } catch( IOException ex ){
					    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
					}
	            }
	        } else {
				throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
	        }
		}
		
	}
}
