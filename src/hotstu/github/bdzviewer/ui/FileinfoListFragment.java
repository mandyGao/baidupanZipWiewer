package hotstu.github.bdzviewer.ui;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.MoImageViewActiviy;
import hotstu.github.bdzviewer.R;
import hotstu.github.bdzviewer.adapter.ThumbnailTextAdapter;
import hotstu.github.bdzviewer.db.ZipfileinfoDAO;
import hotstu.github.bdzviewer.model.FileInfo;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileinfoListFragment extends ListFragment {

    private ThumbnailTextAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        Log.d("MyListFragment", "onAttach");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MyListFragment", "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("MyListFragment", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        adapter = new ThumbnailTextAdapter(getActivity(), getData());
        //getListView().addHeaderView(new View(getActivity()));
        //getListView().addFooterView(new View(getActivity()));
        setListAdapter(adapter);
        setEmptyText(Html.fromHtml(getString(R.string.list_empty)));
        //getListView().setSelector(R.drawable.list_row_selector);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                final FileInfo f = (FileInfo) getListView().getItemAtPosition(position);
                new AlertDialog.Builder(getActivity())
                .setMessage("删除:" + f.getName())
                .setPositiveButton("确认", new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ZipfileinfoDAO.delete(getActivity(), f);
                        reload();
                    }
                }).setNegativeButton("取消", new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        
                    }
                }).create().show();
                return false;
            }
        });
        
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.mainlistNeedReload) {
            App.mainlistNeedReload = false;
            reload();
        }
    }

    private void reload() {
        final ThumbnailTextAdapter adapter = (ThumbnailTextAdapter) getListAdapter();
        if (adapter != null){
            adapter.clear();
            adapter.addAll(getData());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // go to ZIP content
        ImageView iv = (ImageView) v.findViewById(R.id.thumbnail);
        Object tag = iv.getTag(R.id.TAG_NEED_THUMB);
        boolean needThumb = true;
        if (tag != null && (Boolean) tag == false) {
            needThumb = false;
        }
        FileInfo f = (FileInfo) l.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), MoImageViewActiviy.class);
        intent.putExtra(App.bundleKeyFileinfo, f);
        intent.putExtra("NEED_THUMB", needThumb);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("MyListFragment", "onCreateView");
        if (null == inflater)
            inflater = LayoutInflater.from(getActivity());
        return inflater.inflate(R.layout.listfragment_content_layout, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setEmptyText(CharSequence text) {
        TextView emptyTv = (TextView) getView().findViewById(android.R.id.empty);
        emptyTv.setMovementMethod(LinkMovementMethod.getInstance());
        emptyTv.setText(text);
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        super.setListAdapter(adapter);
    }

    private List<FileInfo> getData() {
//        DBHelper dbh = new DBHelper(getActivity());
//        String dataString = dbh.queryForData();
        return ZipfileinfoDAO.getAllFileinfo(getActivity());
    }

    
}
