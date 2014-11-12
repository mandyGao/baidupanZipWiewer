package hotstu.github.bdzviewer.ui;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import hotstu.github.bdzviewer.R;
import hotstu.github.bdzviewer.adapter.FileInfoAdaper;
import hotstu.github.bdzviewer.baiduapi.Entity;
import hotstu.github.bdzviewer.baiduapi.RESTBaiduPathLoader;
import hotstu.github.bdzviewer.baiduapi.UnzipTask;
import hotstu.github.bdzviewer.utils.FileUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class PathLoaderFragment extends ListFragment implements
        LoaderCallbacks<List<Entity>> {
    /** 当前路径，例如/myfolder/ **/
    private String curPath;
    
    private Stack<String> mPathStack;

    private FileInfoAdaper mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new FileInfoAdaper(getActivity(),
                R.layout.list_item_icon_text);
        setListAdapter(mAdapter);
        setListShown(false);
        
        if (savedInstanceState != null && savedInstanceState.getSerializable("stack") != null) {
            mPathStack = (Stack<String>) savedInstanceState.getSerializable("stack");
            curPath = mPathStack.pop();
        }
        else {
            mPathStack = new Stack<String>();
            curPath = "/";
        }
        getLoaderManager().initLoader(0, null, this);
    }
    
    /**
     * 
     * @return ture if 已经处理
     */
    public boolean onBackPressed(){
        String backpath = getBackPath();
        if (backpath == null) {
            return false;
        }
        else {
            setListShown(false);
            System.out.println("path:"+backpath);
            Bundle args = new Bundle();
            args.putString("dir", backpath);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        }
       
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("stack", mPathStack);
    }



    @Override
    public Loader<List<Entity>> onCreateLoader(int id, Bundle args) {
        return new RESTBaiduPathLoader(getActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<List<Entity>> loader, List<Entity> data) {
        mAdapter.clear();
        if (data != null) {
            mAdapter.addAll(data);
        }
        
        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Entity>> loader) {
        Log.d("PathLoaderFragment", "onLoaderReset");
        mAdapter.clear();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Entity e = (Entity) l.getItemAtPosition(position);
        if (e.isIsdir() ) {
            setListShown(false);
            System.out.println("path:"+e.getPath()+" name:"+e.getFilename());
            Bundle args = new Bundle();
            args.putString("dir", e.getPath());
            getLoaderManager().restartLoader(0, args, this);
            
            if (!curPath.equals(e.getPath())) {
                mPathStack.push(curPath);
                curPath = e.getPath();
            }
            return;
        }
        if (!e.isIsdir() && FileUtil.isZipfile(e.getFilename())) {
            String path = e.getPath();
            Log.d("onListItemClick", path);
            new UnzipTask(getActivity(), path).execute();
            return;
        }
        Toast.makeText(getActivity(), "unsupport file type", Toast.LENGTH_LONG).show();
        
    }
    
    private String getBackPath() {
        try {
            return mPathStack.pop();
        } catch (EmptyStackException e) {
            return null;
        }
    }
    

}
