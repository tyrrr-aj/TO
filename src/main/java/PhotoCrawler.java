import model.Photo;
import model.PhotoSize;
import util.PhotoDownloader;
import util.PhotoProcessor;
import util.PhotoSerializer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.schedulers.Schedulers;

public class PhotoCrawler {

    private static final Logger log = Logger.getLogger(PhotoCrawler.class.getName());

    private final PhotoDownloader photoDownloader;

    private final PhotoSerializer photoSerializer;

    private final PhotoProcessor photoProcessor;

    public PhotoCrawler() throws IOException {
        this.photoDownloader = new PhotoDownloader();
        this.photoSerializer = new PhotoSerializer("./photos");
        this.photoProcessor = new PhotoProcessor();
    }

    public void resetLibrary() throws IOException {
        photoSerializer.deleteLibraryContents();
    }

    public void downloadPhotoExamples() {
        try {
        	photoDownloader.getPhotoExamples().subscribe(photo -> photoSerializer.savePhoto(photo),
        			error -> log.log(Level.SEVERE, "Downloading photo examples error", error));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Downloading photo examples error", e);
        }
    }

    public void downloadPhotosForQuery(String query) throws IOException {
        photoDownloader.searchForPhotos(query)
        			   .compose(processPhotos())
        			   .subscribe(photo -> photoSerializer.savePhoto(photo),
        					   error -> log.log(Level.SEVERE, "Downloading photo examples error", error));
    }
    
    public void downloadPhotosForMultipleQueries(List<String> queries) {
    	photoDownloader.searchForPhotos(queries)
    				   .compose(processPhotos())
    				   .subscribe(photo -> photoSerializer.savePhoto(photo),
    						   error -> log.log(Level.SEVERE, "Downloading photo examples error", error));
    }
    
    public ObservableTransformer<Photo, Photo> processPhotos() {
    	return new ObservableTransformer<Photo, Photo>() {
			@Override
			public ObservableSource<Photo> apply(Observable<Photo> upstream) {
				return upstream.filter(photo -> photoProcessor.isPhotoValid(photo))
						.map(photo -> photoProcessor.convertToMiniature(photo));
			}
    	};
    }
    
    public void advancedDownloadPhotosForMultipleQueries(List<String> queries) {
    	photoDownloader.searchForPhotos(queries)
    				   .groupBy(photo -> PhotoSize.resolve(photo))
    				   .subscribe(group -> {
    						   if (group.getKey().equals(PhotoSize.SMALL)) {
    							   group.take(0);
    						   }
    						   else if (group.getKey().equals(PhotoSize.MEDIUM)) {
    							   group
    							   		.buffer(5, TimeUnit.SECONDS)
    							   		.subscribe(photosList -> {for (Photo photo : photosList) photoSerializer.savePhoto(photo);},
    									   		   error -> log.log(Level.SEVERE, "Downloading photo examples error", error));
    						   }
    						   else {
    							   group
	    							   .observeOn(Schedulers.computation())
	    							   .map(photo -> photoProcessor.convertToMiniature(photo))
	    							   .subscribe(photo -> photoSerializer.savePhoto(photo),
	    									   	  error -> log.log(Level.SEVERE, "Downloading photo examples error", error));
    						   }
    				   		}
    					);
    }
}
