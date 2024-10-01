package it.mm.supportlibrary.ui.dialog;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.Serializable;
import java.util.List;

import it.mm.supportlibrary.R;

public class SearchableListAppCompatDialog extends AppCompatDialog implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public AlertDialog searchableListAppCompatDialog;
    private static final String ITEMS = "items";

    private ArrayAdapter listAdapter;
    private List items;
    private ListView _listViewItems;

    private SearchableItem _searchableItem;

    private OnSearchTextChanged _onSearchTextChanged;

    private SearchView _searchView;

    private SimpleSearchView simpleSearchView;

    private String _strTitle;

    private String _strPositiveButtonText;

    private OnClickListener _onClickListener;

    String textToSearch = "";

    public Handler handler = new Handler();
    public Runnable runnable;
    public Activity activity;

    public SearchableListAppCompatDialog(Activity activity, Context context, List items) {
        super(context);
        this.activity = activity;
        this.items = items;
        this.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.searchable_list_dialog, null, false);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        _searchView = (SearchView) rootView.findViewById(R.id.search);
        _searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName
                ()));
        _searchView.setIconifiedByDefault(false);
        _searchView.setOnQueryTextListener(this);
        _searchView.setOnCloseListener(this);
        _searchView.clearFocus();
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(_searchView.getWindowToken(), 0);

        simpleSearchView = (SimpleSearchView) rootView.findViewById(R.id.simpleSearchView);

        rootView.post(new Runnable() {
            @Override
            public void run() {
                simpleSearchView.showSearch();
                simpleSearchView.setClearIconDrawable(ContextCompat.getDrawable(activity, R.drawable.cancel_circle));
                simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        textToSearch = newText;
                        if (TextUtils.isEmpty(newText)) {
                            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(null);
                            _listViewItems.setVisibility(View.VISIBLE);
                        } else {
                            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(newText, new Filter.FilterListener() {
                                @Override
                                public void onFilterComplete(int i) {
                                    _listViewItems.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        if (null != _onSearchTextChanged) {
                            _onSearchTextChanged.onSearchTextChanged(newText);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextCleared() {
//                        simpleSearchView.clearFocus();
                        return false;
                    }
                });

                simpleSearchView.setOnSearchViewListener(new SimpleSearchView.SearchViewListener() {
                    @Override
                    public void onSearchViewShown() {

                    }

                    @Override
                    public void onSearchViewClosed() {
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                simpleSearchView.showSearch();
                            }
                        }, 500);
                    }

                    @Override
                    public void onSearchViewShownAnimation() {

                    }

                    @Override
                    public void onSearchViewClosedAnimation() {
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                simpleSearchView.showSearch();
                            }
                        }, 500);
                    }
                });
            }
        });

        _listViewItems = (ListView) rootView.findViewById(R.id.listItems);

        //create the adapter by passing your ArrayList data
        listAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_1, items);
        //attach the adapter to the list
        _listViewItems.setAdapter(listAdapter);

        _listViewItems.setTextFilterEnabled(true);

        _listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _searchableItem.onSearchableItemClicked(listAdapter.getItem(position), position);
                searchableListAppCompatDialog.dismiss();
            }
        });

        searchableListAppCompatDialog = new MaterialAlertDialogBuilder(getContext())
                .setCancelable(true)
                .setView(rootView)
                .create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(searchableListAppCompatDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        searchableListAppCompatDialog.getWindow().setAttributes(lp);

    }

    public void show() {
        searchableListAppCompatDialog.show();
    }

    @Override
    public boolean onClose() {
        return false;
    }

    public void setSimpleSearchViewBackground(Drawable drawable) {
        simpleSearchView.setBackground(drawable);
    }

    public void setTitle(String strTitle) {
        _strTitle = strTitle;
    }

    public void setHint(String hint) {simpleSearchView.setHint(hint);}

    public void setPositiveButton(String strPositiveButtonText) {
        _strPositiveButtonText = strPositiveButtonText;
    }

    public void setPositiveButton(String strPositiveButtonText, OnClickListener onClickListener) {
        _strPositiveButtonText = strPositiveButtonText;
        _onClickListener = onClickListener;
    }

    public void setOnSearchableItemClickListener(SearchableItem searchableItem) {
        this._searchableItem = searchableItem;
    }

    public void setOnSearchTextChangedListener(OnSearchTextChanged onSearchTextChanged) {
        this._onSearchTextChanged = onSearchTextChanged;
    }

    public AlertDialog getMaterialAlertDialog() {
        return searchableListAppCompatDialog;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        _searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
//        listAdapter.filterData(s);
        if (TextUtils.isEmpty(s)) {
//                _listViewItems.clearTextFilter();
            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(null);
        } else {
            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(s);
        }
        if (null != _onSearchTextChanged) {
            _onSearchTextChanged.onSearchTextChanged(s);
        }
        return true;
    }

    public interface SearchableItem<T> extends Serializable {
        void onSearchableItemClicked(T item, int position);
    }

    public interface OnSearchTextChanged {
        void onSearchTextChanged(String strText);
    }
}
