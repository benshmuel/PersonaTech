package HandlerPackage;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by benshmuel on 10/06/2018.
 */
public class ImageLoader {

    static File dir = null;

    // array of supported extensions (use a List if you prefer)
    static final String[] EXTENSIONS = new String[]{"png"// and other formats you need
    };
    // filter to identify images based on their extensions
    static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };


    public ImageLoader(String path) {
        dir= new File(path);


    }
}
