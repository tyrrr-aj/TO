import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CrawlerApp  {

    private static List<String> TOPICS = Arrays.asList("legwan zielony", "agama b³otna", "kameleon jemeñski", "anolis", "bazyliszek che³miasty");


    public static void main(String[] args) throws IOException {
        PhotoCrawler photoCrawler = new PhotoCrawler();
        photoCrawler.resetLibrary();
//        photoCrawler.downloadPhotoExamples();
//        photoCrawler.downloadPhotosForQuery(TOPICS.get(0));
        photoCrawler.advancedDownloadPhotosForMultipleQueries(TOPICS);
        try {
			Thread.sleep(100_000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}