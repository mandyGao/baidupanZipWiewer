package hotstu.github.bdzviewer.baiduapi;

/**
 * server_mtime 1415222494
 * category        6
 * fs_id           345678
 * server_ctime    1415222494
 * local_mtime     1415222494
 * size            355765673
 * isdir           0
 * path            "/2.zip"
 * local_ctime     1415222494
 * md5             "dfghjkl"
 * server_filename "2.zip"
 * @author foo
 *
 */
public class FileEntity extends Entity {
    String md5;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

}
