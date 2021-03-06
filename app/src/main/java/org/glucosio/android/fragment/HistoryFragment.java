package org.glucosio.android.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.glucosio.android.R;
import org.glucosio.android.activity.MainActivity;
import org.glucosio.android.adapter.HistoryAdapter;
import org.glucosio.android.listener.RecyclerItemClickListener;
import org.glucosio.android.presenter.HistoryPresenter;
import org.glucosio.android.tools.FormatDateTime;

public class HistoryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private HistoryPresenter presenter;
    private Boolean isToolbarScrolling = true;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();


        return fragment;
    }

    public HistoryFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mFragmentView;
        presenter = new HistoryPresenter(this);
        presenter.loadDatabase();

        mFragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        mRecyclerView = (RecyclerView) mFragmentView.findViewById(R.id.fragment_history_recycler_view);
        mAdapter = new HistoryAdapter(super.getActivity().getApplicationContext(), presenter);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(super.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // Do nothing
            }

            @Override
            public void onItemLongClick(final View view, final int position) {
                CharSequence colors[] = new CharSequence[]{getResources().getString(R.string.dialog_edit), getResources().getString(R.string.dialog_delete)};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // EDIT
                            TextView idTextView = (TextView) view.findViewById(R.id.item_history_id);
                            final int idToEdit = Integer.parseInt(idTextView.getText().toString());
                            ((MainActivity) getActivity()).showEditDialog(idToEdit);
                        } else {
                            // DELETE
                            TextView idTextView = (TextView) view.findViewById(R.id.item_history_id);
                            final int idToDelete = Integer.parseInt(idTextView.getText().toString());
                            final CardView item = (CardView) view.findViewById(R.id.item_history);
                            item.animate().alpha(0.0f).setDuration(2000);
                            Snackbar.make(((MainActivity) getActivity()).getFabView(), R.string.fragment_history_snackbar_text, Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    switch (event) {
                                        case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                            // Do nothing, see Undo onClickListener
                                            break;
                                        case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                            presenter.onDeleteClicked(idToDelete);
                                            break;
                                    }
                                }

                                @Override
                                public void onShown(Snackbar snackbar) {
                                    // Do nothing
                                }
                            }).setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    item.clearAnimation();
                                    item.setAlpha(1.0f);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }).setActionTextColor(getResources().getColor(R.color.glucosio_accent)).show();
                        }
                    }
                });
                builder.show();
            }
        }));

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mRecyclerView.removeOnLayoutChangeListener(this);
                updateToolbarBehaviour();
            }
        });

        return mFragmentView;
    }

    public void updateToolbarBehaviour(){
        if (mLayoutManager.findLastCompletelyVisibleItemPosition() == presenter.getReadingsNumber()-1) {
            isToolbarScrolling = false;
            ((MainActivity) getActivity()).turnOffToolbarScrolling();
        } else {
            if (!isToolbarScrolling){
                isToolbarScrolling = true;
                ((MainActivity)getActivity()).turnOnToolbarScrolling();
            }
        }
    }

    public String convertDate(String date){
        FormatDateTime dateTime = new FormatDateTime(getActivity().getApplicationContext());
        return dateTime.convertDate(date);
    }

    public void notifyAdapter(){
        mAdapter.notifyDataSetChanged();
    }

    public void reloadFragmentAdapter(){
        ((MainActivity)getActivity()).reloadFragmentAdapter();
        ((MainActivity)getActivity()).checkIfEmptyLayout();
    }
}
