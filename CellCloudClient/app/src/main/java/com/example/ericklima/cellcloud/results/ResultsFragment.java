package com.example.ericklima.cellcloud.results;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.ericklima.cellcloud.R;

public class ResultsFragment extends Fragment {

    private ResultAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.results_fragment, container, false);
        GridView gridview = (GridView) v.findViewById(R.id.result_grid);
        adapter = new ResultAdapter(getActivity());
        gridview.setAdapter(adapter);

        return v;
    }

    public void addBitmap(Bitmap b) {
        adapter.addImageBitmap(b);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter.clear();
    }
}
