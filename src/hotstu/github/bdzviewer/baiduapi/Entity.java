package hotstu.github.bdzviewer.baiduapi;

public abstract class Entity {
    /** fs_id 345678 **/
    String id;
    
    /** server_filename "shared" **/
    String filename;
    
    /** category 6 **/
    int category;

    /** size 0 **/
    long size;
    
    /** path "/shared" **/
    String path;
    
    /** isdir 1 **/
    boolean isdir;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIsdir() {
        return isdir;
    }

    public void setIsdir(boolean isdir) {
        this.isdir = isdir;
    }


}
