package hotstu.github.bdzviewer;


import hotstu.github.bdzviewer.ui.PathLoaderFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

public class FileViewerActivity extends ActionBarActivity {
    private PathLoaderFragment list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileviewer);

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(R.id.fragment_content) == null) {
            list = new PathLoaderFragment();
            fm.beginTransaction().replace(R.id.fragment_content, list).commit();
        }
    }
    @Override
    public void onBackPressed() {
        if (!list.onBackPressed())
            super.onBackPressed();
    }
    
}
